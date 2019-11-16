package com.action.dualcontrol.Contract

import com.action.dualcontrol.base.IBaseModel
import com.action.dualcontrol.base.IBaseView
import com.action.dualcontrol.base.ResponseCallback
import com.action.dualcontrol.model.DeviceSerachModel
import com.action.dualcontrol.model.DevicesInfo

interface DeviceSerachContract {
    interface Model : IBaseModel{

        fun startSearchDevice(callback : ResponseCallback<ArrayList<DevicesInfo>>)
        fun stopSearchDevice()

    }
    interface View : IBaseView{
        fun updateList(devices:ArrayList<DevicesInfo>)
    }
    interface Presenter{
        fun startSearchDevice()
        fun stopSearchDevice()
    }

}