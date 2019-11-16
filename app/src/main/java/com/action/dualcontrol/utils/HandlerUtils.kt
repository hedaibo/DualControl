package com.action.dualcontrol.utils

import android.app.Activity
import android.os.Handler
import android.os.Message
import com.action.dualcontrol.base.BaseActivity
import com.action.dualcontrol.base.IBaseHandler

import java.lang.ref.WeakReference

class HandlerUtils<T : IBaseHandler> : Handler{
    var mWrf : WeakReference<T>
    constructor(activity : T){
        mWrf = WeakReference(activity)
    }

    override fun handleMessage(msg: Message?) {
        val mActivity = mWrf.get()
        if (mActivity == null){
            return
        }
        //LogUtils.i("isActivity:"+(mActivity is Activity))
        if (mActivity is Activity && mActivity.isFinishing){
           return
        }
        mActivity.handleMessage(msg)
        super.handleMessage(msg)
    }

    //static method and attribute
    companion object{

        const val SEARCH_DEVICE_DELAY: Long = 300
        const val SEARCH_DEVICE: Int = 1

        const val AUTO_UPDATE_LIST_DELAY: Long = 1000
        const val AUTO_UPDATE_LIST: Int = 2
        const val UPDATE_LIST: Int = 3

        const val SPLASH_REMOVE_IMAGE = 1
        const val SPLASH_REMOVE_IMAGE_DELAY : Long = 2000

        const val NETWORK_CONNECTED = 1
        const val NETWORK_DISCONNECTED = 2
        const val NETWORK_CONNECTED_AGAIN = 3
        const val NETWORK_CONNECTED_DELAY : Long = 500
        const val NETWORK_CONNECTED_AGAIN_DELAY : Long = 5000

        const val RECEIVER_ALIVE_TIMEOUT = 1
        const val RECEIVER_ALIVE_TIMEOUT_DELAY: Long = 10000
        const val SHOE_IMAGE = 2
        const val CONNECT_SUCCESS = 3
        const val CONNECT_FAIL = 4
        const val RECONNECT = 5
        const val RECONNECT_DELAY:Long = 3000
    }



}