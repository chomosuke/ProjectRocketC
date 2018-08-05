package com.chomusukestudio.projectrocketc

import android.app.Activity
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.chomusukestudio.projectrocketc.GLRenderer.generateLeftRightBottomTop
import java.util.logging.Level
import java.util.logging.Logger


@Volatile var widthInPixel: Float = 0f
@Volatile var heightInPixel: Float = 0f

fun transformToMatrixX(x: Float): Float {
    var resultX = x
    // transformation
    resultX -= widthInPixel / 2
    resultX /= widthInPixel / 2
    val leftRightBottomTop = generateLeftRightBottomTop(widthInPixel/ heightInPixel)
    resultX *= leftRightBottomTop[0]
    // assuming left == - right is true
    // need improvement to obey OOP principles by getting rid of the assumption above
    if (!(leftRightBottomTop[0] == -leftRightBottomTop[1]))
        throw RuntimeException("need improvement to obey OOP principles by not assuming left == - right is true.\n" +
                "or you can ignore the above and just twist the code to get it work and not give a fuck about OOP principles.\n" +
                "which is what i did when i wrote those code.  :)")
    return resultX
}

fun transformToMatrixY(y: Float): Float {
    var resultY = y
    // transformation
    resultY -= heightInPixel / 2
    resultY /= heightInPixel / 2
    val leftRightBottomTop = generateLeftRightBottomTop(widthInPixel/ heightInPixel)
    resultY *= leftRightBottomTop[2]
    // assuming top == - bottom is true
    // need improvement to obey OOP principles by getting rid of the assumption above
    if (!(leftRightBottomTop[3] == -leftRightBottomTop[2]))
        throw RuntimeException("need improvement to obey OOP principles by not assuming top == - bottom is true.\n" +
                "or you can ignore the above and just twist the code to get it work and not give a fuck about OOP principles.\n" +
                "which is what i did when i wrote those code.  :)")
    return resultY
}

fun giveVisualText(string: String, visualTextView: TouchableView<TextView>) {
    visualTextView.touchView { textView ->
        textView.visibility = View.VISIBLE
        textView.text = string
        val visualEffectAnimation = AnimationUtils.loadAnimation(visualTextView.activity, R.anim.visual_text_effect)
        // Now Set your animation
        textView.startAnimation(visualEffectAnimation)
        textView.visibility = View.INVISIBLE
    }
}

class TouchableView<out V : View>(val view: V, val activity: Activity) {
    fun touchView(touch: (V) -> Unit) {
        activity.runOnUiThread { touch(view) }
    }
}

@Volatile var pausedTime: Long = 0L
fun upTimeMillis(): Long {
    return SystemClock.uptimeMillis() - pausedTime
}

fun <R>runWithExceptionChecked(runnable: () -> R): R {
    try {
        return runnable()
    } catch (e: Exception) {
        val logger = Logger.getAnonymousLogger()
        logger.log(Level.SEVERE, "an exception was thrown in nextFrameThread", e)
        Log.e("exception", "in processingThread" + e)
        throw e
    }
}