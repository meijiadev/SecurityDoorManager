package com.sjb.securitydoormanager.serialport

import java.lang.StringBuilder
import java.util.*
import kotlin.experimental.and
import kotlin.jvm.JvmOverloads

object Utils {
    fun getBytes(buffer: ByteArray, offset: Int, length: Int): ByteArray {
        var num = 0
        val dstbyte = ByteArray(length)
        num = 1
        while (num <= length) {
            dstbyte[num - 1] = buffer[offset + num - 1]
            num++
        }
        return dstbyte
    }

    fun putBytes(dst: ByteArray, offset: Int, source: ByteArray, length: Int): Int {
        var num = 0
        num = 0
        while (num < length) {
            dst[offset + num] = source[num]
            num++
        }
        return offset + num
    }

    fun getBytes(buffer: Array<Byte>, offset: Int, length: Int): ByteArray {
        var num = 0
        val dstbyte = ByteArray(length)
        num = 1
        while (num <= length) {
            dstbyte[num - 1] = buffer[offset + num - 1]
            num++
        }
        return dstbyte
    }

    /**
     * 数组转为16进制字符串输凿
     *
     * @param buffer
     * @return
     */
    @JvmOverloads
    fun byteBufferToHexString(buffer: ByteArray, length: Int = buffer.size): String {
        val mBuilder = StringBuilder()
        val startIndex = 0
        for (i in startIndex until length) {
            val v: Int = (buffer[i] and 0xFF.toByte()).toInt()
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                mBuilder.append(0)
            }
            mBuilder.append(hv)
            //mBuilder.append(" ");
        }
        return ("len:" + length + " [ " + mBuilder.toString().uppercase(Locale.getDefault())
                + " ] ")
    }

    /**
     * 把一个整数变化为丿个小敿
     * 妿115-> 0.115
     * 50->0.5
     * @param v
     * @return
     */
    fun intToDec(v: Int): Double {
        return if (v < 10) {
            v / 10.0
        } else if (v < 100) {
            v / 100.0
        } else if (v < 1000) {
            v / 1000.0
        } else {
            v / 10000.0
        }
    }

    //byte ԫ int քРۥתۻ
    fun intToByte(x: Int): Byte {
        return x.toByte()
    }

    fun byteToInt(b: Byte): Int {
        return (b and 0xFF.toByte()).toInt()
    }
}