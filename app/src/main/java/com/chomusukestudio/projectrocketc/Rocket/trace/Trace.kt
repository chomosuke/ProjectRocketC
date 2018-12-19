package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.Shape.Shape

abstract class Trace {
    protected abstract val traceShapes: Collection<Shape>
    abstract fun generateTrace(now: Long, previousFrameTime: Long, originX: Float, originY: Float)
    abstract fun fadeTrace(now: Long, previousFrameTime: Long)
    open fun moveTrace(dx: Float, dy: Float) {
        for (traceShape in traceShapes)
            traceShape.moveShape(dx, dy)
    }
    open fun removeTrace() {
        for (traceShape in traceShapes)
            traceShape.removeShape()
    }
}