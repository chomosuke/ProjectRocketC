package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import kotlin.math.PI

class SaturnV(surrounding: Surrounding, rocketPhysics: RocketPhysics, layers: Layers): Rocket(surrounding, rocketPhysics, layers) {
	override val trace = AccelerationTrace(7, 1.01f, 0.2f, 0.5f, 1000, 100,
					0.004f, Color(1f, 1f, 0f, 3f), layers)
	override val rocketQuirks = RocketQuirks(2f, 0.004f, 0.003f,
				0.000002f, 0.000001f)
	override val components = run { // refer to rockets' points/SaturnV.PNG
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
		for (i in pR.indices) {
            pR[i] = pR[i].offset(-305f, -414.5f) // bring it to center
					.scaleXY(Vector(1.3f, 1f))
					.rotateVector(PI.toFloat() / 2) // point to right which is rotation 0
			pR[i] = pR[i] * 0.002f
        }
		val pL = Array(pR.size) { pR[it].mirrorXAxis() }
		val white = Color(1f, 1f, 1f, 1f)
		val black = Color(0.3f, 0.3f, 0.3f, 1f)
		val build = BuildShapeAttr(0.5f, true, layers)
		return@run arrayOf(TriangularShape(pR[0], pR[1], pL[1], white, build),
				QuadrilateralShape(pR[1], pR[2], pL[2], pL[1], white, build),
				QuadrilateralShape(pR[2], pR[3], pL[3], pL[2], white, build),
				QuadrilateralShape(pR[4], pR[5], pL[5], pL[4], black, build),
				QuadrilateralShape(pR[5], pR[6], pL[6], pL[5], white, build),
				QuadrilateralShape(pR[6], pR[7], pL[7], pL[6], white, build),
				QuadrilateralShape(pR[7], pR[8], pL[8], pL[7], white, build),
				QuadrilateralShape(pR[8], pR[9], pL[9], pL[8], black, build),
				QuadrilateralShape(pR[9], pR[10], pL[10], pL[9], white, build),
				QuadrilateralShape(pR[10], pR[40], pL[40], pL[10], black, build),
				QuadrilateralShape(pR[40], pR[11], pR[38], pR[39], white, build),
				QuadrilateralShape(pL[40], pL[11], pL[38], pL[39], black, build),
				QuadrilateralShape(pR[11], pR[12], pL[12], pL[11], white, build),
				QuadrilateralShape(pR[12], pR[13], pL[13], pL[12], black, build),
				QuadrilateralShape(pR[13], pR[14], pR[34], pR[37], black, build),
				QuadrilateralShape(pR[34], pR[35], pR[36], pR[37], white, build),
				QuadrilateralShape(pL[13], pL[14], pL[34], pL[37], white, build),
				QuadrilateralShape(pL[34], pL[35], pL[36], pL[37], black, build),
				QuadrilateralShape(pR[14], pR[16], pL[16], pL[14], white, build),
				PolygonalShape(arrayOf(pR[33], pR[16], pR[17], pR[18], pR[21], pR[22], pR[27]), black, build),
				PolygonalShape(arrayOf(pR[33], pL[16], pL[17], pL[18], pL[21], pL[22], pL[27]), white, build),
				QuadrilateralShape(pR[33], pR[27], pR[31], pR[32], white, build),
				QuadrilateralShape(pL[33], pL[27], pL[31], pL[32], black, build),
				QuadrilateralShape(pR[18], pR[19], pR[20], pR[21], white, build),
				QuadrilateralShape(pL[18], pL[19], pL[20], pL[21], white, build),
				PolygonalShape(arrayOf(pR[23], pR[24], pR[25], pR[26], pR[27]), black, build),
				PolygonalShape(arrayOf(pL[23], pL[24], pL[25], pL[26], pL[27]), black, build),
				EarClipPolygonalShape(arrayOf(pL[28], pL[29], pL[30], pR[30], pR[29], pR[28]), black, build)
				)
	}
	override val width = 0.5f

	init {
		setRotation(surrounding.centerOfRotation, surrounding.rotation)
	}
	
	override fun generateTrace(now: Long, previousFrameTime: Long) {
		val origin = ((components.last() as EarClipPolygonalShape).getVertex(2) +
				(components.last() as EarClipPolygonalShape).getVertex(3)) * 0.5f
		trace.generateTrace(now, previousFrameTime, origin, RocketState(currentRotation, velocity))
	}
	
}