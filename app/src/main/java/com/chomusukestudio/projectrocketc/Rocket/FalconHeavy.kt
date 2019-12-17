package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Image
import com.chomusukestudio.projectrocketc.MainActivity
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.Color
import com.chomusukestudio.projectrocketc.Shape.ISolid
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding

class FalconHeavy(surrounding: Surrounding, crashSound: MediaPlayer, mainActivity: MainActivity, rocketPhysics: RocketPhysics, layers: Layers) : Rocket(surrounding, crashSound, rocketPhysics, layers) {
    override val trace: Trace = AccelerationTrace(7, 1.01f, 0.5f, 0.6f, 1000, 256,
    0.004f, Color(1f, 1f, 0f, 3f), layers)
    override val rocketQuirks: RocketQuirks = RocketQuirks(2f, 0.004f, 0.003f,
            0.000002f, 0.000001f)
    override val components: Array<ISolid> = arrayOf(Image(mainActivity, R.drawable.falcon_heavy,
            Vector(-0.2f, 0.6f), Vector(0.2f, 0.6f), Vector(0.2f, -0.6f), Vector(-0.2f, -0.6f),
            arrayOf(Vector(0.15f, 0.6f), Vector(0.15f, -0.6f), Vector(-0.15f, -0.6f), Vector(-0.15f, 0.6f)),
            0.5f, layers))
    override val width = 0.4f

    override fun generateTrace(now: Long, previousFrameTime: Long) {

    }

}