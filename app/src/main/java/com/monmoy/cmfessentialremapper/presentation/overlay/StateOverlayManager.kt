package com.monmoy.cmfessentialremapper.presentation.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.monmoy.cmfessentialremapper.R

/**
 * Visual icons supported by the HUD overlay.
 */
enum class OverlayIcon {
    RING,
    VIBRATE,
    SILENT,
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    SCREENSHOT,
    NOTIFICATIONS,
    QUICK_SETTINGS,
    LOCK
}

/**
 * State presentation payload for the overlay.
 */
data class OverlayState(
    val title: String,
    val subtitle: String? = null,
    val icon: OverlayIcon,
    val accentColor: Int = 0xFFD71921.toInt() // Default Nothing Red
)

/**
 * Lightweight, hardware-accelerated Nothing OS style HUD overlay.
 * Uses TYPE_ACCESSIBILITY_OVERLAY from the AccessibilityService without requiring SYSTEM_ALERT_WINDOW.
 */
class StateOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val density = context.resources.displayMetrics.density

    private var overlayView: LinearLayout? = null
    private var iconView: ImageView? = null
    private var titleView: TextView? = null
    private var subtitleView: TextView? = null
    private var statusDotView: View? = null
    private var isAttached = false

    private val dismissRunnable = Runnable { dismiss() }

    fun show(state: OverlayState) {
        mainHandler.post {
            ensureOverlayCreated()
            bindState(state)

            mainHandler.removeCallbacks(dismissRunnable)

            val view = overlayView ?: return@post

            if (!isAttached) {
                try {
                    val params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        y = (54 * density).toInt() // Position safely below camera punch hole
                    }

                    view.alpha = 0f
                    view.translationY = -dpToPx(28f)
                    view.scaleX = 0.92f
                    view.scaleY = 0.92f

                    windowManager?.addView(view, params)
                    isAttached = true

                    view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220)
                        .setInterpolator(OvershootInterpolator(1.15f))
                        .start()

                } catch (_: Throwable) {
                    isAttached = false
                }
            } else {
                // Quick pulse animation when changing states while already showing
                view.animate()
                    .scaleX(1.06f)
                    .scaleY(1.06f)
                    .setDuration(80)
                    .withEndAction {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }

            mainHandler.postDelayed(dismissRunnable, 1800)
        }
    }

    fun dismiss() {
        mainHandler.post {
            val view = overlayView ?: return@post
            if (!isAttached) return@post

            view.animate()
                .alpha(0f)
                .translationY(-dpToPx(24f))
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(200)
                .setInterpolator(AccelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.animate().setListener(null)
                        removeOverlaySafely()
                    }
                })
                .start()
        }
    }

    fun destroy() {
        mainHandler.removeCallbacks(dismissRunnable)
        mainHandler.post {
            removeOverlaySafely()
            overlayView = null
        }
    }

    private fun removeOverlaySafely() {
        if (isAttached && overlayView != null) {
            try {
                windowManager?.removeViewImmediate(overlayView)
            } catch (_: Throwable) {}
            isAttached = false
        }
    }

    private fun ensureOverlayCreated() {
        if (overlayView != null) return

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dpToPx(16f).toInt(),
                dpToPx(10f).toInt(),
                dpToPx(16f).toInt(),
                dpToPx(10f).toInt()
            )

            // Nothing OS Matte Dark Pill with subtle border
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(24f)
                setColor(Color.parseColor("#F2121212")) // Deep matte black with high opacity
                setStroke(dpToPx(1f).toInt(), Color.parseColor("#333333")) // Refined Nothing border
            }
        }

        // Left Icon
        val iv = ImageView(context).apply {
            val size = dpToPx(20f).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = dpToPx(12f).toInt()
            }
        }
        container.addView(iv)
        iconView = iv

        // Center Text Column
        val textCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val tvTitle = TextView(context).apply {
            textSize = 13.5f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#EEEEEE"))
            letterSpacing = 0.04f
        }
        textCol.addView(tvTitle)
        titleView = tvTitle

        val tvSubtitle = TextView(context).apply {
            textSize = 10.5f
            setTextColor(Color.parseColor("#888888"))
            visibility = View.GONE
        }
        textCol.addView(tvSubtitle)
        subtitleView = tvSubtitle

        container.addView(textCol)

        // Right Status Dot (Nothing Dot)
        val dot = View(context).apply {
            val dotSize = dpToPx(7f).toInt()
            layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                marginStart = dpToPx(14f).toInt()
            }
        }
        container.addView(dot)
        statusDotView = dot

        overlayView = container
    }

    private fun bindState(state: OverlayState) {
        val drawableRes = when (state.icon) {
            OverlayIcon.RING -> R.drawable.ic_overlay_ring
            OverlayIcon.VIBRATE -> R.drawable.ic_overlay_vibrate
            OverlayIcon.SILENT -> R.drawable.ic_overlay_silent
            OverlayIcon.FLASHLIGHT_ON -> R.drawable.ic_overlay_flashlight
            OverlayIcon.FLASHLIGHT_OFF -> R.drawable.ic_overlay_flashlight
            OverlayIcon.SCREENSHOT -> R.drawable.ic_overlay_screenshot
            OverlayIcon.NOTIFICATIONS -> R.drawable.ic_overlay_notifications
            OverlayIcon.QUICK_SETTINGS -> R.drawable.ic_overlay_quick_settings
            OverlayIcon.LOCK -> R.drawable.ic_overlay_lock
        }

        val drawable = ContextCompat.getDrawable(context, drawableRes)?.mutate()
        drawable?.setTint(state.accentColor)
        iconView?.setImageDrawable(drawable)

        titleView?.text = state.title

        if (!state.subtitle.isNullOrBlank()) {
            subtitleView?.text = state.subtitle
            subtitleView?.visibility = View.VISIBLE
        } else {
            subtitleView?.visibility = View.GONE
        }

        statusDotView?.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(state.accentColor)
        }
    }

    private fun dpToPx(dp: Float): Float = dp * density
}
