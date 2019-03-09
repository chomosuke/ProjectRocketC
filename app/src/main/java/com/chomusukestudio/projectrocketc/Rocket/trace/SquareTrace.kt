package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.RegularPolygonalShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.coordinate.square
import java.lang.Math.random
import kotlin.math.atan2
import kotlin.math.sqrt

class SquareTrace(private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                  private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float,
                  val z: Float, private val layers: Layers) : Trace() {

    private var unfilledDs = 0f
    override fun generateTrace(now: Long, previousFrameTime: Long, originX: Float, originY: Float) {
        val dx = originX - lastOriginX
        val dy = originY - lastOriginY
        var ds = sqrt(square(dx) + square(dy))
        ds += unfilledDs
        val I_MAX = ds / 128f * 1000f - 0.25f - (random().toFloat() * 0.5f)
        if (I_MAX <= 0) { // if we are not adding any trace this frame
            // let the next frame know
            unfilledDs = ds // there is unfinished work
        } else {
            unfilledDs = 0f // also update it so the next frame know there is no unfinished work

            var i = 0
            while (i < I_MAX) {

                val newTraceShape = SquareTraceShape(originX, originY, random().toFloat() * 0.1f - 0.05f, random().toFloat() * 0.1f - 0.05f,
                        initialWidth, finalWidth, duration, initialRed, initialGreen, initialBlue, initialAlpha, BuildShapeAttr(z, true, layers))
                newTraceShape.rotateShape(originX, originY, atan2(dx, dy))

                val margin = /*random();*/i / I_MAX/* * (0.5f + (1 * (float) random()))*/
                newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + random()).toInt()) // + 0.5 for rounding
                newTraceShape.moveShape(-dx * margin, -dy * margin)

                i++
            }
        }
        lastOriginX = originX
        lastOriginY = originY
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