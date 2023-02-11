package com.sjb.securitydoormanager.serialport

import HexUtil
import com.orhanobut.logger.Logger
import java.util.ArrayDeque

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
                if (buf[i+1]==DataProtocol.HEAD_CMD_2){
                    cmdLen = buf[i + 2].toInt()
                    if (bufLen - i -1< cmdLen) {
                        // 从命令头部开始的数据长度不足一条完整的命令
                        // 将已读指针往后移，在开始命令之前的数据舍弃掉
                        pollQueue(i)
                        return false
                    } else {
                        pollQueue(i)
                        // 取出一条完整的命令
                        val oneCmd=Utils.getBytes(buf,i,cmdLen)
                        val msg= HexUtil.formatHexString(oneCmd,true)
                        Logger.i("serialPort Receive：$msg")

                    }
                }

            }
        }
        return true
    }
}

