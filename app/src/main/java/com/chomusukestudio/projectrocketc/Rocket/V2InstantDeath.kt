package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import com.chomusukestudio.projectrocketc.GLRenderer.AllLayers
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.AccelerationTrace
import com.chomusukestudio.projectrocketc.Shape.*

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding

/**
 * Created by Shuang Li on 11/03/2018.
 */

class V2InstantDeath(surrounding: Surrounding, crashSound: MediaPlayer, rocketPhysics: RocketPhysics, val allLayers: AllLayers)
	: Rocket(surrounding, crashSound, rocketPhysics, allLayers) {
	override val trace = //RegularPolygonalTrace(7, 1.01f, 0.24f,  0.4f, 2000, 1f, 1f, 0f, 1f, layers)
//        SquareTrace(0.24f,  0.4f, 2000, 1f, 1f, 0f, 1f,1.01f, layers)
			AccelerationTrace(7, 1.01f, 0.14f, 0.5f, 1000, 100,
					0.004f, Color(1f, 1f, 0f, 3f), allLayers)
	
	override fun generateTrace(now: Long, previousFrameTime: Long) {
		val p1 = (components[9] as QuadrilateralShape).vertex2
		val p2 = (components[10] as QuadrilateralShape).vertex2
		val origin = (p1 + p2) * 0.5f
		trace.generateTrace(now, previousFrameTime, origin, RocketState(currentRotation, velocity))
	}
	
	override val rocketQuirks = RocketQuirks(2f, 0.004f, 0.003f,
			0.000002f, 0.000001f)
	
	override val width = 0.3f
	
	override val components: Array<Shape> = run {
		val white = Color(1f, 1f, 1f, 1f)
		val black = Color(0.3f, 0.3f, 0.3f, 1f)
		
		// when the rocket is created it's pointed towards right which is angle 0
		val scaleX = 0.8f;
		val scaleY = 0.6f
		val p1 = Vector(0.65f * scaleX, 0f)
		val p2 = Vector(0.35f * scaleX, 0.125f * scaleY)
		val p3 = Vector(0f, 0.155f * scaleY)
		val p4 = Vector(-0.3f * scaleX, 0.145f * scaleY)
		val p5 = Vector(-0.415f * scaleX, 0.3f * scaleY)
		val p6 = Vector(-0.67f * scaleX, 0.32f * scaleY)
		val p7 = Vector(-0.67f * scaleX, 0.215f * scaleY)
		val p8 = Vector(-0.64f * scaleX, 0.145f * scaleY)
		val p9 = Vector(-0.64f * scaleX, 0.085f * scaleY)
		val p10 = Vector(-0.6f * scaleX, 0.085f * scaleY)
		val p11 = Vector(-0.6f * scaleX, 0.055f * scaleY)
		val p12 = Vector(-0.64f * scaleX, 0.07f * scaleY)
		val p13 = Vector(-0.64f * scaleX, 0.015f * scaleY)
		val p14 = Vector(-0.6f * scaleX, 0.032f * scaleY)
		
		val buildShapeAttr = BuildShapeAttr(1f, true, allLayers.shapeLayers)
		return@run arrayOf(
				// defined components of rocket around centerOfRotation set by surrounding
				// 0
				TriangularShape(p1, p2, p2.mirrorXAxis(),
						black, buildShapeAttr),
				// 1
				QuadrilateralShape(p2, Vector(p2.x, 0f), Vector(p3.x, 0f), p3,
						white, buildShapeAttr),
				// 2
				QuadrilateralShape(p2.mirrorXAxis(), Vector(p2.x, 0f), Vector(p3.x, 0f), p3.mirrorXAxis(),
						black, buildShapeAttr),
				// 3
				QuadrilateralShape(p4, Vector(p4.x, 0f), Vector(0f, 0f), p3,
						black, buildShapeAttr),
				// 4
				QuadrilateralShape(p4.mirrorXAxis(), Vector(p4.x, 0f), Vector(0f, 0f), p3.mirrorXAxis(),
						white, buildShapeAttr),
				// 5
				QuadrilateralShape(p10, Vector(p10.x, 0f), Vector(p4.x, 0f), p4,
						white, buildShapeAttr),
				// 6
				QuadrilateralShape(p10.mirrorXAxis(), Vector(p10.x, 0f), Vector(p4.x, 0f), p4.mirrorXAxis(),
						black, BuildShapeAttr(1f, true, allLayers.shapeLayers)),
				// 7
				PolygonalShape(arrayOf(p4, p5, p6, p7, p8, p9, p10/*, Vector(p10.x, 0f), Vector(p4.x, 0f)*/),
						black, buildShapeAttr),
				// 8
				PolygonalShape(arrayOf(p4.mirrorXAxis(), p5.mirrorXAxis(), p6.mirrorXAxis(), p7.mirrorXAxis(),
						p8.mirrorXAxis(), p9.mirrorXAxis(), p10.mirrorXAxis()/*, Vector(p10.x, 0f), Vector(p4.x, 0f)*/),
						white, buildShapeAttr),
				// 9
				QuadrilateralShape(p11, p12, p13, p14, black, buildShapeAttr),
				// 10
				QuadrilateralShape(p11.mirrorXAxis(), p12.mirrorXAxis(), p13.mirrorXAxis(), p14.mirrorXAxis(),
						black, buildShapeAttr)
		)
	}
	
	override val crashOverlappers: Array<Overlapper>
		get() = arrayOf(PointOverlapper((components[0] as TriangularShape).vertex1),
				PointOverlapper((components[0] as TriangularShape).vertex2),
				PointOverlapper((components[0] as TriangularShape).vertex3),
				PointOverlapper((components[3] as QuadrilateralShape).vertex4),
				PointOverlapper((components[4] as QuadrilateralShape).vertex4),
				PointOverlapper((components[7] as PolygonalShape).getVertex(1)),
				PointOverlapper((components[8] as PolygonalShape).getVertex(1)),
				PointOverlapper((components[7] as PolygonalShape).getVertex(2)),
				PointOverlapper((components[8] as PolygonalShape).getVertex(2)))
	
	private val rf = 0.006f
	private val repulsiveForces = arrayOf(Vector(-rf, 0f), Vector(0f, -rf),
			Vector(0f, rf), Vector(0f, -rf), Vector(0f, rf), Vector(0f, -rf),
			Vector(0f, rf), Vector(0f, -rf), Vector(0f, rf))
	
	// initialize for surrounding to set centerOfRotation
	init {
		setRotation(surrounding.centerOfRotation, surrounding.rotation)
	}
}