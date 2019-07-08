package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.*
import com.chomusukestudio.projectrocketc.distance
import com.chomusukestudio.projectrocketc.square
import com.chomusukestudio.projectrocketc.widthInPixel
import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.acos

/**
 * Created by Shuang Li on 12/03/2018.
 */

class CircularShape(center: Vector, radius: Float, private val performanceIndex: Double, color: Color, private val buildShapeAttr: BuildShapeAttr) : Shape() {
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(getNumberOfEdges(radius, performanceIndex),
            center, radius, color, buildShapeAttr))
    private var regularPolygonalShape
        set(value) { componentShapes[0] = value }
        get() = componentShapes[0] as RegularPolygonalShape

    override val isOverlapMethodLevel: Double = 1.0
    // parameters needed for isOverlapToOverride method.
    val center
        get() = regularPolygonalShape.center

    var radius
        set(value) {
            regularPolygonalShape.radius = value
            if (abs(radius) > abs(lastChangeOfNumberOfEdgesRadius * 1.25) || abs(radius) < abs(lastChangeOfNumberOfEdgesRadius * 0.8)
                    && getNumberOfEdges(radius, performanceIndex) != regularPolygonalShape.numberOfEdges) {
                lastChangeOfNumberOfEdgesRadius = radius
                val color = shapeColor
                regularPolygonalShape.removeShape()
                regularPolygonalShape = RegularPolygonalShape(getNumberOfEdges(radius, performanceIndex),
                        center, radius, color, buildShapeAttr.newAttrWithNewVisibility(visibility)/*visibility might have changed*/)
            }
        }
        get() = regularPolygonalShape.radius

    
    constructor(center: Vector, radius: Float, color: Color, buildShapeAttr: BuildShapeAttr) : this(center, radius, 1.0, color, buildShapeAttr)

    private var lastChangeOfNumberOfEdgesRadius = radius
    fun resetParameter(center: Vector, radius: Float) {
        this.radius = radius
        regularPolygonalShape.resetCenter(center)
    }
    
    public override// isOverlapMethodLevel is 1 nowXY!
    // remember this!
    fun isOverlapToOverride(anotherShape: Shape): Boolean {
        return isOverlap(anotherShape, center, radius)
    }
    
    override fun isInside(point: Vector): Boolean {
        return distance(point, center) <= radius
    }

    companion object {
        
        fun isOverlap(anotherShape: Shape, center: Vector, radius: Float): Boolean {
            if (anotherShape is CircularShape) {
                return distance(center, anotherShape.center) <= radius + anotherShape.radius
            } else if (anotherShape is TriangularShape) {
                val vertex1 = anotherShape.vertex1
                val vertex2 = anotherShape.vertex2
                val vertex3 = anotherShape.vertex3
                //
                // TEST 1: Vertex within circle
                //
                if (distance(center, vertex1) <= radius ||
                        distance(center, vertex2) <= radius ||
                        distance(center, vertex3) <= radius)
                    return true
                //
                // TEST 2: Circle centre within triangle
                //
                if (anotherShape.isInside(center))
                    return true
                //
                // TEST 3: Circle intersects edge
                //
                if (circleOverlapWithLine(center.x, center.y, radius, vertex1.x, vertex1.y, vertex2.x, vertex2.y) ||
                        circleOverlapWithLine(center.x, center.y, radius, vertex3.x, vertex3.y, vertex2.x, vertex2.y) ||
                        circleOverlapWithLine(center.x, center.y, radius, vertex1.x, vertex1.y, vertex3.x, vertex3.y))
                    return true
                // We're done, no intersection
            } else {
                for (componentShapeOfAnotherShape in anotherShape.componentShapes)
                    if (isOverlap(componentShapeOfAnotherShape, center, radius))
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
            val pixelOnRadius = (radius / abs(rightEnd - leftEnd) * widthInPixel + 0.5).toInt() // +0.5 for rounding
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
