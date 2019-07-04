package com.chomusukestudio.projectrocketc.Rocket.RocketRelated

import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Color
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.square
import com.chomusukestudio.projectrocketc.randFloat
import java.lang.Math.random
import kotlin.math.PI

abstract class ExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, protected val duration: Long) : Shape() {
    override val isOverlapMethodLevel: Double
        get() = throw IllegalAccessException("explosionShape can't overlap anything")

    abstract fun drawExplosion(timePassed: Long)

    override fun removeShape() {
        for (componentShape in componentShapes)
            if (!componentShape.removed)
                componentShape.removeShape()
    }
}

class RedWhiteExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, duration: Long, buildShapeAttr: BuildShapeAttr) : ExplosionShape(centerX, centerY, approximateRadius, duration) {

    override var componentShapes: Array<Shape> = arrayOf(RedExplosionShape(centerX, centerY, approximateRadius, duration, buildShapeAttr),
            WhiteExplosionShape(centerX, centerY, approximateRadius, duration, buildShapeAttr))

    override fun drawExplosion(timePassed: Long) {
        (componentShapes[0] as ExplosionShape).drawExplosion(timePassed)
        (componentShapes[1] as ExplosionShape).drawExplosion(timePassed)
    }
}

class RedExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, duration: Long, buildShapeAttr: BuildShapeAttr) : ExplosionShape(centerX, centerY, approximateRadius, duration) {
    private val componentShapesSize = 5

    private val timeSinceMade = LongArray(componentShapesSize)
    override lateinit var componentShapes: Array<Shape>

    init {
        val rotationOrder = IntArray(componentShapesSize) { i -> i }.asList().shuffled().toIntArray() // randomOrder
        componentShapes = Array(componentShapesSize) { i ->
            if (i == 0) {
                CircularShape(centerX, centerY, approximateRadius, Color(1f, 0.675f, 0.114f, 1f), buildShapeAttr.newAttrWithChangedZ(-0.01f * i))
            } else {
                val distantToCenter = Math.random().toFloat() * approximateRadius * 1.25f
                val centers = rotatePoint(centerX, centerY + distantToCenter, centerX, centerY, (rotationOrder[i] * PI / 4).toFloat())
                CircularShape(centers[0], centers[1], 0f, Color(1f, randFloat(0.675f, 0.975f), randFloat(0.114f, 0.514f), 1f),
                        buildShapeAttr.newAttrWithChangedZ(-0.01f * i))
            }
        }
    }

    private val initialRadius = FloatArray(componentShapesSize) { i ->
        if (i == 0)
            (componentShapes[i] as CircularShape).radius
        else
            randFloat(0.2f, 0.4f)
    }

    override fun drawExplosion(timePassed: Long) {
        for (i in componentShapes.indices) {

            if ((componentShapes[i] as CircularShape).radius != 0f || timeSinceMade[i] != 0L) { // already showing

                timeSinceMade[i] += timePassed

                if (!componentShapes[i].removed) { // if it is still not removed

                    (componentShapes[i] as CircularShape).radius = initialRadius[i] * (-square(timeSinceMade[i].toFloat() / duration) + 1)

                    if ((componentShapes[i] as CircularShape).radius <= 0f)
                        componentShapes[i].removeShape()
                }
            } else { // not yet showing

                if (timeSinceMade[i - 1] > 128 * random()) {
                    // one per 128 * random() second
                    (componentShapes[i] as CircularShape).radius = initialRadius[i]
                }
            }
        }
    }
}

class WhiteExplosionShape(private val centerX: Float, private val centerY: Float, private val approximateRadius: Float, duration: Long, buildShapeAttr: BuildShapeAttr) : ExplosionShape(centerX, centerY, approximateRadius, duration) {

    private var timeSinceMade = 0L
    override fun drawExplosion(timePassed: Long) {

        timeSinceMade += timePassed



//        val radius = approximateRadius - square(timeSinceMade.toDouble() / duration - 1).toFloat() + 1
//
//        if (radius < 0 && !componentShapes[0].removed) {
//            for (componentShape in componentShapes)
//                componentShape.removeShape()
//        }
//
//        if (!componentShapes[0].removed){
//            (componentShapes[0] as CircularShape).resetParameter(centerX, centerY, radius)
//
//            for (i in 1 until componentShapes.size) {
//                val centers = rotatePoint(centerX, centerY + radius, centerX, centerY, (i*2*PI / (componentShapesSize - 1)).toFloat())
//                (componentShapes[i] as CircularShape).resetParameter(centers[0], centers[1], radius / 4f)
//            }
//        }
    }

    private val componentShapesSize = 17

    override lateinit var componentShapes: Array<Shape>

    init {
        val rotationOrder = IntArray(componentShapesSize) { i -> i }.asList().shuffled().toIntArray() // randomOrder
        componentShapes = Array(componentShapesSize) { i ->
            if (i == 0) {
                CircularShape(centerX, centerY, approximateRadius, Color(0.996f, 0.675f, 0.114f, 1f), buildShapeAttr.newAttrWithChangedZ(-0.01f * i))
            } else {
                val distantToCenter = Math.random().toFloat() * approximateRadius * 1.25f
                val centers = rotatePoint(centerX, centerY + distantToCenter, centerX, centerY, (rotationOrder[i] * 2*PI / componentShapesSize).toFloat())
                CircularShape(centers[0], centers[1], 0f, Color(0.996f, 0.875f, 0.314f, 1f), buildShapeAttr.newAttrWithChangedZ(-0.01f * i))
            }
        }
    }

    private val initialRadius = FloatArray(componentShapesSize) { i ->
        if (i == 0)
            (componentShapes[i] as CircularShape).radius
        else
            randFloat(1.5f, 0.5f)
    }
}