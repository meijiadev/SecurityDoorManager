package com.sjb.securitydoormanager.serialport

import com.orhanobut.logger.Logger
import java.util.ArrayDeque
import kotlin.experimental.and

/**
 * author:10500
 * date: 2023/1/12 9:42
 * desc: DataMcuProcess 数据最终解析处理的类
 */
class DataMcuProcess : DataProcBase() {

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
                    DataProtocol.funcCode = oneCmd[2].toInt()
                    if (DataProtocol.funcCode == 0x01) {
                        // 手机门上传的数据
                        if (cmdLen == 62) {
                            phoneDoorParse(oneCmd)
                        }
                        // 普通门上传的数据
                        if (cmdLen == 46) {

                        }
                    }

                    val msg = HexUtil.formatHexString(oneCmd, true)
                    Logger.i("serialPort Receive：$msg")

                }


            }
        }
        return true
    }

    private val ff = 0xFF.toByte()

    /**
     * 解析手机门的上传数据
     */
    fun phoneDoorParse(data: ByteArray) {
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
        // 从前往后的报警数量
        val forwardWaring =
            data[9].and(ff).toInt().shl(14) +
                    data[10].and(ff).toInt().shl(7) +
                    data[11].and(ff).toInt()
        val backwardWaring =
            data[12].and(ff).toInt().shl(14) +
                    data[13].and(ff).toInt().shl(7) +
                    data[14].and(ff).toInt()
        val zoneType = data[15].and(ff).toInt()
        // 门分区 0:6区   1:12区   2:18区   3:36区   4:33区   5:无极分区
        //7: 8区  8:16区  9:24区  10:48区  11:45区  12:无极分区（8线圈）
        //13:6区   14:12区   15:18区 （门柱灯不一样，不能大于18区）
        //16:3区   17:6区   18:9区  19:18区  20:15区  21:3区   22：6区   23:9区   24:无极分区（3线圈）
        //25:6区   26:12区   27:18区 （数码管门，不能大于18区）


    }
}

