package com.chomusukestudio.projectrocketc.GLRenderer

abstract class Triangle {
    abstract val triangleCoords: TriangleCoords
    abstract val RGBA: RGBAArray
    abstract val z: Float
    abstract fun removeTriangle()
    abstract fun moveTriangle(dx: Float, dy: Float) // for per
    abstract class TriangleCoords {
        abstract operator fun get(index: Int): Float
        abstract operator fun set(index: Int, value: Float)
        abstract val floatArray : FloatArray
    }
    abstract class RGBAArray {
        abstract operator fun get(index: Int): Float
        abstract operator fun set(index: Int, value: Float)
        abstract val floatArray : FloatArray
    }
    abstract fun setTriangleCoords(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float)
    abstract fun setTriangleRGBA(red: Float, green: Float, blue: Float, alpha: Float)
}

const val X1 = 0
const val Y1 = 1
const val X2 = 2
const val Y2 = 3
const val X3 = 4
const val Y3 = 5