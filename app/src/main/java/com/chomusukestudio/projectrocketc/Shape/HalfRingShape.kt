package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin

class HalfRingShape(centerX: Float, centerY: Float, a: Float, b: Float, factor: Float, red: Float, green: Float, blue: Float, alpha: Float, z: Float) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override lateinit var componentShapes: Array<Shape>
    
    init {
        
        val numberOfEdges = CircularShape.getNumberOfEdges((a + b + 1f) / 2) / 2 // + 1 is for the rounding.
    
        val componentShapes = arrayOfNulls<QuadrilateralShape>(numberOfEdges)
        
        var previousX2 = centerX + a * sin(PI / numberOfEdges).toFloat()
        var previousY2 = centerY + b * cos(PI / numberOfEdges).toFloat()
        var previousX3 = centerX + factor * a * sin(PI / numberOfEdges).toFloat()
        var previousY3 = centerY + factor * b * cos(PI / numberOfEdges).toFloat()
        for (i in componentShapes.indices) {
            val x2 = centerX + a * sin(PI * (i + 1) / numberOfEdges).toFloat()
            val y2 = centerY + b * cos(PI * (i + 1) / numberOfEdges).toFloat()
            val x3 = centerX + factor * a * sin(PI * (i + 1) / numberOfEdges).toFloat()
            val y3 = centerY + factor * b * cos(PI * (i + 1) / numberOfEdges).toFloat()
            componentShapes[i] = QuadrilateralShape(previousX2, previousY2, x2, y2, x3, y3, previousX3, previousY3,
                    red, green, blue, alpha, z)
            previousX2 = x2
            previousY2 = y2
            previousX3 = x3
            previousY3 = y3
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }
}
