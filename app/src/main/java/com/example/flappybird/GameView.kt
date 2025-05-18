package com.example.flappybird

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val thread: GameThread
    private val bird = RectF(100f, 100f, 150f, 150f)
    private var velocity = 0f
    private val gravity = 0.6f
    private val jump = -10f

    private val pipes = mutableListOf<RectF>()
    private val paint = Paint()

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
            bird.offsetTo(100f, 100f)
            velocity = 0f
        }

        if (pipes.isEmpty() || pipes.last().right < width - 300) {
            val gap = 300f
            val pipeWidth = 200f
            val topHeight = (100..(height - gap.toInt() - 100)).random().toFloat()
            pipes.add(RectF(width.toFloat(), 0f, width + pipeWidth, topHeight))
            pipes.add(RectF(width.toFloat(), topHeight + gap, width + pipeWidth, height.toFloat()))
        }

        val iterator = pipes.iterator()
        while (iterator.hasNext()) {
            val pipe = iterator.next()
            pipe.offset(-5f, 0f)
            if (pipe.right < 0) iterator.remove()
            if (RectF.intersects(pipe, bird)) {
                bird.offsetTo(100f, 100f)
                velocity = 0f
                pipes.clear()
                break
            }
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawColor(Color.CYAN)
        paint.color = Color.YELLOW
        canvas.drawOval(bird, paint)
        paint.color = Color.GREEN
        for (pipe in pipes) {
            canvas.drawRect(pipe, paint)
        }
    }

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
