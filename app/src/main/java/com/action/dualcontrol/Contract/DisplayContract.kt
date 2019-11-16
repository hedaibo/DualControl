package com.action.dualcontrol.Contract

import android.graphics.Bitmap
import android.view.MotionEvent
import com.action.dualcontrol.base.IBaseModel
import com.action.dualcontrol.base.IBaseView
import com.action.dualcontrol.base.ModelCallback
import com.action.dualcontrol.base.ResponseCallback
import com.action.dualcontrol.model.DevicesInfo

interface DisplayContract {
    interface Model : IBaseModel {
        fun connect(firstIp:String?,secondIp:String?,callback: ModelCallback)
        fun close()
        fun sendTouchData(actionType:Int,x:Int,y:Int)
        fun setScreenSize(widthPixels: Int, heightPixels: Int)
    }
    interface View : IBaseView {
        fun connectSuccess()
        fun connectFail()
        fun displayBitmap(bitmap: Bitmap?)
        fun showLoading()
        fun hideLoading()
    }
    interface Presenter{
        fun connect(firstIp: String?,secondIp:String?)
        fun disConnect()
        fun onTouchEvent(event: MotionEvent?)
        fun setScreenSize(widthPixels: Int, heightPixels: Int)

    }
}