package com.action.dualcontrol.presenter

import com.action.dualcontrol.Contract.DeviceSerachContract
import com.action.dualcontrol.base.BasePresenter
import com.action.dualcontrol.base.ResponseCallback
import com.action.dualcontrol.model.DeviceSerachModel
import com.action.dualcontrol.model.DevicesInfo

class DeviceSerachPresent: BasePresenter<DeviceSerachContract.Model, DeviceSerachContract.View>() ,DeviceSerachContract.Presenter{
    override fun startSearchDevice() {
        if (isViewAttached()){
            getModel()?.startSearchDevice(object:ResponseCallback<ArrayList<DevicesInfo>>{
                override fun upateUi(response: ArrayList<DevicesInfo>) {
                    getView()?.updateList(response)
                }
            })
        }
    }


    override fun stopSearchDevice() {
        getModel()?.stopSearchDevice()
    }

    override fun CreateModel(): DeviceSerachContract.Model {
        return DeviceSerachModel()
    }
}