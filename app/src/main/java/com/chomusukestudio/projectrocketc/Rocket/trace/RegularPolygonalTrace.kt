
package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.randFloat
import java.lang.Math.random
import kotlin.math.pow
import kotlin.math.sqrt

class RegularPolygonalTrace(val numberOfEdges: Int, val z: Float, private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                            private val initialColor: Color, private val layers: Layers) : Trace() {

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
            
                val newTraceShape = newRegularPolygonalTraceShape(origin, Vector(randFloat(-0.05f, 0.05f), randFloat(-0.05f, 0.05f)),
                        initialWidth / 2, finalWidth / 2, duration, initialColor)
                newTraceShape.rotate(origin, (2 * Math.PI * random()).toFloat())
            
                val margin = /*random();*/i / I_MAX/* * (0.5f + (1 * (float) random()))*/
                newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + random()).toInt()) // + 0.5 for rounding
                newTraceShape.move(-dOrigin * margin)
            
                i++
            }
        }
    }

    private var unfilledDs = 0f

    private fun newRegularPolygonalTraceShape(center: Vector, delta: Vector, initialRadius: Float, finalRadius: Float,
                                              duration: Long, initialColor: Color): RegularPolygonalTraceShape {
        val trace = RegularPolygonalTraceShape(numberOfEdges, center, initialRadius, finalRadius,
                duration, initialColor, BuildShapeAttr(z, true, layers))
        traceShapes.add(trace)
        return trace
    }
}

open class RegularPolygonalTraceShape(numberOfEdges: Int, private var center: Vector, private val initialRadius: Float, finalRadius: Float,
                                         private val duration: Long, initialColor: Color, buildShapeAttr: BuildShapeAttr) : TraceShape() {
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(numberOfEdges, center, initialRadius, initialColor, buildShapeAttr))
    private var deltaRadius: Float = finalRadius - initialRadius
    private var alphaEveryMiniSecond: Float = (1f / 256 / initialColor.alpha).pow(1f / duration)

    private var timeSinceBorn: Float = 0f

    override fun move(displacement: Vector) {
        super.move(displacement)
        center += displacement
    }

    override fun rotate(centerOfRotation: Vector, angle: Float) {
        super.rotate(centerOfRotation, angle)
        center = center.rotateVector(centerOfRotation, angle)
    }

    override fun fadeTrace(now: Long, previousFrameTime: Long) {
        val color = componentShapes[0].shapeColor
        if (color.red > 250f / 256f
                && color.green > 250f / 256f
                && color.blue > 250f / 256f) {
            // this is basically white nowXY
            componentShapes[0].resetAlpha(color.alpha * alphaEveryMiniSecond.pow((now - previousFrameTime).toFloat()))
        } else {
            componentShapes[0].resetShapeColor(Color(color.red + (1 - color.red) * (now - previousFrameTime).toFloat() * 15f / duration,
                    color.green + (1 - color.green) * (now - previousFrameTime).toFloat() * 15f / duration,
                    color.blue + (1 - color.blue) * (now - previousFrameTime).toFloat() * 15f / duration,
                    color.alpha * alphaEveryMiniSecond.pow((now - previousFrameTime).toFloat())))
        }
        // and change radius
        (componentShapes[0] as RegularPolygonalShape).radius = deltaRadius * (0.75f * sqrt(timeSinceBorn / duration) + 0.25f * timeSinceBorn / duration) + initialRadius
        if (color.alpha <= 1f / 256f) {
            needToBeRemoved = true
        }

        timeSinceBorn += now - previousFrameTime
        //        changeSpeedMultiply(1f - (nowXY - previousFrameTime) / duration * 200);
    }
}