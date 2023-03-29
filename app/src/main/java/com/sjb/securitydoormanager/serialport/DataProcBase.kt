package com.sjb.securitydoormanager.serialport

import java.util.ArrayDeque

/**
 * author:MJ
 * date: 2023/1/11 10:04
 * desc: DataProcBase
 */
abstract class DataProcBase : IDataProc {

    protected var mQueueByte = ArrayDeque<Byte>(1024)

    abstract fun findCmdProc(queueByte: ArrayDeque<Byte>): Boolean


    /*
     * 解析读取的字节数组
     */
    override fun onDataReceive(buffer: ByteArray, size: Int) {
        for (i in 0 until size) {
            mQueueByte.add(buffer[i])
        }
      findCmdProc(mQueueByte)
//        while (cmd){
//            cmd=findCmdProc(mQueueByte)
//        }

    }

    /**
     *  删除元素
     */
    fun pollQueue(count: Int) {
        for (i in 0 until count) {
            mQueueByte.poll()
        }

    }


}