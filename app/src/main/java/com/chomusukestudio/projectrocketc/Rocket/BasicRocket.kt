package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.RedExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.trace.RegularPolygonalTrace
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Shape.coordinate.Coordinate

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding

/**
 * Created by Shuang Li on 11/03/2018.
 */

open class BasicRocket(surrounding: Surrounding, private val crashSound: MediaPlayer) : Rocket(surrounding) {
    override val trace = RegularPolygonalTrace(6, 1.01f, 0f,  0.4f, 500, 1f, 1f, 0f, 3f)

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        val x1 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX4)
        val y1 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY4)
        val x2 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX3)
        val y2 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY3)
        val originX = (x1 + x2) / 2
        val originY = (y1 + y2) / 2
        trace.generateTrace(now, previousFrameTime, originX, originY)
    }

    override fun fadeTrace(now: Long, previousFrameTime: Long) {
        trace.fadeTrace(now, previousFrameTime)
    }

    override var radiusOfRotation = 2f
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

    // initialize for surrounding to set centerOfRotation
    init {
        setRotation(surrounding.centerOfRotationX, surrounding.centerOfRotationY, surrounding.rotation)
    }

    private val explosionCoordinates = arrayOf(
            Coordinate(with(components[0] as TriangularShape) { (x1 + x2 + x3) / 3 }, with(components[0] as TriangularShape) { (y1 + y2 + y3) / 3 }),
            Coordinate(with(components[1] as QuadrilateralShape) { (x1 + x2 + x3 + x4) / 4 }, with(components[1] as QuadrilateralShape) { (y1 + y2 + y3 + y4) / 4 }),
            Coordinate((components[2] as CircularShape).centerX, (components[2] as CircularShape).centerY),
            Coordinate(with(components[3] as QuadrilateralShape) { (x1 + x2 + x3 + x4) / 4 }, with(components[3] as QuadrilateralShape) { (y1 + y2 + y3 + y4) / 4 }))

    override fun drawExplosion(now: Long, previousFrameTime: Long) {
        if (explosionShape == null) {
            val explosionCoordinate = Coordinate(centerOfRotationX, centerOfRotationY)
//                    this.explosionCoordinates[components.indexOf(crashedComponent)]
//            explosionCoordinate.rotateCoordinate(centerOfRotationX, centerOfRotationY, currentRotation)
            explosionShape = RedExplosionShape(explosionCoordinate.x, explosionCoordinate.y, 0.75f, 1000)
        } else {
            // rocket already blown up
            for (component in components)
                if (!component.removed)
                    component.removeShape()

            explosionShape!!.drawExplosion(now - previousFrameTime)
        }
    }

    override fun isCrashed(surrounding: Surrounding): Boolean {
        return if (super.isCrashed(surrounding)) {
            crashSound.start()
            true
        } else false
    }

    override fun removeAllShape() {
        for (component in components)
            if (!component.removed)
                component.removeShape()
        trace.removeTrace()
        explosionShape?.removeShape()
    }
}