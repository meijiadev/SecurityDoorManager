package com.sjb.securitydoormanager.serialport

import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.orhanobut.logger.Logger
import java.util.ArrayDeque
import kotlin.experimental.and

/**
 * author:10500
 * date: 2023/1/12 9:42
 * desc: DataMcuProcess 数据最终解析处理的类
 */
class DataMcuProcess : DataProcBase() {
    var alarmGoodsEvent = UnPeekLiveData<String>()      // 报警物品
    var passNumberEvent = UnPeekLiveData<Int>()         // 通过的人数
    var alarmNumberEvent = UnPeekLiveData<Int>()        // 通过的报警次数
    var locationEvent = UnPeekLiveData<String>()        // 报警的位置

    var enterType = 0                  // 进入的方向 0： 从前往后进   1：从后往前

    override fun findCmdProc(queueByte: ArrayDeque<Byte>): Boolean {
        val buf = queueByte.toByteArray()
        if (buf == null) {
            return false
        }
        val bufLen = buf.size
        var cmdLen = 0
        // 判断命令长度是否满足最短长度
        if (bufLen < DataProtocol.MIN_CMD_LENG) {
            return false
        }

        for (i in buf.indices) {
            // 当前位置的字节是命令头部
            if (buf[i] == DataProtocol.HEAD_CMD_1) {
                if (i == buf.size - 1) return false
                cmdLen = buf[i + 1].toInt()
                if (bufLen - i < cmdLen) {
                    // 从命令头部开始的数据长度不足一条完整的命令
                    // 将已读指针往后移，在开始命令之前的数据舍弃掉
                    pollQueue(i)
                    return false
                } else {
                    pollQueue(i)
                    // 取出一条完整的命令
                    val oneCmd = Utils.getBytes(buf, i, cmdLen)
                    //取出完整命令后，将队列中的数据清楚
                    pollQueue(cmdLen)
                    DataProtocol.funcCode = oneCmd[2].toInt()
                    val msg = HexUtil.formatHexString(oneCmd, true)
                    // 安检门上传的数据
                    if (DataProtocol.funcCode == 0x01) {
                        // 手机门上传的数据
                        if (cmdLen == 62) {
                            Logger.i("serialPort Receive：$msg")
                            phoneDoorParse(oneCmd)
                        }
                        // 普通门上传的数据
                        if (cmdLen == 46) {

                        }
                    }

                    if (DataProtocol.funcCode == 0x00) {
                        Logger.i("同步成功！serialPort Receive：$msg")
                    }

                    if (DataProtocol.funcCode == 0x04) {
                        Logger.i("已设置有人通过自动上传数据：$msg")
                    }

                    if (DataProtocol.funcCode == 0x66) {      // code=102
                        // 红外开关状态变化时上传一次
                        //1: 从前往后，挡住前面红外       2: 从前往后，离开前面红外
                        //3: 从前往后，挡住后面红外       4: 从前往后，离开后面红外
                        //11: 从后往前，挡住后面红外      12: 从后往前，离开后面红外
                        //13: 从后往前，挡住前面红外      14: 从后往前，离开前面红外
                        val irState = oneCmd[3].toInt()
                        enterType =
                            if (irState == 1 || irState == 2 || irState == 3 || irState == 4) {
                                0
                            } else {
                                1
                            }
                        Logger.i("红外发生变化：$irState")
                    }
                }
            }
        }
        return true
    }

    private val ff = 0xFF.toByte()

    /**
     * 解析手机门的上传数据
     */
    private fun phoneDoorParse(data: ByteArray) {
        var zoneNumber = 6           // 默认6区
        locationEvent.postValue("")
        // 从前往后的数量
        val passForward =
            data[3].and(ff).toInt().shl(14) +
                    data[4].and(ff).toInt().shl(7) +
                    data[5].and(ff).toInt()
        // 从后往前的数量
        val passBackward =
            data[6].and(ff).toInt().shl(14) +
                    data[7].and(ff).toInt().shl(7) +
                    data[8].and(ff).toInt()
        Logger.i("从前往后走的数量：$passForward,从后往前走的数量：$passBackward")
        passNumberEvent.postValue(passForward + passBackward)
        // 从前往后的报警数量
        val forwardWaring =
            data[9].and(ff).toInt().shl(14) +
                    data[10].and(ff).toInt().shl(7) +
                    data[11].and(ff).toInt()
        val backwardWaring =
            data[12].and(ff).toInt().shl(14) +
                    data[13].and(ff).toInt().shl(7) +
                    data[14].and(ff).toInt()
        Logger.i("从前往后走的报警数：$forwardWaring,从后往前走的报警数：$backwardWaring")
        alarmNumberEvent.postValue(forwardWaring + backwardWaring)
        val zoneType = data[15].and(ff).toInt()     // 判断多少区的安检门
        if (zoneType == 0 || zoneType == 13 || zoneType == 17 || zoneType == 22 || zoneType == 25) {
            zoneNumber = 6
        }
        if (zoneType == 1 || zoneType == 14 || zoneType == 26) {
            zoneNumber = 12
        }

        if (zoneType == 2 || zoneType == 15 || zoneType == 19 || zoneType == 27) {
            zoneNumber = 18
        }

        // 门分区 0:6区   1:12区   2:18区   3:36区   4:33区   5:无极分区
        //7: 8区  8:16区  9:24区  10:48区  11:45区  12:无极分区（8线圈）
        //13:6区   14:12区   15:18区 （门柱灯不一样，不能大于18区）
        //16:3区   17:6区   18:9区  19:18区  20:15区  21:3区   22：6区   23:9区   24:无极分区（3线圈）
        //25:6区   26:12区   27:18区 （数码管门，不能大于18区）
        // data16 ---data39 表示
        val zone0_6 = hexToBinaryArray(data[40])              // 从 0-6 区是否报警
        val zone7_13 = hexToBinaryArray(data[41])             // 7-13区
        // 以下在区位大于六区才用得上
        val zone14_20 = hexToBinaryArray(data[42])            // 14-20区
        val zone21_27 = hexToBinaryArray(data[43])            // 21-27区
        val zone28_31 = hexToBinaryArray(data[44])            // 28-31区

        val alarm6Zone = BooleanArray(12)
        for (i in 0..11) {
            if (i > 6) {
                alarm6Zone[i] = zone7_13[i - 6 - 1]
            } else {
                alarm6Zone[i] = zone0_6[i]
            }
        }
        // 总共报警的区位列表
        val alarmZoneList = mutableListOf<Int>()
        var alarmZoneStr = ""
        var alarmLocation = ""
        for (i in 0..5) {
            if (zoneNumber == 6) {
                if (alarm6Zone[i * 2] && alarm6Zone[i * 2 + 1]) {
                    alarmZoneList.add(i + 1)
                    Logger.i("报警的区位是：${i + 1}")
                    val location = resolutionZone(i)
                    Logger.i("报警的位置是：$location")
                    alarmLocation += location
                }
            }
            if (zoneNumber == 12 || zoneNumber == 18) {
                if (alarm6Zone[i * 2] || alarm6Zone[i * 2 + 1]) {
                    alarmZoneList.add(i + 1)
                    Logger.i("报警的区位是：${i + 1}")
                    val location = resolutionZone(i)
                    Logger.i("报警的位置是：$location")
                    alarmLocation += location
                }
            }
            alarmZoneStr += if (alarm6Zone[i * 2]) "1" else "0"
            alarmZoneStr += if (alarm6Zone[i * 2 + 1]) "1" else "0"

        }
        locationEvent.postValue(alarmLocation)
        Logger.i("报警区位分布：$alarmZoneStr")
        //80 3e 01 00 00 7a 00 00 7b 00 00 67 00 00 68 0d 0a 03 14 00 24 0b 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 40 01 00 00 00 00 00 00 00 00 00 00 06 00 00 00 00 00 00 00 00 7f
        //80 3e 01 00 00 7a 00 00 7b 00 00 67 00 00 68 0d 0a 03 14 00 24 0b 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 40 01 00 00 00 00 00 00 00 00 00 00 06 00 00 00 00 00 00 00 00 7f
        // 0：没有金属          1：工具刀枪          2：保温杯
        //3：违禁品            4：铜块铝块          5：易拉罐
        //6：电子产品          7：日常金属          8：雨伞
        //9：混合金属          10：铁罐铁盒         11：耳机等电子产品
        // data[45]-data[60] 总共 16个区
        val metalTypes = mutableListOf<Int>()
        for (i in 0..5) {
            val type = data[45 + i * 2 + 1].and(ff).toInt()
            metalTypes.add(type)
            if (type != 0) {
                Logger.i("${(i + 1) * 2 - 1}区报警，东西是：$type")
                if (type == 6 || type == 11) {
                    alarmGoodsEvent.postValue("电子产品")
                    return
                } else {
                    alarmGoodsEvent.postValue("其他物品")
                }
            }
        }
    }

    /**
     * 分析区位对应身体的哪个位置
     */
    private fun resolutionZone(zone: Int): String {
        val location = when (zone) {
            0 -> "小腿"
            1 -> "大腿"
            2 -> "腰部"
            3 -> "胸部"
            4 -> "颈部"
            5 -> "头部"
            else -> ""
        }
        return location
    }

    private fun hexToBinaryArray(hexByte: Byte): BooleanArray {
        val binaryArray = BooleanArray(7)
        var n = hexByte.toInt() and 0xFF // 确保为正数
        Logger.i("hexByte：$n")
        for (j in 0..7) {
            if (j != 0) {
                binaryArray[j - 1] =
                    n and 0x80 != 0                                   //  0000 0001       0100 0000              80= 1000 0000
            }
            n = n shl 1
        }
        binaryArray.reverse()
//        var msg=""
//        for (i in 0..6){
//            msg+=if (binaryArray[i]) "1" else "0"
//        }
//        Logger.i("msg:$msg")
        return binaryArray
    }
}


// 1000 0000
// 0000 0110
// 0 00 00 00