package com.example.bouncingview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.animation.ValueAnimator
import kotlin.random.Random

class BouncingLogoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 1. Настройки отрисовки
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var logoText = "ANDROID"
    private val textBounds = Rect()

    // 2. Физика
    private var x = 0f
    private var y = 0f
    private var speedX = 7f
    private var speedY = 7f

    // 3. Аниматор
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1000L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            if (width > 0 && height > 0) {
                updatePhysics()
                invalidate()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (x == 0f && y == 0f) {
            x = w / 2f
            y = h / 2f
        }
        paint.getTextBounds(logoText, 0, logoText.length, textBounds)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    // 4. Обработка касаний (Клик мышью или пальцем)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Телепортируем объект в точку клика
                x = event.x
                y = event.y

                // Меняем направление скорости на противоположное
                // (опционально: можно добавить случайности)
                speedX = -speedX
                speedY = -speedY

                // Меняем цвет для визуального отклика
                changeColor()

                // Перерисовываем сразу (не ждем следующего кадра аниматора)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(logoText, x, y - textBounds.exactCenterY(), paint)
    }

    private fun updatePhysics() {
        val w = width.toFloat()
        val h = height.toFloat()

        x += speedX
        y += speedY

        val halfWidth = textBounds.width() / 2f
        val halfHeight = textBounds.height() / 2f

        // Отскок от правой стены
        if (x + halfWidth >= w) {
            x = w - halfWidth
            speedX = -speedX
            changeColor()
        }
        // Отскок от левой стены
        if (x - halfWidth <= 0) {
            x = halfWidth
            speedX = -speedX
            changeColor()
        }
        // Отскок от нижней стены
        if (y + halfHeight >= h) {
            y = h - halfHeight
            speedY = -speedY
            changeColor()
        }
        // Отскок от верхней стены
        if (y - halfHeight <= 0) {
            y = halfHeight
            speedY = -speedY
            changeColor()
        }
    }

    private fun changeColor() {
        paint.color = Color.rgb(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )
    }
}