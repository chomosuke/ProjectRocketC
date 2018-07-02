package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.COORDS_PER_VERTEX

/**
 * Created by Shuang Li on 3/03/2018.
 */

class QuadrilateralShape : Shape {
    constructor(x1: Float, y1: Float, // in order:
                x2: Float, y2: Float,
                x3: Float, y3: Float,
                x4: Float, y4: Float,
                red: Float, green: Float, blue: Float, alpha: Float, z: Float) : super(0.0) {
        
        componentShapes = arrayOfNulls<TriangularShape>(2)
        componentShapes[0] = TriangularShape(x1, y1, x2, y2, x3, y3, red, green, blue, alpha, z)
        componentShapes[1] = TriangularShape(x1, y1, x4, y4, x3, y3, red, green, blue, alpha, z)
    }// as no special isOverlapToOverride method is provided.
    
    fun setQuadrilateralShapeCoords(x1: Float, y1: Float, // in order:
                                    x2: Float, y2: Float,
                                    x3: Float, y3: Float,
                                    x4: Float, y4: Float) {
        (componentShapes!![0] as TriangularShape).setTriangleCoords(x1, y1, x2, y2, x3, y3)
        (componentShapes!![1] as TriangularShape).setTriangleCoords(x1, y1, x4, y4, x3, y3)
    }
    
    constructor(coords1: FloatArray, coords2: FloatArray, red: Float, green: Float, blue: Float, alpha: Float, z: Float) : super(0.0) {
        
        componentShapes = arrayOfNulls<TriangularShape>(2)
        componentShapes[0] = TriangularShape(coords1, red, green, blue, alpha, z)
        componentShapes[1] = TriangularShape(coords2, red, green, blue, alpha, z)
    }// as no special isOverlapToOverride method is provided.
    
    fun getQuadrilateralShapeCoords(coord1: Boolean): FloatArray {
        return if (coord1)
            (componentShapes!![0] as TriangularShape).triangularShapeCoords
        else
            (componentShapes!![1] as TriangularShape).triangularShapeCoords
    }
    
    fun getQuadrilateralShapeCoords(coord: Int): Float {
        return if (coord < 6)
            (componentShapes!![0] as TriangularShape).getTriangularShapeCoords(coord)
        else
            (componentShapes!![1] as TriangularShape).getTriangularShapeCoords(coord - 4)
    }
    
    companion object {
        
        val X1 = TriangularShape.X1
        val Y1 = TriangularShape.Y1
        val X2 = TriangularShape.X2
        val Y2 = TriangularShape.Y2
        val X3 = TriangularShape.X3
        val Y3 = TriangularShape.Y3
        val X4 = TriangularShape.X2 + 2 * COORDS_PER_VERTEX
        val Y4 = TriangularShape.Y2 + 2 * COORDS_PER_VERTEX
    }
}

