package com.sjb.securitydoormanager.ui.model

import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.SJBApp
import com.sjb.securitydoormanager.idr.IDCardInfo
import com.sjb.securitydoormanager.idr.usbmanager.ZKUSBManager
import com.sjb.securitydoormanager.idr.usbmanager.ZKUSBManagerListener
import com.zkteco.android.IDReader.IDPhotoHelper
import com.zkteco.android.IDReader.WLTService
import com.zkteco.android.biometric.core.device.ParameterHelper
import com.zkteco.android.biometric.core.device.TransportType
import com.zkteco.android.biometric.core.utils.LogHelper
import com.zkteco.android.biometric.module.idcard.IDCardReader
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory
import com.zkteco.android.biometric.module.idcard.IDCardType
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException
import java.util.HashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * desc : viewmodel
 */
class HomeViewModel : BaseViewModel() {

    /**
     * 身份证阅读器当前读取得信息
     */
    var idCardLiveData = UnPeekLiveData<IDCardInfo>()

    /**
     * 身份证阅读器相关参数
     */
    private var zkusbManager: ZKUSBManager? = null
    private var idCardReader: IDCardReader? = null
    private val VID = 1024 //IDR VID
    private val PID = 50010 //IDR PID
    private var countDownLatch: CountDownLatch? = null
    private var bCancel = false
    private var bRepeatRead = false
    private var bStarted = false

    /**
     * 初始化身份证阅读模块
     */
    fun initIDR() {
        zkusbManager = ZKUSBManager(SJBApp.context, zkUsbManagerListener)
        zkusbManager?.registerUSBPermissionReceiver()
    }


    private val zkUsbManagerListener: ZKUSBManagerListener = object : ZKUSBManagerListener {
        override fun onCheckPermission(result: Int) {
            openDevice()
        }





        override fun onUSBArrived(device: UsbDevice?) {
            Logger.e("发现阅读器接入")
        }

        override fun onUSBRemoved(device: UsbDevice?) {
            Logger.e("阅读器USB被拔出")
        }
    }

    /**
     * 开启设备
     */
    private fun openDevice() {
        initIdCardReader()
        try {
            idCardReader?.open(0)
            countDownLatch = CountDownLatch(1)
            Thread {
                bCancel = false

                while (!bCancel) {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    var nTickStart = System.currentTimeMillis()
                    try {
                        idCardReader?.findCard(0)
                        idCardReader?.selectCard(0)
                    } catch (e: IDCardReaderException) {
                        if (!bRepeatRead) {
                            continue
                        }
                    }
                    try {
                        Thread.sleep(50)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    var cardType: Int? = 0
                    try {
                        cardType = idCardReader?.readCardEx(0, 0)
                    } catch (e: IDCardReaderException) {
                        Logger.e("读卡失败，错误信息：" + e.message)
                        continue
                    }
                    if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_PRP || cardType == IDCardType.TYPE_CARD_GAT) {
                        var nTickComUsed = (System.currentTimeMillis() - nTickStart)
                        if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_GAT) {
                            var info = idCardReader?.lastIDCardInfo
                            val idCardInfo = IDCardInfo()
                            idCardInfo.run {
                                name = info?.name
                                sex = info?.sex
                                nation = info?.nation
                                birth = info?.birth
                                cardId = info?.id
                                depart = info?.depart
                                expireDate = info?.validityTime
                                addr = info?.address
                                passNo = info?.passNum
                            }
                            Logger.e("刷卡信息:\n ${idCardInfo.toString()}")
                            if ((info?.photolength ?: 0) > 0) {
                                val buf = ByteArray(WLTService.imgLength)
                                if (1 == WLTService.wlt2Bmp(info?.photo, buf)) {
                                    val curBMPhoto = IDPhotoHelper.Bgr2Bitmap(buf)
                                    idCardInfo.cardPic = curBMPhoto
                                }
                            }
                            idCardLiveData.postValue(idCardInfo)

                        }
                    }
                }
                countDownLatch?.countDown()
            }.start()
            bStarted = true
            Logger.e("打开设备成功，SAMID:" + idCardReader?.getSAMID(0))
        } catch (e: IDCardReaderException) {
            Logger.e("打开设备失败:" + e.message)
        }

    }

    /**
     * 初始化读卡器
     */
    private fun initIdCardReader() {
        if (null != idCardReader) {
            IDCardReaderFactory.destroy(idCardReader)
            idCardReader = null
        }
        LogHelper.setLevel(Log.VERBOSE)
        // Start fingerprint sensor
        var idrparams = HashMap<String, Any>()
        idrparams[ParameterHelper.PARAM_KEY_VID] = VID
        idrparams[ParameterHelper.PARAM_KEY_PID] = PID
        idCardReader =
            IDCardReaderFactory.createIDCardReader(SJBApp.context, TransportType.USB, idrparams)
        idCardReader?.setLibusbFlag(true)
    }

    /**
     * 关闭设备
     */
     fun closeDevice() {
        if (bStarted) {
            bCancel = true
            if (null != countDownLatch) {
                try {
                    countDownLatch?.await(2 * 1000, TimeUnit.MILLISECONDS)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                countDownLatch = null
            }
            try {
                idCardReader?.close(0)
            } catch (e: IDCardReaderException) {
                e.printStackTrace()
            }
            bStarted = false
        }
        zkusbManager?.unRegisterUSBPermissionReceiver()
    }
    /**
     * 开始读卡，处于监听状态
     */
     fun startRead() {
        if (!enumSensor()) {
            Logger.e("找不到设备")
            return
        }
        tryGetUSBPermission()
    }

    private fun enumSensor(): Boolean {
        val usbManager = SJBApp.context.getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager
        for (device in usbManager.deviceList.values) {
            val device_vid = device.vendorId
            val device_pid = device.productId
            if (device_vid == VID && device_pid == PID) {
                return true
            }
        }
        return false
    }

    private fun stopRead() {
        closeDevice()
        Logger.e("设备断开连接")
    }

    private fun tryGetUSBPermission() {
        zkusbManager?.initUSBPermission(
            VID,
            PID
        )
    }



}