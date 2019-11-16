package com.action.dualcontrol.base

import android.os.Message

interface IBaseHandler {
    fun handleMessage(msg: Message?)
}