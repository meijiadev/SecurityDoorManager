package com.sjb.base.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.sjb.base.ext.getVmClazz


abstract class BaseMvActivity<V : ViewBinding, VM : BaseViewModel> : AppCompatActivity(),ToastAction,
    IBaseView {

    //    /** 状态栏沉浸 */
//    private var immersionBar: ImmersionBar? = null
    protected lateinit var binding: V
    protected lateinit var viewModel: VM

    abstract fun getViewBinding(): V


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel = createViewModel()
        setContentView(binding.root)
        onViewCreated()
    }


    private fun onViewCreated() {
        initStatus()
        initView()
        initData()
        initListener()
        initViewObservable()

    }


    /**
     * 状态栏沉浸
     */
    private fun initStatus() {
        ImmersionBar.with(this) // 默认状态栏字体颜色为黑色
            .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
            .init()
    }

    /**
     *创建ViewModel对象
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    /**
     * 跳转 Activity 简化版
     */
    open fun startActivity(clazz: Class<out Activity?>?) {
        startActivity(Intent(this, clazz))
    }

}