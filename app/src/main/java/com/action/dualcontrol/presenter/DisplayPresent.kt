package com.action.dualcontrol.presenter

import android.graphics.Bitmap
import android.view.MotionEvent
import com.action.dualcontrol.Contract.DisplayContract
import com.action.dualcontrol.base.BasePresenter
import com.action.dualcontrol.base.ModelCallback
import com.action.dualcontrol.model.DisplayModel
import com.action.dualcontrol.utils.LogUtils

class DisplayPresent:BasePresenter<DisplayContract.Model,DisplayContract.View>(),DisplayContract.Presenter {
    override fun setScreenSize(widthPixels: Int, heightPixels: Int) {
        getModel()?.setScreenSize(widthPixels,heightPixels)
    }

    override fun connect(firstIp:String?,secondIp:String?) {
        LogUtils.i("DisplayPresent-----connect-")
        if (isViewAttached()){
            getView()?.showLoading()
            getModel()?.connect(firstIp,secondIp,object:ModelCallback{
                override fun connectSuccess() {
                    getView()?.connectSuccess()
                    getView()?.hideLoading()
                }

                override fun connectFail() {
                    getView()?.connectFail()
                    getView()?.hideLoading()
                }

                override fun displayBitmap(bitmap: Bitmap?) {
                    getView()?.displayBitmap(bitmap)
                }

            })
        }
    }

    override fun disConnect() {
        getModel()?.close()
    }

    override fun CreateModel(): DisplayContract.Model {
        return DisplayModel()
    }

    override fun onTouchEvent(event: MotionEvent?) {
        event?.let { getModel()?.sendTouchData(event.action,event.x.toInt(),event.y.toInt()) }

    }


}