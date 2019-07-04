package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.randFloat

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.random
import java.lang.Math.sin

class JupiterShape(centerX: Float, centerY: Float, radius: Float, buildShapeAttr: BuildShapeAttr) : PlanetShape(centerX, centerY, radius) {
    override val isOverlapMethodLevel: Double = 2.0
    override lateinit var componentShapes: Array<Shape>
    
    init {
        
        
        val numberOfEdges = CircularShape.getNumberOfEdges(radius)
        val componentShapes = arrayOfNulls<QuadrilateralShape>(numberOfEdges / 2)
        val mainColor = floatArrayOf(
                randFloat(0.2f, 0.8f),
                randFloat(0.2f, 0.8f),
                randFloat(0.2f, 0.8f))
        
        // generate components triangularShape for RegularPolygonalShape isInUse center and radius
        var colorUsing = mainColor
        var lastColorChange = 0
        for (i in componentShapes.indices) {
            
            // change color?
            if (i - lastColorChange > random() * 8) {
                lastColorChange = i
                val randomDarker = randFloat(0.8f, 1.2f)
                colorUsing = floatArrayOf(
                        randFloat(0.95f, 1.05f) * randomDarker * mainColor[0],
                        randFloat(0.95f, 1.05f) * randomDarker * mainColor[1],
                        randFloat(0.95f, 1.05f) * randomDarker * mainColor[2])
            }
            
            componentShapes[i] = QuadrilateralShape(
                    centerX + radius * sin(2.0 * -PI * (i + 1) / numberOfEdges).toFloat(),
                    centerY + radius * cos(2.0 * -PI * (i + 1) / numberOfEdges).toFloat(),
                    centerX + radius * sin(2.0 * -PI * i / numberOfEdges).toFloat(),
                    centerY + radius * cos(2.0 * -PI * i / numberOfEdges).toFloat(),
                    centerX + radius * sin(2.0 * PI * i / numberOfEdges).toFloat(),
                    centerY + radius * cos(2.0 * PI * i / numberOfEdges).toFloat(),
                    centerX + radius * sin(2.0 * PI * (i + 1) / numberOfEdges).toFloat(),
                    centerY + radius * cos(2.0 * PI * (i + 1) / numberOfEdges).toFloat(),
                    colorUsing[0], colorUsing[1], colorUsing[2], 1f, buildShapeAttr) // close for modification
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }// one level higher circularShape
}
