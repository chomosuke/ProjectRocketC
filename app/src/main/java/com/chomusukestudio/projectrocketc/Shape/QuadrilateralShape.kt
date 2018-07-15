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
                red: Float, green: Float, blue: Float, alpha: Float, z: Float, visibility: Boolean) {
        componentShapes = arrayOf(TriangularShape(x1, y1, x2, y2, x3, y3, red, green, blue, alpha, z, visibility),
                TriangularShape(x1, y1, x4, y4, x3, y3, red, green, blue, alpha, z, visibility))
    }

    fun setQuadrilateralShapeCoords(x1: Float, y1: Float, // in order:
                                    x2: Float, y2: Float,
                                    x3: Float, y3: Float,
                                    x4: Float, y4: Float) {
        (componentShapes[0] as TriangularShape).setTriangleCoords(x1, y1, x2, y2, x3, y3)
        (componentShapes[1] as TriangularShape).setTriangleCoords(x1, y1, x4, y4, x3, y3)
    }

    constructor(coords1: FloatArray, coords2: FloatArray, red: Float, green: Float, blue: Float, alpha: Float, z: Float) {
        componentShapes = arrayOf(TriangularShape(coords1, red, green, blue, alpha, z, visibility),
                TriangularShape(coords2, red, green, blue, alpha, z, visibility))
    }// as no special isOverlapToOverride method is provided.

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

