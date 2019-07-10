package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin

class EllipseShape(center: Vector, a: Float, b: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override lateinit var componentShapes: Array<Shape>
    
    init {
    
        val numberOfEdges = CircularShape.getNumberOfEdges((a + b) / 2)
        // just initialize it.
        val componentShapes = arrayOfNulls<TriangularShape>(numberOfEdges - 2)
    
        // generate components triangularShape for EllipseShape isInUse center and a and b
        for (i in 1 until numberOfEdges - 1)
            componentShapes[i - 1] = TriangularShape(Vector(center.x, center.y + b),
                    Vector(center.x + a * sin(2.0 * PI * i.toDouble() / numberOfEdges).toFloat(),
                            center.y + b * cos(2.0 * PI * i.toDouble() / numberOfEdges).toFloat()),
                    Vector(center.x + a * sin(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat(),
                            center.y + b * cos(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat()),
                    color, buildShapeAttr) // close for modification
    
        this.componentShapes = componentShapes as Array<Shape>
    }
}
