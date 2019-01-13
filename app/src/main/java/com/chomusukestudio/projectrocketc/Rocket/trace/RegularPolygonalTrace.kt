
package com.chomusukestudio.projectrocketc.Rocket.trace

import android.util.Log
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.Shape.coordinate.square
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI
import java.lang.Math.random
import kotlin.math.sqrt

class RegularPolygonalTrace(val numberOfEdges: Int, val z: Float, private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                            private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float, private val layers: Layers) : Trace() {
    override val traceShapes = ArrayList<Shape>()

    private var lastOriginX = 0f
    private var lastOriginY = 0f
    override fun generateTrace(now: Long, previousFrameTime: Long, originX: Float, originY: Float) {
        val dx = originX - lastOriginX
        val dy = originY - lastOriginY
        generateTraces(previousFrameTime, now, originX, originY, dx, dy)
        lastOriginX = originX
        lastOriginY = originY
    }

    private var unfilledDs = 0f
    private fun generateTraces(previousFrameTime: Long, now: Long, originX: Float, originY: Float, dx: Float, dy: Float) {
        var ds = sqrt(square(dx) + square(dy))
        // lay down trace

        //        if (random() < 0.5)
        //            traceShapes.add(new TrapezoidalTraceShape(((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QX4), ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QY4),
        //                    ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QX3), ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QY3), 2,
        //                    -0.5 * speed * sin(currentRotation), -0.5 * speed * cos(currentRotation), 0.9, 0.9, 0.1, 1, 1.01));

        ds += unfilledDs // finish last frame unfinished work

        val I_MAX = ds / 128f * 1000f - 0.25f - (Math.random().toFloat() * 0.5f)
        if (I_MAX <= 0) { // if we are not adding any trace this frame
            // let the next frame know
            unfilledDs = ds // there is unfinished work
        } else {
            unfilledDs = 0f // also update it so the next frame know there is no unfinished work

            var i = 0
            while (i < I_MAX) {

                val newTraceShape = newRegularPolygonalTraceShape(originX, originY, random().toFloat() * 0.1f, random().toFloat() * 0.1f,
                        initialWidth / 2, finalWidth / 2, duration, initialRed, initialGreen, initialBlue, initialAlpha)
                newTraceShape.rotateShape(originX, originY, (2 * Math.PI * Math.random()).toFloat())

                val margin = /*random();*/i / I_MAX/* * (0.5f + (1 * (float) random()))*/
                newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + Math.random()).toInt()) // + 0.5 for rounding
                newTraceShape.moveShape(dx * margin, -dy * margin)

                i++
            }
        }
    }

    private var numberOfRemovedTrace = 0
    private val parallelForIForFadeTraces = ParallelForI(8, "fade traceShapes thread")
    override fun fadeTrace(now: Long, previousFrameTime: Long) {
//        parallelForIForFadeTraces.waitForLastRun()
        numberOfRemovedTrace = 0 // for concurrent modifying while removing trace from traceShapes
        parallelForIForFadeTraces.run({ i ->
            val trace = traceShapes[i - numberOfRemovedTrace] as RegularPolygonalTraceShape

            trace.fadeTrace(now, /*now - ((now - */previousFrameTime/*) * refreshFactor)*/)
            // fadeTrace will mark itself if needs to be removed
            if (trace.needToBeRemoved) {
                trace.removeShape()
                traceShapes.remove(trace)
                numberOfRemovedTrace++
            }
        }, traceShapes.size)
    }

    private val parallelForIForMoveTraces = ParallelForI(8, "move traceShapes thread")
    override fun moveTrace(dx: Float, dy: Float) {
//        parallelForIForMoveTraces.waitForLastRun()
        parallelForIForMoveTraces.run({ i ->
            val traceShape = traceShapes[i] as RegularPolygonalTraceShape
                traceShape.moveShape(dx, dy)
        }, traceShapes.size)
        lastOriginX += dx
        lastOriginY += dy
    }

    private fun newRegularPolygonalTraceShape(centerX: Float, centerY: Float, dx: Float, dy: Float, initialRadius: Float, finalRadius: Float,
                                              duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float): RegularPolygonalTraceShape {
        val trace = RegularPolygonalTraceShape(numberOfEdges, centerX, centerY, dx, dy, initialRadius, finalRadius,
                duration, initialRed, initialGreen, initialBlue, initialAlpha, BuildShapeAttr(z, true, layers))
        traceShapes.add(trace)
        return trace
    }
}

private class RegularPolygonalTraceShape(numberOfEdges: Int, centerX: Float, centerY: Float, dx: Float, dy: Float, initialRadius: Float, finalRadius: Float,
                                         duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double
        get() = throw IllegalAccessException("trace can't overlap anything")
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(numberOfEdges, 0f, 0f, 0f, 0f, 0f, 0f, 0f, buildShapeAttr))
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var dx: Float = 0f
    private var dy: Float = 0f
    private var initialRed: Float = 0f
    private var initialGreen: Float = 0f
    private var initialBlue: Float = 0f
    private var initialAlpha: Float = 0f
    private var duration: Float = 0f
    private var initialRadius: Float = 0f
    private var deltaRadius: Float = 0f
    private var AlphaEveryMiniSecond: Float = 0f
    var needToBeRemoved = true

    private var timeSinceReset: Float = 0f

    init {
        needToBeRemoved = false
        this.centerX = centerX
        this.centerY = centerY
        this.dx = dx
        this.dy = dy
        this.initialRed = initialRed
        this.initialGreen = initialGreen
        this.initialBlue = initialBlue
        this.initialAlpha = initialAlpha
        this.duration = duration.toFloat()
        this.deltaRadius = finalRadius - initialRadius
        this.initialRadius = initialRadius
        (componentShapes[0] as RegularPolygonalShape).resetParameter(centerX, centerY, initialRadius)
        componentShapes[0].resetShapeColor(initialRed, initialGreen, initialBlue, initialAlpha)
        AlphaEveryMiniSecond = Math.pow(1.0 / 256 / initialAlpha, 1.0 / duration).toFloat()
        timeSinceReset = 0f
    }

    override fun moveShape(dx: Float, dy: Float) {
        super.moveShape(dx, dy)
        centerX += dx
        centerY += dy
    }

    override fun rotateShape(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        super.rotateShape(centerOfRotationX, centerOfRotationY, angle)
        val result = rotatePoint(centerX, centerY, centerOfRotationX, centerOfRotationY, angle)
        centerX = result[0]
        centerY = result[1]
    }

    fun fadeTrace(now: Long, previousFrameTime: Long) {
        val color = componentShapes[0].shapeColor
        if (color[0] > 250f / 256f
                && color[1] > 250f / 256f
                && color[2] > 250f / 256f) {
            // this is basically white now
            componentShapes[0].resetAlpha(color[3] * Math.pow(AlphaEveryMiniSecond.toDouble(), (now - previousFrameTime).toDouble()).toFloat())
        } else {
            componentShapes[0].resetShapeColor(color[0] + (1 - color[0]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[1] + (1 - color[1]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[2] + (1 - color[2]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[3] * Math.pow(AlphaEveryMiniSecond.toDouble(), (now - previousFrameTime).toDouble()).toFloat())
        }
        val deltaRadius = this.deltaRadius * sqrt(timeSinceReset / duration)
        // also move trace
        (componentShapes[0] as RegularPolygonalShape).resetParameter(centerX + sqrt(timeSinceReset / duration) * dx,
                centerY + sqrt(timeSinceReset / duration) * dy, deltaRadius + initialRadius)
        if (color[3] <= 1f / 256f) {
            needToBeRemoved = true
        }

        timeSinceReset += now - previousFrameTime
        //        changeSpeedMultiply(1f - (now - previousFrameTime) / duration * 200);
    }

}