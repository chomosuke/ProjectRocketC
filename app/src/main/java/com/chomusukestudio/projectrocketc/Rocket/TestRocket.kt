package com.chomusukestudio.projectrocketc.Rocket

import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI
import com.chomusukestudio.projectrocketc.Shape.TraceShape.RegularPolygonalTraceShape
import com.chomusukestudio.projectrocketc.Shape.coordinate.Coordinate

import java.util.ArrayList

import com.chomusukestudio.projectrocketc.Shape.coordinate.distance
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.State
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.state
import java.lang.Math.*
import kotlin.math.PI

/**
 * Created by Shuang Li on 11/03/2018.
 */

open class TestRocket(surrounding: Surrounding) : Rocket(surrounding) {
    override var radiusOfRotation = 2f
    final override val initialSpeed = 4f / 1000f
    override var speed = initialSpeed

    private var unfilledDs = 0f
    
    private val parallelForIForTraces = ParallelForI(8, "traces thread")
    
    override val width = 0.3f

    final override val components: Array<Shape> = Array(4) { i ->
        when (i) {
        // defined components of rocket around centerOfRotation set by surrounding
            0 ->
                TriangularShape(centerOfRotationX, centerOfRotationY + 0.5f,
                        centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f,
                        1f, 1f, 1f, 1f, 1f, true)
            1 ->
                QuadrilateralShape(centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f, centerOfRotationX - 0.15f, centerOfRotationY - 0.3f,
                        centerOfRotationX + 0.15f, centerOfRotationY - 0.3f, 1f, 1f, 1f, 1f, 1f, true)
            2 ->
                CircularShape(centerOfRotationX, centerOfRotationY /*+ 0.38*/, 0.07f,
                        0.1f, 0.1f, 0.1f, 1f, 0.9999f, true)
            3 ->
                QuadrilateralShape(centerOfRotationX + 0.1f, centerOfRotationY - 0.3f,
                        centerOfRotationX - 0.1f, centerOfRotationY - 0.3f, centerOfRotationX - 0.12f, centerOfRotationY - 0.4f,
                        centerOfRotationX + 0.12f, centerOfRotationY - 0.4f, 1f, 1f, 1f, 1f, 1f, true)
            else -> {
                throw IndexOutOfBoundsException()
            }
        }
    }

    // initialize for surrounding to set centerOfRotation
    init {
        // initialize trace
        traces = ArrayList(NUMBER_OF_TRACES)
        val numberOfEdges = CircularShape.getNumberOfEdges(0.6f) // to give 8 ish
        for (i in 0 until NUMBER_OF_TRACES) {
            traces.add(RegularPolygonalTraceShape(numberOfEdges, 1.01f, true))
        }
        
        setRotation(surrounding.centerOfRotationX, surrounding.centerOfRotationY, surrounding.rotation)
    }
    
    public override fun generateTraces(previousFrameTime: Long, now: Long, ds: Float) {
        var ds = ds
        // lay down trace
        
        //        if (random() < 0.5)
        //            traces.add(new TrapezoidalTraceShape(((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QX4), ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QY4),
        //                    ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QX3), ((QuadrilateralShape) components[3]).getQuadrilateralShapeCoords(QY3), 2,
        //                    -0.5 * speed * sin(currentRotation), -0.5 * speed * cos(currentRotation), 0.9, 0.9, 0.1, 1, 1.01));
        
        ds += unfilledDs // finish last frame unfinished work
        if (state == State.InGame) {
            val sinCurrentRotation = sin(currentRotation.toDouble()).toFloat()
            val cosCurrentRotation = cos(currentRotation.toDouble()).toFloat()
            val I_MAX = ds / 128f * 1000f - random().toFloat()
            if (I_MAX <= 0) { // if we are not adding any trace this frame
                // let the next frame know
                unfilledDs = ds // there is unfinished work
            } else {
                unfilledDs = 0f // also update it so the next frame know there is no unfinished work
                
                var i = 0
                while (i < I_MAX) {
                    val initialRadius = (0.25f + 0.1f * random().toFloat()) * distance((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX4), (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY4),
                            (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX3), (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY3))
                    val x1 = ((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX4) - initialRadius * cosCurrentRotation)
                    val y1 = ((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY4) + initialRadius * sinCurrentRotation)
                    val x2 = ((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX3) + initialRadius * cosCurrentRotation)
                    val y2 = ((components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY3) - initialRadius * sinCurrentRotation).toDouble()
                    
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
                    newTraceShape.moveTraceShape(now, previousFrameTime + ((1 - random) * (now - previousFrameTime) + random()).toInt()) // + 0.5 for rounding
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

    private val explosionCoordinates = arrayOf(
            Coordinate(with(components[0] as TriangularShape) { (x1 + x2 + x3) / 3 }, with(components[0] as TriangularShape) { (y1 + y2 + y3) / 3 }),
            Coordinate(with(components[1] as QuadrilateralShape) { (x1 + x2 + x3 + x4) / 4 }, with(components[1] as QuadrilateralShape) { (y1 + y2 + y3 + y4) / 4 }),
            Coordinate((components[2] as CircularShape).centerX, (components[2] as CircularShape).centerY),
            Coordinate(with(components[3] as QuadrilateralShape) { (x1 + x2 + x3 + x4) / 4 }, with(components[3] as QuadrilateralShape) { (y1 + y2 + y3 + y4) / 4 }))

    protected open var explosionShape: ExplosionShape? = null
    protected open class ExplosionShape(centerX: Float, centerY: Float, approximateWholeSize: Float, approximateIndividualSize: Float, private val duration: Long) : Shape() {
        override val isOverlapMethodLevel: Double
            get() = throw IllegalAccessException("explosionShape can't overlap anything")

        final override var componentShapes = Array<Shape>(24) { i ->
            if (i < 8) {
                val distantToCenter = random().toFloat()*approximateWholeSize*0.6f
                val centers = rotatePoint(centerX, centerY + distantToCenter, centerX, centerY, (i*PI/4).toFloat())
                CircularShape(centers[0], centers[1], 0f, 99.6f, 87.5f, 31.4f, 1f, -10f, true)
            }
            else {
                val distantToCenter = random().toFloat()*approximateWholeSize*1.1f
                val centers = rotatePoint(centerX, centerY + distantToCenter, centerX, centerY, (i*PI/8).toFloat())
                CircularShape(centers[0], centers[1], 0f, 1f, 1f, 1f, 1f, -10f, true)
            }
        }

        val individualRadius = FloatArray(componentShapes.size) { approximateIndividualSize * (0.5 + 1 * random()).toFloat() }

        private val alphaEveryMiniSecond = pow(1.0 / 256, 1.0 / duration).toFloat()

        private var timeSinceExplosion = 0L
        fun drawExplosion(timePassed: Long) {
            timeSinceExplosion += timePassed

            for (i in componentShapes.indices) {
                val color = componentShapes[i].shapeColor
                    componentShapes[i].resetAlpha(color[3] * Math.pow(alphaEveryMiniSecond.toDouble(), timePassed.toDouble()).toFloat())
                val radius = individualRadius[i] * Math.sqrt((timeSinceExplosion / duration).toDouble()).toFloat()
                (componentShapes[i] as RegularPolygonalShape).resetParameter((componentShapes[0] as RegularPolygonalShape).centerX,
                        (componentShapes[0] as RegularPolygonalShape).centerY, radius)
            }
        }
    }
    override fun drawExplosion(now: Long, previousFrameTime: Long) {
        if (explosionShape == null) {
            val explosionCoordinate = this.explosionCoordinates[components.indexOf(crashedComponent)]
            explosionShape = ExplosionShape(explosionCoordinate.x, explosionCoordinate.y, 1f, 0.3f, 1000)
        } else {
            explosionShape!!.drawExplosion(now - previousFrameTime)
        }
    }

    override fun removeAllShape() {
        super.removeAllShape()
        explosionShape?.removeShape()
    }
}

private const val NUMBER_OF_TRACES = 300
