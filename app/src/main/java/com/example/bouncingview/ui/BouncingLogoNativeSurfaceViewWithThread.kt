package com.example.bouncingview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Looper
import android.util.AttributeSet
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class BouncingLogoNativeSurfaceViewWithThread @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private var logoText = "HARDCORE"
    private val textBounds = Rect()

    // Состояние (видимое из обоих потоков)
    private var x = 0f
    private var y = 0f
    private var speedX = 15f
    private var speedY = 15f

    // Поток рендеринга и его управление
    private var renderThread: Thread? = null

    @Volatile
    private var isRunning = false

    init {
        holder.addCallback(this)
        setWillNotDraw(true)
        paint.getTextBounds(logoText, 0, logoText.length, textBounds)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        renderThread = Thread {
            // 1. Обязательно подготавливаем Looper для фонового потока!
            // Без этого Choreographer не сможет работать.
            Looper.prepare()

            // 2. Получаем Choreographer, привязанный к ЭТОМУ потоку
            val choreographer = Choreographer.getInstance()

            var lastFrameTimeNanos = System.nanoTime()

            // 3. Создаем колбэк, который будет вызываться на каждый VSync
            val frameCallback = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    if (!isRunning) {
                        // Останавливаем цикл
                        Looper.myLooper()?.quit()
                        return
                    }

                    // Считаем deltaTime (как в игровых движках)
                    val deltaTimeNanos = frameTimeNanos - lastFrameTimeNanos
                    lastFrameTimeNanos = frameTimeNanos
                    val stepFactor = deltaTimeNanos / 16_666_666f

                    // Обновляем физику и рисуем
                    updatePhysics(stepFactor)
                    drawFrame()

                    // Запрашиваем следующий кадр
                    choreographer.postFrameCallback(this)
                }
            }

            // Запускаем первый кадр
            choreographer.postFrameCallback(frameCallback)

            // 4. Запускаем бесконечный цикл обработки сообщений для этого потока
            Looper.loop()
        }.apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (x == 0f && y == 0f) {
            x = width / 2f
            y = height / 2f
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        // Ждем, пока фоновый поток корректно завершит работу (выйдет из Looper.loop)
        renderThread?.join()
        renderThread = null
    }

    private fun drawFrame() {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas()
            if (canvas != null) {
                canvas.drawColor(Color.BLACK)
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