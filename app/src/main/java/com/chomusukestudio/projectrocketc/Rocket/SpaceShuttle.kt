package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Image
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.ISolid
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.UI.MainActivity

class SpaceShuttle(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, layers: Layers) : Rocket(surrounding, mainActivity, rocketPhysics, layers) {
    override val traces: Array<Trace> = arrayOf()
    override val rocketQuirks: RocketQuirks = RocketQuirks(2f, 0.003f, 0.003f,
                0.000002f, 0.000001f)
    override val components: Array<ISolid> = run {
        val sX = 1.8f
        val sY = 1.8f

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

        arrayOf(Image(mainActivity, R.drawable.space_shuttle,
                Vector(0.341f * sX, 0.158f * sY),
                Vector(0.341f * sX, -0.158f * sY),
                Vector(-0.341f * sX, -0.158f * sY),
                Vector(-0.341f * sX, 0.158f * sY),
                overlapperVertexes, false, 0.5f, layers))
    }

    override val width: Float = 0.432f

    override fun generateTrace(now: Long, previousFrameTime: Long) {
    }
}