package com.action.dualcontrol.base

import android.content.Context
import com.action.dualcontrol.model.DeviceSerachModel
import java.lang.ref.WeakReference

abstract class BasePresenter<M :IBaseModel,V:IBaseView> {
    var mWrf :WeakReference<V> ?= null
    var mModel:M ?= null
    open fun attachView(view:V){
        mWrf = WeakReference(view)
        if (null == mModel){
            mModel = CreateModel()
        }

    }

    fun detachView(){
        mWrf?.clear()
        mWrf = null
        mModel = null
    }
    fun isViewAttached():Boolean{
        return  null != mWrf?.get()
    }
    fun getView(): V? {
        return mWrf?.get()
    }
    fun getModel():M?{
        return mModel
    }

    abstract fun CreateModel(): M
}