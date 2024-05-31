package com.sjb.securitydoormanager.serialport

/**
 * author:10500
 * date: 2023/1/12 11:13
 * desc: DataProtocol ，数据格式协议
 */
object DataProtocol {
    // 最短的命令长度至少为5个字节
    val MIN_CMD_LENG = 4

    // 头部命令
    val HEAD_CMD_1 = 0x80.toByte()

    // 类型标志
    var funcCode = 0x00

    // 十二路adc采集的数据
    data class Zone12(
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

    val CODE_102 = 102              // 红外开关状态有变化时上传一次状态信息 安检门 TO 上位机
    val CODE_6 = 0x06              // 安检门开关机
    val CODE_1 = 0x01             // 安检门上传数据  普通门-数据长度42，总长度为46   手机门-数据长度58，总长度为62




    /**
     * 数据同步，和安检门建立连接
     */
    val connectData = byteArrayOf(
        HEAD_CMD_1,
        0x04,
        0x00,
        0x7F
    )


    /**
     * 只要有人通过安检门自动上传数据    -TO 安检门
     */
    val data_0x04 = byteArrayOf(
        HEAD_CMD_1,
        0x04,
        0x04,
        0x7F
    )

    /**
     * 关闭安检门自动上传数据
     */
    val data_0x05 = byteArrayOf(
        HEAD_CMD_1,
        0x04,
        0x05,
        0x7F
    )

    // 要求安检门上传配置参数
    val data_0x02 = byteArrayOf(
        HEAD_CMD_1,
        0x04,
        0x02,
        0x7F
    )


}