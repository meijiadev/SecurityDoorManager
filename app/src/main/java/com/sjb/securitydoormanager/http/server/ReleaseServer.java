package com.sjb.securitydoormanager.http.server;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestServer;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyHttp
 *    time   : 2019/05/19
 *    desc   : 正式环境
 */
public class ReleaseServer implements IRequestServer {


    @NonNull
    @Override
    public String getHost() {
        // 公司平台地址
        return "https://www.hdvsiot.com/";
    }
}