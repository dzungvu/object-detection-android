package com.luke.objectdetection.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.luke.objectdetection.R
import kotlin.math.abs

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var bounds = Rect()

    //region handle click
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L

    private var onChooseBoxListener: OnChooseBoxListener? = null
    //endregion

    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP -> {
                val upX = event.x
                val upY = event.y
                val upTime = System.currentTimeMillis()

                val duration = upTime - downTime
                val distanceX = abs(upX - downX)
                val distanceY = abs(upY - downY)

                if (duration < 200 && distanceX < 10 && distanceY < 10) {
                    // It's a click
                    handleClick(upX, upY)
                }
            }
        }
        return true
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results.forEach {
            val left = it.x1 * width
            val top = it.y1 * height
            val right = it.x2 * width
            val bottom = it.y2 * height

            canvas.drawRect(left, top, right, bottom, boxPaint)
            val drawableText = it.clsName

            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)

        }
    }

    private fun handleClick(x: Float, y: Float) {
        val clickedBoxes = results.filter { box ->
            val left = box.x1 * width
            val right = box.x2 * width
            val top = box.y1 * height
            val bottom = box.y2 * height
            x in left..right && y in top..bottom
        }

        if (clickedBoxes.isNotEmpty()) {
            var boxWidth = Float.MAX_VALUE
            var boxHeight = Float.MAX_VALUE
            var selectedBox: BoundingBox? = null
            clickedBoxes.forEach { box ->
                calculateWidthHeightOfSelectedBox(box).apply {
                    if(boxWidth > first && boxHeight > second) {
                        boxWidth = first
                        boxHeight = second
                        selectedBox = box
                    }
                }
            }
            if(selectedBox == null) selectedBox = clickedBoxes.last()
            selectedBox?.let {
                Log.d("OverlayView", "Clicked on ${it.clsName}")
                onChooseBoxListener?.onChooseBox(it)
            }
        }
    }

    private fun calculateWidthHeightOfSelectedBox(box: BoundingBox): Pair<Float, Float> {
        val left = box.x1 * width
        val right = box.x2 * width
        val top = box.y1 * height
        val bottom = box.y2 * height
        return Pair(right - left, bottom - top)
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }

    fun setOnChooseBoxListener(listener: OnChooseBoxListener) {
        onChooseBoxListener = listener
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }

    interface OnChooseBoxListener {
        fun onChooseBox(box: BoundingBox)
    }
}