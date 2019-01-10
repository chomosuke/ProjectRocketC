package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.RedExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.trace.RegularPolygonalTrace
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Shape.coordinate.Coordinate
import com.chomusukestudio.projectrocketc.Shape.coordinate.distance

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding

/**
 * Created by Shuang Li on 11/03/2018.
 */

open class Rocket1(surrounding: Surrounding, private val crashSound: MediaPlayer) : Rocket(surrounding) {
    override val trace = RegularPolygonalTrace(6, 1.01f, 0.24f,  0.4f, 1000, 1f, 1f, 0f, 3f)

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        val x1 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX4)
        val y1 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY4)
        val x2 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX3)
        val y2 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY3)
        val originX = (x1 + x2) / 2
        val originY = (y1 + y2) / 2
        trace.generateTrace(now, previousFrameTime, originX, originY)
    }

    override val radiusOfRotation = 2f
    final override val initialSpeed = 4f / 1000f
    override var speed = initialSpeed
    
    override val width = 0.3f

    final override val components: Array<Shape> = Array(4) { i ->
        when (i) {
        // defined components of rocket around centerOfRotation set by surrounding
            0 ->
                TriangularShape(centerOfRotationX, centerOfRotationY + 0.5f,
                        centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f,
                        1f, 1f, 1f, 1f, 1f, true)
            1 ->
                QuadrilateralShape(centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f, centerOfRotationX - 0.15f, centerOfRotationY - 0.3f,
                        centerOfRotationX + 0.15f, centerOfRotationY - 0.3f, 1f, 1f, 1f, 1f, 1f, true)
            2 ->
                CircularShape(centerOfRotationX, centerOfRotationY /*+ 0.38*/, 0.07f,
                        0.1f, 0.1f, 0.1f, 1f, 0.9999f, true)
            3 ->
                QuadrilateralShape(centerOfRotationX + 0.1f, centerOfRotationY - 0.3f,
                        centerOfRotationX - 0.1f, centerOfRotationY - 0.3f, centerOfRotationX - 0.12f, centerOfRotationY - 0.4f,
                        centerOfRotationX + 0.12f, centerOfRotationY - 0.4f, 1f, 1f, 1f, 1f, 1f, true)
            else -> {
                throw IndexOutOfBoundsException()
            }
        }
    }

    // make the crash sound
    override fun isCrashed(surrounding: Surrounding): Boolean {
        return if (super.isCrashed(surrounding)) {
            crashSound.start()
            true
        } else false
    }
}