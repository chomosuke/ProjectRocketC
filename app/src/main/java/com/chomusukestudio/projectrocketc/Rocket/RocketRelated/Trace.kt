package com.chomusukestudio.projectrocketc.Rocket.RocketRelated

import android.util.Log
import com.chomusukestudio.projectrocketc.Shape.RegularPolygonalShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import java.lang.Math.pow
import java.util.ArrayList

abstract class Trace {
    private val traceShapes = ArrayList<Shape>()
    abstract fun refreshTrace(now: Long, previousFrameTime: Long, originX: Float, originY: Float)
    abstract fun moveTrace(dx: Float, dy: Float)
    open fun removeTrace() {
        for (traceShape in traceShapes)
            traceShape.removeShape()
    }
}

