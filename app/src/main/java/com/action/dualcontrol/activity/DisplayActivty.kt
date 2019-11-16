package com.action.dualcontrol.activity

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.action.dualcontrol.Contract.DisplayContract
import com.action.dualcontrol.R
import com.action.dualcontrol.base.BaseActivity
import com.action.dualcontrol.presenter.DisplayPresent
import com.action.dualcontrol.utils.HideSystemUIUtils
import com.action.dualcontrol.utils.IpUtils
import com.action.dualcontrol.utils.LogUtils
import com.action.dualcontrol.widget.ImageSurfaceView
import com.action.dualcontrol.widget.LosthostDialog


class DisplayActivty:BaseActivity() ,DisplayContract.View{

    var ipFirst:String?=null
    var ipSecond:String?=null
    var mPresent:DisplayPresent? = null

    var widthPixels :Int = 0
    var heightPixels :Int = 0

    var dialog:AlertDialog? = null

    val flFail:LinearLayout by lazy {   View.inflate(this, com.action.dualcontrol.R.layout.fl_connect_fail, null) as LinearLayout}
    val fl by lazy {  findViewById<FrameLayout>(com.action.dualcontrol.R.id.fl_disconnect) }

    override fun showLoading() {
        pbLoading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        pbLoading.visibility = View.GONE
    }

    override fun connectSuccess() {
    }

    override fun connectFail() {
        LogUtils.i("connectFail")
        //createDialog()
        showLost()
    }

    fun showLost(){
        if (dialog != null && dialog?.isShowing!!) return
        val build = LosthostDialog(this)
        build.setOnclickListener(object : LosthostDialog.OnclickListener {
            override fun onClick() {
                LogUtils.i("showLost---finish")
                finish()
            }
        })
        build?.show()
    }

    fun createDialog(){
        if (dialog != null && dialog?.isShowing!!) return
        val build = AlertDialog.Builder(this)
        build.setTitle(com.action.dualcontrol.R.string.display_lost_host)
        build.setMessage(com.action.dualcontrol.R.string.network_please_check_the_network)
        build.setPositiveButton("OK") { _, _ ->
            finish()
        }
        dialog = build.show()
    }

    override fun displayBitmap(bitmap: Bitmap?) {
//        LogUtils.i("displayBitmap---bitmap:$bitmap")
        bitmap?.let {
            sImageView.setBitmap(it)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)
    }

    override fun getLayoutResID(): Int {
        return com.action.dualcontrol.R.layout.activity_display
    }

    val sImageView by lazy { findViewById<ImageSurfaceView>(com.action.dualcontrol.R.id.image_sv) }
    val pbLoading by lazy { findViewById<ProgressBar>(com.action.dualcontrol.R.id.display_pb) }


    override fun initPresent() {
        if(mPresent == null){
            mPresent = DisplayPresent()
            mPresent?.attachView(DisplayActivity@this)
        }


    }

    override fun detachPresenter() {
        mPresent?.detachView()
        mPresent = null
    }
    override fun initData() {
        super.initData()
        sImageView.setBitmap(BitmapFactory.decodeResource(resources, com.action.dualcontrol.R.mipmap.voxx_link_new)) // test
        getIp()
        getScreenSize()
        HideSystemUIUtils.hideSystemUI(this@DisplayActivty)
    }

    private fun getScreenSize(){
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val defaultDisplay = wm.defaultDisplay
        val displayMetrics = DisplayMetrics()
        defaultDisplay.getRealMetrics(displayMetrics)

        widthPixels = displayMetrics.widthPixels
        heightPixels = displayMetrics.heightPixels

        LogUtils.i( "onCreate：dm---:$widthPixels heightPixels$heightPixels")
        //出现获取的手机分辨率为反的情况，导致向车机传输坐标不准确的情况
        val temp: Int
        if (widthPixels < heightPixels) {
            temp = widthPixels
            widthPixels = heightPixels
            heightPixels = temp
        }
    }

    private fun getIp() {
        val extras = intent.extras
        ipFirst = extras.getString(IpUtils.IP_FIRST,null)
        ipSecond = extras.getString(IpUtils.IP_SECOND,null)
        LogUtils.i("getIp---ipFirst:${ipFirst}  ipSecond:${ipSecond}")
    }

    override fun onResume() {
        super.onResume()
        LogUtils.i("onResume")
        mPresent?.setScreenSize(widthPixels,heightPixels)
        mPresent?.connect(ipFirst,ipSecond)
    }

    override fun onPause() {
        super.onPause()
        mPresent?.disConnect()
        finish()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mPresent?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }


}