package com.sjb.securitydoormanager.util

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.*
import android.view.View
import java.nio.charset.Charset


/**
 *    author : MJ
 *    time   : 2022/04/30
 *    desc   : 字符串工具类
 */
object StringUtil {
    //纯Kotlin判断是否是空字符，相当于Android中的TextUtils.isEmpty
    fun isEmpty(s: String?): Boolean {
        s?.let {
            for (element in it) {
                if (!Character.isWhitespace(element)) {
                    return false
                }
            }
        }
        return true
    }

    //设置同一个字符串不同字体大小和颜色
    fun createTextSpan(
        content: String,
        startIndex: Int,
        endIndex: Int,
        textColor: Int,
        textSize: Int,
        textStyle: Int = Typeface.NORMAL,
        showUnderLine: Boolean = false,         //是否设置下划线
        listener: ((v: View) -> Unit)? = null  //设置点击事件
    ): SpannableString {
        var endIndex = endIndex
        if (endIndex > content.length) {
            endIndex = content.length
        }
        val spannableString = SpannableString(content)
        with(spannableString) {
            //Spanned.SPAN_INCLUSIVE_EXCLUSIVE：从起始下标到终止下标，包括起始下标，不包括终止下标
            //Spanned.SPAN_INCLUSIVE_INCLUSIVE：从起始下标到终止下标，同时包括起始下标和终止下标
            //Spanned.SPAN_EXCLUSIVE_EXCLUSIVE：从起始下标到终止下标，但都不包括起始下标和终止下标
            //Spanned.SPAN_EXCLUSIVE_INCLUSIVE：从起始下标到终止下标，不包括起始下标，包括终止下标

            //设置不同字体颜色
            setSpan(ForegroundColorSpan(textColor), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            //设置不同字体大小
            setSpan(AbsoluteSizeSpan(textSize), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            //设置不同的textStyle
            setSpan(StyleSpan(textStyle), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            //是否设置下划线
            if (showUnderLine) {
                setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            //设置点击事件
            listener?.let {
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        it.invoke(widget)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = textColor  //设置字体颜色
                        ds.isUnderlineText = showUnderLine  //是否显示下划线
                    }

                }, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
        return spannableString
    }

    /**
     * 16进制直接转换成为字符串(无需Unicode解码)
     * @param hexStr
     * @return
     */
    fun hexStr2Str(hexStr: String): String? {
        val str = "0123456789ABCDEF"
        val hexs = hexStr.toCharArray()
        val bytes = ByteArray(hexStr.length / 2)
        var n: Int
        for (i in bytes.indices) {
            n = str.indexOf(hexs[2 * i]) * 16
            n += str.indexOf(hexs[2 * i + 1])
            bytes[i] = (n and 0xff).toByte()
        }
        return String(bytes, Charset.forName("UTF-8"))
    }

}