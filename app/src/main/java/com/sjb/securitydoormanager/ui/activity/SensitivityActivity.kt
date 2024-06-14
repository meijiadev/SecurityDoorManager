package com.sjb.securitydoormanager.ui.activity

import android.widget.SeekBar
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvActivity
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.bean.SensitivityData
import com.sjb.securitydoormanager.databinding.ActivitySensitivityBinding
import com.sjb.securitydoormanager.serialport.DataMcuProcess
import com.sjb.securitydoormanager.serialport.DataProtocol
import com.sjb.securitydoormanager.serialport.SerialPortManager

class SensitivityActivity : BaseMvActivity<ActivitySensitivityBinding, BaseViewModel>(),
    SeekBar.OnSeekBarChangeListener {

    private var zone1Progress = 0
    private var zone2Progress = 0
    private var zone3Progress = 0
    private var zone4Progress = 0
    private var zone5Progress = 0
    private var zone6Progress = 0

    private var sensitivityData: SensitivityData? = null

    override fun getViewBinding(): ActivitySensitivityBinding {
        return ActivitySensitivityBinding.inflate(layoutInflater)
    }

    override fun initParam() {

    }

    override fun initData() {

    }

    override fun initViewObservable() {
        binding.run {
            ivZone1Reduce.setOnClickListener {
                zone1Progress -= 1
                seekbarZone1.progress = zone1Progress
                tvZone1.text = zone1Progress.toString()
            }
            ivZone1Add.setOnClickListener {
                zone1Progress += 1
                seekbarZone1.progress = zone1Progress
                tvZone1.text = zone1Progress.toString()
            }

            ivZone2Reduce.setOnClickListener {
                zone2Progress -= 1
                seekbarZone2.progress = zone2Progress
                tvZone2.text = zone2Progress.toString()
            }
            ivZone2Add.setOnClickListener {
                zone2Progress += 1
                seekbarZone2.progress = zone2Progress
                tvZone2.text = zone2Progress.toString()
            }

            ivZone3Reduce.setOnClickListener {
                zone3Progress -= 1
                seekbarZone3.progress = zone3Progress
                tvZone3.text = zone3Progress.toString()
            }
            ivZone3Add.setOnClickListener {
                zone3Progress += 1
                seekbarZone3.progress = zone3Progress
                tvZone3.text = zone3Progress.toString()
            }

            ivZone4Reduce.setOnClickListener {
                zone4Progress -= 1
                seekbarZone4.progress = zone4Progress
                tvZone4.text = zone4Progress.toString()
            }
            ivZone4Add.setOnClickListener {
                zone4Progress += 1
                seekbarZone4.progress = zone4Progress
                tvZone4.text = zone4Progress.toString()
            }

            ivZone5Reduce.setOnClickListener {
                zone5Progress -= 1
                seekbarZone5.progress = zone5Progress
                tvZone5.text = zone5Progress.toString()
            }
            ivZone5Add.setOnClickListener {
                zone5Progress += 1
                seekbarZone5.progress = zone5Progress
                tvZone5.text = zone5Progress.toString()
            }
            ivZone6Reduce.setOnClickListener {
                zone6Progress -= 1
                seekbarZone6.progress = zone6Progress
                tvZone6.text = zone6Progress.toString()
            }
            ivZone6Add.setOnClickListener {
                zone6Progress += 1
                seekbarZone6.progress = zone6Progress
                tvZone6.text = zone6Progress.toString()
            }



            btSave.setOnClickListener {
                if (zone1Progress != sensitivityData?.zone1s) {

                }
            }
        }
    }

    override fun initView() {
        binding.run {
            seekbarZone1.setOnSeekBarChangeListener(this@SensitivityActivity)
            seekbarZone2.setOnSeekBarChangeListener(this@SensitivityActivity)
            seekbarZone3.setOnSeekBarChangeListener(this@SensitivityActivity)
            seekbarZone4.setOnSeekBarChangeListener(this@SensitivityActivity)
            seekbarZone5.setOnSeekBarChangeListener(this@SensitivityActivity)
            seekbarZone6.setOnSeekBarChangeListener(this@SensitivityActivity)
        }
    }

    override fun initListener() {
        DataMcuProcess.instance.sensitivityEvent.observe(this) {
            it?.let { sensitivity ->
                sensitivityData = sensitivity
                zone1Progress = sensitivity.zone1s
                zone2Progress = sensitivity.zone2s
                zone3Progress = sensitivity.zone3s
                zone4Progress = sensitivity.zone4s
                zone5Progress = sensitivity.zone5s
                zone6Progress = sensitivity.zone6s
                initZone()
            }
        }
    }

    private fun initZone() {
        binding.run {
            tvZone1.text = zone1Progress.toString()
            tvZone2.text = zone2Progress.toString()
            tvZone3.text = zone3Progress.toString()
            tvZone4.text = zone4Progress.toString()
            tvZone5.text = zone5Progress.toString()
            tvZone6.text = zone6Progress.toString()
            seekbarZone1.progress = zone1Progress
            seekbarZone2.progress = zone2Progress
            seekbarZone3.progress = zone3Progress
            seekbarZone4.progress = zone4Progress
            seekbarZone5.progress = zone5Progress
            seekbarZone6.progress = zone6Progress
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val msg = progress.toString()
        when (seekBar?.id) {
            binding.seekbarZone1.id -> {
                zone1Progress = progress
                binding.tvZone1.text = msg
            }

            binding.seekbarZone2.id -> {
                zone2Progress = progress
                binding.tvZone2.text = msg
            }

            binding.seekbarZone3.id -> {
                zone3Progress = progress
                binding.tvZone3.text = msg
            }

            binding.seekbarZone4.id -> {
                zone4Progress = progress
                binding.tvZone4.text = msg
            }

            binding.seekbarZone5.id -> {
                zone5Progress = progress
                binding.tvZone5.text = msg
            }

            binding.seekbarZone6.id -> {
                zone6Progress = progress
                binding.tvZone6.text = msg
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        Logger.i("-----${seekBar?.id},onStartTrackingTouch")
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        Logger.i("----${seekBar?.id},onStopTrackingTouch")
        when (seekBar?.id) {
            binding.seekbarZone1.id -> {
                val byte3 = (zone1Progress.shr(7) and 0x7f).toByte()
                val byte4 = (zone1Progress and 0x7f).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X06,
                    0x0c,
                    byte3,
                    byte4,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
            }

            binding.seekbarZone2.id -> {

            }

            binding.seekbarZone3.id -> {

            }

            binding.seekbarZone4.id -> {

            }

            binding.seekbarZone5.id -> {

            }

            binding.seekbarZone6.id -> {

            }
        }
    }



}