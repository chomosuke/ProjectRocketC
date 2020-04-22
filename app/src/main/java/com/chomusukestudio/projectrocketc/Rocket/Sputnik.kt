package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.prcandroid2dgameengine.Image
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.*
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.MultiTrace
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.UI.MainActivity

class Sputnik(surrounding: Surrounding, mainActivity: MainActivity, rocketPhysics: RocketPhysics, drawData: DrawData)
	: Rocket(surrounding, mainActivity, rocketPhysics, drawData) {
	override val traces = arrayOf<Trace>(MultiTrace(3, {
		if (it == 1)
			AccelerationTrace(0.08f, 0.01f, 0.15f, 1200, 320, 0.004f, Color(1f, 1f, 0f, 3f),
					1.01f, drawData)
		else
			AccelerationTrace(0.08f, 0.01f, 0.15f, 800, 256, 0.004f, Color(1f, 1f, 0f, 3f),
					1.01f, drawData)
	}, 0.37f))
	override val rocketQuirks: RocketQuirks = RocketQuirks("Sputnik", 2f, 0.003f, 0.003f,
			0.000002f, 0.000001f, 5000, 0.5f, 1, 1)
	override val components: Array<ISolid> = run {
		
		val scaleX = 0.7f
		val scaleY = 0.58f
		val overlapperVertexes = arrayOf(Vector(0.65f * scaleX, 0f),
				Vector(0.5f * scaleX, -0.085f * scaleY),
				Vector(0.2f * scaleX, -0.085f * scaleY),
				Vector(0f * scaleX, -0.16f * scaleY),
				Vector(-0.58f * scaleX, -0.25f * scaleY),
				Vector(-0.61f * scaleX, -0.32f * scaleY),
				Vector(-0.67f * scaleX, -0.25f * scaleY),
				Vector(-0.67f * scaleX, 0.25f * scaleY),
				Vector(-0.61f * scaleX, 0.32f * scaleY),
				Vector(-0.58f * scaleX, 0.25f * scaleY),
				Vector(0f * scaleX, 0.16f * scaleY),
				Vector(0.2f * scaleX, 0.085f * scaleY),
				Vector(0.5f * scaleX, 0.085f * scaleY)
		)
		val imageVertexes = arrayOf(
				Vector(0.65f * scaleX, 0.32f * scaleY),
				Vector(0.65f * scaleX, -0.32f * scaleY),
				Vector(-0.67f * scaleX, -0.32f * scaleY),
				Vector(-0.67f * scaleX, 0.32f * scaleY))
		val image = Image(R.drawable.sputnik, imageVertexes[0], imageVertexes[1], imageVertexes[2], imageVertexes[3],
				overlapperVertexes, false, 0.5f, drawData)
		arrayOf(image)
	}
	override val width = 0.37f
//	val widthSquare = QuadrilateralShape(Vector(width / 2, 1f), Vector(-width / 2, 1f),
//			Vector(-width / 2, -1f), Vector(width / 2, -1f), Color(0f, 1f, 0f, 0.2f), BuildShapeAttr(10f, true, drawData))
	
	
	override fun generateTrace(now: Long, previousFrameTime: Long) {
		val origin = ((components[0] as Image).vertex3 + (components[0] as Image).vertex4) / 2f
		traces[0].generateTrace(now, previousFrameTime, origin, RocketState(currentRotation, velocity))
	}
}