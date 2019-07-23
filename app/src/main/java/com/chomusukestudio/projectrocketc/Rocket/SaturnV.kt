package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import kotlin.math.PI

class SaturnV(surrounding: Surrounding, rocketPhysics: RocketPhysics, layers: Layers): Rocket(surrounding, rocketPhysics, layers) {
	override val trace: Trace
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val rocketQuirks: RocketQuirks
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val components = run {
		val pR = arrayOf(Vector(305f, 92f), Vector(308f, 99f), Vector(308f, 128f),
				Vector(312f, 134f), Vector(309f, 134f), Vector(309f, 152f),
				Vector(315f, 164f), Vector(315f, 189f), Vector(326f, 237f),
				Vector(326f, 248f), Vector(326f, 316f), Vector(338f, 351f),
				Vector(338f, 462f), Vector(338f, 519f), Vector(338f, 596f),
				Vector(338f, 622f), Vector(338f, 672f), Vector(342f, 684f),
				Vector(367f, 696f), Vector(367f, 708f), Vector(348f, 708f),
				Vector(350f, 713f), Vector(337f, 713f), Vector(344f, 726f),
				Vector(346f, 737f), Vector(324f, 737f), Vector(325f, 710f),
				Vector(318f, 710f), Vector(309f, 714f), Vector(318f, 737f))
		for (i in pR.indices)
			pR[i] = pR[i].offset(-305f, -322.5f) // bring it to center
					.rotateVector(PI.toFloat()/2) * 0.001f // point to right which is rotation 0
		val pL = Array(pR.size) { pR[it].mirrorXAxis() }
		TODO("not implemented")
	}
	override val width: Float
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	
	override fun generateTrace(now: Long, previousFrameTime: Long) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
}