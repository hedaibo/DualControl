package com.action.dualcontrol.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.action.dualcontrol.R

class LosthostDialog: AlertDialog {

    constructor(context: Context?) : super(context)
    constructor(
        context: Context?,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    constructor(context: Context?, themeResId: Int) : super(context, themeResId)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fl_connect_fail)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btOk = findViewById<Button>(R.id.bt_lost_ok)
        btOk.setOnClickListener {
            mListener?.onClick()
        }
    }
    var mListener:OnclickListener? = null
    fun setOnclickListener(listener:OnclickListener){
        mListener = listener
    }
    interface OnclickListener{
        fun onClick()
    }
}