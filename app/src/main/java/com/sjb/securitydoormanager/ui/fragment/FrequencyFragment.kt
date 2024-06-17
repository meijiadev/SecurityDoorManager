package com.sjb.securitydoormanager.ui.fragment

import android.view.View
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.databinding.FragmentFrequencyBinding
import com.sjb.securitydoormanager.serialport.DataMcuProcess
import com.sjb.securitydoormanager.serialport.DataProtocol
import com.sjb.securitydoormanager.serialport.SerialPortManager
import kotlin.experimental.or

/**
 * Create by MJ on 2024/6/17.
 * Describe :
 */

class FrequencyFragment : BaseMvFragment<FragmentFrequencyBinding, BaseViewModel>(),
    SeekBar.OnSeekBarChangeListener {
    // 是否是自动设频
    private var isAutoFrequency = false

    private var frequency = 0
    override fun initParam() {
        binding.lyTitle.tvTitle.text = "频率设置"
    }

    override fun initData() {

    }

    override fun initViewObservable() {
        binding.run {
            lyTitle.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            // 自动设频
            llFrequencyAuto.setOnClickListener {
                if (!isAutoFrequency) {
                    llFrequencyHand.shapeDrawableBuilder.setSolidColor(requireActivity().getColor(R.color.gray15))
                        .intoBackground()
                    llFrequencyAuto.shapeDrawableBuilder.setSolidColor(requireActivity().getColor(R.color.common_button_pressed_color))
                        .intoBackground()
                    binding.rlSeek.visibility= View.GONE
                    isAutoFrequency = true
                    sendFrequencyType()
                }
            }

            llFrequencyHand.setOnClickListener {
                if (isAutoFrequency) {
                    llFrequencyHand.shapeDrawableBuilder.setSolidColor(requireActivity().getColor(R.color.common_button_pressed_color))
                        .intoBackground()
                    llFrequencyAuto.shapeDrawableBuilder.setSolidColor(requireActivity().getColor(R.color.gray15))
                        .intoBackground()
                    binding.rlSeek.visibility= View.VISIBLE
                    isAutoFrequency = false
                    sendFrequencyType()
                }
            }

            btSave.setOnClickListener {
                sendFrequencyToDoor(frequency)
            }
        }
    }

    override fun initView() {
        binding.run {
            seekFre.setOnSeekBarChangeListener(this@FrequencyFragment)
        }
    }

    override fun initListener() {
        DataMcuProcess.instance.frequencyAutoEvent.observe(this) {
            isAutoFrequency = it
            binding.rlSeek.visibility = if (isAutoFrequency) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        DataMcuProcess.instance.frequencyEvent.observe(this) {
            frequency = it
            binding.seekFre.progress = it
            binding.tvProgress.text = it.toString()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        frequency = progress
        binding.tvProgress.text = progress.toString()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    /**
     * 发送频率到安检门
     */
    private fun sendFrequencyToDoor(frequency: Int) {
        val data = byteArrayOf(
            DataProtocol.HEAD_CMD_1,
            0x05,
            0x16,              // 类型标志 22
            frequency.toByte(),
            0x7F
        )
        SerialPortManager.instance.sendMsg(data)
    }

    /**
     * 设置频率类型 自动还是手动
     */
    private fun sendFrequencyType() {
        val autoByte = if (isAutoFrequency) {
            0b01000000
        } else {
            0b00000000
        }
        DataMcuProcess.instance.doorParamEvent.value?.let {
            val byte3 = it[24].toInt().or(autoByte).toByte()
            val byte4 = it[25]
            val data = byteArrayOf(
                DataProtocol.HEAD_CMD_1,
                0x06,
                0x1d,                 // 29类型标志
                byte3,
                byte4,
                0x7F
            )
            SerialPortManager.instance.sendMsg(data)
        }
    }
}