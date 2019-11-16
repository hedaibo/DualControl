package com.action.dualcontrol.utils

import kotlin.experimental.and
import kotlin.experimental.or

fun main() {
    println("hello 0xFF:${0xFF} byte:${0xFF.toByte()}")

    var bytes = ByteArray(3)
    bytes[0] = 0xFF.toByte()
    bytes[1] = 0xFF.toByte()
    bytes[2] = 0xFF.toByte()

   // println("111111----${bytes[0].toInt() and 0xFF}")
    println("   ${bufferToInt(bytes)}")

}

fun bufferToInt(bytes:ByteArray):Int{
    var value:Int = ((bytes[0].toInt() and 0xFF) or ((bytes[1].toInt() and 0xFF) shl 8 )  or ((bytes[2].toInt() and 0xFF) shl 16))
    return value
}


