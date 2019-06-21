package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI

abstract class Trace {
    protected open val traceShapes = ArrayList<TraceShape>()
    
    private var generateTraceCalledThisFrame = true
    private var lastOriginX: Float? = null
    private var lastOriginY: Float? = null
    fun generateTrace(now: Long, previousFrameTime: Long, originX: Float, originY: Float, direction: Float) {
        if (lastOriginX != null && lastOriginY != null)
            generateTraceOverride(now, previousFrameTime, originX, originY, lastOriginX!!, lastOriginY!!, direction)
        this.lastOriginX = originX
        this.lastOriginY = originY
        generateTraceCalledThisFrame = true
    }
    
    protected abstract fun generateTraceOverride(now: Long, previousFrameTime: Long, originX: Float, originY: Float, lastOriginX: Float, lastOriginY: Float, direction: Float)

    private val parallelForIForFadeTraces = ParallelForI(8, "fade traceShapes thread")
    private val parallelForIForMoveTraces = ParallelForI(8, "move traceShapes thread")
    open fun fadeTrace(now: Long, previousFrameTime: Long) {
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
        lastOriginX = lastOriginX?.plus(dx)
        lastOriginY = lastOriginY?.plus(dy) // what elegancy lol
        if (generateTraceCalledThisFrame)
            generateTraceCalledThisFrame = false // initialize for next frame
        else {
            lastOriginX = null
            lastOriginY = null
        }
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