package com.chomusukestudio.projectrocketc.rocket

import com.chomusukestudio.prcandroid2dgameengine.Image
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.*
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.rocket.trace.Trace
import com.chomusukestudio.projectrocketc.surrounding.Surrounding
import com.chomusukestudio.projectrocketc.userInterface.MainActivity

class Falcon9(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, drawData: DrawData) : Rocket(surrounding, mainActivity, rocketPhysics, drawData) {
    override val traces = arrayOf<Trace>(
            AccelerationTrace(0.08f, 0.015f, 0.2f, 1000, 256, 0.004f, Color(1f, 1f, 0f, 3f),
                    1.01f, drawData)
//            SquareTrace(0.007f, 0.15f, 0.075f, 400, 32,
//                    Color(1f, 1f, 1f, 1f), Color(1f, 0.75f, 0f, 1f),
//                    1.01f, layers)
    )
    override val rocketQuirks = falcon9RocketQuirks
    override val description = "The first partially reusable rocket. It was built by SpaceX aimed at making it cheaper to send things into space."
    
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
    override val width = 0.16f
//    val widthSquare = QuadrilateralShape(Vector(width/2, 1f), Vector(-width/2, 1f),
//            Vector(-width/2, -1f), Vector(width/2, -1f), Color(0f, 1f, 0f, 0.2f), BuildShapeAttr(10f, true, drawData))

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        traces[0].generateTrace(now, previousFrameTime, ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) * 0.5f, RocketState(currentRotation, velocity))
    }
}