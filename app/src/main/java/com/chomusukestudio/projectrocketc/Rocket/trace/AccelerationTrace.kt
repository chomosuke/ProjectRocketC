package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.Color
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.decelerateVelocity
import com.chomusukestudio.projectrocketc.randFloat
import kotlin.math.PI

class AccelerationTrace(private val numberOfEdges: Int, val z: Float, private val width: Float, private val initialBubbleSize: Float, private val finalBubbleSize: Float,
						private val duration: Long, private val perSecRate: Long, private val initialSpeed: Float,
						private val initialColor: Color, private val layers: Layers) : Trace() {
	
	private var preUnfinishedHalfIs = 0f
	override fun generateTraceOverride(now: Long, previousFrameTime: Long, origin: Vector, lastOrigin: Vector, rocketState: RocketState) {
		val dOrigin = origin - lastOrigin
		val direction = rocketState.currentRotation + PI.toFloat()
		
		val iMax = perSecRate * (now - previousFrameTime) / 1000f + preUnfinishedHalfIs
		var i = 0
		while (i < iMax) {
			
			val widthMargin = randFloat(-width/2, width/2)
			val center = origin + Vector(0f, widthMargin).rotateVector(direction)
			val initialVelocity = Vector(initialSpeed, 0f).rotateVector(direction) + rocketState.velocity
			val newTraceShape = newAccelerationTraceShape(center, randFloat(initialBubbleSize/2, initialBubbleSize*2), finalBubbleSize,
					initialVelocity, duration, initialColor)

			newTraceShape.rotate(center, (2 * Math.PI * Math.random()).toFloat())
			
			val margin = /*random();*/i / iMax/* * (0.5f + (1 * (float) random()))*/
			newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + Math.random()).toInt()) // + 0.5 for rounding
			newTraceShape.move(-dOrigin * margin)
			
			i++
		}
		
	}
	
	private fun newAccelerationTraceShape(center: Vector, initialRadius: Float, finalRadius: Float, initialSpeed: Vector,
										  duration: Long, initialColor: Color): RegularPolygonalTraceShape {
		val trace = AccelerationTraceShape(numberOfEdges, center, initialRadius, finalRadius, duration,
				initialSpeed, 0.00002f, initialColor, BuildShapeAttr(z, true, layers))
		traceShapes.add(trace)
		return trace
	}
}

class AccelerationTraceShape(numberOfEdges: Int, center: Vector, initialRadius: Float, finalRadius: Float, duration: Long, initialVelocity: Vector,
							 private val deceleration: Float, initialColor: Color, buildShapeAttr: BuildShapeAttr)
	: RegularPolygonalTraceShape(numberOfEdges, center, initialRadius, finalRadius, duration, initialColor, buildShapeAttr) {
	private var velocity = initialVelocity
	override fun fadeTrace(now: Long, previousFrameTime: Long) {
		super.fadeTrace(now, previousFrameTime)
		// moveTrace with velocity
		velocity = decelerateVelocity(velocity, deceleration, (now - previousFrameTime))
		move(velocity * (now - previousFrameTime).toFloat())
	}
}