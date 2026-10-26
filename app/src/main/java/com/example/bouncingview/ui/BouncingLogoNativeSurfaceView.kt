package com.example.bouncingview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class BouncingLogoNativeSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Choreographer.FrameCallback {

    // 1. Настройки отрисовки
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private var logoText = "WINDOWS XP"
    private val textBounds = Rect()

    // 2. Физика
    private var x = 0f
    private var y = 0f
    private var speedX = 15f
    private var speedY = 15f
    private var lastFrameTimeNanos = 0L

    // 3. Управление рендерингом
    private val choreographer = Choreographer.getInstance()

    @Volatile
    private var isRunning = false

    init {
        holder.addCallback(this)
        setWillNotDraw(true)
        paint.getTextBounds(logoText, 0, logoText.length, textBounds)
    }

    // ==========================================
    // ЖИЗНЕННЫЙ ЦИКЛ SURFACE
    // ==========================================

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        lastFrameTimeNanos = System.nanoTime()
        // Запускаем первый кадр. Дальше Choreographer сам будет вызывать нас.
        choreographer.postFrameCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (x == 0f && y == 0f) {
            x = width / 2f
            y = height / 2f
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        // Отписываемся от Choreographer, чтобы не было утечек
        choreographer.removeFrameCallback(this)
    }

    // ==========================================
    // ИГРОВОЙ ЦИКЛ (Вызывается Choreographer на VSync)
    // ==========================================

    override fun doFrame(frameTimeNanos: Long) {
        if (!isRunning) return

        // 1. Вычисляем deltaTime (время с прошлого кадра)
        // Используем frameTimeNanos от Choreographer, а не System.nanoTime()
        val deltaTimeNanos = frameTimeNanos - lastFrameTimeNanos
        lastFrameTimeNanos = frameTimeNanos

        // Переводим наносекунды в "количество кадров по 60 FPS"
        val stepFactor = deltaTimeNanos / 16_666_666f

        // 2. Обновляем физику
        updatePhysics(stepFactor)

        // 3. Рисуем
        drawFrame()

        // 4. Планируем следующий кадр (рекурсивно)
        choreographer.postFrameCallback(this)
    }

    private fun drawFrame() {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas()
            if (canvas != null) {
                canvas.drawColor(Color.BLACK) // Очищаем экран
                canvas.drawText(logoText, x, y - textBounds.exactCenterY(), paint)
            }
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun updatePhysics(stepFactor: Float) {
        val w = width.toFloat()
        val h = height.toFloat()

        x += speedX * stepFactor
        y += speedY * stepFactor

        val halfWidth = textBounds.width() / 2f
        val halfHeight = textBounds.height() / 2f

        if (x + halfWidth >= w) {
            x = w - halfWidth; speedX = -speedX; changeColor()
        }
        if (x - halfWidth <= 0) {
            x = halfWidth; speedX = -speedX; changeColor()
        }
        if (y + halfHeight >= h) {
            y = h - halfHeight; speedY = -speedY; changeColor()
        }
        if (y - halfHeight <= 0) {
            y = halfHeight; speedY = -speedY; changeColor()
        }
    }

    private fun changeColor() {
        paint.color = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
    }
}