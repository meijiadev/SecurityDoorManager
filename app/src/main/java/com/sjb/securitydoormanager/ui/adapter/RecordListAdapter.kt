package com.sjb.securitydoormanager.ui.adapter

import android.app.AppComponentFactory
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.bean.RecordData

/**
 * desc: 安检记录的列表
 * time:2023/07/26
 */
class RecordListAdapter(layoutId: Int) : BaseQuickAdapter<RecordData, BaseViewHolder>(layoutId) {


    override fun convert(holder: BaseViewHolder, item: RecordData) {
        holder.getView<AppCompatTextView>(R.id.time_tv).text = item.time
        holder.getView<AppCompatTextView>(R.id.mode_tv).text = item.mode
        holder.getView<AppCompatTextView>(R.id.status_tv).text = item.status

    }


}