package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.*
import kotlin.math.sign

class TriangularShape(vertex1: Vector, vertex2: Vector, vertex3: Vector,
                      color: Color, val buildShapeAttr: BuildShapeAttr) : Shape() {

    override val isOverlapMethodLevel: Double = 0.0
    
    private var triangle: GLTriangle? = if (buildShapeAttr.visibility) GLTriangle(vertex1.x, vertex1.y, vertex2.x, vertex2.y, vertex3.x, vertex3.y,
            color.red, color.green, color.blue, color.alpha, buildShapeAttr) else null
    // nullable because sometimes invisible
    
    private var triangleCoords = /*if (visibility) FloatArray(6) else */floatArrayOf(vertex1.x, vertex1.y, vertex2.x, vertex2.y, vertex3.x, vertex3.y)
    private var RGBA = /*if (visibility) FloatArray(4) else */floatArrayOf(color.red, color.green, color.blue, color.alpha)

    override val shapeColor: Color
        get() =
            if (visibility)
                Color(triangle!!.RGBA[0], triangle!!.RGBA[1], triangle!!.RGBA[2], triangle!!.RGBA[3])
            else
                Color(RGBA[0], RGBA[1], RGBA[2], RGBA[3])

    override fun resetAlpha(alpha: Float) {
        if (visibility)
            triangle!!.RGBA[3] = alpha
        else
            RGBA[3] = alpha
    }

    override fun changeShapeColor(dRed: Float, dGreen: Float, dBlue: Float, dAlpha: Float) {
        if (visibility) {
            triangle!!.RGBA[0] += dRed
            triangle!!.RGBA[1] += dGreen
            triangle!!.RGBA[2] += dBlue
            triangle!!.RGBA[3] += dAlpha
        } else {
            RGBA[0] += dRed
            RGBA[1] += dGreen
            RGBA[2] += dBlue
            RGBA[3] += dAlpha
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
                if (this.isInside(anotherComponentShape.vertex1) ||
                        this.isInside(anotherComponentShape.vertex2) ||
                        this.isInside(anotherComponentShape.vertex3) ||
                        anotherComponentShape.isInside(vertex1) ||
                        anotherComponentShape.isInside(vertex2) ||
                        anotherComponentShape.isInside(vertex3)) {
                    return true
                }
            } else {
                // call the function again
                isOverlap(anotherComponentShape)
            }
        }
        return false
    }

    override fun isInside(vector: Vector): Boolean {
        val areaA1 = getArea(vector.x, vector.y,
                vertex2.x, vertex2.y,
                vertex3.x, vertex3.y)
        val areaA2 = getArea(vertex1.x, vertex1.y,
                vector.x, vector.y,
                vertex3.x, vertex3.y)
        val areaA3 = getArea(vertex1.x, vertex1.y,
                vertex2.x, vertex2.y,
                vector.x, vector.y)
        return sign(areaA1) == sign(areaA2) && sign(areaA1) == sign(areaA3)
        // https://stackoverflow.com/questions/13300904/determine-whether-point-lies-inside-triangle
        // https://www.geeksforgeeks.org/check-whether-a-given-point-lies-inside-a-triangle-or-not/
    }

    private fun getArea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
        return (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2
    }
    
    val vertex1 get() = getTriangularShapeCoords(0)
    val vertex2 get() = getTriangularShapeCoords(1)
    val vertex3 get() = getTriangularShapeCoords(2)
    private fun getTriangularShapeCoords(coord: Int): Vector {
        return if (visibility)
            Vector(triangle!!.triangleCoords[coord * 2], triangle!!.triangleCoords[coord * 2 + 1])
        else {
            Vector(triangleCoords[coord * 2], triangleCoords[coord * 2 + 1])
        }
    }

    fun setTriangleCoords(vertex1: Vector, vertex2: Vector, vertex3: Vector) {
        if (visibility) {
            triangle!!.setTriangleCoords(vertex1.x, vertex1.y, vertex2.x, vertex2.y, vertex3.x, vertex3.y)
        }
        else {
            triangleCoords[0] = vertex1.x
            triangleCoords[1] = vertex1.y
            triangleCoords[2] = vertex2.x
            triangleCoords[3] = vertex2.y
            triangleCoords[4] = vertex3.x
            triangleCoords[5] = vertex3.y
        }
    }

    override fun moveShape(displacement: Vector) {
        val dx = displacement.x
        val dy = displacement.y
        if (visibility) {
            triangle!!.moveTriangle(dx, dy)
        }
        else {
            triangleCoords[0] += dx
            triangleCoords[1] += dy
            triangleCoords[2] += dx
            triangleCoords[3] += dy
            triangleCoords[4] += dx
            triangleCoords[5] += dy
        }
    }

    override fun rotateShape(centerOfRotation: Vector, angle: Float) {
        if (visibility) {
            var i = 0
            while (i < CPT) {
                // rotate score
                val result = Vector(triangle!!.triangleCoords[i], triangle!!.triangleCoords[i + 1]).rotateVector(centerOfRotation, angle)
                triangle!!.triangleCoords[i] = result.x
                triangle!!.triangleCoords[i + 1] = result.y
                i += COORDS_PER_VERTEX
            }
        }
        else {
            var i = 0
            while (i < CPT) {
                // rotate point
                val result = Vector(triangleCoords[i], triangleCoords[i + 1]).rotateVector(centerOfRotation, angle)
                triangleCoords[i] = result.x
                triangleCoords[i + 1] = result.y
                i += COORDS_PER_VERTEX
            }
        }
    }

    override fun resetShapeColor(color: Color) {
        if (visibility) {
            triangle!!.setTriangleRGBA(color.red, color.green, color.blue, color.alpha)
        } else {
            RGBA[0] = color.red
            RGBA[1] = color.green
            RGBA[2] = color.blue
            RGBA[3] = color.alpha
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
