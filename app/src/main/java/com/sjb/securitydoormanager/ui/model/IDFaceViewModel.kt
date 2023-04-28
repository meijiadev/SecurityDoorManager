package com.sjb.securitydoormanager.ui.model

import androidx.lifecycle.viewModelScope
import com.arcsoft.idcardveri.DetectFaceResult
import com.arcsoft.idcardveri.IdCardVerifyError
import com.arcsoft.idcardveri.IdCardVerifyListener
import com.arcsoft.idcardveri.IdCardVerifyManager
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.SJBApp
import com.sjb.veriface.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/**
 * 人证比对相关业务
 */
class IDFaceViewModel : BaseViewModel() {

    /**
     * 人证对比是否成功
     */
    var cardFaceCompareResult = UnPeekLiveData<Boolean>()

    var isInit = false
    // private val activeService = Executors.newSingleThreadExecutor()

    //视频或图片人脸数据是否检测到
    private var isCurrentReady = false

    //身份证人脸数据是否检测到
    private var isIdCardReady = false
    private val THRESHOLD = 0.82

    /**
     * 获取人证识别的激活结果，是否激活
     */
    fun initIdCardVerify() {
        val initResult =
            IdCardVerifyManager.getInstance().init(SJBApp.context, idCardVerifyListener)
        isInit = initResult == IdCardVerifyError.OK
        Logger.e("init Result:$initResult")
    }

    /**
     * 激活设备
     */
    fun executeIDCardVer() {
        viewModelScope.launch(Dispatchers.IO) {
            var activeResult =
                IdCardVerifyManager.getInstance()
                    .active(SJBApp.context, Constants.APP_ID, Constants.SDK_KEY)
            Logger.e("激活结果：$activeResult")
            withContext(Dispatchers.Main) {
                toast("activeResult:$activeResult")
                if (activeResult == IdCardVerifyError.OK) {
                    // 初始化
                    val initResult = IdCardVerifyManager.getInstance().init(
                        SJBApp.context, idCardVerifyListener
                    )
                    isInit = initResult == IdCardVerifyError.OK
                    Logger.e("init result:$initResult")
                    if (!isInit) {
                        toast("init result:$initResult")
                    }
                }
            }
        }
    }

    private val idCardVerifyListener: IdCardVerifyListener = object : IdCardVerifyListener {
        override fun onPreviewResult(
            detectFaceResult: DetectFaceResult,
            bytes: ByteArray,
            i: Int,
            i1: Int
        ) {
            Logger.e("onPreviewResult ：${detectFaceResult.errCode}")
            if (detectFaceResult.errCode == IdCardVerifyError.OK) {
                isCurrentReady = true
                viewModelScope.launch(Dispatchers.Main) {
                    compare()
                }

            }
        }

        override fun onIdCardResult(
            detectFaceResult: DetectFaceResult,
            bytes: ByteArray,
            i: Int,
            i1: Int
        ) {
            Logger.e("onIdCardResult:${detectFaceResult.errCode}")
            if (detectFaceResult.errCode == IdCardVerifyError.OK) {
                isIdCardReady = true
                viewModelScope.launch(Dispatchers.Main) {
                    compare()
                }
            }
        }
    }

    /**
     * 对比当前人脸和身份证的头像
     */
    private fun compare() {
        if (!isCurrentReady || !isIdCardReady) {
            return
        }
        // 认证比对
        var compareResult = IdCardVerifyManager.getInstance().compareFeature(THRESHOLD)
        Logger.e("compareFeature: result${compareResult.result} \n" + "isSuccess: ${compareResult.isSuccess} \n" + "errCode:${compareResult.errCode}")
        if (compareResult.isSuccess) {
            cardFaceCompareResult.postValue(true)
            // speakIdCardSuccess()
        } else {
            cardFaceCompareResult.postValue(false)
            // speakIdCardFailed()
        }
        isCurrentReady = false
        isIdCardReady = false
    }

}