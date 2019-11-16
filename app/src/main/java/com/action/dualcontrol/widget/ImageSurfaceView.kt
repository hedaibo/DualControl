package com.action.dualcontrol.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.os.SystemClock

import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.action.dualcontrol.utils.LogUtils

class ImageSurfaceView: SurfaceView, SurfaceHolder.Callback {
    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        surfaceHodler = p0
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var surfaceHodler:SurfaceHolder? = null
    private val mMatrix by lazy { Matrix() }
    private val mPaint by lazy { Paint()}
    init {
        surfaceHodler = holder
        surfaceHodler?.addCallback(this@ImageSurfaceView)
    }

    fun setBitmap(bitmap:Bitmap){
        val canvas = surfaceHodler?.lockCanvas()
        if (null == canvas || null == surfaceHodler) return
        if (bitmap == null) return
        var time = SystemClock.uptimeMillis()
//        var reduceBitmap = getReduceBitmap(bitmap,width,height)
//        LogUtils.i("setBitmap---reduceBitmap-----time:${SystemClock.uptimeMillis() - time}")
       // canvas.drawBitmap(bitmap,mMatrix,mPaint)
        canvas.drawBitmap(bitmap,Rect(0,0,bitmap.width,bitmap.height),Rect(0,0,width,height),mPaint)
        LogUtils.i("setBitmap----drawBitmap----time:${SystemClock.uptimeMillis() - time}")
        surfaceHodler?.unlockCanvasAndPost(canvas)
    }


    private fun getReduceBitmap(bitmap: Bitmap, w: Int, h: Int): Bitmap {

        val width = bitmap.width
        val hight = bitmap.height
        val matrix = Matrix()
        val wScake = w.toFloat() / width
        val hScake = h.toFloat() / hight
        matrix.postScale(wScake, hScake)
        return Bitmap.createBitmap(bitmap, 0, 0, width, hight, matrix, true)
    }


}