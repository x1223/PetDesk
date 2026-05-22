package com.deskpet

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.abs

class PetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface OnPositionChangedListener {
        fun onPositionChanged(deltaX: Int, deltaY: Int)
    }

    var positionListener: OnPositionChangedListener? = null

    private val imageView: ImageView
    private var currentAnimation: AnimationDrawable? = null
    private var currentState: String = "idle"

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isDragging = false
    private var longPressHideRunnable: Runnable? = null

    init {
        imageView = ImageView(context).apply {
            layoutParams = LayoutParams(240, 264)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        addView(imageView)
        setState("idle")
    }

    fun setState(state: String) {
        if (state == currentState) return
        currentState = state

        currentAnimation?.stop()

        val drawableRes = when (state) {
            "idle" -> R.drawable.anim_idle
            "thinking" -> R.drawable.anim_thinking
            "working" -> R.drawable.anim_working
            "building" -> R.drawable.anim_building
            "happy" -> R.drawable.anim_happy
            "sleeping" -> R.drawable.anim_sleeping
            "error" -> R.drawable.anim_error
            else -> R.drawable.anim_idle
        }

        imageView.setImageResource(drawableRes)
        currentAnimation = imageView.drawable as? AnimationDrawable
        currentAnimation?.start()
    }

    fun getState(): String = currentState

    fun animateTap() {
        imageView.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(100)
            .withEndAction {
                imageView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    fun animateHideTemporary() {
        this.animate()
            .alpha(0.3f)
            .setDuration(300)
            .withEndAction {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    this.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }, 2000)
            }
            .start()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.rawX
                touchStartY = event.rawY
                isDragging = false
                longPressHideRunnable?.let {
                    android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it)
                }
                longPressHideRunnable = Runnable {
                    if (!isDragging) {
                        animateHideTemporary()
                    }
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                    longPressHideRunnable!!, 800
                )
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - touchStartX
                val dy = event.rawY - touchStartY
                if (!isDragging && (abs(dx) > 10 || abs(dy) > 10)) {
                    isDragging = true
                }
                if (isDragging) {
                    positionListener?.onPositionChanged(dx.toInt(), dy.toInt())
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                longPressHideRunnable?.let {
                    android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it)
                }
                isDragging = false
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick() {
        super.performClick()
    }
}