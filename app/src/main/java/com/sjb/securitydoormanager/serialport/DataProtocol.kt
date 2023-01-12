package com.sjb.securitydoormanager.serialport

/**
 * author:10500
 * date: 2023/1/12 11:13
 * desc: DataProtocol ，数据格式协议
 */
object DataProtocol {
    // 最短的命令长度至少为5个字节
    val MIN_CMD_LENG = 5

    // 头部命令
    val HEAD_CMD = 0xAA.toByte()

    // 功能码
    val CODE_CMD = ""


}