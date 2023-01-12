package com.sjb.securitydoormanager.serialport

/**
 * author: MJ
 * date: 2023/1/10 14:17
 * desc: IDataProc 接收到的串口数据
 */
interface IDataProc {
    fun onDataReceive(buffer: ByteArray, size: Int)
}