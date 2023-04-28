package com.sjb.securitydoormanager.idr

import android.graphics.Bitmap

class IDCardInfo {
    var name: String? = null
    var sex: String? = null
    var nation: String? = null
    var birth: String? = null
    var cardId: String? = null
    var depart: String? = null
    var expireDate: String? = null
    var addr: String? = null
    var passNo: String? = null
    var cardPic: Bitmap? = null

    override fun toString(): String {
        return "姓名：$name \n" +
                "性别：$sex \n" +
                "民族：$nation \n" +
                "出生日期：$birth \n" +
                "住址：$addr \n" +
                "证件号码：$cardId"
    }
}