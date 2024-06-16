package com.sjb.securitydoormanager.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.util.DisplayMetrics
import android.view.Surface
import android.view.SurfaceHolder
import com.android.util.DateUtil
import com.arcsoft.idcardveri.IdCardVerifyError
import com.arcsoft.idcardveri.IdCardVerifyManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.constant.PassInfoManager
import com.sjb.securitydoormanager.databinding.FragmentHomeBinding
import com.sjb.securitydoormanager.idr.IDCardInfo
import com.sjb.securitydoormanager.media.MediaPlayerManager
import com.sjb.securitydoormanager.serialport.DataMcuProcess
import com.sjb.securitydoormanager.ui.dialog.IDCardDialog
import com.sjb.securitydoormanager.ui.dialog.SettingDialog
import com.sjb.securitydoormanager.ui.model.HomeViewModel
import com.sjb.securitydoormanager.ui.model.IDFaceViewModel
import com.sjb.securitydoormanager.ui.model.MqttSerModel
import com.sjb.securitydoormanager.veriface.DrawUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * Create by MJ on 2024/6/16.
 * Describe :
 */

class HomeFragment : BaseMvFragment<FragmentHomeBinding, BaseViewModel>(), SurfaceHolder.Callback {

    private var pieDataSet: PieDataSet? = null
    private var updateUIAction = UnPeekLiveData<Int>()

    /**
     *权限请求是否通过
     */
    private var isPermissionGranted = false
    private lateinit var faceModel: IDFaceViewModel
    private lateinit var mqttSerModel: MqttSerModel
    private lateinit var homeModel: HomeViewModel

    /**
     * 身份证相关信息
     */
    private var mIDCardInfo: IDCardInfo? = null

    /**
     * 是否需要抓拍
     */
    private var hasCapture = false

    /**
     * 抓拍的图片
     */
    private var captureBitmap: Bitmap? = null


    override fun initParam() {
        faceModel = getActivityViewModel(IDFaceViewModel::class.java)
        mqttSerModel = getActivityViewModel(MqttSerModel::class.java)
        homeModel = getActivityViewModel(HomeViewModel::class.java)
        faceModel.cardFaceCompareResult.observe(this) {
            //hasCapture = true
            if (it == true) {
                toast("人证验证成功！")
                mIDCardInfo?.identifyStatus = "success"
                mIDCardInfo?.identifyMsg = "人脸识别成功"
                speakIdCardSuccess()
            } else {
                toast("人证验证失败！")
                mIDCardInfo?.identifyStatus = "failed"
                mIDCardInfo?.identifyMsg = "人脸识别失败"
                speakIdCardFailed()
            }
        }

        // livedata
        homeModel.idCardLiveData.observe(this) {
            Logger.e("接收身份证信息")
            mIDCardInfo = it
            hasCapture = true
            showCardDialog(it)
            it.cardPic?.let { bitmap ->
                inputCardImage(bitmap)
            }
        }
    }


    /**
     * 开机自检
     */
    private fun powerOnSelfTest() {

    }

    override fun initData() {
        updateTime()
        TimeThread().start()
        homeModel.initIDR()
        MainScope().launch {
            delay(1500)
            binding.scanView.startScanAnim()
        }

        Logger.i("初始化相关参数")
    }


    private var idCardDialog: IDCardDialog? = null
    private fun showCardDialog(idCardInfo: IDCardInfo) {
        if (idCardDialog == null || idCardDialog?.isShow == false) {
            idCardDialog = IDCardDialog(requireContext())
                .setCardInfo(idCardInfo)
            XPopup.Builder(requireContext())
                .isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(idCardDialog)
                .show()
                .delayDismiss(3000)
        }
    }


    override fun initViewObservable() {
        // 点击设置按钮
        binding.settingIv.setOnClickListener {
            val settingDialog = SettingDialog(requireContext()).sensitivityOnClick {
                // startActivity(SensitivityActivity::class.java)
            }.frequencyOnClick {

            }.zoneSelectOnClick {
                //startActivity(ZoneSettingActivity::class.java)
            }.probeTypeOnclick {

            }.recordQueryOnclick {

            }
            XPopup.Builder(requireContext())
                .isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(settingDialog)
                .show()
        }
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
        binding.surfaceViewRect.holder.addCallback(this)
        binding.surfaceViewRect.setZOrderMediaOverlay(true)
        binding.surfaceViewRect.holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    /**
     * 设置扇形图的数据
     */
    @SuppressLint("SetTextI18n")
    private fun setChartData() {
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(PassInfoManager.hasPass, "净通过"))
        entries.add(PieEntry(PassInfoManager.phoneAlarms, "电子产品报警"))
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
            phoneAlarmTv.text = "电子产品：" + PassInfoManager.phoneAlarms.toInt()
            otherAlarmTv.text = "其他报警：" + PassInfoManager.otherAlarms.toInt()
        }
    }

    override fun initListener() {


        DataMcuProcess.instance.locationEvent.observe(this) {
            binding.tvLocation.text = it
        }

        updateUIAction.observe(this) {
            it?.let {
                updateTime()
            }
        }
        DataMcuProcess.instance.alarmGoodsEvent.observe(this) {
            if (!binding.tvType.text.toString().contains(it)) {
                binding.tvType.text = it
            }
            if (it == "电子产品") {
                speak()
                PassInfoManager.phoneAlarms += 1
                PassInfoManager.otherAlarms =
                    PassInfoManager.totalAlarms - PassInfoManager.phoneAlarms
                setChartData()
            }
        }

        binding.passTv.setOnClickListener {
            PassInfoManager.totalPass += 1
            PassInfoManager.hasPass += 1
            setChartData()
//            binding.scanView.startScanAnim()
//            mqttSerModel.uploadRecord(0, "IN", mIDCardInfo, captureBitmap)

        }

        binding.phoneAlarmTv.setOnClickListener {
            PassInfoManager.totalPass += 1
            PassInfoManager.phoneAlarms += 1
            setChartData()
//            mqttSerModel.uploadRecord(1, "IN", mIDCardInfo, captureBitmap)
//            speak()
        }




        binding.otherAlarmTv.setOnClickListener {
            PassInfoManager.totalPass += 1
            PassInfoManager.otherAlarms += 1
            setChartData()
//            mqttSerModel.uploadRecord(1, "OUT", mIDCardInfo, captureBitmap)
            //faceModel.executeIDCardVer()
        }


        DataMcuProcess.instance.passNumberEvent.observe(this) {
            it?.toFloat()?.let {
                PassInfoManager.totalPass = it
                setChartData()
            }
        }
        DataMcuProcess.instance.alarmNumberEvent.observe(this) {
            it?.toFloat()?.let {
                PassInfoManager.totalAlarms = it
                PassInfoManager.hasPass = PassInfoManager.totalPass - PassInfoManager.totalAlarms
                setChartData()
            }
        }

    }


    /**
     * 更新左上角的时间
     */
    @SuppressLint("SetTextI18n")
    private fun updateTime() {
        binding.timeTv.text =
            DateUtil.getCurrentDateTime(DateUtil.Y_M_D_H_M) + " " + DateUtil.getCurrentDayOfWeekCH()
    }


    /**
     *  疑似电子物品
     */
    fun speak() {
        MediaPlayerManager.setDataSource(requireContext(), R.raw.electronic_goods)
    }

    /**
     * 身份识别成功
     */
    private fun speakIdCardSuccess() {
        MediaPlayerManager.setDataSource(requireContext(), R.raw.idcard_success)
        Logger.e("身份识别成功")
        mqttSerModel.uploadRecord(0, "IN", mIDCardInfo, captureBitmap)
    }

    /**
     * 身份识别失败
     */
    private fun speakIdCardFailed() {
        MediaPlayerManager.setDataSource(requireContext(), R.raw.idcard_failed)
        Logger.e("身份识别失败")
        mqttSerModel.uploadRecord(0, "IN", mIDCardInfo, captureBitmap)
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

    private fun inputCardImage(cardData: Bitmap) {
        val width = cardData.width / 4 * 4
        val height = cardData.height / 2 * 2
        var nv21Data = DrawUtils.bitmapToNav21(cardData, width, height)
        //
        if (faceModel.isInit) {
            Logger.e("图片的宽高：${width},${height}")
            val result =
                IdCardVerifyManager.getInstance()
                    .inputIdCardData(nv21Data, width, height)
            Logger.e("input image result:${result.errCode}")
        }
    }

    private var camereId = 0
    private var camera: Camera? = null
    private var displayOrientation = 0
    override fun surfaceCreated(holder: SurfaceHolder) {
        camereId = Camera.getNumberOfCameras() - 1
        camera = Camera.open(camereId)
        kotlin.runCatching {
            val metrics = DisplayMetrics()
            requireActivity().windowManager?.defaultDisplay?.getMetrics(metrics)
            val parameters = camera?.getParameters()
            parameters?.let {
                val previewSize: Camera.Size =
                    getBestSupportedSize(it.supportedPreviewSizes, metrics)
                parameters.setPreviewSize(previewSize.width, previewSize.height)
                camera?.setParameters(parameters)
                val mWidth = previewSize.width
                val mHeight = previewSize.height
                displayOrientation =
                    getCameraOri(requireActivity().windowManager.defaultDisplay.rotation)
                camera?.setDisplayOrientation(displayOrientation)
                Logger.e("当前实时画面")
                camera?.setPreviewDisplay(holder)
                camera?.setPreviewCallback { data, camera ->
                    if (faceModel.isInit) {
                        val result = IdCardVerifyManager.getInstance()
                            .onPreviewData(data, mWidth, mHeight, true)

                        if (hasCapture) {
                            captureBitmap = capture(data, camera)
                            hasCapture = false
                        }
                        if (result.errCode != IdCardVerifyError.OK) {
                            when (result.errCode) {
                                65539 -> {
                                    // Logger.i("未检测到人脸")
                                }
                            }
                        }

                        binding.surfaceViewRect.let { surfaceRect ->
                            val canvas: Canvas = surfaceRect.holder.lockCanvas()
                            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
                            val rect = result.faceRect
                            rect?.let {
                                val adjustedRect: Rect? = DrawUtils.adjustRect(
                                    rect,
                                    mWidth,
                                    mHeight,
                                    canvas.width,
                                    canvas.height,
                                    displayOrientation,
                                    camereId
                                )
                                DrawUtils.drawFaceRect(canvas, adjustedRect, Color.YELLOW, 5)
                                //画人脸框
                                //Logger.e("绘制人脸框")
                            }
                            surfaceRect.holder.unlockCanvasAndPost(canvas)
                        }
                    }
                }
                camera?.startPreview()
            }
        }.onFailure {
            Logger.e("出错：${it.message}")
            camera = null
        }
    }


    private fun isCamera(): Boolean {
        return true
    }

    /**
     * 抓拍
     */
    private fun capture(data: ByteArray, camera: Camera): Bitmap? {
        if (data == null || camera == null) {
            return null
        }
        val size = camera.parameters.previewSize
        try {
            val image = YuvImage(data, ImageFormat.NV21, size.width, size.height, null)
            if (image != null) {
                val stream = ByteArrayOutputStream()
                image.compressToJpeg(Rect(0, 0, size.width, size.height), 80, stream)
                val bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
                stream.close()
                Logger.i("图片抓拍成功！")
                return bitmap
            }
        } catch (e: java.lang.Exception) {
            Logger.e("抓拍图片报错：${e.message}")
        }
        return null

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.setPreviewCallback(null)
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun getBestSupportedSize(
        sizes: List<Camera.Size>,
        metrics: DisplayMetrics
    ): Camera.Size {
        var bestSize = sizes[0]
        var screenRatio = metrics.widthPixels.toFloat() / metrics.heightPixels.toFloat()
        if (screenRatio > 1) {
            screenRatio = 1 / screenRatio
        }
        for (s in sizes) {
            if (Math.abs(s.height / s.width.toFloat() - screenRatio) < Math.abs(bestSize.height / bestSize.width.toFloat() - screenRatio)) {
                bestSize = s
            }
        }
        return bestSize
    }

    private fun getCameraOri(rotation: Int): Int {
        var degrees = rotation * 90
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
            else -> {}
        }
        var result: Int
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(camereId, info)
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        return result
    }

}