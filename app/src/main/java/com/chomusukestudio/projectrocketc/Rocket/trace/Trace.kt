package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI

abstract class Trace {
    protected open val traceShapes = ArrayList<TraceShape>()
    
    private var generateTraceCalledThisFrame = true
    private var lastOrigin: Vector? = null
    fun generateTrace(now: Long, previousFrameTime: Long, origin: Vector, rocketState: RocketState) {
        if (lastOrigin != null)
            generateTraceOverride(now, previousFrameTime, origin, lastOrigin!!, rocketState)
        this.lastOrigin = origin
        generateTraceCalledThisFrame = true
    }
    
    protected abstract fun generateTraceOverride(now: Long, previousFrameTime: Long, origin: Vector, lastOrigin: Vector, rocketState: RocketState)

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
            traceShapes[i].fadeTrace(now, /*nowXY - ((nowXY - */previousFrameTime/*) * refreshFactor)*/)
        }, traceShapes.size)
    }

    fun moveTrace(vector: Vector) {
        parallelForIForMoveTraces.run({ i ->
            traceShapes[i].moveShape(vector)
        }, traceShapes.size)
        lastOrigin = lastOrigin?.plus(vector) // what elegancy lol
        if (generateTraceCalledThisFrame)
            generateTraceCalledThisFrame = false // initialize for next frame
        else {
            lastOrigin = null
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