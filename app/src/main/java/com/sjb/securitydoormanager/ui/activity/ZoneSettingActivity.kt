package com.sjb.securitydoormanager.ui.activity

import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvActivity
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.databinding.ActivityZoneSettingBinding
import com.sjb.securitydoormanager.serialport.DataMcuProcess
import com.sjb.securitydoormanager.serialport.DataProtocol
import com.sjb.securitydoormanager.serialport.SerialPortManager


/**
 * Create by MJ on 2024/6/15.
 * Describe :
 */
class ZoneSettingActivity : BaseMvActivity<ActivityZoneSettingBinding, BaseViewModel>() {
    override fun getViewBinding(): ActivityZoneSettingBinding {
        return ActivityZoneSettingBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        binding.lyTitle.tvTitle.text = "防区设置"
    }

    override fun initData() {

    }

    override fun initViewObservable() {
        binding.run {
            lyTitle.ivBack.setOnClickListener {
                finish()
            }

            llDoor6Zone.setOnClickListener {
                initZoneDefault()
                llDoor6Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.common_button_pressed_color))
                    .intoBackground()
                val byte3 = (0b01101).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X05,
                    0x15,                   // 设置分区 21
                    byte3,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)

            }
            llDoor12Zone.setOnClickListener {
                initZoneDefault()
                llDoor12Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.common_button_pressed_color))
                    .intoBackground()
                val byte3 = (0b01110).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X05,
                    0x15,                   // 设置分区 21
                    byte3,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
            }

            llDoor18Zone.setOnClickListener {
                initZoneDefault()
                llDoor18Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.common_button_pressed_color))
                    .intoBackground()
                val byte3 = (0b01111).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X05,
                    0x15,                   // 设置分区 21
                    byte3,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
            }


        }
    }

    override fun initView() {

    }

    override fun initListener() {
        DataMcuProcess.instance.zoneSettingEvent.observe(this) {
            when (it) {
                // 6区
                0b01101 -> {
                    Logger.i("当前设置的是6区门")
                    initZoneDefault()
                    binding.llDoor6Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.common_button_pressed_color))
                        .intoBackground()
                }
                // 12 区
                0b01110 -> {
                    Logger.i("当前设置的是12区门")
                    initZoneDefault()
                    binding.llDoor12Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.common_button_pressed_color))
                        .intoBackground()
                }
                // 18 区
                0b01111 -> {
                    Logger.i("当前设置的是18区门")
                    initZoneDefault()
                    binding.llDoor18Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.common_button_pressed_color))
                        .intoBackground()
                }
            }
        }
    }

    private fun initZoneDefault() {
        binding.llDoor6Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.gray15))
            .intoBackground()
        binding.llDoor12Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.gray15))
            .intoBackground()
        binding.llDoor18Zone.shapeDrawableBuilder.setSolidColor(getColor(R.color.gray15))
            .intoBackground()
    }

}