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
    val HEAD_CMD_1 = 0x0D.toByte()
    val HEAD_CMD_2 = 0x0A.toByte()
    // 功能码
    var funcCode = 0x00
    // 红外状态
    var infraCode=0x00

    // 十二路adc采集的数据
    data class ADC12(
        var adc1: Int,
        var adc2: Int,
        var adc3: Int,
        var adc4: Int,
        var adc5: Int,
        var adc6: Int,
        var adc7: Int,
        var adc8: Int,
        var adc9: Int,
        var adc10: Int,
        var adc11: Int,
        var adc12: Int

    )



}