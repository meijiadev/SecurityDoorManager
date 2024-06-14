package com.sjb.securitydoormanager.serialport

import androidx.lifecycle.MutableLiveData
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

    var doorParamEvent = MutableLiveData<ByteArray>()    // 安检门参数

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
                    // 上位机和安检门同步成功
                    if (DataProtocol.funcCode == 0x00) {
                        Logger.i("同步成功！serialPort Receive：$msg")
                    }
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
                    // 安检门返回参数指令
                    if (DataProtocol.funcCode == 0x02) {
                        Logger.i("serialPort Receive：$msg")
                        // 普通门
                        if (cmdLen == 35) {

                        }
                        // 手机安检门
                        if (cmdLen == 97) {
                            phoneDoorArgParse(oneCmd)
                        }

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
     * 手机安检门参数解析
     */
    private fun phoneDoorArgParse(data: ByteArray) {
        doorParamEvent.postValue(data)
        // 1区的灵敏度  0-999
        val zone1Spl = data[3].toInt() * 128 + data[4].toInt()
        val zone2Spl = data[5].toInt() * 128 + data[6].toInt()
        val zone3Spl = data[7].toInt() * 128 + data[8].toInt()
        val zone4Spl = data[9].toInt() * 128 + data[10].toInt()
        val zone5Spl = data[11].toInt() * 128 + data[12].toInt()
        val zone6Spl = data[13].toInt() * 128 + data[14].toInt()

        val overallSpl = data[19].toInt() * 128 + data[20].toInt()
        Logger.i(
            "当前整体灵敏度：$overallSpl,\n " +
                    "1区灵敏度：$zone1Spl \n" +
                    "2区灵敏度：$zone2Spl \n" +
                    "3区灵敏度：$zone3Spl \n" +
                    "4区灵敏度：$zone4Spl \n" +
                    "5区灵敏度：$zone5Spl \n" +
                    "6区灵敏度：$zone6Spl"
        )
        val zones = data[21].toInt() and 0x1f
        when (zones) {
            // 6区
            0b01101 -> {
                Logger.i("当前设置的是6区门")
            }
            // 12 区
            0b01110 -> {
                Logger.i("当前设置的是12区门")
            }
            // 18 区
            0b01111 -> {
                Logger.i("当前设置的是18区门")
            }
        }

        val frequency = data[22].toInt()
        Logger.i("当前频点：$frequency")
        //6--2位：语言0:中文  1:英文   2:土耳其语   3:俄语   4:波兰语
        //第0位：语言锁      0:语言可选          1:只有英文
        val languageLock = data[23].toInt() and 0b0000001
        val language = data[23].toInt().shr(2)  //右移2位
        Logger.i("语言锁：$languageLock,当前语言$language,原始数据：${data[23].toInt().toString(2)}")
        //6位：开机自动设频   0：否   1：是
        //5位：上电自动开机   0：是   1：否
        //4位：报警模式       0：挡住红外触发   1：飞物报警
        //3~0位：0：测温测金属门  1：测温门   2：普通测金属门  3：便携门
        //        4：手机门        5：带测温手机门
        // 获取安检门的类型
        val doorType = data[24].toInt() and 0b00001111
        // 报警模式
        val alarmMode = data[24].toInt().shr(4) and 0b00000001
        val powerOnAuto = data[24].toInt().shr(5) and 0b00000001
        val freOnAuto = data[24].toInt().shr(6) and 0b00000001
        Logger.i(
            "原始数据：${data[24].toInt().toString(2)} \n" +
                    "安检门类型：$doorType \n" +
                    "报警模式：$alarmMode \n" +
                    "上电自动开机:$powerOnAuto \n" +
                    "上电自动设频:$freOnAuto"
        )
        //5~4位：探测模式    0:所有金属   1:探测铁磁物     2:探测非铁磁物
        //3~2位：红外计数模式  0:统一计数        1:前后分别计数
        //                     2:前加后减         3:前减后加
        //（tance_mode和IR_mode这两个参数对手机门无意义，可以不管）
        //1~0位：闸机动作    00: 报警打开        01: 报警关闭
        //                    10: 不报警开        11: 不报警关
        val brakeOpera = data[25].toInt() and 0b00000011
        //3~0位：铃声         0000~1001  铃声0~9
        val ringtones = data[26].toInt()
        // 休眠时间(0~99分钟），stand_time分钟内没人通过安检门，安检门进入休眠状态，有人通过自动唤醒。stand_time设置为0表示永不休眠
        val standTime = data[27].toInt()
        // 屏幕亮度 0--100
        val lightValue = data[28].toInt()
        // 音量：6~0位 0--------9(预留后续升级到0----100)
        val volume = data[30].toInt()
        val keyVolume = data[31].toInt()
        //6~0位  0----9：报警时LED灯亮的时间，步进0.5S
        val ledDelay = data[32].toInt()
        //报警持续的时间 6~0位  0----9：报警时铃声持续时间，步进0.5S
        val alarmDelay = data[33].toInt()
        Logger.i(
            "铃声：$ringtones \n" +
                    "休眠时间：$standTime \n" +
                    "屏幕亮度：$lightValue \n" +
                    "音量：$volume \n" +
                    "按键音量：$keyVolume \n" +
                    "报警时led灯亮时间：$ledDelay \n" +
                    "报警持续的时间：$alarmDelay"
        )
        // 手机门相关特有参数
        // 1-8区报手机灵敏度 0~999
        val phoneSensi1 = data[34].toInt() * 128 + data[35].toInt()
        val phoneSensi2 = data[36].toInt() * 128 + data[37].toInt()
        val phoneSensi3 = data[38].toInt() * 128 + data[39].toInt()
        val phoneSensi4 = data[40].toInt() * 128 + data[41].toInt()
        val phoneSensi5 = data[42].toInt() * 128 + data[43].toInt()
        val phoneSensi6 = data[44].toInt() * 128 + data[45].toInt()
        val phoneSensi7 = data[46].toInt() * 128 + data[47].toInt()
        val phoneSensi8 = data[48].toInt() * 128 + data[49].toInt()
        Logger.i("1-8区手机报警的灵敏度：$phoneSensi1,\n $phoneSensi2,\n $phoneSensi3,\n $phoneSensi4,\n $phoneSensi5,\n $phoneSensi6,\n $phoneSensi7,\n $phoneSensi8")
        //  磁性报警灵敏度0~999，数值越小灵敏度越高。被测物磁性超过设置的灵敏度时报警（类似金属量超过金属灵敏度）。所有区都是一样的灵敏度
        val magnetism0 = data[50].toInt() * 128 + data[51].toInt()
        // 磁性金属比值灵敏度0~999，用来辅助判断金属分类。主要用于判断磁铁，带磁工具刀枪和耳机等。这个数值越小越容易报这几种物品
        val magnetism1 = data[52].toInt() * 128 + data[53].toInt()
        //磁性金属灵敏度0~999 ,磁性超过这个值的金属划分到磁性金属大类（工具刀枪，保温杯，雨伞，铁制品，手机等），然后在其他条件细分，磁性小于这个值划分到非磁金属大类（铜、铝、铅、金、银等）
        val magnetism2 = data[54].toInt() * 128 + data[55].toInt()
        // 老人机磁性灵敏度：老人机的金属量超过金属灵敏度，磁性超过老人机磁性灵敏度就报手机（不管金属幅度有没有超过报手机灵敏度）
        val magnetism3 = data[56].toInt() * 128 + data[57].toInt()
        Logger.i("磁性各个参数：$magnetism0, \n $magnetism1, \n $magnetism2, \n $magnetism3")
        // data[58]-data[68]    01 02 03 04 05 06 07 08 09 0a 0b    1-11
        //识别为1类金属，报警成A_metal_type[0]类
        //示例：A_metal_type[0]=6；安检门识别到通过的金属为1类（工具刀枪），报警为6类（电子产品） 暂不处理
        //data[69]-data[72] 识别为某种金属怎么报警 暂不处理
        // data[73]-data[94] 金属通过安检门时，相位落在phase_s[`][`]与phase_s[`][`]之间识别为*类金属 暂不处理

        //工作模式:
        //11:违禁品探测模式  12:电子产品探测模式  13:违禁品加电子产品探测模式
        //14:全金属探测模式  10-----14:自定义模式
        val workMode = data[95]
        Logger.i("工作模式：$workMode")
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
        // Logger.i("hexByte：$n")
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