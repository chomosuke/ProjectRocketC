package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin

class TopHalfRingShape(center: Vector, a: Float, b: Float, factor: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override lateinit var componentShapes: Array<Shape>
    
    init {
        
        val numberOfEdges = CircularShape.getNumberOfEdges((a + b + 1f) / 2) / 2 // + 1 is for the rounding.
    
        val componentShapes = arrayOfNulls<QuadrilateralShape>(numberOfEdges)
        
        var previousX2 = center.x + a
        var previousY2 = center.y
        var previousX3 = center.x + factor * a
        var previousY3 = center.y // start at left
        for (i in componentShapes.indices) {
            val x2 = center.x + a * cos(PI * (i + 1) / numberOfEdges).toFloat()
            val y2 = center.y + b * sin(PI * (i + 1) / numberOfEdges).toFloat()
            val x3 = center.x + factor * a * cos(PI * (i + 1) / numberOfEdges).toFloat()
            val y3 = center.y + factor * b * sin(PI * (i + 1) / numberOfEdges).toFloat()
            componentShapes[i] = QuadrilateralShape(Vector(previousX2, previousY2), Vector(x2, y2), Vector(x3, y3), Vector(previousX3, previousY3),
                    color, buildShapeAttr)
            previousX2 = x2
            previousY2 = y2
            previousX3 = x3
            previousY3 = y3
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }
}
