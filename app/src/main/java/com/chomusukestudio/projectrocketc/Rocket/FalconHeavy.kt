package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Image
import com.chomusukestudio.projectrocketc.MainActivity
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding

class FalconHeavy(surrounding: Surrounding, crashSound: MediaPlayer, mainActivity: MainActivity, rocketPhysics: RocketPhysics, layers: Layers) : Rocket(surrounding, crashSound, rocketPhysics, layers) {
    override val traces = arrayOf<Trace>(AccelerationTrace(7, 1.01f, 0.3f, 0.01f, 0.3f, 1000, 512,
            0.004f, Color(1f, 1f, 0f, 3f), layers))
    override val rocketQuirks: RocketQuirks = RocketQuirks(2f, 0.004f, 0.003f,
            0.000002f, 0.000001f)
//    lateinit var e : EarClipPolygonalShape
    override val components: Array<ISolid> = run {
        val arrayForOverlapper = arrayOf(Vector(-0.2f, 0.1f), Vector(-0.08f, 0.3f),
                Vector(-0.08f, 0.55f), Vector(0f, 0.6f), Vector(0.08f, 0.55f),
                Vector(0.08f, 0.3f), Vector(0.2f, 0.1f),
                Vector(0.2f, -0.6f), Vector(-0.2f, -0.6f))
        val imageVertexes = arrayOf(Vector(-0.2f, 0.6f), Vector(0.2f, 0.6f), Vector(0.2f, -0.6f), Vector(-0.2f, -0.6f))
//        e = EarClipPolygonalShape(
//            arrayForOverlapper,
//            Color(0f, 1f, 0f, 0.4f),
//            BuildShapeAttr(-100f, true, layers))
        arrayOf(Image(mainActivity, R.drawable.falcon_heavy,
                imageVertexes[0], imageVertexes[1], imageVertexes[2], imageVertexes[3],
                arrayForOverlapper,
                0.5f, layers))
    }
    override val width = 0.4f

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        traces[0].generateTrace(now, previousFrameTime, ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) / 2f, RocketState(currentRotation, velocity))
    }
}