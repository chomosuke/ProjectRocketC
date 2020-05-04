package com.chomusukestudio.projectrocketc.rocket

import com.chomusukestudio.prcandroid2dgameengine.Image
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.*
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.rocket.trace.MultiTrace
import com.chomusukestudio.projectrocketc.rocket.trace.SquareTrace
import com.chomusukestudio.projectrocketc.rocket.trace.Trace
import com.chomusukestudio.projectrocketc.surrounding.Surrounding
import com.chomusukestudio.projectrocketc.userInterface.MainActivity

class SpaceShuttle(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, drawData: DrawData) : Rocket(surrounding, mainActivity, rocketPhysics, drawData) {
    override val traces: Array<Trace> = arrayOf(MultiTrace(2, {
                AccelerationTrace(0.05f, 0.02f, 0.2f, 750, 256, 0.004f, Color(1f, 0.9f, 0f, 3f),
                        1.01f, drawData)
            }, 0.525f),
            MultiTrace(2, {
                SquareTrace(0.007f, 0.05f, 0.05f, 300, 16,
                        Color(1f, 1f, 1f, 1f), Color(0.1f, 0.2f, 1f, 1f),
                        1.01f, drawData)
            }, 0.15f))
    override val rocketQuirks: RocketQuirks = spaceShuttleRocketQuirks
    
    override val components: Array<ISolid> = run {
        val sX = 1.5f
        val sY = 1.5f

        var overlapperVertexes = arrayOf(
                Vector(0f * sX, 0.158f * sY),
                Vector(0.050f * sX, 0.193f * sY),
                Vector(0.114f * sX, 0.211f * sY),
                Vector(0.194f * sX, 0.217f * sY),
                Vector(0.131f * sX, 0.241f * sY),
                Vector(0.195f * sX, 0.266f * sY),
                Vector(0.458f * sX, 0.266f * sY),
                Vector(0.508f * sX, 0.307f * sY),
                Vector(0.552f * sX, 0.317f * sY),
                Vector(0.559f * sX, 0.267f * sY),
                Vector(0.675f * sX, 0.276f * sY),
                Vector(0.675f * sX, 0.216f * sY),
                Vector(0.600f * sX, 0.220f * sY),
                Vector(0.610f * sX, 0.158f * sY)
        )

        for (i in overlapperVertexes.indices)
            overlapperVertexes[i] = Vector(0.341f * sX - overlapperVertexes[i].x, 0.158f * sY - overlapperVertexes[i].y)

        val mirror = arrayOfNulls<Vector>(overlapperVertexes.size - 2)
        for (i in 2 until overlapperVertexes.size)
            mirror[i-2] = overlapperVertexes[overlapperVertexes.size - i].mirrorXAxis()

        overlapperVertexes += mirror as Array<Vector>

        arrayOf(Image(R.drawable.space_shuttle,
                Vector(0.341f * sX, 0.158f * sY),
                Vector(0.341f * sX, -0.158f * sY),
                Vector(-0.341f * sX, -0.158f * sY),
                Vector(-0.341f * sX, 0.158f * sY),
                overlapperVertexes, false, 0.5f, drawData))
    }

    override val width: Float = 0.48f
//    val widthSquare = QuadrilateralShape(Vector(width/2, 1f), Vector(-width/2, 1f),
//            Vector(-width/2, -1f), Vector(width/2, -1f), Color(0f, 1f, 0f, 0.2f), BuildShapeAttr(10f, true, drawData))

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        traces[0].generateTrace(now, previousFrameTime,
                ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) / 2f,
                RocketState(currentRotation, velocity))
        traces[1].generateTrace(now, previousFrameTime,
                ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) / 2f + Vector(0.15f, 0f).rotateVector(currentRotation),
                RocketState(currentRotation, velocity))
    }
}