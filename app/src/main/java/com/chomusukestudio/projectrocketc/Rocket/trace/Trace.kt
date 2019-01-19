package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI

abstract class Trace {
    protected open val traceShapes = ArrayList<TraceShape>()
    protected var lastOriginX = 0f
    protected var lastOriginY = 0f
    abstract fun generateTrace(now: Long, previousFrameTime: Long, originX: Float, originY: Float)

    private val parallelForIForFadeTraces = ParallelForI(8, "fade traceShapes thread")
    private val parallelForIForMoveTraces = ParallelForI(8, "move traceShapes thread")
    fun fadeTrace(now: Long, previousFrameTime: Long) {
        parallelForIForFadeTraces.waitForLastRun()
        parallelForIForMoveTraces.waitForLastRun()
        // to avoid concurrent modification

        var i = 0
        while (i < traceShapes.size) {
            if (traceShapes[i].needToBeRemoved) {
                traceShapes[i].removeShape()
                traceShapes.removeAt(i)
            } else {
                i++
            }
        }

        parallelForIForFadeTraces.run({ i ->
            traceShapes[i].fadeTrace(now, /*now - ((now - */previousFrameTime/*) * refreshFactor)*/)
        }, traceShapes.size)
    }

    fun moveTrace(dx: Float, dy: Float) {
        parallelForIForMoveTraces.run({ i ->
            traceShapes[i].moveShape(dx, dy)
        }, traceShapes.size)
        lastOriginX += dx
        lastOriginY += dy
    }

    open fun removeTrace() {
        for (traceShape in traceShapes)
            traceShape.removeShape()
    }
}



abstract class TraceShape: Shape() {
    override val isOverlapMethodLevel: Double
        get() = throw IllegalAccessException("trace can't overlap anything")

    var needToBeRemoved = false
        protected set

    abstract fun fadeTrace(now: Long, previousFrameTime: Long)
}