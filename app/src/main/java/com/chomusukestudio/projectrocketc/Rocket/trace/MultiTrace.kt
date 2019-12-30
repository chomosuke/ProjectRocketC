package com.chomusukestudio.projectrocketc.Rocket.trace

import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Shape.Vector

class MultiTrace(private val size: Int, getTrace: (Int) -> Trace, private val width: Float) : Trace() {
    override val traceShapes: ArrayList<TraceShape>
        get() = throw IllegalAccessException()

    private val traces = Array(size, getTrace)
    override fun generateTraceOverride(now: Long, previousFrameTime: Long, origin: Vector, lastOrigin: Vector, rocketState: RocketState) {
        val horizontalUnitVector = Vector(0f, 1f).rotateVector(rocketState.currentRotation)
        for (i in traces.indices) {
            val horizontalDisplacement = horizontalUnitVector * (width * (i + 0.5f) / size - width / 2)
            traces[i].generateTrace(now, previousFrameTime, origin + horizontalDisplacement, rocketState)
        }
    }

    override fun fadeTrace(now: Long, previousFrameTime: Long) {
        for (trace in traces)
            trace.fadeTrace(now, previousFrameTime)
    }

    override fun moveTrace(vector: Vector) {
        for (trace in traces)
            trace.moveTrace(vector)
    }

    override fun removeTrace() {
        for (trace in traces)
            trace.removeTrace()
    }
}