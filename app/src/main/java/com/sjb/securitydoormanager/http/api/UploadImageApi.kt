package com.sjb.securitydoormanager.http.api

import com.hjq.http.annotation.HttpHeader
import com.hjq.http.annotation.HttpRename
import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestServer
import com.hjq.http.model.BodyType
import java.io.File
import java.io.InputStream

class UploadImageApi : IRequestApi, IRequestServer {

    private var mHost: String
    private var mApi: String

//    @HttpHeader
//    @HttpRename("file")
//    private var file: File

//    @HttpHeader
//    @HttpRename("Content-Type")
//    private var contentType = "multipart/form-data"


    constructor(url: String) : this(url, "") {
        //this.file = file
    }

    constructor(url: String, host: String) {
        mHost = url
        mApi = host
      //  this.file = file
    }

    override fun getApi(): String {
        return mApi
    }

    override fun getHost(): String {
        return mHost
    }

    override fun toString(): String {
        return super.toString()
    }

    override fun getBodyType(): BodyType {
        return BodyType.FORM
    }


}