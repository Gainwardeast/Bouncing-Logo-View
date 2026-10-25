package com.example.bouncingview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class BouncingLogoViewSeparateThread @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    // 1. Настройки
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

    // 3. Поток рендеринга
    private var renderThread: Thread? = null

    @Volatile
    private var isRunning = false

    init {
        // Регистрируем колбэки жизненного цикла Surface
        holder.addCallback(this)
        // Говорим, что мы сами будем рисовать в Canvas
        setWillNotDraw(true)
        paint.getTextBounds(logoText, 0, logoText.length, textBounds)
    }

    // ==========================================
    // ЖИЗНЕННЫЙ ЦИКЛ SURFACE
    // ==========================================

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Surface готова к рисованию, запускаем поток
        isRunning = true
        renderThread = Thread {
            gameLoop()
        }.apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface изменила размер (например, поворот экрана)
        if (x == 0f && y == 0f) {
            x = width / 2f
            y = height / 2f
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface уничтожается, останавливаем поток
        isRunning = false
        try {
            renderThread?.join() // Ждем, пока поток корректно завершится
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        renderThread = null
    }

    // ==========================================
    // ИГРОВОЙ ЦИКЛ (В фоновом потоке)
    // ==========================================

    private fun gameLoop() {
        var lastTime = System.nanoTime()
        val targetFrameTime = 16_666_666L // ~16 мс (60 FPS)

        while (isRunning) {
            val now = System.nanoTime()
            val deltaTime = now - lastTime
            lastTime = now

            // 1. Обновляем физику (используем deltaTime для плавности)
            // Если кадр занял 33мс, объект сдвинется на 2 шага, чтобы не тормозить
            val stepFactor = deltaTime / targetFrameTime
            updatePhysics(stepFactor)

            // 2. Рисуем
            drawFrame()

            // 3. Ограничиваем FPS, чтобы не сжигать CPU впустую
            val elapsedTime = System.nanoTime() - now
            val sleepTime = (targetFrameTime - elapsedTime) / 1_000_000 // в мс
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun drawFrame() {
        var canvas: Canvas? = null
        try {
            // Блокируем Canvas (монопольный доступ)
            canvas = holder.lockCanvas()
            if (canvas != null) {
                // Очищаем экран (так как Surface не очищается автоматически)
                canvas.drawColor(Color.BLACK)

                // Рисуем
                canvas.drawText(logoText, x, y - textBounds.exactCenterY(), paint)
            }
        } finally {
            if (canvas != null) {
                // Отправляем кадр на экран и освобождаем Canvas
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun updatePhysics(stepFactor: Long) {
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