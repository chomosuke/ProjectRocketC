
package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.Shape.coordinate.square
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI
import java.lang.Math.random
import kotlin.math.sqrt

class RegularPolygonalTrace(val numberOfEdges: Int, val z: Float, private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                            private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float, private val layers: Layers) : Trace() {

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
                newTraceShape.moveShape(-dx * margin, -dy * margin)

                i++
            }
        }
    }

    private fun newRegularPolygonalTraceShape(centerX: Float, centerY: Float, dx: Float, dy: Float, initialRadius: Float, finalRadius: Float,
                                              duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float): RegularPolygonalTraceShape {
        val trace = RegularPolygonalTraceShape(numberOfEdges, centerX, centerY, dx, dy, initialRadius, finalRadius,
                duration, initialRed, initialGreen, initialBlue, initialAlpha, BuildShapeAttr(z, true, layers))
        traceShapes.add(trace)
        return trace
    }
}

private class RegularPolygonalTraceShape(numberOfEdges: Int, private var centerX: Float, private var centerY: Float, private var dx: Float, private var dy: Float, private var initialRadius: Float, finalRadius: Float,
                                         private var duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float, buildShapeAttr: BuildShapeAttr) : TraceShape() {
    override val isOverlapMethodLevel: Double
        get() = throw IllegalAccessException("trace can't overlap anything")
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(numberOfEdges, centerX, centerY, initialRadius, initialRed, initialGreen, initialBlue, initialAlpha, buildShapeAttr))
    private var deltaRadius: Float = finalRadius - initialRadius
    private var alphaEveryMiniSecond: Float = Math.pow(1.0 / 256 / initialAlpha, 1.0 / duration).toFloat()

    private var timeSinceBorn: Float = 0f

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

    override fun fadeTrace(now: Long, previousFrameTime: Long) {
        val color = componentShapes[0].shapeColor
        if (color[0] > 250f / 256f
                && color[1] > 250f / 256f
                && color[2] > 250f / 256f) {
            // this is basically white now
            componentShapes[0].resetAlpha(color[3] * Math.pow(alphaEveryMiniSecond.toDouble(), (now - previousFrameTime).toDouble()).toFloat())
        } else {
            componentShapes[0].resetShapeColor(color[0] + (1 - color[0]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[1] + (1 - color[1]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[2] + (1 - color[2]) * (now - previousFrameTime).toFloat() * 20f / duration,
                    color[3] * Math.pow(alphaEveryMiniSecond.toDouble(), (now - previousFrameTime).toDouble()).toFloat())
        }
        val deltaRadius = this.deltaRadius * sqrt(timeSinceBorn / duration)
        // also move trace
        (componentShapes[0] as RegularPolygonalShape).resetParameter(centerX + sqrt(timeSinceBorn / duration) * dx,
                centerY + sqrt(timeSinceBorn / duration) * dy, deltaRadius + initialRadius)
        if (color[3] <= 1f / 256f) {
            needToBeRemoved = true
        }

        timeSinceBorn += now - previousFrameTime
        //        changeSpeedMultiply(1f - (now - previousFrameTime) / duration * 200);
    }
}