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
        val p1 = (components[6] as QuadrilateralShape).vertex1
        val p2 = (components[5] as QuadrilateralShape).vertex1
        val origin = (p1 + p2) * 0.5f
        trace.generateTrace(now, previousFrameTime, origin, RocketState(currentRotation, velocity))
    }

    override val rocketQuirks = RocketQuirks(2f, 0.004f, 0.003f, 0.000002f, 0.000001f)

    override val width = 0.3f

    override val components: Array<Shape> = generateComponents(layers)
    private fun generateComponents(layers: Layers): Array<Shape> {
        val white = Color(1f, 1f, 1f, 1f)
        val black = Color(0.2f, 0.2f, 0.2f, 1f)
    
        val scaleX = 1f; val scaleY = 1f
        val v1 = Vector(0.65f * scaleX, 0f)
        val v2 = Vector(0.35f * scaleX, 0.125f * scaleY)
        val v3 = Vector(0f, 0.16f  * scaleY)
        val v4 = Vector(-0.3f * scaleX, 0.145f * scaleY)
        val v7 = Vector(-0.67f * scaleX, 0.32f  * scaleY)
        val v10 = Vector(-0.6f * scaleX, 0.085f * scaleY)
        
        val components = arrayOf(
                // defined components of rocket around centerOfRotation set by surrounding
                // 0
                TriangularShape(v1, v2, v2.mirrorXAxis(),
                        black, BuildShapeAttr(1f, true, layers)),
                // 1
                QuadrilateralShape(v2, Vector(v2.x, 0f), Vector(0f, 0f), v3,
                        white, BuildShapeAttr(1f, true, layers)),
                // 2
                QuadrilateralShape(v2.mirrorXAxis(), Vector(v2.x, 0f), Vector(0f, 0f), v3.mirrorXAxis(),
                        black, BuildShapeAttr(1f, true, layers)),
                // 3
                QuadrilateralShape(v4, Vector(v4.x , 0f), Vector(0f, 0f), v3,
                        black, BuildShapeAttr(1f, true, layers)),
                // 4
                QuadrilateralShape(v4.mirrorXAxis(), Vector(v4.x, 0f), Vector(0f, 0f), v3.mirrorXAxis(),
                        white, BuildShapeAttr(1f, true, layers)),
                // 5
                QuadrilateralShape(v10, Vector(v10.x, 0f), Vector(v4.x, 0f), v4,
                        white, BuildShapeAttr(1f, true, layers)),
                // 6
                QuadrilateralShape(v10.mirrorXAxis(), Vector(v10.x, 0f), Vector(v4.x, 0f), v4.mirrorXAxis(),
                        black, BuildShapeAttr(1f, true, layers)),
                // 7
                QuadrilateralShape(v10, Vector(v10.x, 0f), Vector(v7.x, 0f), v7,
                        white, BuildShapeAttr(1f, true, layers)),
                // 8
                QuadrilateralShape(v10.mirrorXAxis(), Vector(v10.x, 0f), Vector(v7.x, 0f), v7.mirrorXAxis(),
                        black, BuildShapeAttr(1f, true, layers))
        )
        for (component in components)
            component.moveShape(centerOfRotation)
        return components
    }
    
    override val shapeForCrashAppro = QuadrilateralShape(centerOfRotation  + Vector(0.15f, 0.5f),
            centerOfRotation + Vector(-0.15f, 0.5f), centerOfRotation + Vector(-0.15f, -0.4f),
            centerOfRotation + Vector(0.15f, -0.4f), Color(1f, 1f, 1f, 1f), BuildShapeAttr(1f, false, layers))

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