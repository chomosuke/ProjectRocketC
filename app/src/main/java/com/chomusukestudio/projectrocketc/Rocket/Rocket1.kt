package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.trace.SquareTrace
import com.chomusukestudio.projectrocketc.Shape.*

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by Shuang Li on 11/03/2018.
 */

open class Rocket2(surrounding: Surrounding, private val crashSound: MediaPlayer, layers: Layers) : Rocket(surrounding, layers) {
    override val trace = //RegularPolygonalTrace(7, 1.01f, 0.24f,  0.4f, 2000, 1f, 1f, 0f, 1f, layers)
        SquareTrace(0.24f,  0.4f, 2000, 1f, 1f, 0f, 1f,1.01f, layers)
    override fun generateTrace(now: Long, previousFrameTime: Long) {
        val x1 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX4)
        val y1 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY4)
        val x2 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX3)
        val y2 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY3)
        val originX = (x1 + x2) / 2
        val originY = (y1 + y2) / 2
        trace.generateTrace(now, previousFrameTime, originX, originY, RocketState(currentRotation, speed*sin(currentRotation), speed*cos(currentRotation)))
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
                        1f, 1f, 1f, 1f, BuildShapeAttr(1f, true, layers))
            1 ->
                QuadrilateralShape(centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f, centerOfRotationX - 0.15f, centerOfRotationY - 0.3f,
                        centerOfRotationX + 0.15f, centerOfRotationY - 0.3f, 1f, 1f, 1f, 1f, BuildShapeAttr(1f, true, layers))
            2 ->
                CircularShape(centerOfRotationX, centerOfRotationY /*+ 0.38*/, 0.07f,
                        0.1f, 0.1f, 0.1f, 1f, BuildShapeAttr(0.9999f, true, layers))
            3 ->
                QuadrilateralShape(centerOfRotationX + 0.1f, centerOfRotationY - 0.3f,
                        centerOfRotationX - 0.1f, centerOfRotationY - 0.3f, centerOfRotationX - 0.12f, centerOfRotationY - 0.4f,
                        centerOfRotationX + 0.12f, centerOfRotationY - 0.4f, 1f, 1f, 1f, 1f, BuildShapeAttr(1f, true, layers))
            else -> {
                throw IndexOutOfBoundsException()
            }
        }
    }
    override val shapeForCrashAppro = QuadrilateralShape(centerOfRotationX + 0.15f, centerOfRotationY + 0.5f,
            centerOfRotationX - 0.15f, centerOfRotationY + 0.5f, centerOfRotationX - 0.15f, centerOfRotationY - 0.4f,
            centerOfRotationX + 0.15f, centerOfRotationY - 0.4f, 1f, 1f, 1f, 1f, BuildShapeAttr(1f, false, layers))

    // initialize for surrounding to set centerOfRotation
    init {
        setRotation(surrounding.centerOfRotationX, surrounding.centerOfRotationY, surrounding.rotation)
    }

    // make the crash sound
    override fun isCrashed(surrounding: Surrounding): Boolean {
        return if (super.isCrashed(surrounding)) {
            crashSound.start()
            true
        } else false
    }

    override fun removeAllShape() {
        super.removeAllShape()
        crashSound.release()
    }
}