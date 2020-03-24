package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.SquareTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.UI.MainActivity

class Falcon9(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, layers: Layers) : Rocket(surrounding, mainActivity, rocketPhysics, layers) {
    override val traces = arrayOf<Trace>(
//            AccelerationTrace(7, 1.01f, 0.1f, 0.01f, 0.28f, 1000, 128,
//            0.004f, Color(1f, 1f, 0f, 3f), layers)
            SquareTrace(0.007f, 0.15f, 0.075f, 400, 32,
                    Color(1f, 1f, 1f, 1f), Color(1f, 0.75f, 0f, 1f),
                    1.01f, layers)
    )
    override val rocketQuirks = RocketQuirks(2f, 0.003f, 0.003f,
            0.000002f, 0.000001f)
    override val components: Array<ISolid> = run {
        val pR = arrayOf(
                Vector(53f, 20f),
                Vector(71f, 38f),
                Vector(78f, 62f),
                Vector(78f, 144f),
                Vector(71f, 157f),
                Vector(71f, 249f), // 5
                Vector(71f, 318f),
                Vector(73f, 314f),
                Vector(73f, 333f),
                Vector(60f, 333f),
                Vector(60f, 314f), // 10
                Vector(57f, 706f),
                Vector(63f, 633f),
                Vector(67f, 627f),
                Vector(73f, 631f),
                Vector(76f, 731f), // 15
                Vector(72f, 736f),
                Vector(71f, 742f),
                Vector(69f, 744f),
                Vector(71f, 754f),
                Vector(59f, 754f), // 20
                Vector(59f, 756f)
        )
        val pL = convertPointsOnRocket(pR, Vector(53f, 388f), Vector(0.0035f, 0.0018f))
        val white = Color(1f, 1f, 1f, 1f)
        val black = Color(0.25f, 0.25f, 0.25f, 1f)
        val grill = Color(0.4f, 0.4f, 0.4f, 1f)
        val build = BuildShapeAttr(0.5f, true, layers)
        val buildF = BuildShapeAttr(0.49f, true, layers)
        return@run arrayOf(
                CircleSegmentShape(pR[0], pR[2], pR[1], white, build),
                CircleSegmentShape(pL[2], pL[0], pL[1], white, build),
                PolygonalShape(arrayOf(pR[0], pR[2], pR[3], pR[4], pR[5], pL[5], pL[4], pL[3], pL[2]), white, build),
                QuadrilateralShape(pR[5], pR[6], pL[6], pL[5], black, build),
                QuadrilateralShape(pR[7], pR[8], pR[9], pR[10], grill, buildF),
                QuadrilateralShape(pL[7], pL[8], pL[9], pL[10], grill, buildF),
                QuadrilateralShape(pR[6], pR[17], pL[17], pL[6], white, build),
                EarClipPolygonalShape(arrayOf(pL[11], pL[12], pL[13], pL[14], pL[15], pL[16], pL[17], pL[18], pL[19], pL[20], pL[21],
                        pR[21], pR[20], pR[19], pR[18], pR[17], pR[16], pR[15], pR[14], pR[13], pR[12], pR[11]), black, buildF)
        )
    }
    override val width = 0.5f

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        traces[0].generateTrace(now, previousFrameTime, ((components.last() as EarClipPolygonalShape).getVertex(8) + (components.last() as EarClipPolygonalShape).getVertex(15)) * 0.5f, RocketState(currentRotation, velocity))
    }
}