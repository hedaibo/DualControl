package com.action.dualcontrol.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.io.IOException
import java.net.*
import java.util.*

class IpUtils {
    companion object{
        const val IP_TYPE_NORMAL = 0
        const val IP_TYPE_IPV4 = 1
        const val IP_TYPE_IPV6 = 2


        const val MULTIPORT = 9696
        const val MULTIPORT_IPV6 = 19696
        const val DATAPORT = 8686
        const val TOUCHPORT = 8181
        const val BACKPORT = 9191
        const val IPV6_BROADCAST_ADDR = "FF02::1"
        const val IP_FIRST = "ip_first"
        const val IP_SECOND = "ip_second"

        fun getLocalIpType(): Int {
            var ipType = IP_TYPE_NORMAL
            try {
                val en = NetworkInterface
                    .getNetworkInterfaces() ?: return ipType
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        val hostAddress = inetAddress.hostAddress
                        LogUtils.i( "hdb2  $hostAddress")
                        if (inetAddress is Inet6Address) {//ipv6
                            if (!hostAddress.startsWith("fe") && !hostAddress.startsWith("fc") && hostAddress.length > 6) {
                                LogUtils.i("hdb2---ipv6:$hostAddress")
                                ipType = ipType or IP_TYPE_IPV6
                            }
                        } else {//ipv4
                            if (!"127.0.0.1".equals(hostAddress, ignoreCase = true)) {
                                LogUtils.i("hdb2---ipv4:$hostAddress")
                                ipType = ipType or IP_TYPE_IPV4
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            LogUtils.i("ipType:$ipType")
            return ipType
        }

        fun getIpv6NetworkInterface(): NetworkInterface? {

            try {
                val en = NetworkInterface
                    .getNetworkInterfaces() ?: return null
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        val hostAddress = inetAddress.hostAddress
                        LogUtils.i("hdb2  $hostAddress")
                        if (inetAddress is Inet6Address) {//ipv6
                            if (!hostAddress.startsWith("fe") && !hostAddress.startsWith("fc") && hostAddress.length > 6) {
                                LogUtils.i("hdb2---ipv6:$hostAddress")
                                return intf
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }

        @Throws(IOException::class)
        fun getListBroadcastAddress(): List<InetAddress> {
            // 获取本地所有网络接口
            val addList = ArrayList<InetAddress>()
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }
                // getInterfaceAddresses()方法返回绑定到该网络接口的所有 IP 的集合
                for (interfaceAddress in networkInterface.interfaceAddresses) {

                    val broadcast = interfaceAddress.broadcast ?: continue
                    addList.add(broadcast)
                }
            }
            return addList
        }


        fun openWifiBrocast(context: Context): WifiManager.MulticastLock? {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            val multicastLock = wifiManager
                .createMulticastLock("MediaRender")
            multicastLock?.acquire()
            return multicastLock
        }


        fun getHostListIP(): ArrayList<String> {

            val ips = ArrayList<String>()
            try {
                val nis = NetworkInterface.getNetworkInterfaces() ?: return ips
                var ia: InetAddress? = null
                while (nis.hasMoreElements()) {
                    val ni = nis.nextElement() as NetworkInterface
                    val ias = ni.inetAddresses
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement()
                        if (ia is Inet6Address) {
                            continue// skip ipv6
                        }
                        val ip = ia!!.hostAddress
                        if ("127.0.0.1" != ip) {
                            val hostIp = ia.hostAddress
                            ips.add(hostIp)
                        }
                    }
                }
            } catch (e: SocketException) {
                LogUtils.i( "SocketException")
                e.printStackTrace()
            }

            return ips

        }


        fun getLocalIp(): InetAddress? {
            try {
                val en = NetworkInterface
                    .getNetworkInterfaces() ?: return null
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        // logger.error("ip1       " + inetAddress);
                        val hostAddress = inetAddress.hostAddress
                        //LogUtils.i("hdb2  " + hostAddress);
                        if (inetAddress is Inet6Address) {//ipv6
                            if (!hostAddress.startsWith("fe") && !hostAddress.startsWith("fc") && hostAddress.length > 6) {
                                //                            LogUtils.i("hdb2---ipv6:" + hostAddress);
                                return inetAddress
                            }
                        } else {//ipv4
                            if (!"127.0.0.1".equals(hostAddress, ignoreCase = true)) {
                                //                            LogUtils.i("hdb2---ipv4:" + hostAddress);
                                return inetAddress
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e("IP Address", ex.toString())
            }

            return null
        }
    }
}