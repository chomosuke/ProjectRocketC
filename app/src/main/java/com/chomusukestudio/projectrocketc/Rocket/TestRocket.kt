package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.TraceShape.RegularPolygonalTraceShape
import com.chomusukestudio.projectrocketc.Shape.TriangularShape

import java.util.ArrayList

import com.chomusukestudio.projectrocketc.Shape.point.distance
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import java.lang.Math.cos
import java.lang.Math.random
import java.lang.Math.sin

/**
 * Created by Shuang Li on 11/03/2018.
 */

class TestRocket(surrounding: Surrounding) : Rocket(surrounding) {
    override var radiusOfRotation = 2f
    override val initialSpeed = 4f / 1000f
    override var speed = initialSpeed

    private var unfilledDs = 0f
    
    private val parallelForIForTraces = ParallelForI(8, "traces thread")
    
    override val width = 0.3f
    
    
    // initialize for surrounding to set centerOfRotation
    override val components: Array<Shape> = Array(4) { i ->
        when (i) {
        // defined components of rocket around centerOfRotation set by surrounding
            0 ->
                TriangularShape(centerOfRotationX, centerOfRotationY + 0.5f,
                        centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f,
                        1f, 1f, 1f, 1f, 1f)
            1 ->
                QuadrilateralShape(centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f, centerOfRotationX - 0.15f, centerOfRotationY - 0.3f,
                        centerOfRotationX + 0.15f, centerOfRotationY - 0.3f, 1f, 1f, 1f, 1f, 1f)
            2 ->
                CircularShape(centerOfRotationX, centerOfRotationY /*+ 0.38*/, 0.07f,
                        0.1f, 0.1f, 0.1f, 1f, 0.9999f)
            3 ->
                QuadrilateralShape(centerOfRotationX + 0.1f, centerOfRotationY - 0.3f,
                        centerOfRotationX - 0.1f, centerOfRotationY - 0.3f, centerOfRotationX - 0.12f, centerOfRotationY - 0.4f,
                        centerOfRotationX + 0.12f, centerOfRotationY - 0.4f, 1f, 1f, 1f, 1f, 1f)
            else -> {
                throw IndexOutOfBoundsException()
            }
        }
    }
    
    init {
        // initialize trace
        traces = ArrayList(NUMBER_OF_TRACES)
        val numberOfEdges = CircularShape.getNumberOfEdges(0.05f) // to give 8 ish
        for (i in 0 until NUMBER_OF_TRACES) {
            traces.add(RegularPolygonalTraceShape(numberOfEdges, 1.01f))
        }
        
        setRotation(surrounding.centerOfRotationX, surrounding.centerOfRotationY, surrounding.rotation)
    }
    
    public override fun generateTraces(previousFrameTime: Long, now: Long, ds: Float) {
        var ds = ds
        // lay down trace
        
        //        if (random() < 0.5)
        //            traces.add(new TrapezoidalTraceShape(((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QuadrilateralShape.X4), ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QuadrilateralShape.Y4),
        //                    ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QuadrilateralShape.X3), ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QuadrilateralShape.Y3), 2,
        //                    -0.5 * speed * sin(currentRotation), -0.5 * speed * cos(currentRotation), 0.9, 0.9, 0.1, 1, 1.01));
        
        ds += unfilledDs // finish last frame unfinished work
        if (surrounding.isStarted) {
            val sinCurrentRotation = sin(currentRotation.toDouble()).toFloat()
            val cosCurrentRotation = cos(currentRotation.toDouble()).toFloat()
            val I_MAX = ds / 128f * 1000f - random().toFloat() // - (random()) so no every single frame create a trace
            if (I_MAX <= 0) { // if we are not adding any trace this frame
                // let the next frame know
                unfilledDs = ds // there is unfinished work
            } else {
                unfilledDs = 0f // also update it so the next frame know there is no unfinished work
                
                var i = 0
                while (i < I_MAX) {
                    val initialRadius = (0.25f + 0.1f * random().toFloat()) * distance((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QuadrilateralShape.X4), (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QuadrilateralShape.Y4),
                            (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QuadrilateralShape.X3), (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QuadrilateralShape.Y3))
                    val x1 = ((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QuadrilateralShape.X4) - initialRadius * cosCurrentRotation)
                    val y1 = ((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QuadrilateralShape.Y4) + initialRadius * sinCurrentRotation)
                    val x2 = ((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QuadrilateralShape.X3) + initialRadius * cosCurrentRotation)
                    val y2 = ((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QuadrilateralShape.Y3) - initialRadius * sinCurrentRotation).toDouble()
                    
                    var centerX: Double
                    var centerY: Double
                    //            if (random() < 0.4) {
                    //                centerX = (x1 - x2) + x2; centerY = (y1 - y2) + y2;
                    //            } else if (random() > 0.6){
                    //                centerX = x2;             centerY = y2;
                    //            } else {
                    centerX = random() * (x1 - x2) + x2
                    centerY = random() * (y1 - y2) + y2
                    //            }
                    
                    val newTraceShape = newRegularPolygonalTraceShape(centerX.toFloat(), centerY.toFloat(), initialRadius, initialRadius * 4,
                            /*-3 * speed * (float) sin(currentRotation)*/0f, /*-3 * speed * (float) cos(currentRotation)*/0f, (16f / speed).toLong(),
                            1f, 1f, 0f, 3f)
                    
                    val random = /*random();*/i / I_MAX/* * (0.5f + (1 * (float) random()))*/
                    newTraceShape.moveTraceShape(now, previousFrameTime + ((1 - random) * (now - previousFrameTime) + 0.5).toInt()) // + 0.5 for rounding
                    newTraceShape.moveShape(-ds * random * sinCurrentRotation, -ds * random * cosCurrentRotation)
                    i++
                }
            }
        }
        
    }
    
    private fun newRegularPolygonalTraceShape(centerX: Float, centerY: Float, initialRadius: Float, finalRadius: Float, speedX: Float, speedY: Float,
                                              duration: Long, initialRed: Float, initialGreen: Float, initialBlue: Float, initialAlpha: Float): RegularPolygonalTraceShape {
        for (trace in traces) {
            if (!(trace as RegularPolygonalTraceShape).showing) {
                trace.resetRegularPolygonalTraceShape(centerX, centerY, initialRadius, finalRadius,
                        speedX, speedY, duration, initialRed, initialGreen, initialBlue, initialAlpha)
                return trace
            }
        }
        throw IndexOutOfBoundsException("not enough traces")
    }
    
    override fun fadeMoveAndRemoveTraces(now: Long, previousFrameTime: Long, ds: Float) {
        parallelForIForTraces.run({ i ->
            val trace = traces[i] as RegularPolygonalTraceShape
            
            if (trace.showing) {
                trace.moveTraceShape(now, previousFrameTime)
                // move traces with surrounding
                trace.moveShape(-ds * sin(currentRotation.toDouble()).toFloat(), -ds * cos(currentRotation.toDouble()).toFloat())
            }
        }, traces.size)
    }
    
    override fun waitForFadeMoveAndRemoveTraces() {
        parallelForIForTraces.waitForLastRun()
    }
    
    override fun removeTrace() {
        for (trace in traces)
            if ((trace as RegularPolygonalTraceShape).showing)
                if (trace.needToBeRemoved())
                    trace.makeInvisible()
    }
}

private const val NUMBER_OF_TRACES = 300
