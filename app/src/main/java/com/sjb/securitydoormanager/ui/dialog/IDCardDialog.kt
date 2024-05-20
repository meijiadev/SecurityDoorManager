package com.sjb.securitydoormanager.ui.dialog

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.lxj.xpopup.core.CenterPopupView
import com.sjb.securitydoormanager.R
import com.sjb.securitydoormanager.idr.IDCardInfo

class IDCardDialog(context: Context) : CenterPopupView(context) {



    private val tvName: TextView by lazy { findViewById(R.id.tv_name) }
    private val tvCardPic: ImageView by lazy { findViewById(R.id.iv_card_picture) }
    private val tvSex: TextView by lazy { findViewById(R.id.tv_sex) }
    private val tvNation: TextView by lazy { findViewById(R.id.tv_nation) }
    private val tvCardId: TextView by lazy { findViewById(R.id.tv_card_id) }

    // 身份证信息
    private var mIDCardInfo: IDCardInfo? = null



    override fun getImplLayoutId(): Int = R.layout.dialog_idcard_layout


    override fun init() {
        super.init()
        mIDCardInfo?.let {
            tvName.text = "姓名："+it.name
            tvSex.text = "性别："+it.sex
            tvCardId.text ="证件号码："+ it.cardId
            tvNation.text = "民族："+it.nation
            tvCardPic.setImageBitmap(it.cardPic)
        }

    }

    fun setCardInfo(info: IDCardInfo): IDCardDialog = apply {
        this.mIDCardInfo = info
    }


}