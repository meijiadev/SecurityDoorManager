package com.sjb.securitydoormanager.ui.dialog

import android.content.Context
import com.hjq.shape.view.ShapeTextView
import com.lxj.xpopup.core.CenterPopupView
import com.sjb.securitydoormanager.R

class SettingDialog(context: Context) : CenterPopupView(context) {
    private val tvSensitivity: ShapeTextView by lazy { findViewById(R.id.tv_sensitivity) }
    private val tvFre: ShapeTextView by lazy { findViewById(R.id.tv_fre) }
    private val tvZone: ShapeTextView by lazy { findViewById(R.id.tv_zone) }  // 安检门区位选择
    private val tvProbeType: ShapeTextView by lazy { findViewById(R.id.tv_probe_type) }         //  探测类型
    private val tvRecordQuery: ShapeTextView by lazy { findViewById(R.id.tv_record_query) }     // 记录查询


    override fun getImplLayoutId(): Int {
        return R.layout.dialog_setting
    }

    override fun init() {
        super.init()
        tvSensitivity.setOnClickListener {
            toSensitivityOnclick?.invoke()
            dismiss()
        }
        tvFre.setOnClickListener {
            toFrequencyOnclick?.invoke()
            dismiss()
        }
        tvZone.setOnClickListener {
            toZoneSelectOnclick?.invoke()
            dismiss()
        }
        tvProbeType.setOnClickListener {
            toProbeTypeOnclick?.invoke()
            dismiss()
        }

        tvRecordQuery.setOnClickListener {
            toRecordQueryOnclick?.invoke()
            dismiss()
        }

    }


    private var toSensitivityOnclick: (() -> Unit)? = null

    private var toFrequencyOnclick: (() -> Unit)? = null

    private var toZoneSelectOnclick: (() -> Unit)? = null

    private var toProbeTypeOnclick: (() -> Unit)? = null

    private var toRecordQueryOnclick: (() -> Unit)? = null


    fun sensitivityOnClick(listener: () -> Unit): SettingDialog = apply {
        toSensitivityOnclick = listener
    }

    fun frequencyOnClick(listener: () -> Unit): SettingDialog = apply {
        toFrequencyOnclick = listener
    }

    fun zoneSelectOnClick(listener: () -> Unit): SettingDialog = apply {
        toZoneSelectOnclick = listener
    }

    fun probeTypeOnclick(listener: () -> Unit): SettingDialog = apply {
        toProbeTypeOnclick = listener
    }

    fun recordQueryOnclick(listener: () -> Unit): SettingDialog = apply {
        toRecordQueryOnclick = listener
    }


}