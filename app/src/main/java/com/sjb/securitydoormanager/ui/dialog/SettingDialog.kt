package com.sjb.securitydoormanager.ui.dialog

import android.content.Context
import com.hjq.shape.view.ShapeTextView
import com.lxj.xpopup.core.CenterPopupView
import com.sjb.securitydoormanager.R

class SettingDialog(context: Context) : CenterPopupView(context) {
    private val tvSensitivity: ShapeTextView by lazy { findViewById(R.id.tv_sensitivity) }
    private val tvFre: ShapeTextView by lazy { findViewById(R.id.tv_fre) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_setting
    }

    override fun init() {
        super.init()
        tvSensitivity.setOnClickListener {
            toSensitivityOnclick?.invoke()
        }
    }


    private var toSensitivityOnclick: (() -> Unit)? = null

    fun sensitivityOnClick(listener:()->Unit):SettingDialog=apply{
        toSensitivityOnclick=listener
    }


}