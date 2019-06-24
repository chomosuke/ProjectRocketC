package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.*
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import kotlin.math.abs
import kotlin.math.sign

class TriangularShape(x1: Float, y1: Float,
                      x2: Float, y2: Float,
                      x3: Float, y3: Float,
                      red: Float, green: Float, blue: Float, alpha: Float, val buildShapeAttr: BuildShapeAttr) : Shape() {
    constructor(coords: FloatArray, red: Float, green: Float, blue: Float, alpha: Float, buildShapeAttr: BuildShapeAttr): this(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], red, green, blue, alpha, buildShapeAttr)

    override val isOverlapMethodLevel: Double = 0.0
    
    private var triangle: Triangle? = if (buildShapeAttr.visibility) GLTriangle(x1, y1, x2, y2, x3, y3, red, green, blue, alpha, buildShapeAttr) else null
    // nullable because sometimes invisible
    
    private var triangleCoords = /*if (visibility) FloatArray(6) else */floatArrayOf(x1, y1, x2, y2, x3, y3)
    private var RGBA = /*if (visibility) FloatArray(4) else */floatArrayOf(red, green, blue, alpha)

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

    override var visibility: Boolean = buildShapeAttr.visibility
        set(value) {
            if (field != value) {
                if (value) {
                    triangle = GLTriangle(triangleCoords, RGBA, buildShapeAttr.newAttrWithNewVisibility(visibility)/*visibility might have changed*/)
                } else {
                    triangleCoords = triangle!!.triangleCoords.floatArray
                    RGBA = triangle!!.RGBA.floatArray
                    triangle!!.removeTriangle()
                    triangle = null
                }
                field = value
            }
        }

    val z: Float = buildShapeAttr.z
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
        return sign(areaA1) == sign(areaA2) && sign(areaA1) == sign(areaA3)
        // https://stackoverflow.com/questions/13300904/determine-whether-point-lies-inside-triangle
        // https://www.geeksforgeeks.org/check-whether-a-given-point-lies-inside-a-triangle-or-not/
    }

    private fun getArea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
        return (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2
    }

    val x1 get() = getTriangularShapeCoords(X1)
    val y1 get() = getTriangularShapeCoords(Y1)
    val x2 get() = getTriangularShapeCoords(X2)
    val y2 get() = getTriangularShapeCoords(Y2)
    val x3 get() = getTriangularShapeCoords(X3)
    val y3 get() = getTriangularShapeCoords(Y3)
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

    override var removed = false
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
        removed = true
    }
}
