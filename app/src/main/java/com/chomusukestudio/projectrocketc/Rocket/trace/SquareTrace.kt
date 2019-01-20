package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.Shape

class SquareTrace(private val initialWidth: Float, private val finalWidth: Float, private val duration: Long,
                  private val initialRed: Float, private val initialGreen: Float, private val initialBlue: Float, private val initialAlpha: Float, val z: Float, private val layers: Layers) : Trace() {
    override fun generateTrace(now: Long, previousFrameTime: Long, originX: Float, originY: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class SquareTraceShape(centerX: Float, centerY: Float, speedX: Float, speedY: Float, initialSize: Float, finalSize: Float,
                       private val duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float, buildShapeAttr: BuildShapeAttr): TraceShape() {
    override var componentShapes: Array<Shape> = arrayOf(QuadrilateralShape(
            centerX + initialSize / 2, centerY + initialSize / 2,
            centerX + initialSize / 2, centerY - initialSize / 2,
            centerX - initialSize / 2, centerY - initialSize / 2,
            centerX - initialSize / 2, centerY + initialSize / 2,
            initialRed, initialGreen, initialBlue, initialAlpha, buildShapeAttr))

    override fun fadeTrace(now: Long, previousFrameTime: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}