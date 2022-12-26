package com.sjb.securitydoormanager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import com.orhanobut.logger.Logger
import kotlin.math.ceil
import android.util.AttributeSet as AttributeSet1

class ScanView : View{

    /**
     * 扫描的图片drawable
     */
    private var scanImg: Drawable? = null

    private lateinit var paint: Paint
    /**
     * 控件的宽
     */
    private var viewWidth: Int = 0
    /**
     * 控件的高
     */
    private var viewHeight: Int = 0
    private var bitmapMatrix = Matrix()

    private var scanBitmap: Bitmap? = null

    /**
     * 扫描图片需要显示的高度
     */
    private var showBitmapHeight: Float = 0F

    /**
     * 控制动画是竖向或者横向
     */
    private var isVertical = true
    /**
     * 控制动画初始是从底部/左边开始（true），或者从上边/右边开始（false）
     */
    private var isStartFromBottom = true

    private var isPositive = true

    fun setVertical(isVertical: Boolean) {
        this.isVertical = isVertical
        stopScanAnimAndReset()
        setScanBitmap()
    }

    fun setStartFromBootom(isFromBottom: Boolean) {
        this.isStartFromBottom = isFromBottom
        stopScanAnimAndReset()
    }

    /**
     * 属性动画
     */
    private var valueAnimator: ValueAnimator? = null

    /**
     * 动画时长
     */
    private var animDuration: Long = 1000L

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet1?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet1?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttribute(context, attrs)
        init()
    }

    private fun initAttribute(context: Context, attrs: AttributeSet1?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanView)
            scanImg = typedArray.getDrawable(R.styleable.ScanView_unit_scan_img)
            animDuration = typedArray.getInt(R.styleable.ScanView_anim_duration, 1000).toLong()
            typedArray.recycle()
        }
    }

    fun setAnimDuration(time: Long) {
        animDuration = time
    }

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
        var unitImgBitmap = drawable.toBitmap()

        if (unitImgBitmap.isRecycled) {
            return null
        }

        if (unitImgBitmap.isRecycled) {
            return null
        }

        // 处理横置的时候图片的旋转（因为视觉给的图一般是一个竖向的图，因此在横置的时候，手动将图片同步横置）
        if (!isVertical) {
            val matrix = Matrix()
            matrix.postRotate(90f)
            val resizedBitmap = Bitmap.createBitmap(unitImgBitmap, 0, 0,
                unitImgBitmap.width, unitImgBitmap.height, matrix, true)
            if (resizedBitmap != unitImgBitmap && unitImgBitmap != null && !unitImgBitmap.isRecycled) {
                unitImgBitmap.recycle()
                unitImgBitmap = resizedBitmap
            }
        }

        var realWidth: Int
        val finalBitmap: Bitmap
        val realUnitImgWidth: Float

        if (isVertical) {
            realWidth = viewWidth
            finalBitmap = Bitmap.createBitmap(realWidth, unitImgBitmap.height, Bitmap.Config.ARGB_8888)
            realUnitImgWidth = unitImgBitmap.width.toFloat()
        } else {
            realWidth = viewHeight
            finalBitmap = Bitmap.createBitmap(unitImgBitmap.width, realWidth, Bitmap.Config.ARGB_8888)
            realUnitImgWidth = unitImgBitmap.height.toFloat()
        }

        val canvas = Canvas(finalBitmap)
        // 向上取整
        val count = ceil(realWidth / realUnitImgWidth).toInt()

        // 为了解决适配问题，因为不同手机宽度不同，如果 UI 只提供了一个尺寸的素材，则可能会出现拉伸
        // 导致视觉效果不好的问题。这里换一种解决思路，即不将图片进行缩放，而是根据时机的宽度，
        // 去重复拼凑 unitImgBitmap，使其转换为一个充满宽度的整图，从而避免缩放导致的拉伸问题。
        // 需要注意的是，此时需要跟视觉协商，只需要给最小单元的图片素材即可。
        if (isVertical) {
            for (i in 0 until count) {
                canvas.drawBitmap(unitImgBitmap,i * realUnitImgWidth, 0f, paint)
            }
        } else {
            for (i in 0 until count) {
                canvas.drawBitmap(unitImgBitmap,0f, i * realUnitImgWidth, paint)
            }
        }

        return finalBitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        scanBitmap?.let {
            canvas.drawBitmap(it, bitmapMatrix, paint)
        }
    }

    /**
     * 开始做属性动画
     */
    fun startScanAnim() {
        valueAnimator?.takeIf { it.isRunning }?.let { it.cancel() }
        val value = if(isVertical) viewHeight.toFloat() else viewWidth.toFloat()
        Logger.i("当前属性:$value")
        valueAnimator = if (isStartFromBottom) {
            ValueAnimator.ofFloat(value + showBitmapHeight, -showBitmapHeight)
        } else {
            ValueAnimator.ofFloat(-showBitmapHeight, value + showBitmapHeight)
        }
        valueAnimator?.apply {
            // 使得扫描动画在横竖状态下都是相同的速度
            duration = if (isVertical) animDuration else (animDuration * 1.0f / viewWidth * viewHeight).toLong()
            repeatCount = -1
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener(getUpdateListener())
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    // 用于控制 scan img 动画来回时的方向
                    isPositive = !isPositive
                }
            })
            start()
        }
    }

    // 通过直接返回对应的 ValueAnimator.AnimatorUpdateListener，
    // 而不是在 ValueAnimator.AnimatorUpdateListener 回调中做 if 判断，提高性能
    private fun getUpdateListener(): ValueAnimator.AnimatorUpdateListener {
        return if (isVertical) {
            if (isStartFromBottom) {
                ValueAnimator.AnimatorUpdateListener { animation ->
                    val value = animation.animatedValue as? Float ?: return@AnimatorUpdateListener
                    bitmapMatrix.setTranslate(0F, value)
                    // 使得 bitmap 来回动画的时候，方向是相对的
                    bitmapMatrix.preScale(1.0f, if (isPositive) -1.0f else 1.0f)
                    invalidate()
                }
            } else {
                ValueAnimator.AnimatorUpdateListener { animation ->
                    val value = animation.animatedValue as? Float ?: return@AnimatorUpdateListener
                    bitmapMatrix.setTranslate(0F, value)
                    // 使得 bitmap 来回动画的时候，方向是相对的
                    bitmapMatrix.preScale(1.0f, if (isPositive) 1.0f else -1.0f)
                    invalidate()
                }
            }
        } else {
            if (isStartFromBottom) {
                ValueAnimator.AnimatorUpdateListener { animation ->
                    val value = animation.animatedValue as? Float ?: return@AnimatorUpdateListener
                    bitmapMatrix.setTranslate(value, 0f)
                    bitmapMatrix.preScale(if (isPositive) 1.0f else -1.0f, 1.0f)
                    invalidate()
                }
            } else {
                ValueAnimator.AnimatorUpdateListener { animation ->
                    val value = animation.animatedValue as? Float ?: return@AnimatorUpdateListener
                    bitmapMatrix.setTranslate(value, 0f)
                    bitmapMatrix.preScale(if (isPositive) -1.0f else 1.0f, 1.0f)
                    invalidate()
                }
            }
        }
    }

    /**
     * 停止属性动画
     */
    fun stopScanAnimAndReset() {
        valueAnimator?.takeIf { it.isRunning }?.cancel()
        reset(true)
    }

    /**
     * 重置为初始状态
     */
    private fun reset(isInvalidate: Boolean) {
        bitmapMatrix.reset()

        isPositive = true
        if (isVertical) {
            if (isStartFromBottom) {
                bitmapMatrix.setTranslate(0F, viewHeight + showBitmapHeight)
                bitmapMatrix.preScale(1.0f, if (isPositive) -1.0f else 1.0f)
            } else {
                bitmapMatrix.setTranslate(0F, -showBitmapHeight)
                bitmapMatrix.preScale(1.0f, if (isPositive) 1.0f else -1.0f)
            }
        } else {
            if (isStartFromBottom) {
                bitmapMatrix.setTranslate(viewWidth + showBitmapHeight, 0f)
                bitmapMatrix.preScale(if (isPositive) 1.0f else -1.0f, 1.0f)
            } else {
                bitmapMatrix.setTranslate(-showBitmapHeight, 0f)
                bitmapMatrix.preScale(if (isPositive) -1.0f else 1.0f, 1.0f)
            }
        }

        if (isInvalidate) {
            invalidate()
        }
    }

    private fun setScanBitmap() {
        if (scanImg == null || viewWidth <= 0 || viewHeight <= 0) {
            return
        }

        val bitmap = getBitmapFromDrawable(scanImg!!) ?: return
        scanBitmap = bitmap

        showBitmapHeight = if (isVertical) bitmap.height.toFloat() else bitmap.width.toFloat()

        reset(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        setScanBitmap()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopScanAnimAndReset()
    }
}