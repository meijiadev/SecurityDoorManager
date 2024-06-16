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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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

class HomeActivity : BaseMvActivity<ActivityHomeBinding, HomeViewModel>(){


    private lateinit var navHostFragment: NavHostFragment

    private lateinit var navController: NavController

    /**
     *权限请求是否通过
     */
    private var isPermissionGranted = false
    private lateinit var faceModel: IDFaceViewModel
    private lateinit var mqttSerModel: MqttSerModel


    override fun getViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }


    override fun initParam() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHomeHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        initYsAndroidApi()
        faceModel = getActivityViewModel(IDFaceViewModel::class.java)
        mqttSerModel = getActivityViewModel(MqttSerModel::class.java)

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

    }

    override fun initData() {
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
        }

        Logger.i("初始化相关参数")

    }
    override fun initListener() {

    }

    override fun initViewObservable() {

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
//        if (camera != null) {
//            camera?.release()
//        }
        super.onDestroy()
        viewModel.closeDevice()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }



}
