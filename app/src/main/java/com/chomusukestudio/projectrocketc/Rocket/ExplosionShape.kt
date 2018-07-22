package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.Shape.coordinate.square
import com.chomusukestudio.projectrocketc.upTimeMillis
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

class RedWhiteExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, duration: Long) : ExplosionShape(centerX, centerY, approximateRadius, duration) {

    override var componentShapes: Array<Shape> = arrayOf(RedExplosionShape(centerX, centerY, approximateRadius, duration),
            WhiteExplosionShape(centerX, centerY, approximateRadius, duration))

    override fun drawExplosion(timePassed: Long) {
        (componentShapes[0] as ExplosionShape).drawExplosion(timePassed)
        (componentShapes[1] as ExplosionShape).drawExplosion(timePassed)
    }
}

class RedExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, duration: Long) : ExplosionShape(centerX, centerY, approximateRadius, duration) {
    private val componentShapesSize = 8

    private val timeSinceMade = LongArray(componentShapesSize)
    override lateinit var componentShapes: Array<Shape>

    init {
        val rotationOrder = IntArray(componentShapesSize) { i -> i }.asList().shuffled().toIntArray() // randomOrder
        componentShapes = Array(componentShapesSize) { i ->
            if (i == 0) {
                CircularShape(centerX, centerY, approximateRadius, 0.996f, 0.675f, 0.114f, 1f, -11f - 0.1f * i, true)
            } else {
                val distantToCenter = Math.random().toFloat() * approximateRadius * 1.25f
                val centers = rotatePoint(centerX, centerY + distantToCenter, centerX, centerY, (rotationOrder[i] * PI / 4).toFloat())
                CircularShape(centers[0], centers[1], 0f, 0.996f, 0.875f, 0.314f, 1f, -11f - 0.1f * i, true)
            }
        }
    }

    private val initialRadius = FloatArray(componentShapesSize) { i ->
        if (i == 0)
            (componentShapes[i] as CircularShape).radius
        else
            (0.2 + 0.2 * Math.random()).toFloat()
    }

    override fun drawExplosion(timePassed: Long) {
        for (i in componentShapes.indices) {

            if ((componentShapes[i] as CircularShape).radius != 0f || timeSinceMade[i] != 0L) { // already showing

                timeSinceMade[i] += timePassed

                if (!componentShapes[i].removed) { // if it is still not removed
                    val radius = initialRadius[i] * (-square(timeSinceMade[i].toDouble() / duration.toDouble()) + 1).toFloat()

                    (componentShapes[i] as CircularShape).resetParameter((componentShapes[i] as CircularShape).centerX,
                            (componentShapes[i] as CircularShape).centerY, radius)

                    if (radius <= 0f)
                        componentShapes[i].removeShape()
                }
            } else { // not yet showing

                if (timeSinceMade[i - 1] > 128 * random()) {
                    // one per 128 * random() second
                    (componentShapes[i] as CircularShape).resetParameter((componentShapes[i] as CircularShape).centerX,
                            (componentShapes[i] as CircularShape).centerY, initialRadius[i])
                }
            }
        }
    }
}

class WhiteExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, duration: Long) : ExplosionShape(centerX, centerY, approximateRadius, duration) {
    override fun drawExplosion(timePassed: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val componentShapesSize = 8

    override var componentShapes = Array<Shape>(componentShapesSize) { i ->
        if (i == 0)
            CircularShape(centerX, centerY, approximateRadius, 1f, 1f, 1f, 1f, -10f, true)
        else {
            val centers = rotatePoint(centerX, centerY + approximateRadius, centerX, centerY, (i * PI / 8).toFloat())
            CircularShape(centers[0], centers[1], 0f, 1f, 1f, 1f, 1f, -10f, true)
        }
    }

    private val initialRadius = FloatArray(componentShapesSize) { i ->
        if (i == 0)
            (componentShapes[i] as CircularShape).radius
        else
            (0.5 + 1 * Math.random()).toFloat()
    }
}