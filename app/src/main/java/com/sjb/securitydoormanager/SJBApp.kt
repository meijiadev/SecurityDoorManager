package com.sjb.securitydoormanager

import android.content.Context
import com.hjq.toast.ToastUtils
import com.hjq.toast.style.BlackToastStyle
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseApplication



class SJBApp : BaseApplication() {
    companion object {
        lateinit var context: Context

    }

    override fun onCreate() {
        super.onCreate()
        context = this
        Logger.addLogAdapter(AndroidLogAdapter())
        ToastUtils.init(this)
        ToastUtils.setStyle(BlackToastStyle())


    }


}