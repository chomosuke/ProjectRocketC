
package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.square
import com.chomusukestudio.projectrocketc.randFloat
import java.lang.Math.random
import kotlin.math.sqrt

class RegularPolygonalTrace(val numberOfEdges: Int, val z: Float, private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                            private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float, private val layers: Layers) : Trace() {

    override fun generateTraceOverride(now: Long, previousFrameTime: Long, originX: Float, originY: Float, lastOriginX: Float, lastOriginY: Float, rocketState: RocketState) {
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
            
                    val newTraceShape = newRegularPolygonalTraceShape(originX, originY, randFloat(-0.05f, 0.05f), randFloat(-0.05f, 0.05f),
                            initialWidth / 2, finalWidth / 2, duration, initialRed, initialGreen, initialBlue, initialAlpha)
                    newTraceShape.rotateShape(originX, originY, (2 * Math.PI * random()).toFloat())
            
                    val margin = /*random();*/i / I_MAX/* * (0.5f + (1 * (float) random()))*/
                    newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + random()).toInt()) // + 0.5 for rounding
                    newTraceShape.moveShape(-dx * margin, -dy * margin)
            
                    i++
                }
            }
    }

    private var unfilledDs = 0f

    private fun newRegularPolygonalTraceShape(centerX: Float, centerY: Float, dx: Float, dy: Float, initialRadius: Float, finalRadius: Float,
                                              duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float): RegularPolygonalTraceShape {
        val trace = RegularPolygonalTraceShape(numberOfEdges, centerX, centerY, initialRadius, finalRadius,
                duration, initialRed, initialGreen, initialBlue, initialAlpha, BuildShapeAttr(z, true, layers))
        traceShapes.add(trace)
        return trace
    }
}

open class RegularPolygonalTraceShape(numberOfEdges: Int, private var centerX: Float, private var centerY: Float, private val initialRadius: Float, finalRadius: Float,
                                         private val duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float, buildShapeAttr: BuildShapeAttr) : TraceShape() {
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
        // and change radius
        (componentShapes[0] as RegularPolygonalShape).radius = deltaRadius * sqrt(timeSinceBorn / duration) + initialRadius
        if (color[3] <= 1f / 256f) {
            needToBeRemoved = true
        }

        timeSinceBorn += now - previousFrameTime
        //        changeSpeedMultiply(1f - (now - previousFrameTime) / duration * 200);
    }
}