package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.RegularPolygonalShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import kotlin.math.sqrt

class SquareTrace(private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                  private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float, val z: Float, private val layers: Layers) : Trace() {
    override fun generateTrace(now: Long, previousFrameTime: Long, originX: Float, originY: Float) {
        
    }
}

class SquareTraceShape(centerX: Float, centerY: Float, private var speedX: Float, private var speedY: Float, private val initialSize: Float, finalSize: Float,
                       private val duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float, buildShapeAttr: BuildShapeAttr): TraceShape() {
    override var componentShapes: Array<Shape> = arrayOf(QuadrilateralShape(
            centerX + initialSize / 2, centerY + initialSize / 2,
            centerX + initialSize / 2, centerY - initialSize / 2,
            centerX - initialSize / 2, centerY - initialSize / 2,
            centerX - initialSize / 2, centerY + initialSize / 2,
            initialRed, initialGreen, initialBlue, initialAlpha, buildShapeAttr))
    private var deltaSize: Float = finalSize - initialSize
    private var alphaEveryMiniSecond: Float = Math.pow(1.0 / 256 / initialAlpha, 1.0 / duration).toFloat()

    private var timeSinceBorn: Float = 0f

    override fun fadeTrace(now: Long, previousFrameTime: Long) {

        val color = componentShapes[0].shapeColor
        val dt = (now - previousFrameTime).toFloat()
        if (color[0] > 250f / 256f
                && color[1] > 250f / 256f
                && color[2] > 250f / 256f) {
            // this is basically white now
            componentShapes[0].resetAlpha(color[3] * Math.pow(alphaEveryMiniSecond.toDouble(), dt.toDouble()).toFloat())
        } else {
            componentShapes[0].resetShapeColor(color[0] + (1 - color[0]) * dt * 20f / duration,
                    color[1] + (1 - color[1]) * dt * 20f / duration,
                    color[2] + (1 - color[2]) * dt * 20f / duration,
                    color[3] * Math.pow(alphaEveryMiniSecond.toDouble(), dt.toDouble()).toFloat())
        }
        // move trace and change radius
        moveShape(speedX * dt, speedY * dt)
        (componentShapes[0] as RegularPolygonalShape).radius = deltaSize * sqrt(timeSinceBorn / duration) + initialSize
        if (color[3] <= 1f / 256f) {
            needToBeRemoved = true
        }

        timeSinceBorn += dt
        //        changeSpeedMultiply(1f - (now - previousFrameTime) / duration * 200);
    }
}