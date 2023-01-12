package com.sjb.securitydoormanager.serialport

import android.serialport.SerialPort
import androidx.annotation.RequiresPermission.Read
import com.orhanobut.logger.Logger
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
    private var path: String? = null

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
        serialPort = SerialPort.newBuilder(path, baudrate).build()
        mInputStream = serialPort?.inputStream
        mOutputStream = serialPort?.outputStream
    }

    /**
     * 开始读取信息
     */
    fun startRead() {
        if (readThread == null) {
            readThread = ReadThread()
        }
        readThread?.start()
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

                kotlin.runCatching {
                    val buffer = ByteArray(64)
                    mInputStream?.let {
                        ret = it.read(buffer)
                        if (ret > 0) {
                            // 数据回调
                            mIDataProc?.onDataReceive(buffer, ret)
                        }
                    }
                }.onFailure {
                    Logger.e("报错信息：${it.message}")
                }
            }
        }
    }
}