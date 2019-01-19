package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.*
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin

/**
 * Created by Shuang Li on 22/03/2018.
 */

class RegularPolygonalShape(val numberOfEdges: Int, centerX: Float, centerY: Float, radius: Float, red: Float, green: Float, blue: Float, alpha: Float, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override lateinit var componentShapes: Array<Shape>

    var centerY = centerY
        private set
    var centerX = centerX
        private set

    // as no special isOverlapToOverride method is provided.
    init {
    
        // just initialize it.
        val componentShapes = arrayOfNulls<TriangularShape>(numberOfEdges - 2)
    
        // generate components triangularShape for RegularPolygonalShape using center and radius
        var previousSin = sin(2.0 * PI * 1 / numberOfEdges).toFloat()
        var previousCos = cos(2.0 * PI * 1 / numberOfEdges).toFloat()
        for (i in 1 until numberOfEdges - 1) {
            val thisSin = sin(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat()
            val thisCos = cos(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat()
            componentShapes[i - 1] = TriangularShape(centerX, centerY + radius,
                    centerX + radius * previousSin,
                    centerY + radius * previousCos,
                    centerX + radius * thisSin,
                    centerY + radius * thisCos,
                    red, green, blue, alpha, buildShapeAttr) // close for modification
            previousCos = thisCos
            previousSin = thisSin
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }

    fun resetParameter(centerX: Float, centerY: Float, radius: Float) {
        resetCenter(centerX, centerY)

        // and change radius
        this.radius = radius
    }

    fun resetCenter(centerX: Float, centerY: Float) {
        val dx = centerX - this.centerX
        val dy = centerY - this.centerY
        moveShape(dx, dy)
        this.centerX = centerX
        this.centerY = centerY
    }

    var radius = radius
        set(value) {
            if (value != field) {
                // reset components triangularShape for RegularPolygonalShape using center and value
                if (field != 0f) { // if field is 0 factor will be infinity, therefore not possible to use this method
                    val factor = value / field

                    // previous are just this x2 and this y2
                    var x2 = centerX + factor * ((componentShapes[0] as TriangularShape).getTriangularShapeCoords(X2) - centerX)
                    var y2 = centerY + factor * ((componentShapes[0] as TriangularShape).getTriangularShapeCoords(Y2) - centerY)

                    val x1 = centerX + factor * ((componentShapes[0] as TriangularShape).getTriangularShapeCoords(X1) - centerX)
                    val y1 = centerY + factor * ((componentShapes[0] as TriangularShape).getTriangularShapeCoords(Y1) - centerY)

                    for (i in 0 until componentShapes.size) {
                        // new x3, y3 is old one change distance
                        val x3 = centerX + factor * ((componentShapes[i] as TriangularShape).getTriangularShapeCoords(X3) - centerX)
                        val y3 = centerY + factor * ((componentShapes[i] as TriangularShape).getTriangularShapeCoords(Y3) - centerY)

                        (componentShapes[i] as TriangularShape).setTriangleCoords(x1, y1,
                                x2, y2, x3, y3)

                        // x2, y2 is previous x3 and previous y3
                        x2 = x3
                        y2 = y3

                        // i spend so long to write this to try to improve performance but only got ever so slightly...
                    }
                } else {
                    // because the above is not possible when field is zero, use sin and cos to recalculate
                    var previousSin = sin(2.0 * PI * 1 / numberOfEdges).toFloat()
                    var previousCos = cos(2.0 * PI * 1 / numberOfEdges).toFloat()
                    for (i in 1 until numberOfEdges - 1) {
                        val thisSin = sin(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat()
                        val thisCos = cos(2.0 * PI * (i + 1).toDouble() / numberOfEdges).toFloat()
                        (componentShapes[i - 1] as TriangularShape).setTriangleCoords(centerX, centerY + value,
                                centerX + value * previousSin,
                                centerY + value * previousCos,
                                centerX + value * thisSin,
                                centerY + value * thisCos) // close for modification
                        previousCos = thisCos
                        previousSin = thisSin
                    }
                }
            }
            field = value
        }

    override fun moveShape(dx: Float, dy: Float) {
        if (dx == 0f && dy == 0f) {
            return // yeah, i do that a lot
        }
        super.moveShape(dx, dy)
        this.centerX += dx
        this.centerY += dy
    }

    override fun rotateShape(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        super.rotateShape(centerOfRotationX, centerOfRotationY, angle)

        val result = rotatePoint(centerX, centerY, centerOfRotationX, centerOfRotationY, angle)
        centerX = result[0]
        centerY = result[1]
    }
}

// inlined already...
fun changeDistance(Stay : Double, Move : Double, factor : Double) = Stay + factor * (Move - Stay)
