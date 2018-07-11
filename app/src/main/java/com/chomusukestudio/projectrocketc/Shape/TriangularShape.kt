package com.chomusukestudio.projectrocketc.Shape

import android.util.Log
import com.chomusukestudio.projectrocketc.GLRenderer.*
import com.chomusukestudio.projectrocketc.Shape.point.rotatePoint

class TriangularShape(x1: Float, y1: Float,
                      x2: Float, y2: Float,
                      x3: Float, y3: Float,
                      red: Float, green: Float, blue: Float, alpha: Float, z: Float) : Shape() {
    constructor(coords: FloatArray, red: Float, green: Float, blue: Float, alpha: Float, z: Float): this(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], red, green, blue, alpha, z)

    override val isOverlapMethodLevel: Double = 0.0
    
    private var triangle: Triangle? = GLTriangle(x1, y1, x2, y2, x3, y3, red, green, blue, alpha, z)
    // nullable because sometimes invisible
    
    private var triangleCoords = FloatArray(6)
    private var RGBA = FloatArray(4)

    override val shapeColor: FloatArray
        get() =
            if (visibility)
                triangle!!.RGBA.floatArray
            else
                RGBA

    override fun resetAlpha(alpha: Float) {
        if (visibility)
            triangle!!.RGBA[3] = alpha
        else
            RGBA[3] = alpha
    }

    override fun changeShapeColor(red: Float, green: Float, blue: Float, alpha: Float) {
        if (visibility) {
            triangle!!.RGBA[0] += red
            triangle!!.RGBA[1] += green
            triangle!!.RGBA[2] += blue
            triangle!!.RGBA[3] += alpha
        } else {
            RGBA[0] += red
            RGBA[1] += green
            RGBA[2] += blue
            RGBA[3] += alpha
        }
    }
    
    override var componentShapes: Array<Shape> = arrayOf(this)
        set(value) {
            throw IllegalAccessException("TriangularShape itself is the componentShape of itself")
        }
    
    override val size: Int = 1
    
    fun getTriangularShapeCoords() = if (visibility) {
        triangle!!.triangleCoords.floatArray
    } else {
        triangleCoords
    }
    
    override var visibility: Boolean = true
        set(visibility) {
            if (field != visibility) {
                if (visibility) {
                    triangle = GLTriangle(triangleCoords, RGBA, z)
                } else {
                    triangleCoords = triangle!!.triangleCoords.floatArray
                    RGBA = triangle!!.RGBA.floatArray
                    triangle!!.removeTriangle()
//                    triangle = null
                }
                field = visibility
            }
        }
    
    val z: Float = z
        get() = if (visibility) triangle!!.z else field

    public override fun isOverlapToOverride(anotherShape: Shape): Boolean {
        for (anotherComponentShape in anotherShape.componentShapes) {
            if (anotherComponentShape is TriangularShape) {
                if (this.isInside(anotherComponentShape.getTriangularShapeCoords(0), anotherComponentShape.getTriangularShapeCoords(1)) ||
                        this.isInside(anotherComponentShape.getTriangularShapeCoords(2), anotherComponentShape.getTriangularShapeCoords(3)) ||
                        this.isInside(anotherComponentShape.getTriangularShapeCoords(4), anotherComponentShape.getTriangularShapeCoords(5)) ||
                        anotherComponentShape.isInside(this.getTriangularShapeCoords(0), this.getTriangularShapeCoords(1)) ||
                        anotherComponentShape.isInside(this.getTriangularShapeCoords(2), this.getTriangularShapeCoords(3)) ||
                        anotherComponentShape.isInside(this.getTriangularShapeCoords(4), this.getTriangularShapeCoords(5))) { // close for modification
                    return true
                }
            } else {
                // call the function again
                isOverlap(anotherComponentShape)
            }
        }
        return false
    }
    
    override fun isInside(x: Float, y: Float): Boolean { // close for modification
        val areaA = getArea(this.getTriangularShapeCoords(0),
                this.getTriangularShapeCoords(1),
                this.getTriangularShapeCoords(2),
                this.getTriangularShapeCoords(3),
                this.getTriangularShapeCoords(4),
                this.getTriangularShapeCoords(5))
        val areaA1 = getArea(x, y,
                this.getTriangularShapeCoords(2),
                this.getTriangularShapeCoords(3),
                this.getTriangularShapeCoords(4),
                this.getTriangularShapeCoords(5))
        val areaA2 = getArea(this.getTriangularShapeCoords(0),
                this.getTriangularShapeCoords(1),
                x, y,
                this.getTriangularShapeCoords(4),
                this.getTriangularShapeCoords(5))
        val areaA3 = getArea(this.getTriangularShapeCoords(0),
                this.getTriangularShapeCoords(1),
                this.getTriangularShapeCoords(2),
                this.getTriangularShapeCoords(3),
                x, y)
        return areaA == areaA1 + areaA2 + areaA3
        // https://stackoverflow.com/questions/13300904/determine-whether-point-lies-inside-triangle
        // https://www.geeksforgeeks.org/check-whether-a-given-point-lies-inside-a-triangle-or-not/
    }
    
    private fun getArea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
        return if ((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2 < 0)
            -(x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2
        else
            (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2
    }
    
    fun getTriangularShapeCoords(coord: Int): Float {
        return if (visibility)
            triangle!!.triangleCoords[coord]
        else {
            triangleCoords[coord]
        }
    }
    
    fun setTriangleCoords(x1: Float, y1: Float,
                          x2: Float, y2: Float,
                          x3: Float, y3: Float) {
        if (visibility) {
            triangle!!.setTriangleCoords(x1, y1, x2, y2, x3, y3)
        }
        else {
            triangleCoords[X1] = x1
            triangleCoords[Y1] = y1
            triangleCoords[X2] = x2
            triangleCoords[Y2] = y2
            triangleCoords[X3] = x3
            triangleCoords[Y3] = y3
        }
    }
    
    override fun moveShape(dx: Float, dy: Float) {
        if (visibility) {
            triangle!!.moveTriangle(dx, dy)
        }
        else {
            triangleCoords[X1] += dx
            triangleCoords[Y1] += dy
            triangleCoords[X2] += dx
            triangleCoords[Y2] += dy
            triangleCoords[X3] += dx
            triangleCoords[Y3] += dy
        }
    }
    
    override fun rotateShape(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        if (visibility) {
            var i = 0
            while (i < CPT) {
                // rotate score
                val result = rotatePoint(triangle!!.triangleCoords[i], triangle!!.triangleCoords[i + 1], centerOfRotationX, centerOfRotationY, angle)
                triangle!!.triangleCoords[i] = result[0]
                triangle!!.triangleCoords[i + 1] = result[1]
                i += COORDS_PER_VERTEX
            }
        }
        else {
            var i = 0
            while (i < CPT) {
                // rotate point
                val result = rotatePoint(triangleCoords[i], triangleCoords[i + 1], centerOfRotationX, centerOfRotationY, angle)
                triangleCoords[i] = result[0]
                triangleCoords[i + 1] = result[1]
                i += COORDS_PER_VERTEX
            }
        }
    }
    
    override fun resetShapeColor(red: Float, green: Float, blue: Float, alpha: Float) {
        if (visibility) {
            triangle!!.setTriangleRGBA(red, green, blue, alpha)
        } else {
            RGBA[0] = red
            RGBA[1] = green
            RGBA[2] = blue
            RGBA[3] = alpha
        }
    }
    
    override fun removeShape() {
        if (visibility) {
            triangle!!.removeTriangle()
            triangle = null
        } else {
            triangleCoords[0] = UNUSED
            triangleCoords[1] = UNUSED
            triangleCoords[2] = UNUSED
            triangleCoords[3] = UNUSED
            triangleCoords[4] = UNUSED
            triangleCoords[5] = UNUSED
            RGBA[0] = UNUSED
            RGBA[1] = UNUSED
            RGBA[2] = UNUSED
            RGBA[3] = UNUSED
        }
    }

    override fun getZs(): ArrayList<Float> {
        val z = ArrayList<Float>()
        z.add(this.z)
        return z
    }
}
