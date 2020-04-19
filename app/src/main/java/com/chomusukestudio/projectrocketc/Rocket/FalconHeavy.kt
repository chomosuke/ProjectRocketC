package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.prcandroid2dgameengine.Image
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.*
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.MultiTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.UI.MainActivity

class FalconHeavy(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, drawData: DrawData) : Rocket(surrounding, mainActivity, rocketPhysics, drawData) {
    override val traces = arrayOf<Trace>(
            MultiTrace(3, {
//                SquareTrace(0.007f, 0.15f, 0.075f, 400, 32,
//                    Color(1f, 1f, 1f, 1f), Color(1f, 0.75f, 0f, 1f),
//                    1.01f, layers)
                if (it == 1)
                    AccelerationTrace(0.08f, 0.015f, 0.15f, 800, 200, 0.003f, Color(1f, 1f, 0f, 3f),
                            1.01f, drawData)
                else
                    AccelerationTrace(0.08f, 0.015f, 0.15f, 1000, 256, 0.004f, Color(1f, 1f, 0f, 3f),
                            1.01f, drawData)
            }, 0.4f)
    )
    override val rocketQuirks: RocketQuirks = RocketQuirks("Falcon Heavy",2f, 0.003f, 0.003f,
            0.000002f, 0.000001f, 20000, 0.2f, 10, 1)
    override val components: Array<ISolid> = run {
        val arrayForOverlapper = arrayOf(
                Vector(-0.6f, 0.2f), Vector(0.1f, 0.2f),
                Vector(0.2f, 0.15f), Vector(0.15f, 0.05f),
                Vector(0.54f, 0.08f),
                Vector(0.6f, 0f), // top vertex,
                Vector(0.54f, -0.08f),
                Vector(0.15f, -0.05f), Vector(0.2f, -0.15f),
                Vector(0.1f, -0.2f), Vector(-0.6f, -0.2f))
        val imageVertexes = arrayOf(Vector(0.6f, 0.2f), Vector(0.6f, -0.2f), Vector(-0.6f, -0.2f), Vector(-0.6f, 0.2f))

        // scale
        val scale = Vector(1.1f, 1f)
        for (i in arrayForOverlapper.indices)
            arrayForOverlapper[i] *= scale
        for (i in imageVertexes.indices)
            imageVertexes[i] *= scale

        arrayOf(Image(R.drawable.falcon_heavy,
                imageVertexes[0], imageVertexes[1], imageVertexes[2], imageVertexes[3],
                arrayForOverlapper, false,
                0.5f, drawData))
    }
    override val width = 0.4f
//    val widthSquare = QuadrilateralShape(Vector(width/2, 1f), Vector(-width/2, 1f),
//            Vector(-width/2, -1f), Vector(width/2, -1f), Color(0f, 1f, 0f, 0.2f), BuildShapeAttr(10f, true, drawData))

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        traces[0].generateTrace(now, previousFrameTime,
                ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) / 2f,
                RocketState(currentRotation, velocity))
    }
}