package com.sjb.securitydoormanager.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Surface
import android.view.SurfaceHolder
import androidx.fragment.app.Fragment
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
import com.permissionx.guolindev.PermissionX
import com.sjb.base.base.BaseMvActivity
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.constant.PassInfoManager
import com.sjb.securitydoormanager.databinding.ActivityHomeBinding
import com.sjb.securitydoormanager.idr.IDCardInfo
import com.sjb.securitydoormanager.media.MediaPlayerManager
import com.sjb.securitydoormanager.serialport.DataMcuProcess
import com.sjb.securitydoormanager.serialport.DataProtocol
import com.sjb.securitydoormanager.serialport.SerialPortManager
import com.sjb.securitydoormanager.ui.dialog.IDCardDialog
import com.sjb.securitydoormanager.ui.dialog.SettingDialog
import com.sjb.securitydoormanager.ui.fragment.RecordListFragment
import com.sjb.securitydoormanager.ui.fragment.TotalDataFragment
import com.sjb.securitydoormanager.ui.model.HomeViewModel
import com.sjb.securitydoormanager.ui.model.IDFaceViewModel
import com.sjb.securitydoormanager.ui.model.MqttSerModel
import com.sjb.securitydoormanager.veriface.DrawUtils
import com.ys.rkapi.MyManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class HomeActivity : BaseMvActivity<ActivityHomeBinding, HomeViewModel>(), SurfaceHolder.Callback {

    private val totalDataFragment: TotalDataFragment by lazy { TotalDataFragment.newInstance() }
    private val recordListFragment: RecordListFragment by lazy { RecordListFragment.newInstance() }

    private var pieDataSet: PieDataSet? = null
    private var updateUIAction = UnPeekLiveData<Int>()

    /**
     *权限请求是否通过
     */
    private var isPermissionGranted = false
    private lateinit var faceModel: IDFaceViewModel
    private lateinit var mqttSerModel: MqttSerModel

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


    override fun getViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }


    override fun initParam() {
        initYsAndroidApi()
        faceModel = getActivityViewModel(IDFaceViewModel::class.java)
        mqttSerModel = getActivityViewModel(MqttSerModel::class.java)
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
        viewModel.idCardLiveData.observe(this) {
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
     * 初始化安卓开发板的api
     */
    private fun initYsAndroidApi() {
        val myManager = MyManager.getInstance(this)
        myManager.bindAIDLService(this)
        myManager.setConnectClickInterface {
            Logger.e(
                "当前sdk的版本号：${myManager.firmwareVersion} \n " +
                        "当前设备的型号：${myManager.androidModle} \n" +
                        "设备的系统版本：${myManager.androidVersion} \n " +
                        "当前设备的内存容量：${myManager.runningMemory} \n" +
                        "获取设备的sn码：${myManager.sn}"
            )
            if (myManager.firmwareVersion.toInt() < 4) {
                Logger.e("当前SDK的版本号小于4.0")
            }
            // 开机自启动
            myManager.selfStart("com.sjb.securitydoormanager")
            // 打开网络adb连接
            myManager.setNetworkAdb(true)
            // 设置守护进程 0:30s  1：60s   2:180s
            myManager.daemon("com.sjb.securitydoormanager", 2)
        }
    }

    /**
     * 开机自检
     */
    private fun powerOnSelfTest() {

    }

    override fun initView() {
//        binding.run {
//            // 设置是否显示扇形图中的文字
//            pieChart.setDrawEntryLabels(false)
//            // 是否显示圆心
//            pieChart.isDrawHoleEnabled = true
//            pieChart.holeRadius = 50f
//            val desc = Description()
//            desc.isEnabled = false
//            pieChart.description = desc
//            // 设置圆心的颜色
//            pieChart.setHoleColor(Color.TRANSPARENT)
//            val legend = pieChart.legend
//            legend.isWordWrapEnabled = true
//            //设置图例的实际对齐方式
//            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
//            //设置图例水平对齐方式
//            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
//            //设置图例方向
//            legend.orientation = Legend.LegendOrientation.VERTICAL
//            legend.textColor = resources.getColor(R.color.text_c6cbff)
//            //设置图例是否在图表内绘制
//            legend.setDrawInside(false)
//        }
        //setChartData()
        binding.scanView.setVertical(true)
        binding.scanView.setStartFromBottom(false)
        binding.scanView.setAnimDuration(2000)
        binding.surfaceViewRect.holder.addCallback(this)
        binding.surfaceViewRect.setZOrderMediaOverlay(true)
        binding.surfaceViewRect.holder.setFormat(PixelFormat.TRANSLUCENT)
        switchFragment(totalDataFragment)
        totalDataFragment.setMcu(DataMcuProcess.instance)
    }

    /**
     * 当前的fragment
     */
    private var mFragment = Fragment()

    /**
     * 切换fragment视图
     */
    private fun switchFragment(target: Fragment) {
        if (target != mFragment) {
            val transaction = supportFragmentManager.beginTransaction()
            if (target is TotalDataFragment) {
                transaction.setCustomAnimations(
                    R.anim.action_left_enter,
                    R.anim.action_left_exit
                )
            } else {
                transaction.setCustomAnimations(
                    R.anim.action_rigth_enter,
                    R.anim.action_rigth_exit
                )
            }
            // 先判断fragment是否已经被添加到管理器
            if (!target.isAdded) {
                transaction.hide(mFragment).add(R.id.layout_container, target)
                    .commitAllowingStateLoss()
            } else {
                // 添加的fragment 直接显示
                transaction.hide(mFragment).show(target).commitAllowingStateLoss()
            }
            mFragment = target
        }

    }


    override fun initData() {
        updateTime()
        TimeThread().start()
        viewModel.initIDR()
        MainScope().launch {
            val mcuProcess = DataMcuProcess.instance
            delay(1000)
            initPermission()
            val serialManager = SerialPortManager.instance
            serialManager.init()
            serialManager.setIData(mcuProcess)
            serialManager.startRead()
            delay(100)
            // 协议建立链接
            serialManager.sendMsg(DataProtocol.connectData)
            delay(200)
            // 只要有人过就上传数据
            serialManager.sendMsg(DataProtocol.data_0x04)
            delay(200)
            //要求安检门上传参数
            serialManager.sendMsg(DataProtocol.data_0x02)
            viewModel.startRead()
            mqttSerModel.initMqttSer()
            binding.scanView.startScanAnim()
        }

    }

    private var idCardDialog: IDCardDialog? = null
    private fun showCardDialog(idCardInfo: IDCardInfo) {
        if (idCardDialog == null || idCardDialog?.isShow == false) {
            idCardDialog = IDCardDialog(this)
                .setCardInfo(idCardInfo)
            XPopup.Builder(this)
                .isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(idCardDialog)
                .show()
                .delayDismiss(3000)
        }
    }


    override fun initListener() {

        DataMcuProcess.instance.alarmGoodsEvent.observe(this) {
            if (!binding.tvType.text.toString().contains(it)) {
                binding.tvType.text = it
            }

        }
        DataMcuProcess.instance.locationEvent.observe(this) {
            binding.tvLocation.text = it
        }

        updateUIAction.observe(this) {
            it?.let {
                updateTime()
            }
        }
        DataMcuProcess.instance.alarmGoodsEvent.observe(this) {
            if (it == "电子产品") {
                speak()
            }
        }
    }

    override fun initViewObservable() {
        // 点击设置按钮
        binding.settingIv.setOnClickListener {
            val settingDialog = SettingDialog(this).sensitivityOnClick {
                startActivity(SensitivityActivity::class.java)
            }.frequencyOnClick {

            }.zoneSelectOnClick {
                startActivity(ZoneSettingActivity::class.java)
            }.probeTypeOnclick {

            }.recordQueryOnclick {

            }
            XPopup.Builder(this)
                .isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(settingDialog)
                .show()
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
        MediaPlayerManager.setDataSource(this, R.raw.electronic_goods)
    }

    /**
     * 身份识别成功
     */
    private fun speakIdCardSuccess() {
        MediaPlayerManager.setDataSource(this, R.raw.idcard_success)
        Logger.e("身份识别成功")
        mqttSerModel.uploadRecord(0, "IN", mIDCardInfo, captureBitmap)
    }

    /**
     * 身份识别失败
     */
    private fun speakIdCardFailed() {
        MediaPlayerManager.setDataSource(this, R.raw.idcard_failed)
        Logger.e("身份识别失败")
        mqttSerModel.uploadRecord(0, "IN", mIDCardInfo, captureBitmap)
    }

    /**
     * 初始化权限请求
     */
    private fun initPermission() {
        // 安卓版本是否大于 6.0
        if (Build.VERSION.SDK_INT >= 23) {
            // 检查是否开启权限
            if (!PermissionX.isGranted(
                    this,
                    Manifest.permission.CAMERA
                ) || !PermissionX.isGranted(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) || !PermissionX.isGranted(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) || !PermissionX.isGranted(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) || !PermissionX.isGranted(this, Manifest.permission.READ_PHONE_STATE)
            ) {
                // 权限没有开启
                PermissionX.init(this).permissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
                ).request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        isPermissionGranted = true
                        Logger.i("所有申请都已通过")
                        //faceModel.initIdCardVerify()

                    } else {
                        isPermissionGranted = false
                        Logger.i("拒绝了以下的权限：$deniedList")
                    }
                }
            } else {
                isPermissionGranted = true
                //  faceModel.initIdCardVerify()
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
                for (dev in devices) {
                    Logger.i("设备源：${dev.type}")
                }

            }
        } else {
            isPermissionGranted = true
            //   faceModel.initIdCardVerify()
        }
    }


    override fun onStop() {
        super.onStop()
        SerialPortManager.instance.onDestroy()
    }

    override fun onDestroy() {
        //反初始化
        //activeService?.shutdown()
        IdCardVerifyManager.getInstance().unInit()
        if (camera != null) {
            camera?.release()
        }
        super.onDestroy()
        viewModel.closeDevice()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
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
            windowManager.defaultDisplay.getMetrics(metrics)
            val parameters = camera?.getParameters()
            parameters?.let {
                val previewSize: Camera.Size =
                    getBestSupportedSize(it.supportedPreviewSizes, metrics)
                parameters.setPreviewSize(previewSize.width, previewSize.height)
                camera?.setParameters(parameters)
                val mWidth = previewSize.width
                val mHeight = previewSize.height
                displayOrientation = getCameraOri(windowManager.defaultDisplay.rotation)
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

                        binding.surfaceViewRect?.let { surfaceRect ->
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
