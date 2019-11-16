package com.action.dualcontrol.base

import android.graphics.Bitmap

interface ModelCallback {
    fun connectSuccess()
    fun connectFail()
    fun displayBitmap(bitmap: Bitmap?)
}