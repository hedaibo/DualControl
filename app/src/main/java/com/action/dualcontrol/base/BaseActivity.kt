package com.action.dualcontrol.base

import android.os.Bundle
import android.os.Message
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

import com.action.dualcontrol.utils.LogUtils

abstract class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResID())
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initPresent()
        initData()
        initListener()
    }

    override fun onDestroy() {
        LogUtils.i("onDestroy")
        super.onDestroy()
        detachPresenter()
    }

    open fun detachPresenter() {

    }

    open fun initPresent() {

    }


    open fun initListener(){

    }

    open fun initData(){

    }

    abstract fun getLayoutResID(): Int
}