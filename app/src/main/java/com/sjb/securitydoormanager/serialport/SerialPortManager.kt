package com.sjb.securitydoormanager.serialport

import android.serialport.SerialPort
import androidx.annotation.RequiresPermission.Read
import com.orhanobut.logger.Logger
import com.sjb.securitydoormanager.serialport.DataProtocol.HEAD_CMD_1
import java.io.InputStream
import java.io.OutputStream

/**
 * author: MJ
 * date: 2023/1/10 11:31
 * desc: SerialPortManager
 */
class SerialPortManager private constructor() {

    private var serialPort: SerialPort? = null

    /**
     * 串口地址
     */
    private var path: String? = "/dev/ttyS0"

    /**
     * 波特率
     */
    private var baudrate: Int = 115200

    /**
     * 输入流，读取信息
     */
    private var mInputStream: InputStream? = null

    /**
     * 输出流，写入信息
     */
    private var mOutputStream: OutputStream? = null

    private var mIDataProc: IDataProc? = null

    /**
     * 读取线程
     */
    private var readThread: ReadThread? = null


    companion object {
        val instance: SerialPortManager by lazy(mode = LazyThreadSafetyMode.NONE) {
            SerialPortManager()
        }
    }

    fun setIData(iDataProc: IDataProc) {
        this.mIDataProc = iDataProc
    }

    /**
     * 设置串口地址
     */
    fun setPath(path: String) {
        this.path = path
    }

    /**
     * 设置波特率
     */
    fun setRate(rate: Int) {
        baudrate = rate
    }

    fun init() {
        kotlin.runCatching {
            serialPort = SerialPort.newBuilder(path, baudrate).build()
        }.onSuccess {
            mInputStream = serialPort?.inputStream
            mOutputStream = serialPort?.outputStream
            Logger.i("串口初始化：$path,$baudrate")
        }.onFailure {
            Logger.e("串口初始化失败：${it.message}")
        }

    }

    /**
     * 开始读取信息
     */
    fun startRead() {
        if (readThread == null) {
            readThread = ReadThread()
        }
        readThread?.start()
        Logger.i("start readThread serialport")
        // WriteThread().start()
    }


    /**
     * 给串口发送消息
     */
    fun sendMsg(msg: ByteArray) {
        Logger.i("写入数据：${HexUtil.formatHexString(msg, true)}")
        kotlin.runCatching {
            mOutputStream?.write(msg)
        }.onFailure {
            Logger.e("写入错误：${it.message}")
        }

    }

    /**
     * 程序结束时调用
     */
    fun onDestroy() {
        kotlin.runCatching {
            readThread?.interrupt()
            serialPort?.tryClose()
        }.onSuccess {
            readThread = null
            serialPort = null
        }.onFailure {
            Logger.e("报错信息：${it.message}")
        }
    }


    inner class ReadThread : Thread() {
        // 是否停止读取
        private var isReadStop = false

        private var ret = 0

        override fun run() {
            super.run()
            while (true) {
                if (isReadStop) {
                    break
                }
                // sleep(200)
                kotlin.runCatching {
                    val buffer = ByteArray(30)
                    mInputStream?.let {
                        ret = it.read(buffer)
                        if (ret > 0) {
                            mIDataProc?.onDataReceive(buffer, ret)
                        }
                    }
                }.onFailure {
                    Logger.e("报错信息：${it.message},${it.localizedMessage}")
                    init()
                }

            }
        }
    }

//    inner class WriteThread : Thread() {
//        override fun run() {
//            super.run()
//            while (true) {
//                sleep(10)
//                runCatching {
//                    mOutputStream?.write(testByteArray)
//                }.onFailure {
//                    Logger.e("写入错误：${it.message}")
//                }
//            }
//        }
//    }


}