package com.sjb.securitydoormanager.ui.fragment

import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.base.BaseViewModel
import com.sjb.base.ext.dismissLoading
import com.sjb.base.ext.showLoading
import com.sjb.securitydoormanager.bean.SensitivityData
import com.sjb.securitydoormanager.databinding.FragmentSensitivityBinding
import com.sjb.securitydoormanager.serialport.DataMcuProcess
import com.sjb.securitydoormanager.serialport.DataProtocol
import com.sjb.securitydoormanager.serialport.SerialPortManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2024/6/16.
 * Describe :
 */

class SensitivityFragment : BaseMvFragment<FragmentSensitivityBinding, BaseViewModel>(),
    SeekBar.OnSeekBarChangeListener {


    private var zone1Progress = 0
    private var zone2Progress = 0
    private var zone3Progress = 0
    private var zone4Progress = 0
    private var zone5Progress = 0
    private var zone6Progress = 0

    private var zoneOverallProgress = 0

    private var sensitivityData: SensitivityData? = null

    // 是否点击了保存按钮
    private var isTouchSaveBt = false

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

            ivZoneOverallReduce.setOnClickListener {
                zoneOverallProgress -= 1
                seekbarZoneOverall.progress = zoneOverallProgress
                tvZoneOverall.text = zoneOverallProgress.toString()
            }

            ivZoneOverallAdd.setOnClickListener {
                zoneOverallProgress += 1
                seekbarZoneOverall.progress = zoneOverallProgress
                tvZoneOverall.text = zoneOverallProgress.toString()
            }



            btSave.setOnClickListener {
                isTouchSaveBt = true
                saveSensitivityToDoor()
            }

            lyTitle.ivBack.setOnClickListener {
                isTouchSaveBt = false
                saveSensitivityToDoor()
                findNavController().popBackStack()
            }
        }
    }

    override fun initView() {
        binding.run {
            seekbarZone1.setOnSeekBarChangeListener(this@SensitivityFragment)
            seekbarZone2.setOnSeekBarChangeListener(this@SensitivityFragment)
            seekbarZone3.setOnSeekBarChangeListener(this@SensitivityFragment)
            seekbarZone4.setOnSeekBarChangeListener(this@SensitivityFragment)
            seekbarZone5.setOnSeekBarChangeListener(this@SensitivityFragment)
            seekbarZone6.setOnSeekBarChangeListener(this@SensitivityFragment)
            seekbarZoneOverall.setOnSeekBarChangeListener(this@SensitivityFragment)
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
                zoneOverallProgress = sensitivity.zoneOverall
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
            tvZoneOverall.text = zoneOverallProgress.toString()
            seekbarZone1.progress = zone1Progress
            seekbarZone2.progress = zone2Progress
            seekbarZone3.progress = zone3Progress
            seekbarZone4.progress = zone4Progress
            seekbarZone5.progress = zone5Progress
            seekbarZone6.progress = zone6Progress
            seekbarZoneOverall.progress = zoneOverallProgress
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

            binding.seekbarZoneOverall.id -> {
                zoneOverallProgress = progress
                binding.tvZoneOverall.text = msg
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        Logger.i("-----${seekBar?.id},onStartTrackingTouch")
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        Logger.i("----${seekBar?.id},onStopTrackingTouch")
    }

    /**
     * 保存数据到安检门
     */
    private fun saveSensitivityToDoor() {
        MainScope().launch {
            if (sensitivityData == null) {
                toast("获取安检门灵敏度失败，请检查串口是否连接！")
                return@launch
            }
            if (isTouchSaveBt)
                showLoading("正在保存中...")
            if (sensitivityData?.zone1s != zone1Progress) {
                val byte3 = (zone1Progress.shr(7) and 0x7f).toByte()
                val byte4 = (zone1Progress and 0x7f).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X06,
                    0x0c,                   // 12-19 表示 1-8区
                    byte3,
                    byte4,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
                delay(50)
            }
            if (zone2Progress != sensitivityData?.zone2s) {
                val byte3 = (zone2Progress.shr(7) and 0x7f).toByte()
                val byte4 = (zone2Progress and 0x7f).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X06,
                    0x0d,             //13
                    byte3,
                    byte4,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
                delay(50)
            }
            if (zone3Progress != sensitivityData?.zone3s) {
                val byte3 = (zone3Progress.shr(7) and 0x7f).toByte()
                val byte4 = (zone3Progress and 0x7f).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X06,
                    0x0e,                //14
                    byte3,
                    byte4,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
                delay(50)
            }

            if (zone4Progress != sensitivityData?.zone4s) {
                val byte3 = (zone4Progress.shr(7) and 0x7f).toByte()
                val byte4 = (zone4Progress and 0x7f).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X06,
                    0x0f,              //15
                    byte3,
                    byte4,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
                delay(50)
            }
            if (zone5Progress != sensitivityData?.zone5s) {
                val byte3 = (zone5Progress.shr(7) and 0x7f).toByte()
                val byte4 = (zone5Progress and 0x7f).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X06,
                    0x10,         //16
                    byte3,
                    byte4,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
                delay(50)
            }

            if (zone6Progress != sensitivityData?.zone6s) {
                val byte3 = (zone6Progress.shr(7) and 0x7f).toByte()
                val byte4 = (zone6Progress and 0x7f).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X06,
                    0x11,         //17
                    byte3,
                    byte4,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
                delay(50)
            }

            // 看整体灵敏度是否修改，修改了就下发到安检门
            if (zoneOverallProgress != sensitivityData?.zoneOverall) {
                val byte3 = (zoneOverallProgress.shr(7) and 0x7f).toByte()
                val byte4 = (zoneOverallProgress and 0x7f).toByte()
                val data = byteArrayOf(
                    DataProtocol.HEAD_CMD_1,
                    0X06,
                    0x14,         //20
                    byte3,
                    byte4,
                    0x7f
                )
                SerialPortManager.instance.sendMsg(data)
                delay(50)
            }

            //要求安检门上传参数
            SerialPortManager.instance.sendMsg(DataProtocol.data_0x02)
            if (isTouchSaveBt)
                dismissLoading()
        }
    }

    override fun onStop() {
        super.onStop()
        isTouchSaveBt = false
        saveSensitivityToDoor()
    }

//    fun onBackPressed() {
//        isTouchSaveBt = false
//        saveSensitivityToDoor()
//    }
}