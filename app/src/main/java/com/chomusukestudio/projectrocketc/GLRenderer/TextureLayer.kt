package com.chomusukestudio.projectrocketc.GLRenderer

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.chomusukestudio.projectrocketc.Shape.Vector
import java.nio.FloatBuffer

// this layer only have a single image/texture
class TextureLayer(private val context: Context, private val resourceId: Int,
                   vertex1: Vector, vertex2: Vector, vertex3: Vector, vertex4: Vector,
                   z: Float) : Layer(z, 6 /*texture 2d times three vertex*/, 2) {
    override val mProgram: Int
        get() = glProgram
    companion object {
        private var glProgram = -100
        fun createGLProgram() {
            glProgram = createGLProgram(vertexShaderCode, fragmentShaderCode)
        }
        private const val vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec2 tCoords;" +
                        "varying vec2 tCoordsF;" +
                        "void main() {" +
                        "  tCoordsF = tCoords;" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}"

        private const val fragmentShaderCode =
                "precision mediump float;" +
                        "uniform sampler2D texture;" +
                        "varying vec2 tCoordsF;" +
                        "void main() {" +
                        " gl_FragColor = texture2D(texture, tCoordsF);" +
                        "}"
    }

    private var textureHandle = 0

    init {
        triangleCoords[0] = vertex1.x
        triangleCoords[1] = vertex1.y
        triangleCoords[2] = vertex2.x
        triangleCoords[3] = vertex2.y
        triangleCoords[4] = vertex3.x
        triangleCoords[5] = vertex3.y
        triangleCoords[6] = vertex1.x
        triangleCoords[7] = vertex1.y
        triangleCoords[8] = vertex4.x
        triangleCoords[9] = vertex4.y
        triangleCoords[10] = vertex3.x
        triangleCoords[11] = vertex3.y
        fragmentData[0] = 0f
        fragmentData[1] = 1f
        fragmentData[2] = 1f
        fragmentData[3] = 1f
        fragmentData[4] = 1f
        fragmentData[5] = 0f
        fragmentData[6] = 0f
        fragmentData[7] = 1f
        fragmentData[8] = 0f
        fragmentData[9] = 0f
        fragmentData[10] = 1f
        fragmentData[11] = 0f
    }

    private fun loadTexture(context: Context, resourceId: Int): Int {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error generating texture handle.")
        }

        val options = BitmapFactory.Options()
        options.inScaled = false // No pre-scaling

        // Read in the resource
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle()

        return textureHandle[0]
    }

    override fun drawLayer(mProgram: Int, vertexBuffer: FloatBuffer, fragmentBuffer: FloatBuffer, vertexStride: Int, vertexCount: Int, mvpMatrix: FloatArray) {
        if (textureHandle == 0)
            // haven't done the lazy initialization yet
            textureHandle = loadTexture(context, resourceId)

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
        val tCoordsHandle = GLES20.glGetAttribLocation(mProgram, "tCoords")
        // Set colors for drawing the triangle
        GLES20.glEnableVertexAttribArray(tCoordsHandle)
        GLES20.glVertexAttribPointer(tCoordsHandle, 2,
                GLES20.GL_FLOAT, false,
                0, fragmentBuffer)

        val textureUniformHandle = GLES20.glGetUniformLocation(mProgram, "texture")

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0);

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
        GLES20.glDisableVertexAttribArray(tCoordsHandle)
    }
}