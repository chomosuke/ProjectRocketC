package com.chomusukestudio.projectrocketc

import android.content.Context
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.app.Activity
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import com.chomusukestudio.projectrocketc.GLRenderer.*

import com.chomusukestudio.projectrocketc.Joystick.TwoFingersJoystick
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Rocket.TestRocket
import com.chomusukestudio.projectrocketc.Surrounding.BasicSurrounding
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import java.util.concurrent.Executors

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import java.util.concurrent.TimeUnit


class MainActivity : Activity() { // exception will be throw if you try to create any instance of this class on your own... i think.
    lateinit var mGLView: MyGLSurfaceView
    lateinit var  scoreTextView: TextView
    private lateinit var playButton: ImageButton
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // as the ContentView for this Activity.
//        setContentView(findViewById<ImageView>(R.id.splashImage))

        setContentView(R.layout.activity_main)

        playButton = findViewById(R.id.playButton)
        mGLView = findViewById(R.id.MyGLSurfaceView)
        scoreTextView = findViewById(R.id.pointTextView)

        mGLView.initializeRenderer()
    }

    private val updateScoreThread = Executors.newScheduledThreadPool(1)
    fun onPlay(view: View) {
        mGLView.surrounding.start()

        // start refresh score regularly
        val updater = Runnable { this.runOnUiThread { scoreTextView.text = LittleStar.putCommasInInt("" + LittleStar.score) } }
        val scoreHandle = updateScoreThread.scheduleAtFixedRate(updater, 0, 20, TimeUnit.MILLISECONDS)
        updateScoreThread.schedule({ scoreHandle.cancel(true) }, 10, TimeUnit.SECONDS)

        val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_play_button_animation)
        // Now Set your animation
        playButton.startAnimation(fadeOutAnimation)
        playButton.visibility = View.INVISIBLE
    }
    
    fun showPlayButton() {
        runOnUiThread { playButton.visibility = View.VISIBLE }
    }
    
    public override fun onStop() {
        super.onStop()
        Log.i("", "\n\nonStop() called\n\n")
    }
    
    public override fun onPause() {
        super.onPause()
        Log.i("", "\n\nonPause() called\n\n")
    }
    
    public override fun onDestroy() {
        super.onDestroy()
        Log.i("", "\n\nonDestroy() called\n\n")
        mGLView.removeAllShapes()
    }// onDestroy will be called after onDrawFrame() returns so no worry of removing stuff twice :)
    
    override fun onBackPressed() {
        // to home
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
    }
    
    class MyGLSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {

        private var joystick = TwoFingersJoystick()
        lateinit var surrounding: Surrounding
        private lateinit var rocket: Rocket
        private lateinit var processingThread: ProcessingThread
        private lateinit var mRenderer: TheGLRenderer
        
        fun removeAllShapes() {
            processingThread.removeAllShapes()
        } // for onStop() and onDestroy() to remove Layer
        
        internal inner class MyConfigChooser : GLSurfaceView.EGLConfigChooser {// this class is for antialiasing
            
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
        
        init {
            
            // Create an OpenGL ES 3.0(3.1) context
            setEGLContextClientVersion(3)/*
            You can't explicitly request 3.1 when you create the context.
            Based on my understanding, 3.1 is not handled as a context type separate from 3.0.
            Essentially, a context supporting 3.1 is just a 3.0 context that also supports the additional 3.1 features.
             */
            
            setEGLConfigChooser(MyConfigChooser())// antialiasing

            val leftRightBottomTop = generateLeftRightBottomTop(width.toFloat() / height.toFloat())
            surrounding = BasicSurrounding(leftRightBottomTop[0], leftRightBottomTop[1], leftRightBottomTop[2], leftRightBottomTop[3]/*, TouchableView((context as Activity).findViewById(R.id.visualText), context as Activity)*/)
            rocket = TestRocket(surrounding)
            processingThread = ProcessingThread(joystick, surrounding, rocket,
                    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate)
            mRenderer = TheGLRenderer(processingThread)
            surrounding.initializeSurrounding(rocket)

            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(mRenderer)
            //
            //            // Render the view only when there is a change in the drawing data
            //            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            // set width and height of surface view
            
            pixelWidth = width.toFloat()
            pixelHeight = height.toFloat()
        }
        
        fun setRefreshRate(refreshRate: Float) {
            mRenderer.setRefreshRate(refreshRate)
        }

        fun initializeRenderer() {

        }
        
        override fun onTouchEvent(e: MotionEvent): Boolean {
            return processingThread.onTouchEvent(e)
        }
    }
}

var pixelWidth: Float = 0f
var pixelHeight: Float = 0f

fun transformToMatrixX(x: Float): Float {
    var resultX = x
    // transformation
    resultX -= pixelWidth / 2
    resultX /= pixelWidth / 2
    val leftRightBottomTop = generateLeftRightBottomTop(pixelWidth/ pixelHeight)
    resultX *= leftRightBottomTop[0]
    // assuming left == - right is true
    // need improvement to obey OOP principles by getting rid of the assumption above
    if (!(leftRightBottomTop[0] == -leftRightBottomTop[1]))
        throw RuntimeException("need improvement to obey OOP principles by not assuming left == - right is true.\n" +
                "or you can ignore the above and just twist the code to get it work and not give a fuck about OOP principles.\n" +
                "which is what i did when i wrote those code.  :)")
    return resultX
}

fun transformToMatrixY(y: Float): Float {
    var resultY = y
    // transformation
    resultY -= pixelHeight / 2
    resultY /= pixelHeight / 2
    val leftRightBottomTop = generateLeftRightBottomTop(pixelWidth/ pixelHeight)
    resultY *= leftRightBottomTop[2]
    // assuming top == - bottom is true
    // need improvement to obey OOP principles by getting rid of the assumption above
    if (!(leftRightBottomTop[3] == -leftRightBottomTop[2]))
        throw RuntimeException("need improvement to obey OOP principles by not assuming top == - bottom is true.\n" +
                "or you can ignore the above and just twist the code to get it work and not give a fuck about OOP principles.\n" +
                "which is what i did when i wrote those code.  :)")
    return resultY
}

fun giveVisualText(string: String/*, visualTextView: TouchableView<TextView>*/) {
//    visualTextView.activity.runOnUiThread {
//        visualTextView.view.visibility = View.VISIBLE
//        visualTextView.view.text = string
//        val visualEffectAnimation = AnimationUtils.loadAnimation(visualTextView.activity, R.anim.visual_text_effect)
//        // Now Set your animation
//        visualTextView.view.startAnimation(visualEffectAnimation)
//        visualTextView.view.visibility = View.INVISIBLE
//    }
}

class TouchableView<out V : View>(val view: V, val activity: Activity) {

}
