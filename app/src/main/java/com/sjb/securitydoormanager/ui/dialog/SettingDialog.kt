package com.sjb.securitydoormanager.ui.dialog

import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import com.sjb.securitydoormanager.R

class SettingDialog (context: Context) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_setting
    }



}