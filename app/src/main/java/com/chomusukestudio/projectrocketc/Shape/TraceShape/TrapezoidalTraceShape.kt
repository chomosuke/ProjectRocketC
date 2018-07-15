package com.chomusukestudio.projectrocketc.Shape.TraceShape

import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.point.square

import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.random
import java.lang.Math.sin
import java.lang.Math.sqrt

class TrapezoidalTraceShape(protected var xL: Float, protected var yL: Float, protected var xR: Float, protected var yR: Float, private val h: Float, speedX: Float, speedY: Float, red: Float, green: Float, blue: Float, alpha: Float, z: Float) : TraceShape() {
    override lateinit var componentShapes: Array<Shape>
    private val angle: Float
    private val sinAngle: Float
    private val cosAngle: Float
    private val rateOfNarrowing: Float
    private var topWidthGreaterThanZero = true
    private var bottomWidthGreaterThanZero = true
    private val x1: Float
    private val y1: Float
    private val x2: Float
    private val y2: Float
    private var x3: Float = 0.toFloat()
    private var y3: Float = 0.toFloat()
    private var x4: Float = 0.toFloat()
    private var y4: Float = 0.toFloat()
    
    init {
        setSpeed(speedX, speedY)
        
        angle = (-atan2((xL - xR).toDouble(), (yL - yR).toDouble())).toFloat()
        sinAngle = sin(angle.toDouble()).toFloat()
        cosAngle = cos(angle.toDouble()).toFloat()
        
        val initWidth = sqrt(square((xL - xR).toDouble()) + square((yL - yR).toDouble())).toFloat()
        val length = (2.0 * random() * initWidth.toDouble()).toFloat()
        rateOfNarrowing = initWidth / h
        
        x1 = xL
        y1 = yL
        x2 = xR
        y2 = yR
        x3 = x2 + length * cosAngle - 0.5f * rateOfNarrowing * length * sinAngle
        y3 = y2 + length * sinAngle + 0.5f * rateOfNarrowing * length * cosAngle
        x4 = x1 + length * cosAngle + 0.5f * rateOfNarrowing * length * sinAngle
        y4 = y1 + length * sinAngle - 0.5f * rateOfNarrowing * length * cosAngle
        
        componentShapes = arrayOf(QuadrilateralShape(x1, y1, x2, y2, x3, y3, x4, y4, red, green, blue, alpha, z))
        
        val random = (0.5 - random()).toFloat()
        moveShape(initWidth * random * cosAngle, initWidth * random * sinAngle)
    }
    
    override fun moveShape(dx: Float, dy: Float) {
        super.moveShape(dx, dy)
        xR += dx
        xL += dx
        x3 += dx
        x4 += dx
        yR += dy
        yL += dy
        y3 += dy
        y4 += dy
    }
    
    override// super.moveTraceShape() calls this
    fun fadeTraceShape(ds: Float, now: Long, previousFrameTime: Long) {
        val rxL = xL
        val ryL = yL
        val rxR = xR
        val ryR = yR
        val rx3 = x3
        val ry3 = y3
        val rx4 = x4
        val ry4 = y4
        xL += ds * cosAngle + rateOfNarrowing * ds * sinAngle
        yL += ds * sinAngle - rateOfNarrowing * ds * cosAngle
        xR += ds * cosAngle - rateOfNarrowing * ds * sinAngle
        yR += ds * sinAngle + rateOfNarrowing * ds * cosAngle
        x3 += ds * cosAngle - rateOfNarrowing * ds * sinAngle
        y3 += ds * sinAngle + rateOfNarrowing * ds * cosAngle
        x4 += ds * cosAngle + rateOfNarrowing * ds * sinAngle
        y4 += ds * sinAngle - rateOfNarrowing * ds * cosAngle
        (componentShapes[0] as QuadrilateralShape).setQuadrilateralShapeCoords(xL, yL, xR, yR, x3, y3, x4, y4)
        
        if (xL < xR != rxL < rxR || yL < yR != ryL < ryR) {
            topWidthGreaterThanZero = false
            needToBeRemoved = true
        }
        if (x3 < x4 != rx3 < rx4 || y3 < y4 != ry3 < ry4)
            bottomWidthGreaterThanZero = false
    }
}
