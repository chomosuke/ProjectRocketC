package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.RegularPolygonalShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.square
import com.chomusukestudio.projectrocketc.randFloat
import java.lang.Math.random
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

class SquareTrace(private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                  private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float,
                  val z: Float, private val layers: Layers) : Trace() {

    private var unfilledDs = 0f
    override fun generateTraceOverride(now: Long, previousFrameTime: Long, originX: Float, originY: Float, lastOriginX: Float, lastOriginY: Float, direction: Float) {
        val dx = originX - lastOriginX
        val dy = originY - lastOriginY
        var ds = sqrt(square(dx) + square(dy))
        ds += unfilledDs
        val I_MAX = ds / 128f * 1000f - randFloat(0.25f, 0.75f)
        if (I_MAX <= 0) { // if we are not adding any trace this frame
            // let the next frame know
            unfilledDs = ds // there is unfinished work
        } else {
            unfilledDs = 0f // also update it so the next frame know there is no unfinished work

            var i = 0
            while (i < I_MAX) {

                val newTraceShape = SquareTraceShape(originX, originY, 0f, 0f,
                        initialWidth, finalWidth, duration, initialRed, initialGreen, initialBlue, initialAlpha, BuildShapeAttr(z, true, layers))
                newTraceShape.rotateShape(originX, originY, atan2(-dx, -dy) + PI.toFloat() / 4)

                val margin = /*random();*/i / I_MAX/* * (0.5f + (1 * (float) random()))*/
                newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + random()).toInt()) // + 0.5 for rounding
                newTraceShape.moveShape(-dx * margin, -dy * margin)
                traceShapes.add(newTraceShape)

                i++
            }
        }
    }

}

class SquareTraceShape(centerX: Float, centerY: Float, private var speedX: Float, private var speedY: Float, private val initialSize: Float, finalSize: Float,
                       private val duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float, buildShapeAttr: BuildShapeAttr): TraceShape() {
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(4, centerX, centerY, initialSize / 2, initialRed, initialGreen, initialBlue, initialAlpha, buildShapeAttr))
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
        moveShape(speedX * dt, speedY * dt)
        (componentShapes[0] as RegularPolygonalShape).radius = (deltaSize * sqrt(timeSinceBorn / duration) + initialSize) / 2
        if (color[3] <= 1f / 256f) {
            needToBeRemoved = true
        }

        timeSinceBorn += dt
        //        changeSpeedMultiply(1f - (now - previousFrameTime) / duration * 200);
    }
}