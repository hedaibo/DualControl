package com.action.dualcontrol.activity

import android.content.Intent
import android.os.Message
import com.action.dualcontrol.R
import com.action.dualcontrol.base.BaseActivity
import com.action.dualcontrol.base.IBaseHandler
import com.action.dualcontrol.utils.HandlerUtils
import com.action.dualcontrol.utils.HideSystemUIUtils

class SplashActivity : BaseActivity() ,IBaseHandler{

    val splashHandler by lazy { HandlerUtils(this@SplashActivity) }
    override fun handleMessage(msg: Message?) {
        when(msg?.what){
            HandlerUtils.SPLASH_REMOVE_IMAGE ->{
                startActivity(Intent(this@SplashActivity,DeviceListActivty::class.java))
                this@SplashActivity.finish()
            }

        }
    }
    override fun getLayoutResID(): Int {
        return R.layout.activity_splash
    }

    override fun initData() {
        HideSystemUIUtils.hideSystemUI(this@SplashActivity)
    }

    override fun onResume() {
        super.onResume()
        splashHandler.sendEmptyMessageDelayed(HandlerUtils.SPLASH_REMOVE_IMAGE,HandlerUtils.SPLASH_REMOVE_IMAGE_DELAY)
    }

    override fun onPause() {
        super.onPause()
        splashHandler.removeMessages(HandlerUtils.SPLASH_REMOVE_IMAGE)
    }


}
