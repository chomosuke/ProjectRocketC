package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.*
import com.chomusukestudio.projectrocketc.Shape.point.rotatePoint
import com.chomusukestudio.projectrocketc.Shape.point.square
import com.chomusukestudio.projectrocketc.heightInPixel
import com.chomusukestudio.projectrocketc.widthInPixel
import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.acos

/**
 * Created by Shuang Li on 12/03/2018.
 */

class CircularShape(centerX: Float, centerY: Float, radius: Float, private val performanceIndex: Double, red: Float, green: Float, blue: Float, alpha: Float, private val z: Float) : Shape() {
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(getNumberOfEdges(radius, performanceIndex), // + 0.5 for rounding
    centerX, centerY, radius, red, green, blue, alpha, z))
    override val isOverlapMethodLevel: Double = 1.0
    var centerX: Float = 0f
        private set
    var centerY: Float = 0f
        private set
    var radius: Float = 0f
        private set // parameters needed for isOverlapToOverride method.
    // getters
    
    constructor(centerX: Float, centerY: Float, radius: Float, red: Float, green: Float, blue: Float, alpha: Float, z: Float) : this(centerX, centerY, radius, 1.0, red, green, blue, alpha, z)
    
    init {
        // set parameters
        this.centerX = centerX
        this.centerY = centerY
        this.radius = radius
    }
    
    //    public CircularShape(double centerX, double centerY, double radius) { // an empty circularShape for planetShape
    //
    //        super(1); // as no special isOverlapToOverride method is provided.
    //
    //        // set parameters
    //        this.centerX = centerX;
    //        this.centerY = centerY;
    //        this.radius = radius;
    //    }
    fun resetParameter(centerX: Float, centerY: Float, radius: Float) {
        //        if (componentShapes != null)
        // this circularShape is not empty
        if (abs(radius) <= abs(this.radius * 1.25) && abs(radius) >= abs(this.radius * 0.8))
            (componentShapes[0] as RegularPolygonalShape).resetParameter(centerX, centerY, radius)
        else {
            val color = FloatArray(4)
            System.arraycopy(shapeColor, 0, color, 0, color.size)
            componentShapes[0].removeShape()
            componentShapes[0] = RegularPolygonalShape(getNumberOfEdges(radius, performanceIndex),
                    centerX, centerY, radius, color[0], color[1], color[2], color[3], this.z)
        }
        
        this.centerX = centerX
        this.centerY = centerY
        this.radius = radius
    }
    
    fun changeRadius(dr: Float) {
        resetParameter(centerX, centerY, radius + dr)
    }
    
    public override// isOverlapMethodLevel is 1 now!
    // remember this!
    fun isOverlapToOverride(anotherShape: Shape): Boolean {
        return isOverlap(anotherShape, centerX, centerY, radius)
    }
    
    override fun isInside(x: Float, y: Float): Boolean {
        return square((x - centerX).toDouble()) + square((y - centerY).toDouble()) <= square(radius.toDouble())
    }
    
    override fun moveShape(dx: Float, dy: Float) {
        super.moveShape(dx, dy)
        
        // and change the center as well to keep record
        centerX += dx
        centerY += dy
    }
    
    override fun rotateShape(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        super.rotateShape(centerOfRotationX, centerOfRotationY, angle)
        
        // and change the center as well to keep record
        
        val points = rotatePoint(centerX, centerY, centerOfRotationX, centerOfRotationY, angle)
        centerX = points[0]
        centerY = points[1]
    }
    
    companion object {
        
        fun isOverlap(anotherShape: Shape, centerX: Float, centerY: Float, radius: Float): Boolean {
            if (anotherShape is CircularShape) {
                return square((centerX - anotherShape.centerX).toDouble()) + square((centerY - anotherShape.centerY).toDouble()) <= square((radius + anotherShape.radius).toDouble())
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
                if (square((x1 - centerX).toDouble()) + square((y1 - centerY).toDouble()) <= square(radius.toDouble()) ||
                        square((x2 - centerX).toDouble()) + square((y2 - centerY).toDouble()) <= square(radius.toDouble()) ||
                        square((x3 - centerX).toDouble()) + square((y3 - centerY).toDouble()) <= square(radius.toDouble()))
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
            return ((square(getArea(centerX, centerY, x1, y1, x2, y2) * 2) / (square((x1 - x2).toDouble()) // square of distance from center to the edge of triangle.
                    + square((y1 - y2).toDouble())) // calculated by divide the area by length of the edge of triangle.
                    <= square(radius.toDouble())) // is overlap if it's smaller than square of radius.
                    && ((square((x1 - x2).toDouble()) // the above will consider edge as a straight line without ends
                    + square((y1 - y2).toDouble())) // that would be problematic as the edge of the triangle have ends.
                    >= abs((square((centerX - x2).toDouble()) // this would determent if the angle between
                    + square((centerY - y2).toDouble())) // the line from the center of circle to one of the end of the edge
                    
                    - square((centerX - x1).toDouble())// and the edge would be larger than 90 degree
                    
                    - square((centerY - y1).toDouble())))) // as pythagoras thingy.
            
            // another level of maintainability lol
            // this is pretty much the most unmaintainable code i ever wrote in this project
        }
        
        private fun getArea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Double {
            return if ((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2 < 0)
                (-(x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2).toDouble()
            else
                ((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2).toDouble()
        }
        
        var dynamicPerformanceIndex = 1.0

        fun getNumberOfEdges(radius: Float, dynamicPerformanceIndex: Double = 1.0): Int {
            val leftRightBottomTop = generateLeftRightBottomTop(widthInPixel / heightInPixel)
            val pixelOnRadius = (radius / abs(leftRightBottomTop[0] - leftRightBottomTop[1]) * widthInPixel + 0.5).toInt() // +0.5 for rounding
            val numberOfEdges = (PI / acos(1.0 - 0.2 / pixelOnRadius / (dynamicPerformanceIndex * CircularShape.dynamicPerformanceIndex)) / 2.0 + 0.5).toInt() * 2 /*
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