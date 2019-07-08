package com.chomusukestudio.projectrocketc.GLRenderer

import android.util.Log
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr

class GLTriangle (buildShapeAttr: BuildShapeAttr) {

    val layer: Layer = getLayer(buildShapeAttr.z, buildShapeAttr.layers) // the layer this triangle belong

    val z: Float
        get() = layer.z

    private val coordPointer: Int = layer.getCoordPointer() // point to the first of the six layer.triangleCoords[] this triangle is isInUse
    val triangleCoords: TriangleCoords = object : TriangleCoords() {

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
    val RGBA: RGBAArray = object : RGBAArray() {

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

    private fun getLayer(z: Float, layers: Layers): Layer {
        with(layers) {
            for (i in arrayList.indices) {
                if (arrayList[i].z == z) {
                    return arrayList[i] // find the layer with that z
                }
            }

            // there is no layer with that z so create one and return index of that layer
            val newLayer = Layer(z)
            var i = 0
            while (true) {
                if (i == arrayList.size) {
                    // already the last one
                    lockOnArrayList.lock()
                    arrayList.add(newLayer)
                    lockOnArrayList.unlock()
                    break
                }
                if (newLayer.z > arrayList[i].z) {
                    // if the new z is just bigger than this z
                    // put it before this layer
                    lockOnArrayList.lock()
                    arrayList.add(i, newLayer)
                    lockOnArrayList.unlock()
                    break
                }
                i++
            }
            return newLayer
        }
    }

    constructor(x1: Float, y1: Float,
                x2: Float, y2: Float,
                x3: Float, y3: Float,
                red: Float, green: Float, blue: Float, alpha: Float, buildShapeAttr: BuildShapeAttr) : this(buildShapeAttr) {
        setTriangleCoords(x1, y1, x2, y2, x3, y3)

        setTriangleRGBA(red, green, blue, alpha)
    }// as no special isOverlapToOverride method is provided.

    constructor(coords: FloatArray, red: Float, green: Float, blue: Float, alpha: Float, buildShapeAttr: BuildShapeAttr) : this(buildShapeAttr) {
        System.arraycopy(coords, 0, layer.triangleCoords, coordPointer, coords.size)
        setTriangleRGBA(red, green, blue, alpha)
    }

    constructor(coords: FloatArray, color: FloatArray, buildShapeAttr: BuildShapeAttr) : this(buildShapeAttr) {
        System.arraycopy(coords, 0, layer.triangleCoords, coordPointer, coords.size)
        System.arraycopy(color, 0, layer.colors, colorPointer, color.size)
        System.arraycopy(color, 0, layer.colors, colorPointer + 4, color.size)
        System.arraycopy(color, 0, layer.colors, colorPointer + 8, color.size)
    }

    fun moveTriangle(dx: Float, dy: Float) {
        layer.triangleCoords[0 + coordPointer] += dx
        layer.triangleCoords[1 + coordPointer] += dy
        layer.triangleCoords[2 + coordPointer] += dx
        layer.triangleCoords[3 + coordPointer] += dy
        layer.triangleCoords[4 + coordPointer] += dx
        layer.triangleCoords[5 + coordPointer] += dy
    }

    fun setTriangleCoords(x1: Float, y1: Float,
                          x2: Float, y2: Float,
                          x3: Float, y3: Float) {
        layer.triangleCoords[0 + coordPointer] = x1
        layer.triangleCoords[1 + coordPointer] = y1
        layer.triangleCoords[2 + coordPointer] = x2
        layer.triangleCoords[3 + coordPointer] = y2
        layer.triangleCoords[4 + coordPointer] = x3
        layer.triangleCoords[5 + coordPointer] = y3
    }

    fun setTriangleRGBA(red: Float, green: Float, blue: Float, alpha: Float) {
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

    fun removeTriangle() {
        // mark coords as unused
        if (removed)
            throw RuntimeException("triangle is already removed")
        setTriangleCoords(UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, UNUSED)
        removed = true
    }
    private var removed = false
    protected fun finalize() {
        if (!removed) {
            removeTriangle()
            Log.e("triangle finalizer", "triangle isn't removed")
        }
    }
    
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
