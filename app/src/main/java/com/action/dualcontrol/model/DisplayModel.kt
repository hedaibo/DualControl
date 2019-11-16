package com.action.dualcontrol.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Message
import android.os.SystemClock
import android.view.MotionEvent
import com.action.dualcontrol.Contract.DisplayContract
import com.action.dualcontrol.base.IBaseHandler
import com.action.dualcontrol.base.ModelCallback
import com.action.dualcontrol.utils.HandlerUtils
import com.action.dualcontrol.utils.IpUtils
import com.action.dualcontrol.utils.LogUtils
import com.action.dualcontrol.utils.ThreadPoolManager
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.Socket
import kotlin.experimental.and
import kotlin.experimental.or

class DisplayModel : DisplayContract.Model, IBaseHandler {

    val JACTION = "action"
    val JX = "x"
    val JY = "y"
    var touchSocket: Socket? = null
    var touchDos: DataOutputStream? = null
    var touchDis: DataInputStream? = null

    var dataSocket: Socket? = null
    var dataDos: DataOutputStream? = null
    var dataDis: DataInputStream? = null
    var mBitmap: Bitmap? = null

    var bytesDisplayData: ByteArray? = null

    var mCallback: ModelCallback? = null

    var mSecondIp: String? = null

    var scaleX: Float = 0f
    var scaleY: Float = 0f

    var screenX: Int = 0
    var screenY: Int = 0

    val touchLock:Object = Object()

    private val mHandler by lazy { HandlerUtils(this@DisplayModel) }
    override fun handleMessage(msg: Message?) {
        when (msg?.what) {
            HandlerUtils.RECEIVER_ALIVE_TIMEOUT -> {
                LogUtils.i("handleMessage---RECEIVER_ALIVE_TIMEOUT")
                mCallback?.connectFail()
            }
            HandlerUtils.SHOE_IMAGE -> {
//                LogUtils.i("handleMessage---SHOE_IMAGE---mBitmap:$mBitmap")
                mCallback?.displayBitmap(mBitmap)
            }
            HandlerUtils.CONNECT_SUCCESS -> {
                LogUtils.i("handleMessage---CONNECT_SUCCESS")
                mHandler.removeMessages(HandlerUtils.RECONNECT)
                mCallback?.connectSuccess()
            }
            HandlerUtils.CONNECT_FAIL -> {
                LogUtils.i("handleMessage---CONNECT_FAIL")
                mCallback?.connectFail()
            }

            HandlerUtils.RECONNECT -> {
                LogUtils.i("handleMessage---RECONNECT")
                mSecondIp?.let { _connect(it) }
            }

        }
    }

    override fun setScreenSize(widthPixels: Int, heightPixels: Int) {
        screenX = widthPixels
        screenY = heightPixels
    }

    override fun connect(firstIp: String?, secondIp: String?, callback: ModelCallback) {
        LogUtils.i("---connect-")
        mCallback = callback
        firstIp?.let { _connect(it) }
        mSecondIp = secondIp
        mSecondIp?.let {
            if (it.length > 5) {
                mHandler.removeMessages(HandlerUtils.RECONNECT)
                mHandler.sendEmptyMessageDelayed(
                    HandlerUtils.RECONNECT,
                    HandlerUtils.RECONNECT_DELAY
                )
            }
        }

    }

    fun _connect(ip: String) {
        LogUtils.i("---_connect-")
        close()
        connectTouchSocket(ip)
        connectDataSocket(ip)
    }

    private fun connectTouchSocket(ip: String) {
        ThreadPoolManager.mInstance.execute(Runnable {
            try {
                touchSocket = Socket(ip, IpUtils.TOUCHPORT)
                touchDos = DataOutputStream(touchSocket?.getOutputStream())
                touchDis = DataInputStream(touchSocket?.getInputStream())
                mHandler.sendEmptyMessage(HandlerUtils.CONNECT_SUCCESS)
                receiverAliveData()
            } catch (e: Exception) {
                LogUtils.e("connectTouchSocket----error-")
                mHandler.sendEmptyMessage(HandlerUtils.CONNECT_FAIL)
            }

        })
    }

    private fun receiverAliveData() {
        while (touchDis != null) {
            val aLive = ByteArray(5)
            touchDis?.read(aLive)
            mHandler.removeMessages(HandlerUtils.RECEIVER_ALIVE_TIMEOUT)
            mHandler.sendEmptyMessageDelayed(
                HandlerUtils.RECEIVER_ALIVE_TIMEOUT,
                HandlerUtils.RECEIVER_ALIVE_TIMEOUT_DELAY
            )
        }
    }

    private fun connectDataSocket(ip: String) {
        ThreadPoolManager.mInstance.execute(Runnable {
            try {
                dataSocket = Socket(ip, IpUtils.DATAPORT)
                dataDos = DataOutputStream(dataSocket?.getOutputStream())
                dataDis = DataInputStream(dataSocket?.getInputStream())
                mHandler.sendEmptyMessage(HandlerUtils.CONNECT_SUCCESS)
                while (null != dataDis) {
                    readDisplayData()
                }
                mHandler.sendEmptyMessage(HandlerUtils.CONNECT_FAIL)
            } catch (e: Exception) {
                LogUtils.e("connectDataSocket----error-")
                mHandler.sendEmptyMessage(HandlerUtils.CONNECT_FAIL)
            }
        })
    }

    @Synchronized
    private fun readDisplayData() {
        var receveByteslen = ByteArray(4)

        var datalen: ByteArray = ByteArray(3)
        dataDis?.readFully(receveByteslen)
//        LogUtils.i("receveByteslen0:${receveByteslen[0]}--1:${receveByteslen[1]}--2:${receveByteslen[2]}--3:${receveByteslen[3]}")
        sendAck(receveByteslen[0])
        System.arraycopy(receveByteslen, 1, datalen, 0, datalen.size)
//        LogUtils.i("datalen0:${datalen[0]}--1:${datalen[1]}--2:${datalen[2]}")
        val length = bufferToInt(datalen)

//        LogUtils.i("length:$length")
        if (length != 0) {
            bytesDisplayData = ByteArray(length)
            dataDis?.readFully(bytesDisplayData)
            mBitmap = BitmapFactory.decodeByteArray(bytesDisplayData, 0, length)
//            LogUtils.i("mBitmap:$mBitmap")
            bytesDisplayData = null
            mHandler.sendEmptyMessage(HandlerUtils.SHOE_IMAGE)

            mBitmap?.let {
                if (scaleX == 0f) {
                    scaleX = it.width.toFloat() / screenX.toFloat()
                    scaleY = it.height.toFloat() / screenY.toFloat()
                }
            }

        }
    }


    private fun bufferToInt(bytes: ByteArray): Int {
        return ((bytes[0].toInt() and 0xFF) or ((bytes[1].toInt() and 0xFF) shl 8) or ((bytes[2].toInt() and 0xFF) shl 16))
    }

    private fun sendAck(ack: Byte) {
        dataDos?.write(ack.toInt())
        dataDos?.flush()
    }


    override fun sendTouchData(actionType: Int, x: Int, y: Int) {
        LogUtils.i("sendTouchData--actionType:${actionType}")
        //val time = SystemClock.uptimeMillis()

        synchronized(touchLock){
            ThreadPoolManager.mInstance.execute(Runnable {
                //val time1 = SystemClock.uptimeMillis()
                val mX = (x * scaleX).toInt()
                val mY = (y * scaleY).toInt()
                if (mX >= 0 && mY >= 0 && mY <= 600 && mX <= 1024) {
                    val jObject = JSONObject()
                    jObject.put(JACTION, actionType)
                    jObject.put(JX, mX)
                    jObject.put(JY, mY)
                    val jbytes = jObject.toString().toByteArray()
                    val intTobyte = ByteArray(1)
                    intTobyte[0] = jbytes.size.toByte()
                    var data = ByteArray(jbytes.size + 1)
                    System.arraycopy(intTobyte, 0, data, 0, 1)
                    System.arraycopy(jbytes, 0, data, 1, jbytes.size)
                    //Thread.sleep(200)
                    //val time3 = SystemClock.uptimeMillis()
                   // LogUtils.i("sendTouchData--data:${String(data)} time:${ time3 - time1}  allTime:${time3 - time}")
                    writeTouchData(data)
                   // LogUtils.i("writeTouchData-- time:${ SystemClock.uptimeMillis() - time3} ")
                }
            })
        }


    }

    private fun writeTouchData(data: ByteArray) {
        synchronized(touchLock){
            touchDos?.write(data)
            touchDos?.flush()
        }

    }


    override fun close() {
        mHandler.removeMessages(HandlerUtils.RECONNECT)
        closeTouchSocket()
        closeDataSocket()
    }

    private fun closeTouchSocket() {
        touchDis?.close()
        touchDos?.close()
        touchSocket?.close()
        touchDis = null
        touchDos = null
        touchSocket = null
    }

    private fun closeDataSocket() {
        dataDis?.close()
        dataDos?.close()
        dataSocket?.close()
        dataDis = null
        dataDos = null
        dataSocket = null
    }
}