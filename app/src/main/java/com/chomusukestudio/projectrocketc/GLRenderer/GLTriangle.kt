package com.chomusukestudio.projectrocketc.GLRenderer

import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import java.util.*

class GLTriangle (z: Float) : Triangle() {

    val layer: Layer = getLayer(z) // the layer this triangle belong

    override val z: Float
        get() = layer.z

    private val coordPointer: Int = layer.getCoordPointer() // point to the first of the six layer.triangleCoords[] this triangle is using
    override val triangleCoords: Triangle.TriangleCoords = object : Triangle.TriangleCoords() {

        override var floatArray: FloatArray
            get() { return FloatArray(6) { i -> this[i] } }
            set(value) { System.arraycopy(layer.triangleCoords, coordPointer, value, 0, value.size) }

        override fun get(index: Int): Float {
            if (index < 6)
                return layer.triangleCoords[coordPointer + index]
            else
                throw IndexOutOfBoundsException("invalid index for getTriangleCoords: $index")
        }

        override fun set(index: Int, value: Float) {
            if (index < 6)
                layer.triangleCoords[coordPointer + index] = value
            else
                throw IndexOutOfBoundsException("invalid index for setTriangleCoords: $index")
        }
    }

    private val colorPointer = layer.getColorPointer(coordPointer)
    override val RGBA: Triangle.RGBAArray = object : Triangle.RGBAArray() {

        override var floatArray: FloatArray
            get() { return FloatArray(4) { i -> this[i] } }
            set(value) { System.arraycopy(layer.triangleCoords, colorPointer, value, 0, value.size) }

        override fun get(index: Int): Float {
            if (index < 12)
                return layer.colors[colorPointer + index]
            else
                throw IndexOutOfBoundsException("invalid index for getRGBAArray: $index")
        }
        override fun set(index: Int, value: Float) {
            if (index < 4) {
                layer.colors[colorPointer + index] = value
                layer.colors[colorPointer + index + 4] = value
                layer.colors[colorPointer + index + 8] = value
            } else {
                throw IndexOutOfBoundsException("invalid index for setRGBAArray: $index")
            }
        }
    }

    private fun getLayer(z: Float): Layer {
        for (i in layers.indices) {
            if (layers[i].z == z) {
                return layers[i] // find the layer with that z
            }
        }

        // there is no layer with that z so create one and return index of that layer
        val newLayer = Layer(z)
        var i = 0
        while (true) {
            if (i == layers.size) {
                // already the last one
                layers.add(newLayer)
                break
            }
            if (newLayer.z > layers[i].z) {
                // if the new z is just bigger than this z
                // put it before this layer
                layers.add(i, newLayer)
                break
            }
            i++
        }
        return newLayer
    }

    constructor(x1: Float, y1: Float,
                x2: Float, y2: Float,
                x3: Float, y3: Float,
                red: Float, green: Float, blue: Float, alpha: Float, z: Float) : this(z) {
        setTriangleCoords(x1, y1, x2, y2, x3, y3)

        setTriangleRGBA(red, green, blue, alpha)
    }// as no special isOverlapToOverride method is provided.

    constructor(coords: FloatArray, red: Float, green: Float, blue: Float, alpha: Float, z: Float) : this(z) {
        System.arraycopy(coords, 0, layer.triangleCoords, coordPointer, coords.size)
        setTriangleRGBA(red, green, blue, alpha)
    }

    constructor(coords: FloatArray, color: FloatArray, z: Float) : this(z) {
        System.arraycopy(coords, 0, layer.triangleCoords, coordPointer, coords.size)
        System.arraycopy(color, 0, layer.colors, colorPointer, color.size)
        System.arraycopy(color, 0, layer.colors, colorPointer + 4, color.size)
        System.arraycopy(color, 0, layer.colors, colorPointer + 8, color.size)
    }

    fun rotateTriangle(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        var i = 0
        while (i < CPT) {
            // rotate score
            val result = rotatePoint(triangleCoords[i], triangleCoords[i + 1], centerOfRotationX, centerOfRotationY, angle)
            triangleCoords[i] = result[0]
            triangleCoords[i + 1] = result[1]
            i += COORDS_PER_VERTEX
        }
    }

    override fun moveTriangle(dx: Float, dy: Float) {
        layer.triangleCoords[X1 + coordPointer] += dx
        layer.triangleCoords[Y1 + coordPointer] += dy
        layer.triangleCoords[X2 + coordPointer] += dx
        layer.triangleCoords[Y2 + coordPointer] += dy
        layer.triangleCoords[X3 + coordPointer] += dx
        layer.triangleCoords[Y3 + coordPointer] += dy
    }

    override fun setTriangleCoords(x1: Float, y1: Float,
                          x2: Float, y2: Float,
                          x3: Float, y3: Float) {
        layer.triangleCoords[X1 + coordPointer] = x1
        layer.triangleCoords[Y1 + coordPointer] = y1
        layer.triangleCoords[X2 + coordPointer] = x2
        layer.triangleCoords[Y2 + coordPointer] = y2
        layer.triangleCoords[X3 + coordPointer] = x3
        layer.triangleCoords[Y3 + coordPointer] = y3
    }

    override fun setTriangleRGBA(red: Float, green: Float, blue: Float, alpha: Float) {
        layer.colors[0 + colorPointer] = red
        layer.colors[1 + colorPointer] = green
        layer.colors[2 + colorPointer] = blue
        layer.colors[3 + colorPointer] = alpha
        layer.colors[4 + colorPointer] = red
        layer.colors[5 + colorPointer] = green
        layer.colors[6 + colorPointer] = blue
        layer.colors[7 + colorPointer] = alpha
        layer.colors[8 + colorPointer] = red
        layer.colors[9 + colorPointer] = green
        layer.colors[10 + colorPointer] = blue
        layer.colors[11 + colorPointer] = alpha
    }

    override fun removeTriangle() {
        // mark coords as unused
        if (removed)
            throw RuntimeException("triangle is already removed")
        setTriangleCoords(UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, UNUSED)
        removed = true
    }
    private var removed = false
    protected fun finalize() {
        if (!removed)
            throw RuntimeException("triangle isn't removed")
    }

    companion object {
        val layers = ArrayList<Layer>()

//        fun offsetAllLayers(dOffsetX: Float, dOffsetY: Float) {
//            for (layer in layers)
//                layer.offsetLayer(dOffsetX, dOffsetY)
//        }
        
        fun drawAllTriangles() {
            // no need to sort, already in order
            for (i in layers.indices) { // draw layers in order
                // keep for loop instead foreach to enforce the idea of order
                layers[i].drawLayer()
            }
        }
        
        fun passArraysToBuffers() {
            for (layer in layers)
                layer.passArraysToBuffers()
        }
    }
}
