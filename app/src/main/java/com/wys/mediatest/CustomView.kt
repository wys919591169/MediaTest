package com.wys.mediatest

import android.content.Context
import android.view.View
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet


/**
 *  author : wys
 *  date : 2020/4/24 0:31
 *  description :注意构造方法中的3个参数不能少，不然会出错
 */
class CustomView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr){

    internal var paint = Paint()
    internal var bitmap: Bitmap? = null

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        bitmap =
            BitmapFactory.decodeResource(resources,R.mipmap.pic)  // 获取bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 不建议在onDraw做任何分配内存的操作
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, 100f, 200f, paint)
        }
    }
}