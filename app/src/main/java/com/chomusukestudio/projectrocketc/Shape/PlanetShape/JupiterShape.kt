package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.randFloat

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.random
import java.lang.Math.sin

class JupiterShape(center: Vector, radius: Float, buildShapeAttr: BuildShapeAttr) : PlanetShape(center, radius) {
    override lateinit var componentShapes: Array<Shape>

    init {


        val numberOfEdges = CircularShape.getNumberOfEdges(radius)
        val componentShapes = arrayOfNulls<QuadrilateralShape>(numberOfEdges / 2)
        val mainColor = Color(
                randFloat(0.2f, 0.8f),
                randFloat(0.2f, 0.8f),
                randFloat(0.2f, 0.8f), 1f)

        // generate components triangularShape for RegularPolygonalShape isInUse center and radius
        var colorUsing = mainColor
        var lastColorChange = 0
        for (i in componentShapes.indices) {

            // change color?
            if (i - lastColorChange > random() * 8) {
                lastColorChange = i
                val randomDarker = randFloat(0.8f, 1.2f)
                colorUsing = Color(
                        randFloat(0.95f, 1.05f) * randomDarker * mainColor.red,
                        randFloat(0.95f, 1.05f) * randomDarker * mainColor.green,
                        randFloat(0.95f, 1.05f) * randomDarker * mainColor.blue, 1f)
            }

            componentShapes[i] = QuadrilateralShape(
                    Vector(center.x + radius * sin(2.0 * -PI * (i + 1) / numberOfEdges).toFloat(),
                            center.y + radius * cos(2.0 * -PI * (i + 1) / numberOfEdges).toFloat()),
                    Vector(center.x + radius * sin(2.0 * -PI * i / numberOfEdges).toFloat(),
                            center.y + radius * cos(2.0 * -PI * i / numberOfEdges).toFloat()),
                    Vector(center.x + radius * sin(2.0 * PI * i / numberOfEdges).toFloat(),
                            center.y + radius * cos(2.0 * PI * i / numberOfEdges).toFloat()),
                    Vector(center.x + radius * sin(2.0 * PI * (i + 1) / numberOfEdges).toFloat(),
                            center.y + radius * cos(2.0 * PI * (i + 1) / numberOfEdges).toFloat()),
                    colorUsing, buildShapeAttr) // close for modification
        }

        this.componentShapes = componentShapes as Array<Shape>
    }
}