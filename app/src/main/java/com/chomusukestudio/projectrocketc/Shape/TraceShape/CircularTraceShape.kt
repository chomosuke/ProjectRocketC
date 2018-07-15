package com.chomusukestudio.projectrocketc.Shape.TraceShape

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Shape

import java.lang.Math.pow
import java.lang.Math.sqrt

class CircularTraceShape(centerX: Float, centerY: Float, private val initialRadius: Float, private val finalRadius: Float, speedX: Float, speedY: Float, val duration: Long,
                         private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float, z: Float, visibility: Boolean) : TraceShape() {
    override var componentShapes: Array<Shape> = arrayOf(CircularShape(centerX, centerY, initialRadius,
            initialRed, initialGreen, initialBlue, initialAlpha, z, visibility))
    private val AlphaEveryMiniSecond: Float
    
    private var timeSinceMade: Long = 0
    
    init {
        setSpeed(speedX, speedY)
        AlphaEveryMiniSecond = pow(1.0 / 256 / initialAlpha, 1.0 / duration).toFloat()
    }
    
    override fun fadeTraceShape(ds: Float, now: Long, previousFrameTime: Long) {
        val color = componentShapes[0].shapeColor
        componentShapes[0].resetShapeColor(color[0] + (1 - color[0]) * (now - previousFrameTime).toFloat() * 20f / duration,
                color[1] + (1 - color[1]) * (now - previousFrameTime).toFloat() * 20f / duration,
                color[2] + (1 - color[2]) * (now - previousFrameTime).toFloat() * 20f / duration,
                color[3] * pow(AlphaEveryMiniSecond.toDouble(), (now - previousFrameTime).toDouble()).toFloat())
        //        ((RegularPolygonalShape) componentShapes[0]).resetParameter(((RegularPolygonalShape) componentShapes[0]).getCenterX(),
        //                ((RegularPolygonalShape) componentShapes[0]).getCenterY(), finalRadius * sqrt(timeSinceMade / duration) + initialRadius);
        (componentShapes[0] as CircularShape).resetParameter((componentShapes[0] as CircularShape).centerX,
                (componentShapes[0] as CircularShape).centerY, finalRadius * sqrt(timeSinceMade.toDouble() / duration).toFloat() + initialRadius)
        if (color[3] <= 1.0 / 256)
            needToBeRemoved = true
        timeSinceMade += now - previousFrameTime
        changeSpeedMultiply(((now - previousFrameTime) / duration).toDouble())
    }
}
