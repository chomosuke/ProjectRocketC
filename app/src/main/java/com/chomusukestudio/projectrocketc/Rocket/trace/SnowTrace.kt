package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.decelerateSpeedXY
import com.chomusukestudio.projectrocketc.randFloat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SnowTrace(val numberOfEdges: Int, val z: Float, private val initialWidth: Float, private val finalWidth: Float, private val duration: Long, private val perSecRate: Long, private val initialSpeed: Float,
						private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float, private val layers: Layers) : Trace() {
	
	private var preUnfinishedHalfIs = 0f
	override fun generateTraceOverride(now: Long, previousFrameTime: Long, originX: Float, originY: Float, lastOriginX: Float, lastOriginY: Float, direction: Float) {
		val dx = originX - lastOriginX
		val dy = originY - lastOriginY
		
		val iMax = perSecRate * (now - previousFrameTime) / 1000f + preUnfinishedHalfIs
		var i = 0
		while (i < iMax) {
			
			val widthMargin = randFloat(-initialWidth/0.1f, initialWidth/0.1f)
			val newTraceShape = newAccelerationTraceShape(originX + widthMargin*cos(direction), originY + widthMargin*sin(direction),
					randFloat(initialWidth / 16, initialWidth / 4), finalWidth / 2, direction, duration, initialRed, initialGreen, initialBlue, initialAlpha)
			newTraceShape.rotateShape(originX, originY, (2 * Math.PI * Math.random()).toFloat())
			
			val margin = /*random();*/i / iMax/* * (0.5f + (1 * (float) random()))*/
			newTraceShape.fadeTrace(now, previousFrameTime + ((1 - margin) * (now - previousFrameTime) + Math.random()).toInt()) // + 0.5 for rounding
			newTraceShape.moveShape(-dx * margin, -dy * margin)
			
			i++
		}
		
	}
	
	private fun newAccelerationTraceShape(centerX: Float, centerY: Float, initialRadius: Float, finalRadius: Float, direction: Float,
										  duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float): RegularPolygonalTraceShape {
		val trace = AccelerationTraceShape(numberOfEdges, centerX, centerY, initialRadius, finalRadius,
				duration, initialSpeed * sin(direction), initialSpeed * cos(direction), 0.00004f,
				initialRed, initialGreen, initialBlue, initialAlpha, BuildShapeAttr(z, true, layers))
		traceShapes.add(trace)
		return trace
	}
}
