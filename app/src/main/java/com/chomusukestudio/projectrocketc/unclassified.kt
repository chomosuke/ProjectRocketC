package com.chomusukestudio.projectrocketc

import android.app.Activity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector

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

fun putCommasInInt(string: String): String {
    var string = string
    var numCounter = 0
    for (i in string.length - 1 downTo 0) {
        if (string[i].isDigit()) {
            numCounter++
            if (i - 1 >= 0) { // not last one
                if (numCounter == 3 && string[i - 1].isDigit()) {
                    string = string.substring(0, i) + "," + string.substring(i, string.length)
                    numCounter = 0
                }
            }
        } else {
            numCounter = 0
        }
    }
    return string
}

interface IReusable { // stuff that can be stored in a place (e.g. an array) and be reused multiple times
    var isInUse: Boolean
}

fun decelerateVelocity(velocity: Vector, deceleration: Float, frameTime: Long): Vector {
    val dSpeed = deceleration * frameTime
    val resultSpeed = velocity.abs - dSpeed
    return velocity * (resultSpeed / velocity.abs)
}

fun max(a: Float, b: Float) = if(a > b) a else b
fun min(a: Float, b: Float) = if(a < b) a else b