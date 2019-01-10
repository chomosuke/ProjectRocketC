package com.chomusukestudio.projectrocketc.GLRenderer

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.opengl.GLSurfaceView
import android.util.Log

import com.chomusukestudio.projectrocketc.Shape.CircularShape

import javax.microedition.khronos.opengles.GL10

import android.content.ContentValues.TAG
import android.opengl.GLES20
import com.chomusukestudio.projectrocketc.*
import com.chomusukestudio.projectrocketc.processingThread.ProcessingThread

class TheGLRenderer(val processingThread: ProcessingThread, val myGLSurfaceView: MainActivity.MyGLSurfaceView) : GLSurfaceView.Renderer {

    // so you calculate the how many milliseconds have passed since last frame
    private var previousFrameTime: Long = 0
    private var countingFrames = 0
    private var previousTime: Long = 0
    private var now: Long = 0
    private val timer = PauseableTimer()
    
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
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        
        Layer.initializeGLShaderAndStuff()
        Log.i(TAG, "onSurfaceCreated() called")
        
    }

//    val allFrameRate = ArrayList<Int>()
    override fun onDrawFrame(unused: GL10) {
        if (!timer.paused) { // if this is called when paused for some reason don't do anything as nothing suppose to change
            countingFrames++

            now = timer.timeMillis()// so you access timeMillis() less
//                            previousFrameTime = now - 16; // for break point

            if (previousTime == 0L) {
                // initialize it
                previousTime = now
            }
            if (now - previousTime >= 1000) {// just to get frame rates
                Log.i("Frame rate", "" + countingFrames + " and dynamic performance index " + CircularShape.performanceIndex)
//                allFrameRate.add(countingFrames)
                countingFrames = 0
                previousTime = now
            }

            processingThread.waitForLastFrame()

            // can't refresh buffers when processingThread is running or when drawing all triangles
            GLTriangle.passArraysToBuffers()

            processingThread.generateNextFrame(now, previousFrameTime, myGLSurfaceView.mainActivity.state)

            previousFrameTime = now
        }
        // Clear the screen
        //        GLES20.glClear(GL_DEPTH_BUFFER_BIT);
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Draw all!
        GLTriangle.drawAllTriangles()
    }
    
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        // for transformation to matrix
        
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        // refresh width and height of surfaceView
        widthInPixel = width.toFloat()
        heightInPixel = height.toFloat()

        Layer.refreshMatrix()

        previousFrameTime = timer.timeMillis()// just to set this as close to draw as possible
    }

    fun pauseGLRenderer() {
        if (!timer.paused) {
            myGLSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            timer.pause()
        }
    }
    fun resumeGLRenderer() {
        if (timer.paused) {
            // pausedTime have to be set before changing renderMode as change of renderMode will trigger
            // onDrawFrame which will cause timeMillis to be accessed before pauseTime being set
            myGLSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

            timer.resume()
//            // if something happened with time try uncomment this
//            now = timeMillis()
//            previousFrameTime = timeMillis()
        }
    }
    
    companion object {
        
        fun loadShader(type: Int, shaderCode: String): Int {
            
            // create a vertex shader type (GLES31.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES31.GL_FRAGMENT_SHADER)
            val shader = GLES20.glCreateShader(type)
            
            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            
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
//    return arrayOf(left - GLTriangle.layers[0].offsetX, right - GLTriangle.layers[0].offsetX, bottom - GLTriangle.layers[0].offsetY, top - GLTriangle.layers[0].offsetY)
    return arrayOf(left + offsetX, right + offsetX, bottom + offsetY, top + offsetY)
}

var offsetX = 0f
var offsetY = 0f
@Deprecated("failed multiple times, need to change some entire structure to achieve this")
fun offsetCamera(dOffsetX: Float, dOffsetY: Float) {
    offsetX += dOffsetX
    offsetY += dOffsetY

    Layer.refreshMatrix()
}
