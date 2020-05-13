package com.chomusukestudio.projectrocketc.rocket.trace

import com.chomusukestudio.prcandroid2dgameengine.runWithExceptionChecked
import com.chomusukestudio.prcandroid2dgameengine.shape.Shape
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.prcandroid2dgameengine.threadClasses.ParallelForI
import com.chomusukestudio.projectrocketc.rocket.RocketState

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
                traceShapes[i].remove()
                traceShapes.removeAt(i)
            } else {
                i++
            }
        }

        parallelForIForFadeTraces.run({ i ->
            runWithExceptionChecked {
                traceShapes[i].fadeTrace(now, /*nowXY - ((nowXY - */previousFrameTime/*) * refreshFactor)*/)
            }
        }, traceShapes.size)
    }

    open fun moveTrace(vector: Vector) {
        parallelForIForMoveTraces.run({ i ->
            traceShapes[i].move(vector)
        }, traceShapes.size)
        lastOrigin = lastOrigin?.plus(vector) // what elegancy lol
        if (generateTraceCalledThisFrame)
            generateTraceCalledThisFrame = false // initialize for next frame
        else {
            lastOrigin = null
        }
    }

    open fun removeTrace() {
        parallelForIForFadeTraces.waitForLastRun()
        parallelForIForMoveTraces.waitForLastRun()
        // to prevent removing while still fading trace which will cause some trace not being removed

        for (traceShape in traceShapes)
            traceShape.remove()
    }
}



abstract class TraceShape: Shape() {
    
    var needToBeRemoved = false
        protected set

    abstract fun fadeTrace(now: Long, previousFrameTime: Long)
}