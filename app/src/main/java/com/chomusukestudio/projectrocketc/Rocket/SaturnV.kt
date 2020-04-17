package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.prcandroid2dgameengine.Image
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.Color
import com.chomusukestudio.prcandroid2dgameengine.shape.ISolid
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.MultiTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.UI.MainActivity

class SaturnV(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, drawData: DrawData): Rocket(surrounding, mainActivity, rocketPhysics, drawData) {
	override val traces: Array<Trace> = arrayOf(
			MultiTrace(3,
					{
						val t = AccelerationTrace(0.05f, 0.015f, 0.2f, 750, 150, 0.004f, Color(1f, 0.9f, 0f, 3f),
                                1.01f, drawData)
						t.randomization = 1f
						return@MultiTrace t
					},
			0.25f))
	override val rocketQuirks = RocketQuirks("Saturn V", 2f, 0.003f, 0.003f,
				0.000002f, 0.000001f, 5000)
	override val components: Array<ISolid> = run { // refer to rockets' points/SaturnV.PNG
		val pR = arrayOf(Vector(305f, 92f), Vector(308f, 99f), Vector(308f, 128f), // 2
				Vector(312f, 134f), Vector(309f, 134f), Vector(309f, 152f), // 5
				Vector(315f, 164f), Vector(315f, 189f), Vector(326f, 237f), // 8
				Vector(326f, 248f), Vector(326f, 316f), Vector(338f, 351f), // 11
				Vector(338f, 462f), Vector(338f, 473f), Vector(338f, 519f), // 14
				Vector(338f, 596f), Vector(338f, 622f), Vector(338f, 672f), // 17
				Vector(342f, 684f), Vector(367f, 696f), Vector(367f, 708f), // 20
				Vector(348f, 708f), Vector(350f, 713f), Vector(337f, 713f), // 23
				Vector(344f, 726f), Vector(346f, 737f), Vector(324f, 737f), // 26
				Vector(325f, 710f), Vector(318f, 710f), Vector(312f, 714f), // 29
				Vector(318f, 737f), Vector(305f, 710f), Vector(305f, 622f), // 32
				Vector(324f, 622f), Vector(324f, 519f), Vector(305f, 519f), // 35
				Vector(305f, 473f), Vector(324f, 473f), Vector(305f, 351f), // 38
				Vector(305f, 334f), Vector(332f, 334f))
		val scale = Vector(1.3f, 1f)
		val pL = convertPointsOnRocket(pR, Vector(305f, 414.5f), scale * 0.002f)
		val overlapperVertexes = arrayOf(pL[0], pL[1], pL[2], pL[3], pL[4], pL[5], pL[6], pL[7], pL[8], pL[10], pL[11], pL[17], pL[18], pL[19], pL[20], pL[23], pL[25],
										pR[25], pR[23], pR[20], pR[19], pR[18], pR[17], pR[11], pR[10], pR[8], pR[7], pR[6], pR[5], pR[4], pR[3], pR[2], pR[1])
		val x = 0.5f; val y = 0.7f
		val imageVertexes = arrayOf(Vector(x, y), Vector(x, -y), Vector(-x, -y), Vector(-x, y))
		for (i in imageVertexes.indices)
			imageVertexes[i] *= scale

		arrayOf(Image(R.drawable.saturn_v,
				imageVertexes[0], imageVertexes[1], imageVertexes[2], imageVertexes[3],
				overlapperVertexes, false,
				0.5f, drawData))
	}
	override val width = 0.5f
	
	override fun generateTrace(now: Long, previousFrameTime: Long) {
		val origin = ((components[0] as Image).vertex3 +
				(components[0] as Image).vertex4) * 0.5f
		traces[0].generateTrace(now, previousFrameTime, origin, RocketState(currentRotation, velocity))
	}
}