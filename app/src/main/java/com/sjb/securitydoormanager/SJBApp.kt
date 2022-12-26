package com.sjb.securitydoormanager

import com.hjq.toast.ToastUtils
import com.hjq.toast.style.BlackToastStyle
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseApplication

class SJBApp :BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        ToastUtils.init(this)
        ToastUtils.setStyle(BlackToastStyle())
    }
}