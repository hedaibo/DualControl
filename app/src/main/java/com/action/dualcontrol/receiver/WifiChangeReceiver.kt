package com.action.dualcontrol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Message
import com.action.dualcontrol.base.IBaseHandler
import com.action.dualcontrol.base.WifiCallBack
import com.action.dualcontrol.utils.HandlerUtils
import com.action.dualcontrol.utils.IpUtils
import com.action.dualcontrol.utils.LogUtils

class WifiChangeReceiver(callBack: WifiCallBack) : BroadcastReceiver(),IBaseHandler {
    override fun handleMessage(msg: Message?) {
        when(msg?.what){
            HandlerUtils.NETWORK_CONNECTED ->{
                ipType = IpUtils.getLocalIpType()
                mCallBack?.onConnected()
            }
            HandlerUtils.NETWORK_DISCONNECTED ->{
                ipType = IpUtils.IP_TYPE_NORMAL
                mCallBack?.onDisConnected()
            }
            HandlerUtils.NETWORK_CONNECTED_AGAIN ->{ // some phone get ip delay
                var ipT = IpUtils.getLocalIpType()
                LogUtils.i("NETWORK_CONNECTED_AGAIN-----ipT:${ipT}  ipType:${ipType}")
                if (ipT != ipType){
                    ipType = ipT
                    mCallBack?.onConnected()
                }
            }
        }
    }

    private val mHandler by lazy { HandlerUtils(this@WifiChangeReceiver) }

    var mCallBack: WifiCallBack? = null
    var ipType = IpUtils.IP_TYPE_NORMAL
    val AP_STATE_CLOSING = 10
    val AP_STATE_CLOSED = 11
    val AP_STATE_OPENING = 12
    val AP_STATE_OPENED = 12

    init {
        mCallBack = callBack
    }

    override fun onReceive(p0: Context?, p1: Intent?) {

        if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(p1?.action)){ // hostpot
            var state = p1?.getIntExtra("wifi_state",0)
            LogUtils.i("WIFI_AP_STATE_CHANGED-----state:$state")
            if (state == AP_STATE_CLOSING){
                LogUtils.i("WIFI_AP_STATE_CHANGED---正在关闭--state:$state")
                mHandler.sendEmptyMessage(HandlerUtils.NETWORK_DISCONNECTED)
            }else if (state == AP_STATE_CLOSED){
                LogUtils.i("WIFI_AP_STATE_CHANGED---关闭--state:$state")
            }else if (state == AP_STATE_OPENING){
                LogUtils.i("WIFI_AP_STATE_CHANGED---正在开启--state:$state")
            }else if (state == AP_STATE_OPENED){
                LogUtils.i("WIFI_AP_STATE_CHANGED---已经开启--state:$state")
                mHandler.removeMessages(HandlerUtils.NETWORK_CONNECTED)
                mHandler.sendEmptyMessageDelayed(HandlerUtils.NETWORK_CONNECTED,HandlerUtils.NETWORK_CONNECTED_DELAY)

                mHandler.removeMessages(HandlerUtils.NETWORK_CONNECTED_AGAIN)
                mHandler.sendEmptyMessageDelayed(HandlerUtils.NETWORK_CONNECTED_AGAIN,HandlerUtils.NETWORK_CONNECTED_AGAIN_DELAY)
            }
        }else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(p1?.action)){//wifi
            val cManager:ConnectivityManager = p0?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            LogUtils.i("CONNECTIVITY_ACTION---")
            if (networkInfo != null && networkInfo.isConnected){
                LogUtils.i("CONNECTIVITY_ACTION--wifi 连接---")
                mHandler.removeMessages(HandlerUtils.NETWORK_CONNECTED)
                mHandler.sendEmptyMessageDelayed(HandlerUtils.NETWORK_CONNECTED,HandlerUtils.NETWORK_CONNECTED_DELAY)

                mHandler.removeMessages(HandlerUtils.NETWORK_CONNECTED_AGAIN)
                mHandler.sendEmptyMessageDelayed(HandlerUtils.NETWORK_CONNECTED_AGAIN,HandlerUtils.NETWORK_CONNECTED_AGAIN_DELAY)
            }else{
                LogUtils.i("CONNECTIVITY_ACTION---wifi 断开---")
                mHandler.sendEmptyMessage(HandlerUtils.NETWORK_DISCONNECTED)
            }

        }

    }
}