package com.chomusukestudio.projectrocketc.GLRenderer

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import com.chomusukestudio.projectrocketc.Shape.Vector
import java.nio.Buffer
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
        fragmentData[1] = 0f
        fragmentData[2] = 1f
        fragmentData[3] = 0f
        fragmentData[4] = 1f
        fragmentData[5] = 1f
        fragmentData[6] = 0f
        fragmentData[7] = 0f
        fragmentData[8] = 0f
        fragmentData[9] = 1f
        fragmentData[10] = 1f
        fragmentData[11] = 1f
    }

    private fun loadTexture(context: Context, resourceId: Int): Int {
        val textureHandle = IntArray(1)
        GLES30.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error generating texture handle.")
        }

        val options = BitmapFactory.Options()
        options.inScaled = false // No pre-scaling

        // Read in the resource
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

        // Bind to the texture in OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0])

        // Set filtering
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)

        // generate mipmaps for GL_LINEAR_MIPMAP_LINEAR texture min filter
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle()

        return textureHandle[0]
    }

    override fun drawLayer(mProgram: Int, vertexBuffer: FloatBuffer, fragmentBuffer: FloatBuffer, vertexStride: Int, vertexCount: Int, mvpMatrix: FloatArray) {
        if (textureHandle == 0)
            // haven't done the lazy initialization yet
            textureHandle = loadTexture(context, resourceId)

        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        val mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                vertexStride, vertexBuffer)

        // get handle to fragment shader's vColor member
        val tCoordsHandle = GLES30.glGetAttribLocation(mProgram, "tCoords")
        // Set colors for drawing the triangle
        GLES30.glEnableVertexAttribArray(tCoordsHandle)
        GLES30.glVertexAttribPointer(tCoordsHandle, 2,
                GLES30.GL_FLOAT, false,
                0, fragmentBuffer)

        val textureUniformHandle = GLES30.glGetUniformLocation(mProgram, "texture")

        // Set the active texture unit to texture unit 0.
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES30.glUniform1i(textureUniformHandle, 0)

        // get handle to shape's transformation matrix
        val mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        // comment for mMVPMatrixHandle when it's still global: Use to access and set the view transformation

        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        // check for error
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            throw RuntimeException("GL error: $error")
        }

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle)
        GLES30.glDisableVertexAttribArray(tCoordsHandle)
    }
}