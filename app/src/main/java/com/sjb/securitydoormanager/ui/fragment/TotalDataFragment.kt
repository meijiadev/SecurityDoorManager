package com.sjb.securitydoormanager.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.constant.PassInfoManager
import com.sjb.securitydoormanager.constant.PassInfoManager.hasPass
import com.sjb.securitydoormanager.constant.PassInfoManager.otherAlarms
import com.sjb.securitydoormanager.constant.PassInfoManager.phoneAlarms
import com.sjb.securitydoormanager.constant.PassInfoManager.totalAlarms
import com.sjb.securitydoormanager.constant.PassInfoManager.totalPass
import com.sjb.securitydoormanager.databinding.FragmentTotalDataBinding
import com.sjb.securitydoormanager.serialport.DataMcuProcess

/**
 * desc:数据统计总览
 * time:2023/07/19
 */
class TotalDataFragment : BaseMvFragment<FragmentTotalDataBinding, BaseViewModel>() {

    companion object {
        fun newInstance(): TotalDataFragment {
            val args = Bundle()
            val fragment = TotalDataFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var mcuProcess: DataMcuProcess


    fun setMcu(mcu: DataMcuProcess) {
        this.mcuProcess = mcu
    }


    private var pieDataSet: PieDataSet? = null
    private var updateUIAction = UnPeekLiveData<Int>()


    override fun initParam() {

    }

    override fun initView() {
        binding.run {
            // 设置是否显示扇形图中的文字
            pieChart.setDrawEntryLabels(false)
            // 是否显示圆心
            pieChart.isDrawHoleEnabled = true
            pieChart.holeRadius = 50f
            val desc = Description()
            desc.isEnabled = false
            pieChart.description = desc
            // 设置圆心的颜色
            pieChart.setHoleColor(Color.TRANSPARENT)
            val legend = pieChart.legend
            legend.isWordWrapEnabled = true
            //设置图例的实际对齐方式
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            //设置图例水平对齐方式
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            //设置图例方向
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.textColor = resources.getColor(R.color.text_c6cbff)
            //设置图例是否在图表内绘制
            legend.setDrawInside(false)
        }
        setChartData()

    }


    override fun initData() {

    }

    override fun initViewObservable() {

    }

    override fun initListener() {
        binding.passTv.setOnClickListener {
            PassInfoManager.totalPass += 1
            PassInfoManager.hasPass += 1
            setChartData()
//            binding.scanView.startScanAnim()
//            mqttSerModel.uploadRecord(0, "IN", mIDCardInfo, captureBitmap)

        }

        binding.phoneAlarmTv.setOnClickListener {
            totalPass += 1
            phoneAlarms += 1
            setChartData()
//            mqttSerModel.uploadRecord(1, "IN", mIDCardInfo, captureBitmap)
//            speak()
        }




        binding.otherAlarmTv.setOnClickListener {
            totalPass += 1
            otherAlarms += 1
            setChartData()
//            mqttSerModel.uploadRecord(1, "OUT", mIDCardInfo, captureBitmap)
            //faceModel.executeIDCardVer()
        }


        mcuProcess.passNumberEvent.observe(this) {
            it?.toFloat()?.let {
                totalPass = it
                setChartData()
            }
        }
        mcuProcess.alarmNumberEvent.observe(this) {
            it?.toFloat()?.let {
                totalAlarms = it
                hasPass = totalPass - totalAlarms
                setChartData()
            }
        }
        mcuProcess.alarmGoodsEvent.observe(this) {
            if (it == "电子产品") {
                phoneAlarms += 1
                otherAlarms = totalAlarms - phoneAlarms
                setChartData()
            }
        }
    }

    /**
     * 设置扇形图的数据
     */
    @SuppressLint("SetTextI18n")
    private fun setChartData() {
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(PassInfoManager.hasPass, "净通过"))
        entries.add(PieEntry(phoneAlarms, "电子产品报警"))
        entries.add(PieEntry(otherAlarms, "其他报警"))
        pieDataSet = PieDataSet(entries, "")
        pieDataSet?.valueTextSize = 0f
        pieDataSet?.valueTextColor = Color.WHITE
        val colors = mutableListOf<Int>()
        colors.add(resources.getColor(R.color.green))
        colors.add(resources.getColor(R.color.red))
        colors.add(resources.getColor(R.color.yellow))
        pieDataSet?.sliceSpace = 1f
        pieDataSet?.colors = colors
        val pieData = PieData(pieDataSet)
        binding.run {
            pieChart.data = pieData
            pieChart.invalidate()
            passTotalTv.text = "通过总数：" + totalPass.toInt()
            passTv.text = "净通过数：" + PassInfoManager.hasPass.toInt()
            phoneAlarmTv.text = "电子产品：" + PassInfoManager.phoneAlarms.toInt()
            otherAlarmTv.text = "其他报警：" + PassInfoManager.otherAlarms.toInt()
        }
    }
}