package com.chomusukestudio.projectrocketc.GLRenderer

abstract class Triangle {
    abstract val triangleCoords: TriangleCoords
    abstract val RGBA: RGBAArray
    abstract val z: Float
    abstract fun removeTriangle()
    abstract fun moveTriangle(dx: Float, dy: Float) // for performance
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
}

const val X1 = 0
const val Y1 = 1
const val X2 = 2
const val Y2 = 3
const val X3 = 4
const val Y3 = 5