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

class TestingRocket(surrounding: Surrounding, private val crashSound: MediaPlayer, rocketPhysics: RocketPhysics, layers: Layers) : Rocket(surrounding, rocketPhysics, layers) {
    override val trace = //RegularPolygonalTrace(7, 1.01f, 0.24f,  0.4f, 2000, 1f, 1f, 0f, 1f, layers)
//        SquareTrace(0.24f,  0.4f, 2000, 1f, 1f, 0f, 1f,1.01f, layers)
            AccelerationTrace(7, 1.01f, 0.24f,  0.4f, 1000, 100, 0.004f, Color(1f, 1f, 0f, 3f), layers)
    override fun generateTrace(now: Long, previousFrameTime: Long) {
        val p1 = (components[3] as QuadrilateralShape).vertex4
        val p2 = (components[3] as QuadrilateralShape).vertex3
        val origin = (p1 + p2) * 0.5f
        trace.generateTrace(now, previousFrameTime, origin, RocketState(currentRotation, velocity))
    }

    override val rocketQuirks = RocketQuirks(2f, 0.003f, 0.004f, 0.000002f, 0.000001f)

    override val width = 0.3f

    override val components: Array<Shape> = Array(4) { i ->
        when (i) {
            // defined components of rocket around centerOfRotation set by surrounding
            0 ->
                TriangularShape(centerOfRotation.offset(0.5f, 0f),
                        centerOfRotation.offset(0.3f, 0.15f),
                        centerOfRotation.offset(0.3f, -0.15f),
                        Color(1f, 1f, 1f, 1f), BuildShapeAttr(1f, true, layers))
            1 ->
                QuadrilateralShape(centerOfRotation.offset(0.3f, 0.15f),
                        centerOfRotation.offset(0.3f, -0.15f), centerOfRotation.offset(-0.3f, -0.15f),
                        centerOfRotation.offset(-0.3f, 0.15f), Color(1f, 1f, 1f, 1f), BuildShapeAttr(1f, true, layers))
            2 ->
                CircularShape(centerOfRotation, 0.07f,
                        Color(0.1f, 0.1f, 0.1f, 1f), BuildShapeAttr(0.9999f, true, layers))
            3 ->
                QuadrilateralShape(centerOfRotation.offset(-0.3f, 0.1f),
                        centerOfRotation.offset(-0.3f, -0.1f), centerOfRotation.offset(-0.4f, -0.12f),
                        centerOfRotation.offset(-0.4f, 0.12f), Color(1f, 1f, 1f, 1f), BuildShapeAttr(1f, true, layers))
            else -> {
                throw IndexOutOfBoundsException()
            }
        }
    }

    // initialize for surrounding to set centerOfRotation
    init {
        setRotation(surrounding.centerOfRotation, surrounding.rotation)
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