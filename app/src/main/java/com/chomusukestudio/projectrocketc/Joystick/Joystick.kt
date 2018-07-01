package com.chomusukestudio.projectrocketc.Joystick


import android.support.annotation.CallSuper

/**
 * Created by Shuang Li on 31/03/2018.
 */

abstract class Joystick {
    @Volatile
    protected var nowX: Float = 0.toFloat()
    @Volatile
    protected var nowY: Float = 0.toFloat()
    
    @Volatile
    protected var actionDown = false

    @CallSuper
    open fun updateTouchPosition(nowX: Float, nowY: Float) {
        this.nowX = nowX
        this.nowY = nowY
        actionDown = true
    }

    abstract fun getTurningDirection(currentRotation: Float): Float

    abstract fun drawJoystick()
    open fun actionUp() {
        actionDown = false // isn't touched
    }

    abstract fun removeAllShape()
}
