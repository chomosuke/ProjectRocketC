package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.*

/**
 * Created by Shuang Li on 3/03/2018.
 */

class QuadrilateralShape : Shape {
    override val isOverlapMethodLevel: Double = 0.0 // as no special isOverlapToOverride method is provided.
    override var componentShapes: Array<Shape>

    constructor(x1: Float, y1: Float, // in order:
                x2: Float, y2: Float,
                x3: Float, y3: Float,
                x4: Float, y4: Float,
                color: Color, buildShapeAttr: BuildShapeAttr) {
        componentShapes = arrayOf(TriangularShape(x1, y1, x2, y2, x3, y3, color, buildShapeAttr),
                TriangularShape(x1, y1, x4, y4, x3, y3, color, buildShapeAttr))
    }

    fun setQuadrilateralShapeCoords(x1: Float, y1: Float, // in order:
                                    x2: Float, y2: Float,
                                    x3: Float, y3: Float,
                                    x4: Float, y4: Float) {
        (componentShapes[0] as TriangularShape).setTriangleCoords(x1, y1, x2, y2, x3, y3)
        (componentShapes[1] as TriangularShape).setTriangleCoords(x1, y1, x4, y4, x3, y3)
    }

    constructor(coords1: FloatArray, coords2: FloatArray, color: Color, buildShapeAttr: BuildShapeAttr) {
        componentShapes = arrayOf(TriangularShape(coords1, color, buildShapeAttr),
                TriangularShape(coords2, color, buildShapeAttr))
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
        
const val QX1 = X1
const val QY1 = Y1
const val QX2 = X2
const val QY2 = Y2
const val QX3 = X3
const val QY3 = Y3
const val QX4 = X2 + 2 * COORDS_PER_VERTEX
const val QY4 = Y2 + 2 * COORDS_PER_VERTEX

