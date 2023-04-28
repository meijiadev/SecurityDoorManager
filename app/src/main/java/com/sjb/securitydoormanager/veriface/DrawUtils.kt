package com.sjb.securitydoormanager.veriface

import android.graphics.*
import android.hardware.Camera


object DrawUtils {
    fun adjustRect(
        rect: Rect?,
        previewWidth: Int,
        previewHeight: Int,
        canvasWidth: Int,
        canvasHeight: Int,
        cameraOri: Int,
        mCameraId: Int
    ): Rect? {
        var previewWidth = previewWidth
        var previewHeight = previewHeight
        if (rect == null) {
            return null
        }
        if (canvasWidth < canvasHeight) {
            val t = previewHeight
            previewHeight = previewWidth
            previewWidth = t
        }
        val widthRatio = canvasWidth.toFloat() / previewWidth.toFloat()
        val heightRatio = canvasHeight.toFloat() / previewHeight.toFloat()
        if (cameraOri == 0 || cameraOri == 180) {
            rect.left *= widthRatio.toInt()
            rect.right *= widthRatio.toInt()
            rect.top *= heightRatio.toInt()
            rect.bottom *= heightRatio.toInt()
        } else {
            rect.left *= heightRatio.toInt()
            rect.right *= heightRatio.toInt()
            rect.top *= widthRatio.toInt()
            rect.bottom *= widthRatio.toInt()
        }
        val newRect = Rect()
        when (cameraOri) {
            0 -> {
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = canvasWidth - rect.left
                    newRect.right = canvasWidth - rect.right
                } else {
                    newRect.left = rect.left
                    newRect.right = rect.right
                }
                newRect.top = rect.top
                newRect.bottom = rect.bottom
            }
            90 -> {
                newRect.right = canvasWidth - rect.top
                newRect.left = canvasWidth - rect.bottom
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = canvasHeight - rect.left
                    newRect.bottom = canvasHeight - rect.right
                } else {
                    newRect.top = rect.left
                    newRect.bottom = rect.right
                }
            }
            180 -> {
                newRect.top = canvasHeight - rect.bottom
                newRect.bottom = canvasHeight - rect.top
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = rect.left
                    newRect.right = rect.right
                } else {
                    newRect.left = canvasWidth - rect.right
                    newRect.right = canvasWidth - rect.left
                }
            }
            270 -> {
                newRect.left = rect.top
                newRect.right = rect.bottom
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = rect.left
                    newRect.bottom = rect.right
                } else {
                    newRect.top = canvasHeight - rect.right
                    newRect.bottom = canvasHeight - rect.left
                }
            }
            else -> {}
        }
        return newRect
    }

    fun drawFaceRect(canvas: Canvas?, rect: Rect?, color: Int, faceRectThickness: Int) {
        if (canvas == null || rect == null) {
            return
        }
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = faceRectThickness.toFloat()
        paint.color = color
        val mPath = Path()
        mPath.moveTo(rect.left.toFloat(), (rect.top + rect.height() / 4).toFloat())
        mPath.lineTo(rect.left.toFloat(), rect.top.toFloat())
        mPath.lineTo((rect.left + rect.width() / 4).toFloat(), rect.top.toFloat())
        mPath.moveTo((rect.right - rect.width() / 4).toFloat(), rect.top.toFloat())
        mPath.lineTo(rect.right.toFloat(), rect.top.toFloat())
        mPath.lineTo(rect.right.toFloat(), (rect.top + rect.height() / 4).toFloat())
        mPath.moveTo(rect.right.toFloat(), (rect.bottom - rect.height() / 4).toFloat())
        mPath.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        mPath.lineTo((rect.right - rect.width() / 4).toFloat(), rect.bottom.toFloat())
        mPath.moveTo((rect.left + rect.width() / 4).toFloat(), rect.bottom.toFloat())
        mPath.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        mPath.lineTo(rect.left.toFloat(), (rect.bottom - rect.height() / 4).toFloat())
        canvas.drawPath(mPath, paint)
    }

    /**
     * Bitmap 转化为 ARGB 数据，再转化为 NV21 数据
     *
     * @param src 传入的 Bitmap，格式为 Bitmap.Config.ARGB_8888
     * @param width NV21 图像的宽度
     * @param height NV21 图像的高度
     * @return nv21 数据
     */
    fun bitmapToNav21(src: Bitmap,width: Int,height: Int): ByteArray? {
        return if (src != null) {
            val argb = IntArray(width * height)
            src.getPixels(argb, 0, width, 0, 0, width, height)
            argbToNv21(argb, width, height)
        } else {
            null
        }
    }

    /**
     * ARGB 数据转化为 NV21 数据
     *
     * @param argb argb 数据
     * @param width 宽度
     * @param height 高度
     * @return nv21 数据
     */
    private fun argbToNv21(argb: IntArray, width: Int, height: Int): ByteArray? {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var index = 0
        val nv21 = ByteArray(width * height * 3 / 2)
        for (j in 0 until height) {
            for (i in 0 until width) {
                val R = argb[index] and 0xFF0000 shr 16
                val G = argb[index] and 0x00FF00 shr 8
                val B = argb[index] and 0x0000FF
                val Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                val U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                val V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128
                nv21[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                if ((j % 2) == 0 && index % 2 == 0 && (uvIndex < nv21.size
                            - 2)
                ) {
                    nv21[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                    nv21[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                }
                ++index
            }
        }
        return nv21
    }
}