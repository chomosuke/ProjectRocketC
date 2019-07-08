package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.*

/**
 * Created by Shuang Li on 3/03/2018.
 */

class QuadrilateralShape// in order:
(vertex1: Vector, vertex2: Vector, vertex3: Vector, vertex4: Vector, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0 // as no special isOverlapToOverride method is provided.
    override var componentShapes: Array<Shape> = arrayOf(TriangularShape(vertex1, vertex2, vertex3, color, buildShapeAttr),
            TriangularShape(vertex1, vertex4, vertex3, color, buildShapeAttr))

    fun setQuadrilateralShapeCoords(vertex1: Vector, vertex2: Vector, vertex3: Vector, vertex4: Vector) { // in order:
        (componentShapes[0] as TriangularShape).setTriangleCoords(vertex1, vertex2, vertex3)
        (componentShapes[1] as TriangularShape).setTriangleCoords(vertex1, vertex4, vertex3)
    }

    val x1 get() = getQuadrilateralShapeCoords(QX1)
    val y1 get() = getQuadrilateralShapeCoords(QY1)
    val x2 get() = getQuadrilateralShapeCoords(QX2)
    val y2 get() = getQuadrilateralShapeCoords(QY2)
    val x3 get() = getQuadrilateralShapeCoords(QX3)
    val y3 get() = getQuadrilateralShapeCoords(QY3)
    val x4 get() = getQuadrilateralShapeCoords(QX4)
    val y4 get() = getQuadrilateralShapeCoords(QY4)
    fun getQuadrilateralShapeCoords(coord: Int): Float {
        return if (coord < 6)
            (componentShapes[0] as TriangularShape).getTriangularShapeCoords(coord)
        else
            (componentShapes[1] as TriangularShape).getTriangularShapeCoords(coord - 4)
    }
}

const val QX1 = 0
const val QY1 = 1
const val QX2 = 2
const val QY2 = 3
const val QX3 = 4
const val QY3 = 5
const val QX4 = 2 + 2 * COORDS_PER_VERTEX
const val QY4 = 3 + 2 * COORDS_PER_VERTEX

