package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.randFloat
import java.lang.Math.random
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

class SquareTrace(private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                  private val initialColor: Color, val z: Float, private val layers: Layers) : Trace() {

    private var unfilledDs = 0f
    override fun generateTraceOverride(now: Long, previousFrameTime: Long, origin: Vector, lastOrigin: Vector, rocketState: RocketState) {
        val dOrigin = origin - lastOrigin
        var ds = dOrigin.abs
        ds += unfilledDs
        val I_MAX = ds / 128f * 1000f - randFloat(0.25f, 0.75f)
        if (I_MAX <= 0) { // if we are not adding any trace this frame
            // let the next frame know
            unfilledDs = ds // there is unfinished work
        } else {
            unfilledDs = 0f // also update it so the next frame know there is no unfinished work

            var i = 0
            while (i < I_MAX) {

                val newTraceShape = SquareTraceShape(origin, Vector(0f, 0f), initialWidth, finalWidth, duration, initialColor, BuildShapeAttr(z, true, layers))
                newTraceShape.rotate(origin, -dOrigin.direction + PI.toFloat() / 4)

                val margin = /*random();*/i / I_MAX/* * (0.5f + (1 * (float) random()))*/
                newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + random()).toInt()) // + 0.5 for rounding
                newTraceShape.move(-dOrigin * margin)
                traceShapes.add(newTraceShape)

                i++
            }
        }
    }

}

class SquareTraceShape(center: Vector, private var speed: Vector, private val initialSize: Float, finalSize: Float,
                       private val duration: Long, initialColor: Color, buildShapeAttr: BuildShapeAttr): TraceShape() {
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(4, center, initialSize / 2, initialColor, buildShapeAttr))
    private var deltaSize: Float = finalSize - initialSize
    private var alphaEveryMiniSecond: Float = (1f / 256 / initialColor.alpha).pow(1f / duration)

    private var timeSinceBorn: Float = 0f

    override fun fadeTrace(now: Long, previousFrameTime: Long) {

        val color = componentShapes[0].shapeColor
        val dt = (now - previousFrameTime).toFloat()
        if (color.red > 250f / 256f
                && color.green > 250f / 256f
                && color.blue > 250f / 256f) {
            // this is basically white nowXY
            componentShapes[0].resetAlpha(color.alpha * alphaEveryMiniSecond.pow(dt))
        } else {
            componentShapes[0].resetShapeColor(Color(color.red + (1 - color.red) * dt * 20f / duration,
                    color.green + (1 - color.green) * dt * 20f / duration,
                    color.blue + (1 - color.blue) * dt * 20f / duration,
                    color.alpha * alphaEveryMiniSecond.pow(dt)))
        }
        move(speed * dt)
        (componentShapes[0] as RegularPolygonalShape).radius = (deltaSize * sqrt(timeSinceBorn / duration) + initialSize) / 2
        if (color.alpha <= 1f / 256f) {
            needToBeRemoved = true
        }

        timeSinceBorn += dt
        //        changeSpeedMultiply(1f - (nowXY - previousFrameTime) / duration * 200);
    }
}