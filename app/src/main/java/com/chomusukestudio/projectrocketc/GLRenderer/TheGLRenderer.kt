package com.chomusukestudio.projectrocketc.GLRenderer

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log

import com.chomusukestudio.projectrocketc.Shape.CircularShape

import javax.microedition.khronos.opengles.GL10

import android.content.ContentValues.TAG
import android.opengl.GLES20
import com.chomusukestudio.projectrocketc.heightOfSurface
import com.chomusukestudio.projectrocketc.widthOfSurface
import java.util.concurrent.locks.ReentrantLock


class TheGLRenderer(val processingThread: ProcessingThread) : GLSurfaceView.Renderer {
    private var REFRESH_RATE: Float = 0f
    
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    // so you calculate the how many milliseconds have passed since last frame
    private var previousFrameTime: Long = 0
    private var countingFrames: Long = 0
    private var previousTime: Long = 0
    private var now: Long = 0
    
    private val lockForProcessingThread = ReentrantLock()
    private val conditionForProcessingThread = lockForProcessingThread.newCondition()
    
    fun setRefreshRate(refreshRate: Float) {
        REFRESH_RATE = refreshRate
    }
    
    override fun onSurfaceCreated(unused: GL10, config: javax.microedition.khronos.egl.EGLConfig) {
        
        //enable transparency
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnable(GLES20.GL_BLEND)
        //
        //        // Enable depth test
        //        glEnable(GL_DEPTH_TEST);
        //        // Accept fragment if it closer to the camera than the former one
        //        glDepthFunc(GL_LESS);
        
        // Set the background frame color
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        
        
        
        Log.i(TAG, "onSurfaceCreated() called")
        
    }
    
    override fun onDrawFrame(unused: GL10) {
        countingFrames++

        now = SystemClock.uptimeMillis()// so you access SystemClock.uptimeMillis() less
        //                previousFrameTime = now - 16; // for break point

        if (now - previousTime >= 1000) {// just to get frame rates
            Log.i("Frame rate", "" + countingFrames + " and dynamic performance index " + CircularShape.dynamicPerformanceIndex)
            countingFrames = 0
        }

        processingThread.waitForLastFrame()

        // can't refresh buffers when processingThread is running or when drawing all triangles
        GLTriangle.passArraysToBuffers()

        processingThread.generateNextFrame(now, previousFrameTime)

        previousFrameTime = now
        
        // Clear the screen
        //        GLES30.glClear(GL_DEPTH_BUFFER_BIT);
        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        // Draw all!
        GLTriangle.drawAllTriangles(mvpMatrix)
    }
    
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        // for transformation to matrix
        
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        
        val leftRightBottomTop = generateLeftRightBottomTop(width.toFloat() / height.toFloat())
        
        // refresh width and height of surfaceView
        widthOfSurface = width.toFloat()
        heightOfSurface = height.toFloat()
        
        // for debugging
        //        Matrix.orthoM(mProjectionMatrix, 0, left/4*720/512, right/4*720/512, bottom/4*720/512, top/4*720/512, -1000, 1000);
        Matrix.orthoM(mProjectionMatrix, 0, leftRightBottomTop[0], leftRightBottomTop[1], leftRightBottomTop[2], leftRightBottomTop[3], -1000f, 1000f)
        // this game shall be optimised for any aspect ratio as now all left, right, bottom and top are visibility
        
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
        
        previousFrameTime = SystemClock.uptimeMillis()// just to set this as close to draw as possible
    }
    
    fun pauseGLRenderer() {
        // TODO: pause GLRenderer here
    }
    
    companion object {
        
        fun loadShader(type: Int, shaderCode: String): Int {
            
            // create a vertex shader type (GLES31.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES31.GL_FRAGMENT_SHADER)
            val shader = GLES30.glCreateShader(type)
            
            // add the source code to the shader and compile it
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)
            
            return shader
        }
    }
}

fun generateLeftRightBottomTop(widthOverHeight: Float): Array<Float> {
    val left: Float
    val right: Float
    val bottom: Float
    val top: Float
    if (widthOverHeight > 9f / 16f) {// if the screen is wider than a 16:9 screen
        left = widthOverHeight * 8f
        right = widthOverHeight * -8f
        bottom = -8f
        top = 8f
    } else if (1 / widthOverHeight > 16f / 9f) {// if the screen is taller than a 16:9 screen
        left = 4.5f
        right = -4.5f
        bottom = 1 / widthOverHeight * -4.5f
        top = 1 / widthOverHeight * 4.5f
    } else {// if the screen is 16;9
        left = 4.5f
        right = -4.5f
        bottom = -8f
        top = 8f
    }
    return arrayOf(left, right, bottom, top)
}
