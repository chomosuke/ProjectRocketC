package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Shape.*

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding

/**
 * Created by Shuang Li on 11/03/2018.
 */

class V2(surrounding: Surrounding, private val crashSound: MediaPlayer, rocketPhysics: RocketPhysics, layers: Layers) : Rocket(surrounding, rocketPhysics, layers) {
    override val trace = //RegularPolygonalTrace(7, 1.01f, 0.24f,  0.4f, 2000, 1f, 1f, 0f, 1f, layers)
//        SquareTrace(0.24f,  0.4f, 2000, 1f, 1f, 0f, 1f,1.01f, layers)
            AccelerationTrace(7, 1.01f, 0.24f, 0.4f, 1000, 100, 0.004f, Color(1f, 1f, 0f, 3f), layers)

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        val x1 = (components[6] as QuadrilateralShape).getQuadrilateralShapeCoords(QX1)
        val y1 = (components[6] as QuadrilateralShape).getQuadrilateralShapeCoords(QY1)
        val x2 = (components[5] as QuadrilateralShape).getQuadrilateralShapeCoords(QX1)
        val y2 = (components[5] as QuadrilateralShape).getQuadrilateralShapeCoords(QY1)
        val originX = (x1 + x2) / 2
        val originY = (y1 + y2) / 2
        trace.generateTrace(now, previousFrameTime, originX, originY, RocketState(currentRotation, speedX, speedY))
    }

    override val rocketQuirks = RocketQuirks(2f, 0.004f, 0.003f, 0.000002f, 0.000001f)

    override val width = 0.3f

    private val white = Color(1f, 1f, 1f, 1f)
    private val black = Color(0.2f, 0.2f, 0.2f, 1f)
    override val components: Array<Shape> = arrayOf(
            // defined components of rocket around centerOfRotation set by surrounding
            // 0
            TriangularShape(centerOfRotationX, centerOfRotationY + 0.65f,
                    centerOfRotationX + 0.05f, centerOfRotationY + 0.45f,
                    centerOfRotationX - 0.05f, centerOfRotationY + 0.45f,
                    black, BuildShapeAttr(1f, true, layers)),
            // 1
            QuadrilateralShape(centerOfRotationX + 0.05f, centerOfRotationY + 0.45f,
                    centerOfRotationX, centerOfRotationY + 0.45f, centerOfRotationX, centerOfRotationY + 0.15f,
                    centerOfRotationX + 0.08f, centerOfRotationY + 0.15f, white, BuildShapeAttr(1f, true, layers)),
            // 2
            QuadrilateralShape(centerOfRotationX - 0.05f, centerOfRotationY + 0.45f,
                    centerOfRotationX, centerOfRotationY + 0.45f, centerOfRotationX, centerOfRotationY + 0.15f,
                    centerOfRotationX - 0.08f, centerOfRotationY + 0.15f, black, BuildShapeAttr(1f, true, layers)),
            // 3
            QuadrilateralShape(centerOfRotationX + 0.08f, centerOfRotationY - 0.15f,
                    centerOfRotationX, centerOfRotationY - 0.15f,
                    centerOfRotationX, centerOfRotationY + 0.15f,
                    centerOfRotationX + 0.08f, centerOfRotationY + 0.15f, black, BuildShapeAttr(1f, true, layers)),
            // 4
            QuadrilateralShape(centerOfRotationX - 0.08f, centerOfRotationY - 0.15f,
                    centerOfRotationX, centerOfRotationY - 0.15f,
                    centerOfRotationX, centerOfRotationY + 0.15f,
                    centerOfRotationX - 0.08f, centerOfRotationY + 0.15f, white, BuildShapeAttr(1f, true, layers)),
            // 5
            QuadrilateralShape(centerOfRotationX + 0.09f, centerOfRotationY - 0.35f,
                    centerOfRotationX, centerOfRotationY - 0.35f,
                    centerOfRotationX, centerOfRotationY - 0.15f,
                    centerOfRotationX + 0.08f, centerOfRotationY - 0.15f, white, BuildShapeAttr(1f, true, layers)),
            // 6
            QuadrilateralShape(centerOfRotationX - 0.09f, centerOfRotationY - 0.35f,
                    centerOfRotationX, centerOfRotationY - 0.35f,
                    centerOfRotationX, centerOfRotationY - 0.15f,
                    centerOfRotationX - 0.08f, centerOfRotationY - 0.15f, black, BuildShapeAttr(1f, true, layers)))

    override val shapeForCrashAppro = QuadrilateralShape(centerOfRotationX + 0.15f, centerOfRotationY + 0.5f,
            centerOfRotationX - 0.15f, centerOfRotationY + 0.5f, centerOfRotationX - 0.15f, centerOfRotationY - 0.4f,
            centerOfRotationX + 0.15f, centerOfRotationY - 0.4f, Color(1f, 1f, 1f, 1f), BuildShapeAttr(1f, false, layers))

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