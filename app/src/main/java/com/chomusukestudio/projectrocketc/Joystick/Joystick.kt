package com.chomusukestudio.projectrocketc.Joystick


import android.view.MotionEvent
import androidx.annotation.CallSuper
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import kotlin.math.sign

/**
 * Created by Shuang Li on 31/03/2018.
 */

abstract class Joystick(private val drawData: DrawData) {
    @Volatile
    protected var nowXY = Vector(0f, 0f)

    protected val actionDown
        get() = pointers.isNotEmpty() // action is down when there is pointer

    protected var pointers = ArrayList<Int>()

    open fun onTouchEvent(e: MotionEvent) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE->
                if (!pointers.contains(e.getPointerId(e.actionIndex))) // offset the new pointer
                    pointers.add(e.getPointerId(e.actionIndex))
//                else // this actually does happen for some reason
//                    throw RuntimeException("New pointer is already in pointers.") // interesting

            MotionEvent.ACTION_UP -> {
                // last pointer is up
                if (pointers.isNotEmpty()) // pointers.isEmpty() if pointer is from before the game start
                    pointers.remove(e.getPointerId(e.actionIndex))

                if (pointers.isNotEmpty())
                    throw RuntimeException("There still pointers after action up.")

                return
            }
            MotionEvent.ACTION_POINTER_UP ->
                if (pointers.isNotEmpty()) // pointers.isEmpty() if pointer is from before the game start
                    pointers.remove(e.getPointerId(e.actionIndex))
        }

        updateTouchPosition(transformToMatrix(Vector(
                e.getX(e.findPointerIndex(pointers.last())),
                e.getY(e.findPointerIndex(pointers.last())))))
    }

    @CallSuper
    protected open fun updateTouchPosition(nowXY: Vector) {
        this.nowXY = nowXY
    }

    abstract fun getRocketControl(currentRotation: Float): RocketControl

    open fun drawJoystick() {}

    open fun removeAllShape() {}

    fun transformToMatrix(vector: Vector) : Vector {
        var result = vector * drawData.pixelSize // scale
        result *= Vector((drawData.rightEnd - drawData.leftEnd).sign, (drawData.bottomEnd - drawData.topEnd).sign) // orientate
        result += Vector(drawData.leftEnd, drawData.topEnd) // recenter
        return result
    }
}


data class RocketControl(val rotationNeeded: Float, val throttleOn: Boolean = true)