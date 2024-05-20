package com.sjb.base.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.sjb.base.ext.getVmClazz
import java.lang.reflect.ParameterizedType

abstract class BaseMvFragment<V : ViewBinding, VM : BaseViewModel> : Fragment(), ToastAction,
    IBaseView {

    //是否第一次加载
    private var isFirst: Boolean = true
    private var _binding: V? = null
    protected val binding: V get() = _binding!!

    protected lateinit var viewModel: VM

    private lateinit var mActivityProvider: ViewModelProvider



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val type = javaClass.genericSuperclass
        val clazz = (type as ParameterizedType).actualTypeArguments[0] as Class<V>
        val method = clazz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        _binding = method.invoke(null, layoutInflater, container, false) as V
        viewModel = createViewModel()
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        initParam()
        initListener()
        initViewObservable()


        mActivityProvider = ViewModelProvider(this)
    }

    /**
     *创建ViewModel对象
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    /**
     * 获取Activity作用域的ViewModel
     */
    protected fun <T : ViewModel> getActivityViewModel(modelClass: Class<T>): T {
        if (mActivityProvider == null) {
            mActivityProvider = ViewModelProvider(this)
        }
        return mActivityProvider.get(modelClass)
    }

}