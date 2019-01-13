package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI
import kotlin.math.cos
import kotlin.math.sin

class NPointsStarShape(n: Int, centerX: Float, centerY: Float, radius: Float, red: Float, green: Float, blue: Float, alpha: Float, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0 //as no special isOverlapToOverride method is provided
    override lateinit var componentShapes: Array<Shape>
    
    init {
        val componentShapes = arrayOfNulls<Shape>(n + 1);
        componentShapes[0] = RegularPolygonalShape(n, centerX, centerY, radius * (sin(PI / 2f / n) / sin(PI - PI / 2f / n - PI / n)).toFloat(),
                red, green, blue, alpha, buildShapeAttr) // central RegularPolygonalShape
        componentShapes[0]!!.rotateShape(centerX, centerY, PI.toFloat() / n)

        // points
        val dx = (sin(PI/2f/n) / sin(PI - PI/2f/n - PI/n) * cos(PI/n) * radius).toFloat()
        val dy = (sin(PI/2f/n) / sin(PI - PI/2f/n - PI/n) * sin(PI/n) * radius).toFloat()
        for (i in 1 until componentShapes.size) {
            componentShapes[i] = TriangularShape(
                    centerX, centerY + radius, centerX + dx, centerY + dy, centerX - dx, centerY + dy,
                    red, green, blue, alpha, buildShapeAttr)
            componentShapes[i]!!.rotateShape(centerX, centerY, 2f*PI.toFloat() / n * i)
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }
}