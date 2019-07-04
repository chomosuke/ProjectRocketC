package com.chomusukestudio.projectrocketc.Rocket.trace

import android.util.Log
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.Color
import com.chomusukestudio.projectrocketc.decelerateSpeedXY
import com.chomusukestudio.projectrocketc.randFloat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AccelerationTrace(val numberOfEdges: Int, val z: Float, private val initialWidth: Float, private val finalWidth: Float, private val duration: Long, private val perSecRate: Long, private val initialSpeed: Float,
						private val initialColor: Color, private val layers: Layers) : Trace() {
	
	private var preUnfinishedHalfIs = 0f
	override fun generateTraceOverride(now: Long, previousFrameTime: Long, originX: Float, originY: Float, lastOriginX: Float, lastOriginY: Float, rocketState: RocketState) {
		val dx = originX - lastOriginX
		val dy = originY - lastOriginY
		val direction = rocketState.currentRotation + PI.toFloat()
		
		val iMax = perSecRate * (now - previousFrameTime) / 1000f + preUnfinishedHalfIs
		var i = 0
		while (i < iMax) {
			
			val widthMargin = randFloat(-initialWidth/3, initialWidth/3)
			val centerX = originX - widthMargin*cos(direction)
			val centerY = originY + widthMargin*sin(direction)
			val initialSpeedX = initialSpeed * sin(direction) + rocketState.speedX
			val initialSpeedY = initialSpeed * cos(direction) + rocketState.speedY
			val newTraceShape = newAccelerationTraceShape(centerX, centerY, randFloat(initialWidth / 16, initialWidth / 4), finalWidth / 2,
					initialSpeedX, initialSpeedY, duration, initialColor)

			newTraceShape.rotateShape(centerX, centerY, (2 * Math.PI * Math.random()).toFloat())
			
			val margin = /*random();*/i / iMax/* * (0.5f + (1 * (float) random()))*/
			newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + Math.random()).toInt()) // + 0.5 for rounding
			newTraceShape.moveShape(-dx * margin, -dy * margin)
			
			i++
		}
		
	}
	
	private fun newAccelerationTraceShape(centerX: Float, centerY: Float, initialRadius: Float, finalRadius: Float, initialSpeedX: Float, initialSpeedY: Float,
										  duration: Long, initialColor: Color): RegularPolygonalTraceShape {
		val trace = AccelerationTraceShape(numberOfEdges, centerX, centerY, initialRadius, finalRadius, duration,
				initialSpeedX, initialSpeedY, 0.00004f, initialColor, BuildShapeAttr(z, true, layers))
		traceShapes.add(trace)
		return trace
	}
}

class AccelerationTraceShape(numberOfEdges: Int, centerX: Float, centerY: Float, initialRadius: Float, finalRadius: Float, duration: Long, initialSpeedX: Float, initialSpeedY: Float,
							 private val deceleration: Float, initialColor: Color, buildShapeAttr: BuildShapeAttr)
	: RegularPolygonalTraceShape(numberOfEdges, centerX, centerY, initialRadius, finalRadius, duration, initialColor, buildShapeAttr) {
	private var speedX = initialSpeedX
	private var speedY = initialSpeedY
	override fun fadeTrace(now: Long, previousFrameTime: Long) {
		super.fadeTrace(now, previousFrameTime)
		// moveTrace with speed
		val speedXY = decelerateSpeedXY(speedX, speedY, deceleration, (now - previousFrameTime))
		speedX = speedXY[0]
		speedY = speedXY[1]
		moveShape(speedX * (now - previousFrameTime), speedY * (now - previousFrameTime))
	}
}