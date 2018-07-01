package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.Shape

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.random
import java.lang.Math.sin

class JupiterShape(centerX: Float, centerY: Float, radius: Float, z: Float) : PlanetShape(centerX, centerY, radius, 2.0) {
    override lateinit var componentShapes: Array<Shape>
    
    init {
        
        
        val numberOfEdges = CircularShape.getNumberOfEdges(radius)
        val componentShapes = arrayOfNulls<QuadrilateralShape>(numberOfEdges / 2)
        val mainColor = floatArrayOf((random() * 0.6 + 0.2).toFloat(), (random() * 0.6 + 0.2).toFloat(), (random() * 0.6 + 0.2).toFloat())
        
        // generate components triangularShape for RegularPolygonalShape using center and radius
        var colorUsing = mainColor
        var lastColorChange = 0
        for (i in componentShapes.indices) {
            
            // change color?
            if (i - lastColorChange > random() * 8) {
                lastColorChange = i
                val randomDarker = (random() * 0.4 + 0.8).toFloat()
                colorUsing = floatArrayOf(((random() * 0.1 + 0.95) * randomDarker.toDouble() * mainColor[0].toDouble()).toFloat(), ((random() * 0.1 + 0.95) * randomDarker.toDouble() * mainColor[1].toDouble()).toFloat(), ((random() * 0.1 + 0.95) * randomDarker.toDouble() * mainColor[2].toDouble()).toFloat())
            }
            
            componentShapes[i] = QuadrilateralShape(
                    centerX + radius * sin(2.0 * -PI * (i + 1).toDouble() / numberOfEdges).toFloat(),
                    centerY + radius * cos(2.0 * -PI * (i + 1).toDouble() / numberOfEdges).toFloat(),
                    centerX + radius * sin(2.0 * -PI * i.toDouble() / numberOfEdges).toFloat(),
                    centerY + radius * cos(2.0 * -PI * i.toDouble() / numberOfEdges).toFloat(),
                    centerX + radius * sin(2.0 * PI * i.toDouble() / numberOfEdges).toFloat(),
                    centerY + radius * cos(2.0 * PI * i.toDouble() / numberOfEdges).toFloat(),
                    centerX + radius * sin(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat(),
                    centerY + radius * cos(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat(),
                    colorUsing[0], colorUsing[1], colorUsing[2], 1f, z) // close for modification
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }// one level higher circularShape
}
