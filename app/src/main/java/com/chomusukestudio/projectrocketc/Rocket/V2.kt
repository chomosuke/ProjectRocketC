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

    override val components: Array<Shape> = generateComponents(layers)
    private fun generateComponents(layers: Layers): Array<Shape> {
        val white = Color(1f, 1f, 1f, 1f)
        val black = Color(0.2f, 0.2f, 0.2f, 1f)
        
        val scaleX = 1f; val scaleY = 1f
                         val y1 = 0.65f * scaleY
        val x2 = 0.125f * scaleX; val y2 = 0.35f * scaleY
        val x3 = 0.16f  * scaleX
        val x4 = 0.145f * scaleX; val y4 = -0.3f * scaleY
        val x10 = 0.085f * scaleX; val y10 = -0.6f * scaleY
        val x7 = 0.32f  * scaleX; val y7 = -0.67f * scaleY
        
        return arrayOf(
                // defined components of rocket around centerOfRotation set by surrounding
                // 0
                TriangularShape(centerOfRotationX, centerOfRotationY + y1,
                        centerOfRotationX + x2, centerOfRotationY + y2,
                        centerOfRotationX - x2, centerOfRotationY + y2,
                        black, BuildShapeAttr(1f, true, layers)),
                // 1
                QuadrilateralShape(centerOfRotationX + x2, centerOfRotationY + y2,
                        centerOfRotationX, centerOfRotationY + y2,
                        centerOfRotationX, centerOfRotationY,
                        centerOfRotationX + x3, centerOfRotationY,
                        white, BuildShapeAttr(1f, true, layers)),
                // 2
                QuadrilateralShape(centerOfRotationX - x2, centerOfRotationY + y2,
                        centerOfRotationX, centerOfRotationY + y2,
                        centerOfRotationX, centerOfRotationY,
                        centerOfRotationX - x3, centerOfRotationY,
                        black, BuildShapeAttr(1f, true, layers)),
                // 3
                QuadrilateralShape(centerOfRotationX + x4, centerOfRotationY + y4,
                        centerOfRotationX, centerOfRotationY + y4,
                        centerOfRotationX, centerOfRotationY,
                        centerOfRotationX + x3, centerOfRotationY,
                        black, BuildShapeAttr(1f, true, layers)),
                // 4
                QuadrilateralShape(centerOfRotationX - x4, centerOfRotationY + y4,
                        centerOfRotationX, centerOfRotationY + y4,
                        centerOfRotationX, centerOfRotationY,
                        centerOfRotationX - x3, centerOfRotationY,
                        white, BuildShapeAttr(1f, true, layers)),
                // 5
                QuadrilateralShape(centerOfRotationX + x10, centerOfRotationY + y10,
                        centerOfRotationX, centerOfRotationY + y10,
                        centerOfRotationX, centerOfRotationY + y4,
                        centerOfRotationX + x4, centerOfRotationY + y4,
                        white, BuildShapeAttr(1f, true, layers)),
                // 6
                QuadrilateralShape(centerOfRotationX - x10, centerOfRotationY + y10,
                        centerOfRotationX, centerOfRotationY + y10,
                        centerOfRotationX, centerOfRotationY + y4,
                        centerOfRotationX - x4, centerOfRotationY + y4,
                        black, BuildShapeAttr(1f, true, layers)),
                // 7
                QuadrilateralShape(centerOfRotationX + x10, centerOfRotationY + y10,
                        centerOfRotationX, centerOfRotationY + y10,
                        centerOfRotationX, centerOfRotationY + y7,
                        centerOfRotationX + x7, centerOfRotationY + y7,
                        white, BuildShapeAttr(1f, true, layers)),
                // 8
                QuadrilateralShape(centerOfRotationX - x10, centerOfRotationY + y10,
                        centerOfRotationX, centerOfRotationY + y10,
                        centerOfRotationX, centerOfRotationY + y7,
                        centerOfRotationX - x7, centerOfRotationY + y7,
                        black, BuildShapeAttr(1f, true, layers))
    
        )
    }

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