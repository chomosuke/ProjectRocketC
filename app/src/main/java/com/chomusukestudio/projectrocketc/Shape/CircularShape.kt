package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.*
import com.chomusukestudio.projectrocketc.square
import com.chomusukestudio.projectrocketc.heightInPixel
import com.chomusukestudio.projectrocketc.widthInPixel
import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.acos

/**
 * Created by Shuang Li on 12/03/2018.
 */

class CircularShape(centerX: Float, centerY: Float, radius: Float, private val performanceIndex: Double, red: Float, green: Float, blue: Float, alpha: Float, private val buildShapeAttr: BuildShapeAttr) : Shape() {
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(getNumberOfEdges(radius, performanceIndex),
            centerX, centerY, radius, red, green, blue, alpha, buildShapeAttr))
    private var regularPolygonalShape
        set(value) { componentShapes[0] = value }
        get() = componentShapes[0] as RegularPolygonalShape

    override val isOverlapMethodLevel: Double = 1.0
    // parameters needed for isOverlapToOverride method.
    val centerX
        get() = regularPolygonalShape.centerX
    val centerY
        get() = regularPolygonalShape.centerY

    var radius
        set(value) {
            if (abs(radius) > abs(lastChangeOfNumberOfEdgesRadius * 1.25) || abs(radius) < abs(lastChangeOfNumberOfEdgesRadius * 0.8)
                    && getNumberOfEdges(radius, performanceIndex) != regularPolygonalShape.numberOfEdges) {
                lastChangeOfNumberOfEdgesRadius = radius
                val color = FloatArray(4)
                System.arraycopy(shapeColor, 0, color, 0, color.size)
                regularPolygonalShape.removeShape()
                regularPolygonalShape = RegularPolygonalShape(getNumberOfEdges(radius, performanceIndex),
                        centerX, centerY, radius, color[0], color[1], color[2], color[3], buildShapeAttr.newAttrWithNewVisibility(visibility)/*visibility might have changed*/)
            } else
                regularPolygonalShape.radius = value
        }
        get() = regularPolygonalShape.radius

    
    constructor(centerX: Float, centerY: Float, radius: Float, red: Float, green: Float, blue: Float, alpha: Float, buildShapeAttr: BuildShapeAttr) : this(centerX, centerY, radius, 1.0, red, green, blue, alpha, buildShapeAttr)

    private var lastChangeOfNumberOfEdgesRadius = radius
    fun resetParameter(centerX: Float, centerY: Float, radius: Float) {
        this.radius = radius
        regularPolygonalShape.resetCenter(centerX, centerY)
    }
    
    public override// isOverlapMethodLevel is 1 now!
    // remember this!
    fun isOverlapToOverride(anotherShape: Shape): Boolean {
        return isOverlap(anotherShape, centerX, centerY, radius)
    }
    
    override fun isInside(x: Float, y: Float): Boolean {
        return square(x - centerX) + square(y - centerY) <= square(radius)
    }

    companion object {
        
        fun isOverlap(anotherShape: Shape, centerX: Float, centerY: Float, radius: Float): Boolean {
            if (anotherShape is CircularShape) {
                return square(centerX - anotherShape.centerX) + square(centerY - anotherShape.centerY) <= square(radius + anotherShape.radius)
            } else if (anotherShape is TriangularShape) {
                val x1 = anotherShape.getTriangularShapeCoords(X1)
                val y1 = anotherShape.getTriangularShapeCoords(Y1)
                val x2 = anotherShape.getTriangularShapeCoords(X2)
                val y2 = anotherShape.getTriangularShapeCoords(Y2)
                val x3 = anotherShape.getTriangularShapeCoords(X3)
                val y3 = anotherShape.getTriangularShapeCoords(Y3)
                //
                // TEST 1: Vertex within circle
                //
                if (square(x1 - centerX) + square(y1 - centerY) <= square(radius) ||
                        square(x2 - centerX) + square(y2 - centerY) <= square(radius) ||
                        square(x3 - centerX) + square(y3 - centerY) <= square(radius))
                    return true
                //
                // TEST 2: Circle centre within triangle
                //
                if (anotherShape.isInside(centerX, centerY))
                    return true
                //
                // TEST 3: Circle intersects edge
                //
                if (circleOverlapWithLine(centerX, centerY, radius, x1, y1, x2, y2) ||
                        circleOverlapWithLine(centerX, centerY, radius, x2, y2, x3, y3) ||
                        circleOverlapWithLine(centerX, centerY, radius, x1, y1, x3, y3))
                    return true
                // We're done, no intersection
            } else {
                for (componentShapeOfAnotherShape in anotherShape.componentShapes)
                    if (isOverlap(componentShapeOfAnotherShape, centerX, centerY, radius))
                        return true
            }
            return false
        }
        
        private fun circleOverlapWithLine(centerX: Float, centerY: Float, radius: Float,
                                          x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
            // debugged, closed for modification
            return ((square(getArea(centerX, centerY, x1, y1, x2, y2) * 2) / (square(x1 - x2) // square of distance from center to the edge of triangle.
                    + square(y1 - y2)) // calculated by divide the area by length of the edge of triangle.
                    <=
					square(radius)) // is overlap if it's smaller than square of radius.
                    &&
                    ((square(x1 - x2) // the above will consider edge as a straight line without ends
                    + square(y1 - y2)) // that would be problematic as the edge of the triangle have ends.
                    >=
                    abs((square(centerX - x2) // this would determent if the angle between
                    + square(centerY - y2)) // the line from the center of circle to one of the end of the edge
                    - square(centerX - x1)// and the edge would be larger than 90 degree
                    - square(centerY - y1)))) // as pythagoras thingy.
            
            // another level of maintainability lol
            // this is pretty much the most unmaintainable code i ever wrote in this project
        }
        
        private fun getArea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
            return if ((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2 < 0)
                (-(x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2)
            else
                ((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2)
        }
        
        var performanceIndex = 1f

        fun getNumberOfEdges(radius: Float, dynamicPerformanceIndex: Double = 1.0): Int {
            val pixelOnRadius = (radius / abs(leftEnd - rightEnd) * widthInPixel + 0.5).toInt() // +0.5 for rounding
            val numberOfEdges = (PI / acos(1.0 - 0.2 / pixelOnRadius / (dynamicPerformanceIndex * CircularShape.performanceIndex)) / 2.0 + 0.5).toInt() * 2 /*
         /2*2 to make it even +0.5 for rounding */
            return if (numberOfEdges > 64)
                64
            else if (numberOfEdges < 8 && pixelOnRadius > 4)
                8
            else if (numberOfEdges < 3)
                3
            else
                numberOfEdges
        }
    }
}
