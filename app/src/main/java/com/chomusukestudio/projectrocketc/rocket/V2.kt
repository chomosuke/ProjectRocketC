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

/**
 * Created by Shuang Li on 11/03/2018.
 */

class V2(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, drawData: DrawData)
    : Rocket(surrounding, mainActivity, rocketPhysics, drawData) {
    override val traces = arrayOf<Trace>(
            AccelerationTrace(0.1f, 0.02f, 0.25f, 1000, 100, 0.004f, Color(1f, 1f, 0f, 3f),
                    1.01f, drawData))

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        val origin = ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) / 2f
        traces[0].generateTrace(now, previousFrameTime, origin, RocketState(currentRotation, velocity))
    }

    override val rocketQuirks = v2RocketQuirks
    override val description = "The first artificial object to travel into space."

    override val width = 0.26f
//    val widthSquare = QuadrilateralShape(Vector(width/2, 1f), Vector(-width/2, 1f),
//            Vector(-width/2, -1f), Vector(width/2, -1f), Color(0f, 1f, 0f, 0.2f), BuildShapeAttr(10f, true, drawData))

    override val components: Array<ISolid> = run {

        // when the rocket is created it's pointed towards right which is angle 0
        val scaleX = 0.7f
        val scaleY = 0.4f

        val imageVertexes = arrayOf(
                Vector(0.65f * scaleX, 0.32f * scaleY),
                Vector(0.65f * scaleX, -0.32f * scaleY),
                Vector(-0.67f * scaleX, -0.32f * scaleY),
                Vector(-0.67f * scaleX, 0.32f * scaleY))

        val overlapperVertexes = arrayOf(Vector(0.65f * scaleX, 0f),
                Vector(0.35f * scaleX, -0.125f * scaleY),
                Vector(0f, -0.155f * scaleY),
                Vector(-0.3f * scaleX, -0.145f * scaleY),
                Vector(-0.415f * scaleX, -0.3f * scaleY),
                Vector(-0.67f * scaleX, -0.32f * scaleY),
                Vector(-0.67f * scaleX, 0.32f * scaleY),
                Vector(-0.415f * scaleX, 0.3f * scaleY),
                Vector(-0.3f * scaleX, 0.145f * scaleY),
                Vector(0f, 0.155f * scaleY),
                Vector(0.35f * scaleX, 0.125f * scaleY)
        )

        val image = Image(R.drawable.v2_, imageVertexes[0], imageVertexes[1], imageVertexes[2], imageVertexes[3],
                overlapperVertexes, false, 0.5f, drawData)
        image.colorOffset = Color(0.25f, 0.25f, 0.25f, 0f)
//        image.setColorSwap(Color(0f, 0f, 0f, 1f), Color(0.2f, 0.2f, 0.2f, 1f))

        arrayOf(image)
    }

    private val rf = 0.006f
    private val repulsiveForces = arrayOf(Vector(-rf, 0f), Vector(0f, -rf),
            Vector(0f, rf), Vector(0f, -rf), Vector(0f, rf), Vector(0f, -rf),
            Vector(0f, rf), Vector(0f, -rf), Vector(0f, rf))
}