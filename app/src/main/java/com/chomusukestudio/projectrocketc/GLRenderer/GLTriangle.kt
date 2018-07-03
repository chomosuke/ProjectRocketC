package com.chomusukestudio.projectrocketc.GLRenderer

import android.opengl.GLES20.GL_NO_ERROR
import android.opengl.GLES20
import android.util.Log
import com.chomusukestudio.projectrocketc.Shape.point.rotatePoint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

class GLTriangle (x1: Float, y1: Float,
                  x2: Float, y2: Float,
                  x3: Float, y3: Float,
                  red: Float, green: Float, blue: Float, alpha: Float, z: Float) : Triangle {
    
    override val triangleCoords: Triangle.TriangleCoords = object : Triangle.TriangleCoords {
        override fun getFloatArray(): FloatArray {
            return FloatArray(CPT) { i -> this[i] }
        }

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
    
    private val layer: Layer = getLayer(z) // the layer this triangle belong
    private val coordPointer: Int = layer.getCoordPointer() // point to the first of the six layer.triangleCoords[] this triangle is using
    private val colorPointer: Int = layer.getColorPointer(coordPointer) // point to the first of the twelve layer.colors[] this triangle is using
    
    override val z: Float
        get() = layer.z
    
    override val RGBA: Triangle.RGBAArray = object : Triangle.RGBAArray {
        override fun getFloatArray() : FloatArray {
            return FloatArray(12) { i -> this[i] }
        }

        override fun get(index: Int): Float {
            if (index < 12) {
                return layer.colors[colorPointer + index]
            } else {
                throw IndexOutOfBoundsException("invalid index for getRGBAArray: $index")
            }
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
    
    init {
        triangleCoords[X1] = x1
        triangleCoords[Y1] = y1
        triangleCoords[X2] = x2
        triangleCoords[Y2] = y2
        triangleCoords[X3] = x3
        triangleCoords[Y3] = y3
        
        RGBA[0] = red
        RGBA[1] = green
        RGBA[2] = blue
        RGBA[3] = alpha
    }// as no special isOverlapToOverride method is provided.
    
    constructor(coords: FloatArray, red: Float, green: Float, blue: Float, alpha: Float, z: Float)
            : this(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], red, green, blue, alpha, z)
    
    constructor(coords: FloatArray, color: FloatArray, z: Float)
            : this(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], color[0], color[1], color[2], color[3], z)
    
    fun rotateTriangle(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        var i = 0
        while (i < CPT) {
            // rotate score
            val result = rotatePoint(layer.triangleCoords[i + coordPointer], layer.triangleCoords[i + 1 + coordPointer], centerOfRotationX, centerOfRotationY, angle)
            layer.triangleCoords[i + coordPointer] = result[0]
            layer.triangleCoords[i + 1 + coordPointer] = result[1]
            i += COORDS_PER_VERTEX
        }
    }

    fun getBaseTriangleShapeCoords(coord: Int): Float {
        return layer.triangleCoords[coord + coordPointer]
    }

    fun getShapeColor(colorCode: Int): Float {
        return layer.colors[colorCode + colorPointer]
    }
    
    override fun removeTriangle() {
        // mark coords as unused
        triangleCoords[X1] = UNUSED
        triangleCoords[Y1] = UNUSED
        triangleCoords[X2] = UNUSED
        triangleCoords[Y2] = UNUSED
        triangleCoords[X3] = UNUSED
        triangleCoords[Y3] = UNUSED
        // mark colors as unused
        RGBA[0] = UNUSED
        RGBA[1] = UNUSED
        RGBA[2] = UNUSED
        RGBA[3] = UNUSED
    }
    
    companion object {
        private val layers = ArrayList<Layer>()
        
        fun drawAllTriangles(mvpMatrix: FloatArray) {
            // no need to sort, already in order
            for (i in layers.indices) { // draw layers in order
                // keep for loop instead foreach to enforce the idea of order
                layers[i].drawLayer(mvpMatrix)
            }
        }
        
        fun passArraysToBuffers() {
            for (i in layers.indices)
                layers[i].passArraysToBuffers()
        }
    }
}

// if any layer.triangleCoords[i1] or layer.colors[i2] contain this value then it's unused
const val UNUSED = -107584485858583778999908789293009999999f
const val COORDS_PER_VERTEX = 2
const val CPT = COORDS_PER_VERTEX * 3 // number of coordinates per vertex in this array

class Layer(val z: Float) { // depth for the drawing order
    
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    
    private var vertexBuffer: FloatBuffer
    private var colorBuffer: FloatBuffer
    
    private var size = 100 // number of triangle (including unused) in this layer
    // drawing order from the one with the largest z value to the one with the smallest z value
    
    var triangleCoords: FloatArray // coordinate of triangles
    
    private var vertexCount: Int = 300// number of vertex for each layer
    // number of triangle times 3
    
    var colors: FloatArray // colors of triangles
    
    private var lastUsedCoordIndex = 0 // should increase performance by ever so slightly, isn't really necessary.
    /* vertexCount <= size * 3*/// if lastUsedCoordIndex is reached vertexCount then bring it back to 0
    // if all before vertexCount does not have unused triangle left
    // log it
    //                else
    //                    Log.v("number of triangle draw", "" + layer.vertexCount / 3);
    // return the new unused triangle
    // found an unused coords
    @Synchronized fun getCoordPointer(): Int {
        var i = 0
        while (true) {
            
            if (lastUsedCoordIndex >= vertexCount * COORDS_PER_VERTEX)
                lastUsedCoordIndex = 0
            
            if (i >= vertexCount * COORDS_PER_VERTEX) {
                lastUsedCoordIndex = incrementVertexCountAndGiveNewCoordsPointer()
                
                return lastUsedCoordIndex
            }
            
            if (triangleCoords[lastUsedCoordIndex] == UNUSED) {
                return lastUsedCoordIndex
            }
            
            lastUsedCoordIndex += CPT
            i += CPT
        }
    }
    
    init {
        triangleCoords = FloatArray(size * CPT)
        colors = FloatArray(size * 12)
        
        // mark ALL colors and triangleCoords as UNUSED
        for (i in triangleCoords.indices)
            triangleCoords[i] = UNUSED
        for (i in colors.indices)
            colors[i] = UNUSED
        
        // setupBuffers
        val bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating score buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()

        // initialize vertex byte buffer for shape coordinates
        val bb2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                colors.size * 4)
        // use the device hardware's native byte order
        bb2.order(ByteOrder.nativeOrder())

        // create a floating score buffer from the ByteBuffer
        colorBuffer = bb2.asFloatBuffer()
    }
    
    companion object {
        // create empty OpenGL ES Program
        private var mProgram: Int = -1000

        fun initializeTriangularShapeClass() {
            mProgram = GLES20.glCreateProgram()
            // can't do in the declaration as will return 0 because not everything is prepared

            val vertexShader = TheGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                    vertexShaderCode)

            val fragmentShader = TheGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCode)

            // add the vertex shader to program
            GLES20.glAttachShader(mProgram, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(mProgram, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(mProgram)
        }
        
        private const val vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +

                        "attribute vec4 vPosition;" +
                        "attribute vec4 aColor;" +

                        "varying vec4 vColor;" +

                        "void main() {" +
                        "vColor = aColor;" +
                        // the matrix must be included as a modifier of gl_Position
                        // Note that the uMVPMatrix factor *must be first* in order
                        // for the matrix multiplication product to be correct.
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}"
        
        private const val fragmentShaderCode =
                "precision mediump float;" +
                "varying vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"
    }
    
    private fun setupBuffers() {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())
        
        // create a floating score buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()
        
        // initialize vertex byte buffer for shape coordinates
        val bb2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                colors.size * 4)
        // use the device hardware's native byte order
        bb2.order(ByteOrder.nativeOrder())
        
        // create a floating score buffer from the ByteBuffer
        colorBuffer = bb2.asFloatBuffer()
    }
    
    fun passArraysToBuffers() {
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords)
        // set the buffer to read the first coordinate
        vertexBuffer.position(0)
        // add the coordinates to the FloatBuffer
        colorBuffer.put(colors)
        // set the buffer to read the first coordinate
        colorBuffer.position(0)
    }
    
    fun getColorPointer(coordPointer: Int): Int {
        return coordPointer / CPT * 12
    }
    
    private fun increaseSize() {
        size = (size * 1.5).toInt()
        
        // store arrays
        val oldTriangleCoords = triangleCoords
        val oldColor = colors
        
        // create new arrays with new size
        triangleCoords = FloatArray(CPT * size + 1)
        colors = FloatArray(12 * size)
        
        // copy old array to new array
        System.arraycopy(oldTriangleCoords, 0, triangleCoords, 0, oldTriangleCoords.size)
        System.arraycopy(oldColor, 0, colors, 0, oldColor.size)
        
        // initialize the new part of the array
        for (i in oldTriangleCoords.size until triangleCoords.size)
            triangleCoords[i] = UNUSED
        for (i in oldColor.size until colors.size)
            colors[i] = UNUSED
        
        // size buffers with new arrays' sizes
        setupBuffers()
        
        // log it
        if (vertexCount % 50 == 0)
            Log.d("number of triangle draw", "" + vertexCount / 3)
        //                else
        //                    Log.v("number of triangle draw", "" + layer.vertexCount / 3);
    }

    private fun incrementVertexCountAndGiveNewCoordsPointer(): Int {
        val coordsPointerToBeReturned = vertexCount * COORDS_PER_VERTEX
        vertexCount += 300 // one hundred more triangle
        while (vertexCount > size * 3) {
            increaseSize()
        } // check if index out of bound.
        return coordsPointerToBeReturned
    }
    
    fun drawLayer(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)

        // get handle to fragment shader's vColor member
        val mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        // Set colors for drawing the triangle
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(mColorHandle, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuffer)

        // get handle to shape's transformation matrix
        val mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        // comment for mMVPMatrixHandle when it's still global: Use to access and set the view transformation

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // check for error
        val error = GLES20.glGetError()
        if (error != GL_NO_ERROR) {
            throw RuntimeException("GL error: $error")
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
        
    }
}