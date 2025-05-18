package com.example.flappybird

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private val thread: GameThread
    private val bird = RectF(100f, 100f, 150f, 150f)
    private var velocity = 0f
    private val gravity = 0.6f
    private val jump = -10f

    private val pipes = mutableListOf<PipePair>()
    private val paint = Paint()
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 64f
    }
    private var score = 0
    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private var highScore = prefs.getInt("HIGH_SCORE", 0)

    init {
        holder.addCallback(this)
        thread = GameThread(holder, this)
        paint.color = Color.GREEN
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread.running = true
        thread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        thread.running = false
        while (retry) {
            try {
                thread.join()
                retry = false
            } catch (e: InterruptedException) {
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            velocity = jump
        }
        return true
    }

    fun update() {
        velocity += gravity
        bird.offset(0f, velocity)
        if (bird.bottom > height) {
            resetGame()
        }

        if (pipes.isEmpty() || pipes.last().top.right < width - 300) {
            val gap = 300
            val pipeWidth = 200
            val topHeight = (100..(height - gap - 100)).random()
            val top = RectF(width.toFloat(), 0f, (width + pipeWidth).toFloat(), topHeight.toFloat())
            val bottom = RectF(width.toFloat(), (topHeight + gap).toFloat(), (width + pipeWidth).toFloat(), height.toFloat())
            pipes.add(PipePair(top, bottom))
        }

        val iterator = pipes.iterator()
        while (iterator.hasNext()) {
            val pipe = iterator.next()
            pipe.top.offset(-5f, 0f)
            pipe.bottom.offset(-5f, 0f)
            if (pipe.top.right < 0) iterator.remove()
            if (!pipe.scored && pipe.top.right < bird.left) {
                score++
                pipe.scored = true
            }
            if (RectF.intersects(pipe.top, bird) || RectF.intersects(pipe.bottom, bird)) {
                resetGame()
                break
            }
        }
    }

    private fun resetGame() {
        if (score > highScore) {
            highScore = score
            prefs.edit().putInt("HIGH_SCORE", highScore).apply()
        }
        score = 0
        bird.offsetTo(100f, 100f)
        velocity = 0f
        pipes.clear()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawColor(Color.CYAN)
        paint.color = Color.YELLOW
        canvas.drawOval(bird, paint)
        paint.color = Color.GREEN
        for (pipe in pipes) {
            canvas.drawRect(pipe.top, paint)
            canvas.drawRect(pipe.bottom, paint)
        }
        canvas.drawText("Score: $score", 50f, 80f, textPaint)
        canvas.drawText("Best: $highScore", 50f, 160f, textPaint)
    }

    private data class PipePair(val top: RectF, val bottom: RectF, var scored: Boolean = false)

    private class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {
        var running = false

        override fun run() {
            while (running) {
                val canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        gameView.update()
                        gameView.draw(canvas)
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
                sleep(16)
            }
        }
    }
}
