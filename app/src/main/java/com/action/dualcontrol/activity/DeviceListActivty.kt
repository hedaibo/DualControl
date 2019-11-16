package com.action.dualcontrol.activity

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.action.dualcontrol.Contract.DeviceSerachContract
import com.action.dualcontrol.R
import com.action.dualcontrol.adapter.DeviceListAdapter
import com.action.dualcontrol.base.BaseActivity
import com.action.dualcontrol.base.WifiCallBack
import com.action.dualcontrol.model.DevicesInfo
import com.action.dualcontrol.presenter.DeviceSerachPresent
import com.action.dualcontrol.receiver.WifiChangeReceiver
import com.action.dualcontrol.utils.IpUtils
import com.action.dualcontrol.utils.LogUtils

class DeviceListActivty : BaseActivity() ,DeviceSerachContract.View{
    override fun updateList(list: ArrayList<DevicesInfo>) {
        mAdapter?.updateData(list)
    }

    val tvVersion by lazy { findViewById<TextView>(R.id.tv_search_version_number) }
    val rv_list by lazy { findViewById<RecyclerView>(R.id.rv_list) }
    var mAdapter :DeviceListAdapter? = null
    private var mPresent:DeviceSerachPresent? = null
    private var mWifiChangeReceiver:WifiChangeReceiver? = null
    override fun onResume() {
        super.onResume()
        registerNetworkBroadcast()
    }

    override fun onPause() {
        super.onPause()
        unregisterNetworkBroadcast()
        mPresent?.stopSearchDevice()
    }

    fun registerNetworkBroadcast(){
        if (mWifiChangeReceiver == null){
            var filter = IntentFilter()
            filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED")
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            mWifiChangeReceiver = WifiChangeReceiver(object:WifiCallBack{
                override fun onConnected() {
                    mPresent?.startSearchDevice()
                    IpUtils.openWifiBrocast(this@DeviceListActivty)
                }

                override fun onDisConnected() {
                    mPresent?.stopSearchDevice()
                }

            })
            registerReceiver(mWifiChangeReceiver,filter)
        }
    }
    fun unregisterNetworkBroadcast(){
        if (mWifiChangeReceiver != null){
            unregisterReceiver(mWifiChangeReceiver)
            mWifiChangeReceiver = null
        }
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_devicelist
    }

    override fun initData() {
        var version = getVersion()
        LogUtils.i("version:$version")
        tvVersion.setText(version)

        initRecycleViewParameter()
    }


    override fun initPresent(){
        if (mPresent == null){
            mPresent = DeviceSerachPresent()
            mPresent?.attachView(DeviceListActivty@this)
        }
    }

    override fun detachPresenter() {
        if (mPresent == null){
            mPresent?.detachView()
            mPresent = null
        }
    }

    fun initRecycleViewParameter(){
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_list.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this,DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(resources.getDrawable(R.drawable.bule_light))
        rv_list.addItemDecoration(itemDecoration)
        rv_list.itemAnimator = DefaultItemAnimator()
//        var num = 1..100
//        for (i in num){
//            mList.add("192.168.1.$i")
//        }

        mAdapter = DeviceListAdapter(this)
        rv_list.adapter = mAdapter
    }

    fun getVersion():String{
        var packageinfo = application.packageManager.getPackageInfo(packageName,0)
        var versionName = packageinfo.versionName
        var versionCode = packageinfo.versionCode
        LogUtils.i("versionName:${versionName}  versionCode:${versionCode}")
        return "VERSION:${versionName}.$versionCode"
    }


    override fun onDestroy() {
        LogUtils.i("onDestroy----------")
        super.onDestroy()
    }

}