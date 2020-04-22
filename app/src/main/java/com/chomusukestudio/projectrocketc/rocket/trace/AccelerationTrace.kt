package com.chomusukestudio.projectrocketc.rocket.trace

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.randFloat
import com.chomusukestudio.prcandroid2dgameengine.shape.BuildShapeAttr
import com.chomusukestudio.prcandroid2dgameengine.shape.Color
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.projectrocketc.rocket.RocketState
import com.chomusukestudio.projectrocketc.decelerateVelocity
import kotlin.math.PI

class AccelerationTrace(private val width: Float, private val initialBubbleSize: Float, private val finalBubbleSize: Float, private val duration: Long, private val perSecRate: Long,
						private val initialSpeed: Float, private val initialColor: Color, val z: Float,
						private val drawData: DrawData) : Trace() {
	
	private var preUnfinishedHalfIs = 0f
	var randomization = 0f
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
			
			val margin = /*random();*/(i + randomization * randFloat(-0.5f, 0.5f)) / iMax /* * (0.5f + (1 * (float) random()))*/
			newTraceShape.fadeTrace(now,  now - (margin * (now - previousFrameTime) + Math.random()).toInt()) // + 0.5 for rounding
			newTraceShape.move(-dOrigin * margin)
			
			i++
		}
		preUnfinishedHalfIs = iMax - i
	}
	
	private fun newAccelerationTraceShape(center: Vector, initialRadius: Float, finalRadius: Float, initialSpeed: Vector,
										  duration: Long, initialColor: Color): CircularTraceShape {
		val trace = AccelerationTraceShape(center, initialRadius, finalRadius, duration,
				initialSpeed, 0.00002f, initialColor, BuildShapeAttr(z, true, drawData))
		traceShapes.add(trace)
		return trace
	}
}

class AccelerationTraceShape(center: Vector, initialRadius: Float, finalRadius: Float, duration: Long, initialVelocity: Vector,
							 private val deceleration: Float, initialColor: Color, buildShapeAttr: BuildShapeAttr)
	: CircularTraceShape(center, initialRadius, finalRadius, duration, initialColor, buildShapeAttr) {
	private var velocity = initialVelocity
	override fun fadeTrace(now: Long, previousFrameTime: Long) {
		super.fadeTrace(now, previousFrameTime)
		// moveTrace with velocity
		velocity = decelerateVelocity(velocity, deceleration, (now - previousFrameTime))
		move(velocity * (now - previousFrameTime).toFloat())
	}
}