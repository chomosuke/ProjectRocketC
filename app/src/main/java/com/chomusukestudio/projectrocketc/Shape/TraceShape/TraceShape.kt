package com.chomusukestudio.projectrocketc.Shape.TraceShape

import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.coordinate.square

import java.lang.Math.sqrt

abstract class TraceShape : Shape() {
    protected var speedX: Float = 0.toFloat()
    protected var speedY: Float = 0.toFloat()
    protected var needToBeRemoved = false
    
    override val isOverlapMethodLevel: Double
        get() = throw IllegalAccessException("Trace can't overlap anything")
    
    protected fun changeSpeedMultiply(k: Double) {
        speedX *= k.toFloat()
        speedY *= k.toFloat()
    }
    
    fun setSpeed(speedX: Float, speedY: Float) {
        this.speedX = speedX
        this.speedY = speedY
    }
    
    fun needToBeRemoved(): Boolean {
        return needToBeRemoved
    }
    
    abstract fun fadeTraceShape(ds: Float, now: Long, previousFrameTime: Long)
    
    fun moveTraceShape(now: Long, previousTime: Long) {
        val dx = (now - previousTime) * speedX
        val dy = (now - previousTime) * speedY
        moveShape(dx, dy)
        fadeTraceShape(sqrt(square(dx.toDouble()) + square(dy.toDouble())).toFloat(), now, previousTime)
    }
}
