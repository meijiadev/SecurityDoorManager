package com.sjb.securitydoormanager.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Handler
import com.android.util.DateUtil
import com.github.faucamp.simplertmp.RtmpHandler
import com.github.faucamp.simplertmp.RtmpHandler.RtmpListener
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.orhanobut.logger.Logger
import com.permissionx.guolindev.PermissionX
import com.sjb.base.base.BaseMvActivity
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.constant.PassInfoManager
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.databinding.ActivityHomeBinding
import com.sjb.securitydoormanager.mqtt.MqttHelper
import com.sjb.securitydoormanager.mqtt.data.Qos
import com.sjb.securitydoormanager.mqtt.data.Topic
import com.sjb.securitydoormanager.mqtt.host
import com.sjb.securitydoormanager.serialport.DataMcuProcess
import com.sjb.securitydoormanager.serialport.SerialPortManager
import net.ossrs.yasea.SrsEncodeHandler
import net.ossrs.yasea.SrsEncodeHandler.SrsEncodeListener
import net.ossrs.yasea.SrsPublisher
import net.ossrs.yasea.SrsRecordHandler
import net.ossrs.yasea.SrsRecordHandler.SrsRecordListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.IOException
import java.net.SocketException

class HomeActivity : BaseMvActivity<ActivityHomeBinding, BaseViewModel>(), RtmpListener,
    SrsEncodeListener, SrsRecordListener {

    private var pieDataSet: PieDataSet? = null

    private var updateUIAction = UnPeekLiveData<Int>()

    private var mPublisher: SrsPublisher? = null
    private val mWidth = 1270
    private val mHeight = 720

    private val rtmpUrl = "rtmp://112.74.191.164:1935/live/123456789"


    /**
     *权限请求是否通过
     */
    private var isPermissionGranted = false

    override fun getViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)

    }


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
        binding.scanView.setVertical(true)
        binding.scanView.setStartFromBottom(false)
        binding.scanView.setAnimDuration(2000)
    }


    /**
     * 设置扇形图的数据
     */
    private fun setChartData() {
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(PassInfoManager.hasPass, "净通过"))
        entries.add(PieEntry(PassInfoManager.phoneAlarms, "手机报警"))
        entries.add(PieEntry(PassInfoManager.otherAlarms, "其他报警"))
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
            passTotalTv.text = "通过总数：" + PassInfoManager.totalPass.toInt()
            passTv.text = "净通过数：" + PassInfoManager.hasPass.toInt()
            phoneAlarmTv.text = "手机报警：" + PassInfoManager.phoneAlarms.toInt()
            otherAlarmTv.text = "其他报警：" + PassInfoManager.otherAlarms.toInt()
        }
    }


    override fun initData() {
        updateTime()
        TimeThread().start()
        Handler().postDelayed({
            initPermission()
            Logger.i("去链接Mqtt")
            val mqttHelper = MqttHelper(this, host, "SD00000001", "123456")
//            mqttHelper.connect(Topic.TOPIC_MSG, Qos.QOS_TWO, true, object : MqttCallback {
//                override fun connectionLost(cause: Throwable?) {
//                    Logger.e("错误" + cause?.message)
//                }
//
//                override fun messageArrived(topic: String?, message: MqttMessage?) {
//                    // 收到的消息
//                    Logger.i("topic:$topic,message:${message?.payload}")
//                    val msg = message?.payload?.let { String(it) }
//                    Logger.i("收到的消息：$msg")
//                    msg?.let {
//                        val index=msg.indexOf("{")
//                        if (index<1){
//                            Logger.i("Mqtt收到未知消息")
//                            return
//                        }
//                        // 前面的路由
//                        val path=msg.substring(0,index).trim()
//                        // 具体的json消息体
//                        val json=msg.substring(index)
//                        Logger.i("path:$path,json:$json")
//                    }
//                }
//
//                override fun deliveryComplete(token: IMqttDeliveryToken?) {
//                    Logger.i("token:${token?.message}")
//                }
//            })
            val serialManager=SerialPortManager.instance
            val mcuProcess=DataMcuProcess()
            serialManager.init()
            serialManager.setIData(mcuProcess)
            serialManager.startRead()
        }, 3000)

    }


    override fun initListener() {
        binding.passTv.setOnClickListener {
            PassInfoManager.totalPass = PassInfoManager.totalPass + 1
            PassInfoManager.hasPass = PassInfoManager.hasPass + 1
            setChartData()
            binding.scanView.startScanAnim()
        }

        binding.phoneAlarmTv.setOnClickListener {
            PassInfoManager.totalPass = PassInfoManager.totalPass + 1
            PassInfoManager.phoneAlarms = PassInfoManager.phoneAlarms + 1
            setChartData()
        }

        binding.otherAlarmTv.setOnClickListener {
            PassInfoManager.totalPass = PassInfoManager.totalPass + 1
            PassInfoManager.otherAlarms = PassInfoManager.otherAlarms + 1
            setChartData()
        }
    }

    override fun initViewObservable() {
        updateUIAction.observe(this) {
            it?.let {
                updateTime()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTime() {
        binding.timeTv.text =
            DateUtil.getCurrentDateTime(DateUtil.Y_M_D_H_M) + " " + DateUtil.getCurrentDayOfWeekCH()
    }


    /**
     * 初始化推流的相关配置
     */
    private fun initPublish() {
        //binding.curCameraView.setPreviewOrientation(Configuration.ORIENTATION_LANDSCAPE)
        mPublisher = SrsPublisher(binding.curCameraView)
        mPublisher?.let {
            it.setEncodeHandler(SrsEncodeHandler(this))
            it.setRtmpHandler(RtmpHandler(this))
            it.setRecordHandler(SrsRecordHandler(this))
            it.setPreviewResolution(mWidth, mHeight)
            it.setOutputResolution(mHeight, mWidth) // 这里要和preview反过来
            it.setVideoHDMode()
            it.setScreenOrientation(Configuration.ORIENTATION_PORTRAIT)
            // 开启预览
            it.startCamera()
            it.switchToHardEncoder()
            it.startPublish(rtmpUrl)
        }

    }


    /**
     * 初始化权限请求
     */
    private fun initPermission() {
        // 安卓版本是否大于 6.0
        if (Build.VERSION.SDK_INT >= 23) {
            // 检查是否开启权限
            if (!PermissionX.isGranted(this, Manifest.permission.CAMERA) || !PermissionX.isGranted(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) || !PermissionX.isGranted(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) || !PermissionX.isGranted(this, Manifest.permission.RECORD_AUDIO)
            ) {
                // 权限没有开启
                PermissionX.init(this).permissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ).request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        isPermissionGranted = true
                        Logger.i("所有申请都已通过")
                        initPublish()
                    } else {
                        isPermissionGranted = false
                        Logger.i("拒绝了以下的权限：$deniedList")
                    }
                }
            } else {
                isPermissionGranted = true
                initPublish()
            }
        } else {
            isPermissionGranted = true
            initPublish()
        }
    }


    override fun onStart() {
        super.onStart()
        if (mPublisher?.camera == null && isPermissionGranted) {
            mPublisher?.startPublish(rtmpUrl)
            mPublisher?.startCamera()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        if (mPublisher?.camera == null && isPermissionGranted) {
            mPublisher?.stopCamera()
            mPublisher?.stopPublish()
        }
        SerialPortManager.instance.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mPublisher?.startPublish(rtmpUrl)
        mPublisher?.startCamera()
    }


    inner class TimeThread : Thread() {
        override fun run() {
            super.run()
            do {
                sleep(1000 * 10)
                updateUIAction.postValue(1)
            } while (true)
        }
    }

    override fun onRtmpConnecting(msg: String?) {
        toast(msg)
    }

    override fun onRtmpConnected(msg: String?) {
        toast(msg)
    }

    override fun onRtmpVideoStreaming() {

    }

    override fun onRtmpAudioStreaming() {

    }

    override fun onRtmpStopped() {
        toast("rtmp stopped")
    }

    override fun onRtmpDisconnected() {
        toast("rtmp disconnected")
    }

    override fun onRtmpVideoFpsChanged(fps: Double) {
//        Logger.i("rtmp video fps:$fps")
    }

    override fun onRtmpVideoBitrateChanged(bitrate: Double) {
        //  Logger.i("rtmp video bitrate:$bitrate ")
    }

    override fun onRtmpAudioBitrateChanged(bitrate: Double) {
//        Logger.i("rtmp audio bitrate:$bitrate ")
    }

    override fun onRtmpSocketException(e: SocketException?) {
        //  Logger.e("rtmp socket:$e")
    }

    override fun onRtmpIOException(e: IOException?) {
        //  Logger.e("rtmp IO:$e")
    }

    override fun onRtmpIllegalArgumentException(e: IllegalArgumentException?) {
        //  Logger.e("rtmp:$e")
    }

    override fun onRtmpIllegalStateException(e: IllegalStateException?) {
        //  Logger.e("rtmp:$e")
    }

    override fun onNetworkWeak() {
//        Logger.e("网络不稳定！！！")
    }

    override fun onNetworkResume() {
        //  Logger.i("网络正在恢复...")
    }

    override fun onEncodeIllegalArgumentException(e: IllegalArgumentException?) {
        // Logger.e("EncodeIllegalArgumentException:$e")
    }

    override fun onRecordPause() {

    }

    override fun onRecordResume() {

    }

    override fun onRecordStarted(msg: String?) {

    }

    override fun onRecordFinished(msg: String?) {

    }

    override fun onRecordIllegalArgumentException(e: java.lang.IllegalArgumentException?) {

    }

    override fun onRecordIOException(e: IOException?) {

    }


}