package com.chomusukestudio.projectrocketc.Shape.TraceShape

import android.util.Log
import com.chomusukestudio.projectrocketc.Shape.RegularPolygonalShape
import com.chomusukestudio.projectrocketc.Shape.Shape

import java.lang.Math.pow
import java.lang.Math.sqrt


class RegularPolygonalTraceShape(numberOfEdges: Int, z: Float) : TraceShape() {
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(numberOfEdges, 0f, 0f, 0f, 0f, 0f, 0f, 0f, z))
    private var initialRed: Float = 0f
    private var initialGreen: Float = 0f
    private var initialBlue: Float = 0f
    private var initialAlpha: Float = 0f
    private var duration: Float = 0f
    private var initialRadius: Float = 0f
    private var deltaRadius: Float = 0f
    private var AlphaEveryMiniSecond: Float = 0f
    
    private var timeSinceReset: Float = 0f
    var showing = false
        private set
    
    init {
        numberOfTraceInUse = 0
    }
    
    fun resetRegularPolygonalTraceShape(centerX: Float, centerY: Float, initialRadius: Float, finalRadius: Float, speedX: Float, speedY: Float, duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float) {
        showing = true
        needToBeRemoved = false
        setSpeed(speedX, speedY)
        this.initialRed = initialRed
        this.initialGreen = initialGreen
        this.initialBlue = initialBlue
        this.initialAlpha = initialAlpha
        this.duration = duration.toFloat()
        this.deltaRadius = finalRadius - initialRadius
        this.initialRadius = initialRadius
        (componentShapes[0] as RegularPolygonalShape).resetParameter(centerX, centerY, initialRadius)
        componentShapes[0].resetShapeColor(initialRed, initialGreen, initialBlue, initialAlpha)
        AlphaEveryMiniSecond = pow(1.0 / 256 / initialAlpha, 1.0 / duration).toFloat()
        timeSinceReset = 0f
        numberOfTraceInUse++
        if (numberOfTraceInUse % 10 == 0) {
            Log.v("numberOfTraceInUse", "" + numberOfTraceInUse);
        }
    }
    
    override fun fadeTraceShape(ds: Float, now: Long, previousFrameTime: Long) {
        val color = componentShapes[0].shapeColor
        if (color[0] > 250f / 256f
                && color[1] > 250f / 256f
                && color[2] > 250f / 256f) {
            // this is basically white now
            componentShapes[0].resetAlpha(color[3] * pow(AlphaEveryMiniSecond.toDouble(), (now - previousFrameTime).toDouble()).toFloat())
        } else {
            componentShapes[0].resetShapeColor(color[0] + (1 - color[0]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[1] + (1 - color[1]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[2] + (1 - color[2]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[3] * pow(AlphaEveryMiniSecond.toDouble(), (now - previousFrameTime).toDouble()).toFloat())
        }
        val deltaRadius = this.deltaRadius * sqrt((timeSinceReset / duration).toDouble()).toFloat()
        (componentShapes[0] as RegularPolygonalShape).resetParameter((componentShapes[0] as RegularPolygonalShape).centerX,
                (componentShapes[0] as RegularPolygonalShape).centerY, deltaRadius + initialRadius)
        if (color[3] <= 1f / 256f) {
            needToBeRemoved = true
            makeInvisible()
        }
        timeSinceReset += now - previousFrameTime
        //        changeSpeedMultiply(1f - (now - previousFrameTime) / duration * 200);
    }
    
    fun makeInvisible() {
        showing = false
        (componentShapes[0] as RegularPolygonalShape).resetParameter(0f, 0f, 0f)
        componentShapes[0].resetShapeColor(0f, 0f, 0f, 0f)
        numberOfTraceInUse--
    }
    
    companion object {
        
        private var numberOfTraceInUse: Int = 0
    }
}

