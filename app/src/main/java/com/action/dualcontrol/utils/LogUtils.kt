package com.action.dualcontrol.utils

import android.util.Log


object LogUtils {
    const val debug = true
    private fun getClassName(): String {
        val element = Thread.currentThread().stackTrace
        val offset = getStackOffset(element)
        var sElement = element[offset]
        var cls = sElement.className
        var method = sElement.methodName
        var file = sElement.fileName
        //Log.i("cls",  "cls:${cls}  file:${file}  method:${method}")
        return file;
    }

    private fun getStackOffset(trace: Array<StackTraceElement>): Int {
        var i = 3
        while (i < trace.size) {
            val t = trace[i]
            val name = t.className
            if (name != LogUtils::class.java.name) {
                return i
            }
            i++
        }
        return 2
    }

    fun i(msg: String) {
        if (!debug) return
        var tag = getClassName()
        Log.i(tag, "hdb---$msg")
    }

    fun d(msg: String) {
        if (!debug) return
        var tag = getClassName()
        Log.i(tag, msg)
    }

    fun v(msg: String) {
        if (!debug) return
        var tag = getClassName()
        Log.i(tag, msg)
    }

    fun w(msg: String) {
        if (!debug) return
        var tag = getClassName()
        Log.i(tag, msg)
    }

    fun e(msg: String) {
        //if (!debug) return
        var tag = getClassName()
        Log.i(tag, msg)
    }


}