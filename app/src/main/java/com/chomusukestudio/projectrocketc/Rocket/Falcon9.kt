package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.prcandroid2dgameengine.Image
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.Color
import com.chomusukestudio.prcandroid2dgameengine.shape.ISolid
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.UI.MainActivity

class Falcon9(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, drawData: DrawData) : Rocket(surrounding, mainActivity, rocketPhysics, drawData) {
    override val traces = arrayOf<Trace>(
            AccelerationTrace(0.08f, 0.015f, 0.2f, 1000, 256, 0.004f, Color(1f, 1f, 0f, 3f),
                    1.01f, drawData)
//            SquareTrace(0.007f, 0.15f, 0.075f, 400, 32,
//                    Color(1f, 1f, 1f, 1f), Color(1f, 0.75f, 0f, 1f),
//                    1.01f, layers)
    )
    override val rocketQuirks = RocketQuirks(2f, 0.003f, 0.003f,
            0.000002f, 0.000001f, 100000)
    override val components: Array<ISolid> = run {
        val arrayForOverlapper = arrayOf(
                Vector(-0.6f, 0.05f), Vector(0.35f, 0.05f),
                Vector(0.38f, 0.08f), Vector(0.54f, 0.08f),
                Vector(0.6f, 0f), // top vertex,
                Vector(0.54f, -0.08f), Vector(0.38f, -0.08f),
                Vector(0.35f, -0.05f), Vector(-0.6f, -0.05f)
        )
        val imageVertexes = arrayOf(Vector(0.6f, 0.082f), Vector(0.6f, -0.082f), Vector(-0.6f, -0.082f), Vector(-0.6f, 0.082f))

        val scaleX = 1.1f; val scaleY = 1f
        for (i in arrayForOverlapper.indices)
            arrayForOverlapper[i] = Vector(arrayForOverlapper[i].x * scaleX, arrayForOverlapper[i].y * scaleY)
        for (i in imageVertexes.indices)
            imageVertexes[i] = Vector(imageVertexes[i].x * scaleX, imageVertexes[i].y * scaleY)

        arrayOf(Image(R.drawable.falcon_9,
                imageVertexes[0], imageVertexes[1], imageVertexes[2], imageVertexes[3],
                arrayForOverlapper, false,
                0.5f, drawData))
    }
    override val width = 0.5f

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        traces[0].generateTrace(now, previousFrameTime, ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) * 0.5f, RocketState(currentRotation, velocity))
    }
}