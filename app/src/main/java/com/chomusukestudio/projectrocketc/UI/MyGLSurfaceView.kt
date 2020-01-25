package com.chomusukestudio.projectrocketc.UI

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.WindowManager
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.GLRenderer.TheGLRenderer
import com.chomusukestudio.projectrocketc.ProcessingThread
import com.chomusukestudio.projectrocketc.scanForActivity
import java.util.logging.Level
import java.util.logging.Logger
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

class MyGLSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {

    private val mainActivity = scanForActivity(context) as MainActivity

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)
    }

    lateinit var mRenderer: TheGLRenderer

    fun shutDown() {
        try {
            mRenderer.processingThread.shutDown()
        } catch (e: UninitializedPropertyAccessException) {
            // processingThread have not yet being initialized
            // just do some basic clean up
            // actually not any clean up would be meaningful so yeah
            // but i should log it
            Log.w("shutDown()", e)
        }
    } // for onStop() and onDestroy() to remove Layer

    internal inner class MyConfigChooser : EGLConfigChooser {// this class is for antialiasing

        override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig? {

            val attribs = intArrayOf(EGL10.EGL_LEVEL, 0, EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER, EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, 1, EGL10.EGL_SAMPLES, 4, EGL10.EGL_NONE)// This is for 4x MSAA.

            val configs = arrayOfNulls<EGLConfig>(1)
            val configCounts = IntArray(1)
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts)

            return if (configCounts[0] == 0) {
                // Failed! Error handling.
                null
            } else {
                configs[0]
            }
        }
    }

    fun initializeRenderer() {

        setEGLConfigChooser(MyConfigChooser())// antialiasing

        val layers = Layers()
        val processingThread = ProcessingThread(
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate/*60f*/,
                mainActivity, layers) // we know that the context is MainActivity
//            processingThread = TestingProcessingThread()
        mRenderer = TheGLRenderer(processingThread, this, layers)

        // Set the Renderer for drawing on the GLSurfaceView
        debugFlags = DEBUG_LOG_GL_CALLS
        setRenderer(mRenderer)
        //
        //            // Render the view only when there is a change in the drawing data
        //            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        var nullPointerFailure: Boolean
        do {
            try {
                super.surfaceCreated(holder)
                nullPointerFailure = false
            } catch (e: NullPointerException) {
                // this means setRenderer() haven't being called yet (most likely)
                val logger = Logger.getAnonymousLogger()
                logger.log(Level.SEVERE, "an exception was thrown in nextFrameThread", e)
                // so log it and try again
                nullPointerFailure = true
            }
        } while (nullPointerFailure)
    }

//        override fun onMeasure(width: Int, height: Int) {
//            super.onMeasure(width, height)
//            // set width and height
//            widthInPixel = width.toFloat()
//            heightInPixel = height.toFloat()
//
//            GLTriangle.refreshAllMatrix()
//        }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        mainActivity.onTouchMyGLSurface(e)
        return mRenderer.processingThread.onTouchEvent(e) // we know that the context is MainActivity
    }
}