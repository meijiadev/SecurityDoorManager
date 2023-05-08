package com.sjb.securitydoormanager

import android.content.Context
import com.hjq.http.EasyConfig
import com.hjq.http.config.IRequestInterceptor
import com.hjq.http.config.IRequestServer
import com.hjq.http.model.HttpHeaders
import com.hjq.http.model.HttpParams
import com.hjq.http.request.HttpRequest
import com.hjq.toast.ToastUtils
import com.hjq.toast.style.BlackToastStyle
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseApplication
import com.sjb.securitydoormanager.http.model.RequestHandler
import com.sjb.securitydoormanager.http.server.ReleaseServer
import com.tencent.mmkv.MMKV
import okhttp3.OkHttpClient


class SJBApp : BaseApplication() {
    companion object {
        lateinit var context: Context

    }

    override fun onCreate() {
        super.onCreate()
        context = this
        MMKV.initialize(this)
        Logger.addLogAdapter(AndroidLogAdapter())
        ToastUtils.init(this)
        ToastUtils.setStyle(BlackToastStyle())
        initHttp()
    }

    private fun initHttp() {
        val okHttpClient = OkHttpClient.Builder()
            .build()
        // 网络请求框架初始化

        // 网络请求框架初始化
        val server: IRequestServer
        server = ReleaseServer()

        EasyConfig.with(okHttpClient) // 是否打印日志
            //.setLogEnabled(BuildConfig.DEBUG)
            // 设置服务器配置（必须设置）
             .setServer(server) // 设置请求处理策略（必须设置）
            .setHandler(RequestHandler(this)) // 设置请求参数拦截器
            .setInterceptor(object : IRequestInterceptor {
                override fun interceptArguments(
                    httpRequest: HttpRequest<*>,
                    params: HttpParams,
                    headers: HttpHeaders
                ) {
                    headers.put("timestamp", System.currentTimeMillis().toString())
                }
            }) // 设置请求重试次数
            .setRetryCount(1) // 设置请求重试时间
            .setRetryTime(2000) // 添加全局请求参数
           // .addParam("token", "6666666") // 添加全局请求头
            //.addHeader("date", "20191030")
            .into()
    }


}