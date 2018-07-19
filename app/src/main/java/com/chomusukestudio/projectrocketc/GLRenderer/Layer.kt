package com.chomusukestudio.projectrocketc.GLRenderer

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.chomusukestudio.projectrocketc.heightInPixel
import com.chomusukestudio.projectrocketc.widthInPixel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

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
    @Synchronized fun getCoordPointer(): Int {
//        if (!Thread.currentThread().name.equals("nextFrameThread"))
//            Log.d("currentThread", Thread.currentThread().name)
        var i = 0
        while (true) {
            // if lastUsedCoordIndex has reached vertexCount then bring it back to 0
            if (lastUsedCoordIndex == vertexCount * COORDS_PER_VERTEX)
                lastUsedCoordIndex = 0

            if (i >= vertexCount * COORDS_PER_VERTEX / 2) {
                // if half of all before vertexCount does not have unused triangle left
                lastUsedCoordIndex = incrementVertexCountAndGiveNewCoordsPointer()
                return lastUsedCoordIndex
            }

            if (triangleCoords[lastUsedCoordIndex] == UNUSED) {
                // found an unused coords
                return lastUsedCoordIndex
            }

            lastUsedCoordIndex += CPT
            i += CPT
        }
    }

    init {
        // mark ALL colors and triangleCoords as UNUSED
        triangleCoords = FloatArray(size * CPT) { UNUSED }
        colors = FloatArray(size * 12) { UNUSED }

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
        triangleCoords = FloatArray(CPT * size)
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
        // passArraysToBuffers
        passArraysToBuffers()

        // log it
        if (vertexCount % 50 == 0)
            Log.d("number of triangle draw", "" + vertexCount / 3)
        //                else
        //                    Log.v("number of triangle draw", "" + layer.vertexCount / 3);
    }

    private fun incrementVertexCountAndGiveNewCoordsPointer(): Int {
        val coordsPointerToBeReturned = vertexCount * COORDS_PER_VERTEX
        // vertexCount have to be multiple of 3
        vertexCount = (vertexCount * 1.25).toInt() / 3 * 3 // 25% more triangle
        while (vertexCount > size * 3) {
            increaseSize()
        } // check if index out of bound.
        return coordsPointerToBeReturned
    }


    companion object {

        // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
        private val mProjectionMatrix = FloatArray(16)
        private val mViewMatrix = FloatArray(16)
        private val mvpMatrix = FloatArray(16)

        fun refreshMatrix() {
            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            val leftRightBottomTop = generateLeftRightBottomTop(widthInPixel / heightInPixel)

            // for debugging
//        Matrix.orthoM(mProjectionMatrix, 0, leftRightBottomTop[0] * 3, leftRightBottomTop[1] * 3,
//                leftRightBottomTop[2] * 3, leftRightBottomTop[3] * 3, -1000f, 1000f)
            Matrix.orthoM(mProjectionMatrix, 0, leftRightBottomTop[0], leftRightBottomTop[1],
                    leftRightBottomTop[2], leftRightBottomTop[3], -1000f, 1000f)
            // this game shall be optimised for any aspect ratio as now all left, right, bottom and top are visibility

            // Set the camera position (View matrix)
            Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

            // Calculate the projection and view transformation
            Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
        }

        // create empty OpenGL ES Program
        private var mProgram: Int = -1000

        fun initializeGLShaderAndStuff() {
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

            refreshMatrix()
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

    fun drawLayer() {
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
        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException("GL error: $error")
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandle)
    }
}