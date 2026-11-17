package kr.napjaki.wallpaper

import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import kotlin.random.Random

class NapjakiWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = PetEngine()

    inner class PetEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val image = BitmapFactory.decodeResource(resources, R.drawable.napjaki)
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val bubbleFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        private val bubbleEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(80, 145, 180); style = Paint.Style.STROKE; strokeWidth = 3f
        }
        private val words = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; textSize = 34f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        private val sayings = listOf("넌 누구냐!", "이건 뭐지?", "지그시... 빤히...", "캬하하하하하하!")
        private var screenW = 1
        private var screenH = 1
        private var px = 120f
        private var py = 260f
        private var dx = 2.0f
        private var dy = 1.5f
        private var dragging = false
        private var touchX = 0f
        private var touchY = 0f
        private var shown = ""
        private var shownUntil = 0L
        private var nextLineAt = System.currentTimeMillis() + Random.nextLong(15_000, 45_001)
        private var isVisible = false

        private val loop = object : Runnable {
            override fun run() {
                render()
                if (isVisible) handler.postDelayed(this, 33L)
            }
        }

        init { setTouchEventsEnabled(true) }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            handler.removeCallbacks(loop)
            if (visible) handler.post(loop)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            screenW = width; screenH = height
            render()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            isVisible = false
            handler.removeCallbacks(loop)
            super.onSurfaceDestroyed(holder)
        }

        override fun onTouchEvent(event: MotionEvent) {
            val (w, h) = size()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (RectF(px-w/2, py-h/2, px+w/2, py+h/2).contains(event.x, event.y)) {
                        dragging = true; touchX = event.x; touchY = event.y
                        sayRandom()
                    }
                }
                MotionEvent.ACTION_MOVE -> if (dragging) {
                    px += event.x - touchX; py += event.y - touchY
                    touchX = event.x; touchY = event.y
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dragging = false
            }
            super.onTouchEvent(event)
        }

        private fun size(): Pair<Float, Float> {
            val percent = getSharedPreferences("napjaki", MODE_PRIVATE).getInt("size", 65) / 100f
            val w = (screenW * 0.27f * percent / 0.65f).coerceAtLeast(60f)
            return w to (w * image.height / image.width)
        }

        private fun sayRandom() {
            shown = sayings.random()
            shownUntil = System.currentTimeMillis() + 2_000L
        }

        private fun render() {
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.rgb(238, 246, 250))
                    val (w, h) = size()
                    if (!dragging) {
                        px += dx; py += dy
                        if (px < w/2 || px > screenW-w/2) dx = -dx
                        if (py < h/2 || py > screenH-h/2) dy = -dy
                    }
                    px = px.coerceIn(w/2, (screenW-w/2).coerceAtLeast(w/2))
                    py = py.coerceIn(h/2, (screenH-h/2).coerceAtLeast(h/2))
                    canvas.drawBitmap(image, null, RectF(px-w/2, py-h/2, px+w/2, py+h/2), bitmapPaint)

                    val now = System.currentTimeMillis()
                    if (shown.isNotEmpty() && now >= shownUntil) shown = ""
                    if (shown.isEmpty() && now >= nextLineAt) {
                        sayRandom()
                        nextLineAt = now + Random.nextLong(15_000, 45_001)
                    }
                    if (shown.isNotEmpty()) drawSpeech(canvas, shown, px, py-h/2-12f)
                }
            } catch (_: Exception) {
            } finally {
                if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        private fun drawSpeech(canvas: Canvas, text: String, centerX: Float, bottom: Float) {
            val pad = 18f
            val width = words.measureText(text) + pad*2
            val left = (centerX-width/2).coerceIn(8f, (screenW-width-8f).coerceAtLeast(8f))
            val top = (bottom-58f).coerceAtLeast(8f)
            val rect = RectF(left, top, left+width, top+52f)
            canvas.drawRoundRect(rect, 20f, 20f, bubbleFill)
            canvas.drawRoundRect(rect, 20f, 20f, bubbleEdge)
            canvas.drawText(text, rect.left+pad, rect.top+36f, words)
        }
    }
}
