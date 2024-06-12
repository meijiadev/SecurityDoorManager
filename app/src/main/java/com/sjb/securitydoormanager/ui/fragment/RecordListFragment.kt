package com.sjb.securitydoormanager.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.base.BaseViewModel
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.bean.RecordData
import com.sjb.securitydoormanager.databinding.FragmentRecordListBinding
import com.sjb.securitydoormanager.ui.adapter.RecordListAdapter

/**
 *安检记录
 */
class RecordListFragment : BaseMvFragment<FragmentRecordListBinding, BaseViewModel>() {
    private lateinit var recordListAdapter: RecordListAdapter
    private var recordDatas: MutableList<RecordData> = mutableListOf()


    companion object {
        fun newInstance(): RecordListFragment {
            val args = Bundle()
            val fragment = RecordListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initParam() {
        recordDatas.add(RecordData("2023-8-04 12:17:45:225", "全金属模式", "正常"))
        recordDatas.add(RecordData("2023-8-04 14:17:45:225", "手机模式", "正常"))
        recordDatas.add(RecordData("2023-8-04 13:17:45:225", "全金属模式", "正常"))
        recordDatas.add(RecordData("2023-8-04 15:15:45:225", "手机模式", "正常"))
        recordDatas.add(RecordData("2023-8-04 15:16:45:225", "全金属模式", "正常"))
        recordDatas.add(RecordData("2023-8-04 15:17:50:225", "手机模式", "正常"))
    }

    override fun initData() {
        recordListAdapter = RecordListAdapter(R.layout.item_record)
        binding.recordList.adapter = recordListAdapter
        binding.recordList.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recordListAdapter.data = recordDatas

        
    }

    override fun initViewObservable() {

    }



    override fun initView() {

    }

    override fun initListener() {

    }

}