package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Image
import com.chomusukestudio.projectrocketc.UI.MainActivity
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.MultiTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.SquareTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import kotlin.math.PI

class FalconHeavy(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, layers: Layers) : Rocket(surrounding, mainActivity, rocketPhysics, layers) {
    override val traces = arrayOf<Trace>(
            MultiTrace(3, {
//                SquareTrace(0.007f, 0.15f, 0.075f, 400, 32,
//                    Color(1f, 1f, 1f, 1f), Color(1f, 0.75f, 0f, 1f),
//                    1.01f, layers)
                if (it == 1)
                    AccelerationTrace(7, 1.01f, 0.08f, 0.015f, 0.15f, 800, 200,
                            0.003f, Color(1f, 1f, 0f, 3f), layers)
                else
                    AccelerationTrace(7, 1.01f, 0.08f, 0.015f, 0.15f, 1000, 256,
                            0.004f, Color(1f, 1f, 0f, 3f), layers)
            }, 0.4f)
    )
    override val rocketQuirks: RocketQuirks = RocketQuirks(2f, 0.003f, 0.003f,
            0.000002f, 0.000001f)
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
            arrayForOverlapper[i] = arrayForOverlapper[i].scaleXY(scale)
        for (i in imageVertexes.indices)
            imageVertexes[i] = imageVertexes[i].scaleXY(scale)

        arrayOf(Image(mainActivity, R.drawable.falcon_heavy,
                imageVertexes[0], imageVertexes[1], imageVertexes[2], imageVertexes[3],
                arrayForOverlapper, false,
                0.5f, layers))
    }
    override val width = 0.4f

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        traces[0].generateTrace(now, previousFrameTime,
                ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) / 2f,
                RocketState(currentRotation, velocity))
    }
}