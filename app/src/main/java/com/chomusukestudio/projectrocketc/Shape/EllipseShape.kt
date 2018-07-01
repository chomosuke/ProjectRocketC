package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin

class EllipseShape(centerX: Float, centerY: Float, a: Float, b: Float, red: Float, green: Float, blue: Float, alpha: Float, z: Float) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override lateinit var componentShapes: Array<Shape>
    
    init {
        
        val numberOfEdges = CircularShape.getNumberOfEdges((a + b) / 2)
        // just initialize it.
        val componentShapes = arrayOfNulls<TriangularShape>(numberOfEdges - 2)
        
        // generate components triangularShape for EllipseShape using center and a and b
        for (i in 1 until numberOfEdges - 1)
            componentShapes[i - 1] = TriangularShape(centerX, centerY + b,
                    centerX + a * sin(2.0 * PI * i.toDouble() / numberOfEdges).toFloat(),
                    centerY + b * cos(2.0 * PI * i.toDouble() / numberOfEdges).toFloat(),
                    centerX + a * sin(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat(),
                    centerY + b * cos(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat(),
                    red, green, blue, alpha, z) // close for modification
        
        this.componentShapes = componentShapes as Array<Shape>
    }
}
