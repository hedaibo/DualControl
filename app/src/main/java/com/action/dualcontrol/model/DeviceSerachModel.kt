package com.action.dualcontrol.model

import android.os.Message
import android.os.SystemClock
import com.action.dualcontrol.Contract.DeviceSerachContract
import com.action.dualcontrol.base.IBaseHandler
import com.action.dualcontrol.base.ResponseCallback
import com.action.dualcontrol.utils.HandlerUtils
import com.action.dualcontrol.utils.IpUtils
import com.action.dualcontrol.utils.IpUtils.Companion.IPV6_BROADCAST_ADDR
import com.action.dualcontrol.utils.LogUtils
import com.action.dualcontrol.utils.ThreadPoolManager
import java.io.IOException
import java.net.*

class DeviceSerachModel :DeviceSerachContract.Model,IBaseHandler{
    override fun handleMessage(msg: Message?) {
        when(msg?.what){
            HandlerUtils.SEARCH_DEVICE ->{
                sendBroadCast()
                if (isSendAndReceiver){
                    if (mHandler.hasMessages(HandlerUtils.SEARCH_DEVICE)){
                        mHandler.removeMessages(HandlerUtils.SEARCH_DEVICE)
                    }
                    mHandler.sendEmptyMessageDelayed(HandlerUtils.SEARCH_DEVICE,HandlerUtils.SEARCH_DEVICE_DELAY)
                }
            }
            HandlerUtils.AUTO_UPDATE_LIST ->{
                updateListBytime()
                if (isSendAndReceiver){
                    if (mHandler.hasMessages(HandlerUtils.AUTO_UPDATE_LIST)){
                        mHandler.removeMessages(HandlerUtils.AUTO_UPDATE_LIST)
                    }
                    mHandler.sendEmptyMessageDelayed(HandlerUtils.AUTO_UPDATE_LIST,HandlerUtils.AUTO_UPDATE_LIST_DELAY)
                }
            }
            HandlerUtils.UPDATE_LIST -> {
                updateList()
            }
        }
    }

    private val mHandler by lazy { HandlerUtils(this@DeviceSerachModel) }

    var mCallback: ResponseCallback<ArrayList<DevicesInfo>>? = null
    override fun startSearchDevice(callback: ResponseCallback<ArrayList<DevicesInfo>>) {
        LogUtils.i("startSearchDevice")
        mCallback = callback
        initSocket()
        sendBroadCast()
        startReceiverData()

        if (isSendAndReceiver){
            mHandler.sendEmptyMessageDelayed(HandlerUtils.SEARCH_DEVICE,HandlerUtils.SEARCH_DEVICE_DELAY)
            mHandler.sendEmptyMessageDelayed(HandlerUtils.AUTO_UPDATE_LIST,HandlerUtils.AUTO_UPDATE_LIST_DELAY)
        }

    }

    override fun stopSearchDevice() {
        closeSocket()
        deviceList.clear()
        mHandler.sendEmptyMessage(HandlerUtils.UPDATE_LIST)
    }

    fun updateList(){
        mCallback?.upateUi(deviceList)
    }

    fun updateListBytime(){

        var rmList = deviceList.filter {
            (SystemClock.uptimeMillis() - it.time) > DEVICE_OUT_TIME
        }
        if (rmList.isNotEmpty()){
            deviceList.removeAll(rmList)
            mHandler.sendEmptyMessage(HandlerUtils.UPDATE_LIST)
        }


    }

    private  var broadcastAddress6: InetAddress? = null
    private  var multicastSocketIpv6: MulticastSocket? = null
    private  var listBroadcastAddress: List<InetAddress>? = null
    private  var multicastSocket: MulticastSocket? = null
    private  var  isSendAndReceiver = false
    private var deviceList:ArrayList<DevicesInfo> = ArrayList()
    private var ipType:Int = IpUtils.IP_TYPE_NORMAL
    private val DEVICE_OUT_TIME:Long = 5500

    private fun initSocket(){
        ipType = IpUtils.getLocalIpType()
        LogUtils.i("initSocket-----ipType:$ipType")
        if (ipType == IpUtils.IP_TYPE_NORMAL) return
//        IpUtils.openWifiBrocast(mContext) // need move

        closeSocket()
        if((ipType and IpUtils.IP_TYPE_IPV4) == IpUtils.IP_TYPE_IPV4){
            initIpv4Socket()
        }
        if((ipType and IpUtils.IP_TYPE_IPV6) == IpUtils.IP_TYPE_IPV6){
            initIpv6Socket()
        }
        isSendAndReceiver = true
    }

    private fun sendBroadCast(){
        ThreadPoolManager.mInstance.execute(Runnable {
            _sendBroadCast()
        })
    }

    @Throws(IOException::class)
    private fun _sendBroadCast() {
       // val instance = DisPlayActivity.getInstance()
        if (!isSendAndReceiver) {
            return
        }
        //        String ipAddress = IpUtils.getHostIP();
        if ((ipType and IpUtils.IP_TYPE_IPV6) == IpUtils.IP_TYPE_IPV6 && multicastSocketIpv6 != null) {
            val localIp = IpUtils.getLocalIp() ?: return
            val hostAddress = localIp!!.hostAddress
            val split = hostAddress.split("%".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val data = ("phoneip:" + split[0]).toByteArray()
            val packet = DatagramPacket(
                data, data.size,
                Inet6Address.getByName(IPV6_BROADCAST_ADDR), IpUtils.MULTIPORT_IPV6
            )
//            LogUtils.i("hdb----send---data:" + String(data))
            multicastSocketIpv6?.send(packet)
        }
        if (ipType and IpUtils.IP_TYPE_IPV4 === IpUtils.IP_TYPE_IPV4 && multicastSocket != null) {
            val hostListIP = IpUtils.getHostListIP()
            //        String macAddress = IpUtils.getMacAddressFromIp();
            //       LogUtils.i(TAG, "hdb----send---hostListIP:" + hostListIP);
//            LogUtils.i("hdb----send---hostListIP:$hostListIP  listBroadcastAddress:$listBroadcastAddress")
            if (hostListIP != null && hostListIP!!.size > 0 && listBroadcastAddress != null && listBroadcastAddress?.size!! > 0) {
//                LogUtils.i("hdb----send---hostListIP:" + hostListIP!!.size + "  listBroadcastAddress:" + listBroadcastAddress?.size)
                val data = ("phoneip:" + hostListIP!!.get(0)).toByteArray()
                val packet = DatagramPacket(
                    data, data.size,
                    listBroadcastAddress?.get(0), IpUtils.MULTIPORT
                )

                multicastSocket?.send(packet)
                if (listBroadcastAddress?.size!! > 1) {

                    val packet1 = DatagramPacket(
                        data, data.size,
                        listBroadcastAddress?.get(1), IpUtils.MULTIPORT
                    )
//                    delay(100)
                    multicastSocket?.send(packet1)


                }
                //            receiverBack();

            }
        }

    }

    private fun startReceiverData(){
        LogUtils.i("startReceiverData-----isSendAndReceiver:$isSendAndReceiver")
        if (!isSendAndReceiver) {
            return
        }
        startReceiverIpv4Data()
        startReceiverIpv6Data()

    }

    private fun startReceiverIpv4Data() {

        ThreadPoolManager.mInstance.execute(Runnable {
            while (isSendAndReceiver){
                receiverBack()
            }
        })
    }
    private fun startReceiverIpv6Data() {

        ThreadPoolManager.mInstance.execute(Runnable {
            while (isSendAndReceiver){
                receiverBackIpv6()
            }
        })
    }

    private fun receiverBack() {
        //Log.i(TAG,"hdb---receiverBack--multicastSocket:"+multicastSocket);
        if (multicastSocket == null) {
            return
        }
        try {
            val data = ByteArray(100)
            val pack = DatagramPacket(data, data.size)
                multicastSocket?.receive(pack)
            val back = String( pack.data, pack.offset,pack.length)
            LogUtils.i("hdb---receiverBack--$back")
            handleReceiverData(back)
        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    private fun receiverBackIpv6() {
        if (multicastSocketIpv6 == null) {
            return
        }
        try {
            val data = ByteArray(100)
            val pack = DatagramPacket(data, data.size)

            multicastSocketIpv6?.receive(pack)
            val back = String(
                pack.data, pack.offset,
                pack.length
            )
//            LogUtils.i("hdb---receiverBackIpv6--$back")
            handleReceiverData(back)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @Synchronized
    private fun handleReceiverData(back: String?) {
        if (back != null && (back.startsWith("serverip:") || back.startsWith("server6ip"))) {

            var cureentTime = SystemClock.uptimeMillis()
            if (back.startsWith("serverip:")) {
                val split = back.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (split.size == 3) {
                    var remoteServerIp = split[1]
                    var remoteName = split[2]
                    val deviceInfo = getDevice(remoteServerIp)
                    if (deviceInfo == null) {
                        val deviceInfo1 = DevicesInfo(remoteServerIp,"", remoteName,cureentTime)
                        deviceList.add(deviceInfo1)
//                        val activity = ShowDeviceListActivity.getInstance()
//                        if (null != activity && activity!!.isShow) {
//                            activity!!.searchSuccess(deviceList)
//                        }
                        mHandler.sendEmptyMessage(HandlerUtils.UPDATE_LIST)
                    } else {
                        deviceInfo.time = cureentTime
                    }
                }

            } else if (back.startsWith("server6ip:")) {
                val split = back.split(":&&:".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val remoteServerIp = split[2]
                val remoteName = split[3]
                val ipv6Address = split[1]
//                LogUtils.i("hdb--------remoteServerIp:$remoteServerIp  remoteName:$remoteName  ipv6:$ipv6Address")
                val deviceInfo = getDevice(remoteServerIp)
                val device6Info = getDevice(ipv6Address)
                if (deviceInfo == null && device6Info == null) {
                    val deviceInfo1 = DevicesInfo(remoteServerIp,ipv6Address, remoteName,cureentTime)
                    deviceList.add(deviceInfo1)

//                    val activity = ShowDeviceListActivity.getInstance()
//                    if (null != activity && activity!!.isShow) {
//                        activity!!.searchSuccess(deviceList)
//                    }

                } else {
                    if (deviceInfo != null && device6Info == null) {
                        deviceInfo.time = cureentTime
                        deviceInfo.ip6Address = ipv6Address
                    } else if (deviceInfo == null && device6Info != null) {
                        device6Info.time = cureentTime
                        device6Info.ipAddress = remoteServerIp
                    } else if (deviceInfo != null && device6Info != null) {
                        deviceInfo.time = cureentTime
                    }
                }
            }

        }
    }

    /**
     * find device by ip
     */
    private fun getDevice(ip: String): DevicesInfo? {
        if (deviceList.size == 0) return null
        var info = deviceList.find { (it.ip6Address == ip) or (it.ipAddress == ip) }
        return info
    }

    private fun initIpv6Socket() {
        LogUtils.i("hdb----isIPv6---")
        try {
            multicastSocketIpv6 = MulticastSocket(IpUtils.MULTIPORT_IPV6)
            broadcastAddress6 = InetAddress.getByName(IpUtils.IPV6_BROADCAST_ADDR)
            multicastSocketIpv6?.networkInterface = IpUtils.getIpv6NetworkInterface()
            val socketAddress = InetSocketAddress(broadcastAddress6, IpUtils.MULTIPORT_IPV6)
            multicastSocketIpv6?.joinGroup(socketAddress, IpUtils.getIpv6NetworkInterface())
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.i("hdb-----IP_TYPE_IPV6--joinGroup-error-")
        }
        try {
            multicastSocketIpv6?.loopbackMode = true
            //    multicastSocketIpv6.setTimeToLive(255);
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun initIpv4Socket() {
        try {
            multicastSocket = MulticastSocket(IpUtils.MULTIPORT)
            listBroadcastAddress = IpUtils.getListBroadcastAddress()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            multicastSocket?.broadcast = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (listBroadcastAddress?.isNotEmpty()!!) {
            try {
                multicastSocket?.joinGroup(listBroadcastAddress?.get(0))
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.i("hdb--111---IP_TYPE_IPV4--joinGroup-error-" + listBroadcastAddress?.get(0))
            }
            try {
                if (listBroadcastAddress?.size == 2) {
                    multicastSocket?.joinGroup(listBroadcastAddress?.get(1))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.i( "hdb---222--IP_TYPE_IPV4--joinGroup-error-")
            }
        }
        try {
            multicastSocket?.loopbackMode = true
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun closeSocket(){
        isSendAndReceiver = false
        closeIpv4Socket()
        closeIpv6Socket()
    }

    fun closeIpv4Socket(){
        if (multicastSocket != null) {
            if ((ipType and IpUtils.IP_TYPE_IPV4) == IpUtils.IP_TYPE_IPV4 &&  listBroadcastAddress?.isNotEmpty()!!) {
                try {
                    multicastSocket?.leaveGroup(listBroadcastAddress?.get(0))
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (listBroadcastAddress?.size == 2) {
                    try {
                        multicastSocket?.leaveGroup(listBroadcastAddress?.get(0))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            multicastSocket?.close()
            multicastSocket = null
        }
    }

    fun closeIpv6Socket(){
        if (multicastSocketIpv6 != null) {

            if (ipType and IpUtils.IP_TYPE_IPV6 === IpUtils.IP_TYPE_IPV6 && broadcastAddress6 != null) {
                try {
                    multicastSocketIpv6?.leaveGroup(broadcastAddress6)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            multicastSocketIpv6?.close()
            multicastSocketIpv6 = null
        }
    }
}