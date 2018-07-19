//package com.chomusukestudio.projectrocketc.Rocket
//
//import com.chomusukestudio.projectrocketc.Shape.CircularShape
//import com.chomusukestudio.projectrocketc.Shape.Shape
//import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
//import kotlin.math.PI
//
//open class ExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, private val duration: Long) : Shape() {
//    override val isOverlapMethodLevel: Double
//        get() = throw IllegalAccessException("explosionShape can't overlap anything")
//
//    override var componentShapes: Array<Shape> = arrayOf(CircularShape(centerX, centerY, approximateRadius, 0.996f, 0.875f, 0.314f, 1f, -11f, true),
//            RedExplosionShape(centerX, centerY, approximateRadius, duration),
//            WhiteExplosionShape(centerX, centerY, approximateRadius, duration))
//
//    private val alphaEveryMiniSecond = Math.pow(1.0 / 256, 1.0 / duration).toFloat()
//
//    private var timeSinceExplosion = 0L
//    fun drawExplosion(timePassed: Long) {
//        timeSinceExplosion += timePassed
//
//        for (i in componentShapes.indices) {
//
////                val color = componentShapes[i].shapeColor
////                componentShapes[i].resetAlpha(color[3] * Math.pow(alphaEveryMiniSecond.toDouble(), timePassed.toDouble()).toFloat())
//
//            val radius = individualRadius[i] * Math.sqrt(timeSinceExplosion.toDouble() / duration.toDouble()).toFloat()
//            if (radius <= 0)
//                componentShapes[i].removeShape()
//            if (!componentShapes[i].removed) // if it is still not removed
//                (componentShapes[i] as CircularShape).resetParameter((componentShapes[i] as CircularShape).centerX,
//                        (componentShapes[i] as CircularShape).centerY, radius)
//        }
//    }
//}
//
//class RedExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, private val duration: Long) : ExplosionShape(centerX, centerY, approximateRadius, duration) {
//    val individualRadius = FloatArray(componentShapes.size) { i ->
//        when {
//            i == 0 -> (0.5 + 1 * Math.random()).toFloat()
//            else -> (0.5 + 1 * Math.random()).toFloat()
//        }
//    }
//    val distantToCenter = Math.random().toFloat() * approximateRadius * 0.6f
//    val centers = rotatePoint(centerX, centerY + distantToCenter, centerX, centerY, (i * PI / 4).toFloat())
//    CircularShape(centers[0], centers[1], 0f, 0.996f, 0.875f, 0.314f, 1f, -11f + 0.1f * i, true)
//}
//
//class WhiteExplosionShape(centerX: Float, centerY: Float, approximateRadius: Float, private val duration: Long) : ExplosionShape(centerX, centerY, approximateRadius, duration) {
//    val individualRadius = FloatArray(componentShapes.size) { i ->
//        when {
//            i == 0 -> (0.5 + 1 * Math.random()).toFloat()
//            else -> (0.5 + 1 * Math.random()).toFloat()
//        }
//    }
//    val distantToCenter = Math.random().toFloat() * approximateRadius * 1.1f
//    val centers = rotatePoint(centerX, centerY + distantToCenter, centerX, centerY, (i * PI / 8).toFloat())
//    CircularShape(centers[0], centers[1], 0f, 1f, 1f, 1f, 1f, -10f, true)
//}