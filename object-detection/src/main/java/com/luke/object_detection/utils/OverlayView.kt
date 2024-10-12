package com.luke.object_detection.utils

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
import java.util.Locale
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
        textBackgroundPaint.color = Color.WHITE
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 36f

        textPaint.color = Color.BLACK
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 36f

        boxPaint.color = ContextCompat.getColor(context!!, android.R.color.white)
        boxPaint.strokeWidth = BACKGROUND_STROKE
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results.forEach { result ->
            val left = result.x1 * width
            val top = result.y1 * height
            val right = result.x2 * width
            val bottom = result.y2 * height

            canvas.drawRoundRect(left, top, right, bottom, BOX_RADIUS, BOX_RADIUS, boxPaint)

            val drawableText = result.clsName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }

            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()

            val rectLeft = left - BACKGROUND_STROKE / 2
            val rectTop = top - BACKGROUND_STROKE / 2
            val rectRight = left + textWidth + BOUNDING_RECT_TEXT_PADDING
            val rectBottom = top + textHeight + BOUNDING_RECT_TEXT_PADDING

            val path = android.graphics.Path().apply {
                moveTo(rectLeft, rectTop + BOX_RADIUS)
                arcTo(rectLeft, rectTop, rectLeft + BOX_RADIUS, rectTop + BOX_RADIUS, 180f, 90f, false)
                if(rectRight > right) {
                    lineTo(rectRight - BOX_RADIUS, rectTop)
                    arcTo(rectRight - BOX_RADIUS, rectTop, rectRight, rectTop + BOX_RADIUS, 270f, 90f, false)
                } else {
                    lineTo(rectRight, rectTop)
                }
                lineTo(rectRight, rectBottom - BOX_RADIUS)
                arcTo(rectRight - BOX_RADIUS, rectBottom - BOX_RADIUS, rectRight, rectBottom, 0f, 90f, false)
                lineTo(rectLeft, rectBottom)
                lineTo(rectLeft, rectTop + BOX_RADIUS)
                close()
            }
            canvas.drawPath(path, textBackgroundPaint)

            val textX = rectLeft + (rectRight - rectLeft) / 2 - textWidth / 2
            val textY = rectTop + (((rectBottom - rectTop) / 2) + (textHeight / 2)) - BIAS

            canvas.drawText(drawableText, textX, textY, textPaint)

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
                    if (boxWidth > first && boxHeight > second) {
                        boxWidth = first
                        boxHeight = second
                        selectedBox = box
                    }
                }
            }
            if (selectedBox == null) selectedBox = clickedBoxes.last()
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
        private const val BOX_RADIUS = 48f
        private const val BOUNDING_RECT_TEXT_PADDING = 48f
        private const val BIAS = 2f
        private const val BACKGROUND_STROKE = 6f
    }

    interface OnChooseBoxListener {
        fun onChooseBox(box: BoundingBox)
    }
}