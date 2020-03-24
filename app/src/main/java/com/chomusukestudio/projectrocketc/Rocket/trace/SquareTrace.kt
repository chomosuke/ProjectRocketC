package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.randFloat
import java.lang.Math.random
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

class SquareTrace(private val speed: Float, private val width: Float, private val initialSize: Float, private val perSecRate: Int, private val duration: Long,
                  private val initialColor: Color, private val finalColor: Color, val z: Float, private val layers: Layers) : Trace() {

    private var preUnfinishedHalfIs = 0f
    override fun generateTraceOverride(now: Long, previousFrameTime: Long, origin: Vector, lastOrigin: Vector, rocketState: RocketState) {
        val iMax = perSecRate * (now - previousFrameTime) / 1000f + preUnfinishedHalfIs
        var i = 0
        while (i < iMax) {

            val traceVelocity = Vector(-speed, 0f).rotateVector(rocketState.currentRotation)
            val traceCenter = origin + Vector(0f, (width-initialSize) * randFloat(-0.5f, 0.5f)).rotateVector(rocketState.currentRotation)
            val newTraceShape = SquareTraceShape(traceCenter, traceVelocity, initialSize, 0f,
                    duration, initialColor, finalColor, BuildShapeAttr(z, true, layers))
            newTraceShape.rotate(traceCenter, rocketState.currentRotation + PI.toFloat()/4)

            val margin = /*random();*/i / iMax /*+ randFloat(-0.5f, 0.5f)*/ /** (0.5f + (1 * random().toFloat()))*/
            newTraceShape.fadeTrace(now,  now - (margin * (now - previousFrameTime) + random()).toInt()) // + 0.5 for rounding
            newTraceShape.move((lastOrigin - origin) * margin
                    + Vector(-1f, 0f).rotateVector(rocketState.currentRotation) * initialSize/2f)

            traceShapes.add(newTraceShape)

            i++
        }
        preUnfinishedHalfIs = iMax - i
    }

    override fun moveTrace(vector: Vector) {}
}

class SquareTraceShape(center: Vector, private var speed: Vector, private val initialSize: Float, finalSize: Float, private val duration: Long,
                       initialColor: Color, finalColor: Color, buildShapeAttr: BuildShapeAttr): TraceShape() {
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(4, center, initialSize / 2, initialColor, buildShapeAttr))
    private var deltaSize: Float = finalSize - initialSize
    private var colorEveryMiniSecond: Color = Color(
            (finalColor.red - initialColor.red)/duration,
            (finalColor.green - initialColor.green)/duration,
            (finalColor.blue - initialColor.blue)/duration,
            (finalColor.alpha - initialColor.alpha)/duration
    )


    private var timeSinceBorn: Float = 0f

    override fun fadeTrace(now: Long, previousFrameTime: Long) {

        val dt = (now - previousFrameTime).toFloat()
        componentShapes[0].changeShapeColor(
                colorEveryMiniSecond.red * dt,
                colorEveryMiniSecond.green * dt,
                colorEveryMiniSecond.blue * dt,
                colorEveryMiniSecond.alpha * dt
        )

        move(speed * dt)
        (componentShapes[0] as RegularPolygonalShape).radius = (deltaSize * timeSinceBorn / duration + initialSize) / 2

        timeSinceBorn += dt

        if (timeSinceBorn >= duration) {
            needToBeRemoved = true
        }
        //        changeSpeedMultiply(1f - (nowXY - previousFrameTime) / duration * 200);
    }
}