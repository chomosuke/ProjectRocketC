package com.chomusukestudio.projectrocketc.GLRenderer

interface Triangle {
    val triangleCoords: TriangleCoords
    val RGBA: RGBAArray
    val z: Float
    fun removeTriangle()
    interface TriangleCoords {
        operator fun get(index: Int): Float
        operator fun set(index: Int, value: Float)
        fun getFloatArray() : FloatArray
    }
    interface RGBAArray {
        operator fun get(index: Int): Float
        operator fun set(index: Int, value: Float)
        fun getFloatArray() : FloatArray
    }
}

const val X1 = 0
const val Y1 = 1
const val X2 = 2
const val Y2 = 3
const val X3 = 4
const val Y3 = 5