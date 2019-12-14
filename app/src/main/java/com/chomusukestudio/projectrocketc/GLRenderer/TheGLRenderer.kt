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
import com.chomusukestudio.projectrocketc.ProcessingThread

class TheGLRenderer(val processingThread: ProcessingThread, val myGLSurfaceView: MainActivity.MyGLSurfaceView, private val allLayers: AllLayers) : GLSurfaceView.Renderer {


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
//        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
//                arrayOf("a_Position", "a_Color", "a_Normal", "a_TexCoordinate"))

        // The below glEnable() call is a holdover from OpenGL ES 1, and is not needed in OpenGL ES 2.
        // Enable texture mapping
        // GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        //
        //        // Enable depth test
        //        glEnable(GL_DEPTH_TEST);
        //        // Accept fragment if it closer to the camera than the former one
        //        glDepthFunc(GL_LESS);

        // Set the background frame color
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        ShapeLayer.createGLProgram()
        Layer.refreshMatrix()
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
//                allFrameRate.offset(countingFrames)
                countingFrames = 0
                previousTime = now
            }

            processingThread.waitForLastFrame()

            // can't refresh buffers when processingThread is running or when drawing all triangles
            allLayers.passArraysToBuffers()

            processingThread.generateNextFrame(now, previousFrameTime)

            previousFrameTime = now
        }
        // Clear the screen
        //        GLES20.glClear(GL_DEPTH_BUFFER_BIT);

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // this is required on certain devices

        // Draw all!
        allLayers.drawAll()
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
//            nowXY = timeMillis()
//            previousFrameTime = timeMillis()
        }
    }
}

var rightEnd: Float = 0f
    get() { if(field == 0f) throw UninitializedPropertyAccessException() else return field }
var leftEnd: Float = 0f
    get() { if(field == 0f) throw UninitializedPropertyAccessException() else return field }
var bottomEnd: Float = 0f
    get() { if(field == 0f) throw UninitializedPropertyAccessException() else return field }
var topEnd: Float = 0f
    get() { if(field == 0f) throw UninitializedPropertyAccessException() else return field }

fun generateLeftRightBottomTopEnd(widthOverHeight: Float) {
    if (widthOverHeight > 9f / 16f) {// if the screen is wider than a 16:9 screen
        rightEnd = widthOverHeight * 8f
        leftEnd = widthOverHeight * -8f
        bottomEnd = -8f
        topEnd = 8f
    } else if (1 / widthOverHeight > 16f / 9f) {// if the screen is taller than a 16:9 screen
        rightEnd = 4.5f
        leftEnd = -4.5f
        bottomEnd = 1 / widthOverHeight * -4.5f
        topEnd = 1 / widthOverHeight * 4.5f
    } else {// if the screen is 16;9
        rightEnd = 4.5f
        leftEnd = -4.5f
        bottomEnd = -8f
        topEnd = 8f
    }
//    return arrayOf(rightEnd - GLTriangle.arrayList[0].offsetX, leftEnd - GLTriangle.arrayList[0].offsetX, bottomEnd - GLTriangle.arrayList[0].offsetY, topEnd - GLTriangle.arrayList[0].offsetY)
}

var offsetX = 0f
var offsetY = 0f
@Deprecated("failed multiple times, need to change some entire structure to achieve this")
fun offsetCamera(dOffsetX: Float, dOffsetY: Float) {
    offsetX += dOffsetX
    offsetY += dOffsetY

    Layer.refreshMatrix()
}
