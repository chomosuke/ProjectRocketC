package com.chomusukestudio.projectrocketc

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.opengl.GLES20
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import com.chomusukestudio.projectrocketc.GLRenderer.*

import com.chomusukestudio.projectrocketc.Joystick.TwoFingersJoystick
import com.chomusukestudio.projectrocketc.Rocket.TestRocket
import com.chomusukestudio.projectrocketc.Surrounding.BasicSurrounding
import com.chomusukestudio.projectrocketc.ThreadClasses.ScheduledThread

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.processingThread.ProcessingThread
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.logging.Logger


class MainActivity : Activity() { // exception will be throw if you try to create any instance of this class on your own... i think.
    lateinit var mGLView: MyGLSurfaceView
    lateinit var  scoreTextView: TextView
    private lateinit var playButton: ImageButton
    private lateinit var playButtonAnimationImageView: ImageView
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        playButton = findViewById(R.id.playButton)
        mGLView = findViewById(R.id.MyGLSurfaceView)
        scoreTextView = findViewById(R.id.pointTextView)
        playButtonAnimationImageView = findViewById(R.id.playButtonAnimationImageView)

        // display splashScreen
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        heightInPixel = displayMetrics.heightPixels.toFloat()
        widthInPixel = displayMetrics.widthPixels.toFloat()

        val chomusukeView = findViewById<ImageView>(R.id.chomusukeView)
        chomusukeView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_splash_image))

        Executors.newSingleThreadExecutor().submit {
            BasicSurrounding.fillUpPlanetShapes()

            mGLView.initializeRenderer()
            this.runOnUiThread {
                findViewById<View>(R.id.preGameLayout).visibility = View.VISIBLE
                mGLView.visibility = View.VISIBLE
                chomusukeView.visibility = View.INVISIBLE
            }
        }

    }

    private fun disableClipOnParents(v: View) {
        if (v.parent == null) {
            return
        }

        if (v is ViewGroup) {
            v.clipChildren = false
        }

        if (v.parent is View) {
            disableClipOnParents(v.parent as View)
        }
    }

    private val updateScoreThread = ScheduledThread(16) { // 16 millisecond should be good
        this.runOnUiThread { scoreTextView.text = LittleStar.putCommasInInt("" + LittleStar.score) }
    }

    fun onPlay(view: View) {
        mGLView.processingThread.isStarted = true // start surrounding


        // start refresh score regularly
        updateScoreThread.run()

        val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_play_button_animation)
        playButton.visibility = View.INVISIBLE
        playButtonAnimationImageView.visibility = View.VISIBLE
        // Now Set your animation
        playButtonAnimationImageView.startAnimation(fadeOutAnimation)
        playButtonAnimationImageView.visibility = View.INVISIBLE
    }

    fun onPause(view: View) {
        try {
            if (mGLView.mRenderer.paused) {
                mGLView.mRenderer.resumeGLRenderer()
                (view as TextView).text = "PauseMe"
            } else {
                mGLView.mRenderer.pauseGLRenderer()
                (view as TextView).text = "ResumeMe"
            }
        } catch (e: UninitializedPropertyAccessException) {
            // TODO: we should do something here
        }
    }

    fun onCrashed() {
        mGLView.mRenderer.pauseGLRenderer()
        updateScoreThread.pause()
        runOnUiThread {
            playButton.visibility = View.VISIBLE
//            playButton.invalidate()
            playButton.bringToFront()
        }
        mGLView.processingThread.isStarted = false
        mGLView.resetGame()
        mGLView.mRenderer.resumeGLRenderer()
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
        mGLView.shutDown()
    }// onDestroy will be called after onDrawFrame() returns so no worry of removing stuff twice :)
    
    override fun onBackPressed() {
        // to home
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
    }
    
    class MyGLSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {

        lateinit var processingThread: ProcessingThread
        lateinit var mRenderer: TheGLRenderer
        
        fun shutDown() {
            processingThread.shutDown()
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
            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2)
        }

        fun initializeRenderer() {
            setEGLConfigChooser(MyConfigChooser())// antialiasing

            val leftRightBottomTop = generateLeftRightBottomTop(width.toFloat() / height.toFloat())

            val surrounding = BasicSurrounding(leftRightBottomTop[0], leftRightBottomTop[1], leftRightBottomTop[2], leftRightBottomTop[3],
                    TouchableView((context as Activity).findViewById(R.id.visualText), context as Activity))
            val rocket = TestRocket(surrounding)

            processingThread = ProcessingThread(
                    TwoFingersJoystick(),
                    surrounding,
                    rocket,
                    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate/*60f*/,
                    context as MainActivity) // we know that the context is MainActivity
//            processingThread = TestingProcessingThread()
            mRenderer = TheGLRenderer(processingThread, this)
            processingThread.surrounding.initializeSurrounding(processingThread.rocket)

            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(mRenderer)
            GLES20.glEnable(DEBUG_LOG_GL_CALLS)
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

        fun resetGame() {
            val leftRightBottomTop = generateLeftRightBottomTop(width.toFloat() / height.toFloat())
            processingThread.surrounding = BasicSurrounding(leftRightBottomTop[0], leftRightBottomTop[1], leftRightBottomTop[2], leftRightBottomTop[3],
                    TouchableView((context as Activity).findViewById(R.id.visualText), context as Activity))
            processingThread.rocket = TestRocket(processingThread.surrounding)
            processingThread.surrounding.initializeSurrounding(processingThread.rocket)
            processingThread.joystick = TwoFingersJoystick()
        }
        
        override fun onTouchEvent(e: MotionEvent): Boolean {
            return processingThread.onTouchEvent(e)
        }
    }
}

@Volatile var widthInPixel: Float = 0f
@Volatile var heightInPixel: Float = 0f

fun transformToMatrixX(x: Float): Float {
    var resultX = x
    // transformation
    resultX -= widthInPixel / 2
    resultX /= widthInPixel / 2
    val leftRightBottomTop = generateLeftRightBottomTop(widthInPixel/ heightInPixel)
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
    resultY -= heightInPixel / 2
    resultY /= heightInPixel / 2
    val leftRightBottomTop = generateLeftRightBottomTop(widthInPixel/ heightInPixel)
    resultY *= leftRightBottomTop[2]
    // assuming top == - bottom is true
    // need improvement to obey OOP principles by getting rid of the assumption above
    if (!(leftRightBottomTop[3] == -leftRightBottomTop[2]))
        throw RuntimeException("need improvement to obey OOP principles by not assuming top == - bottom is true.\n" +
                "or you can ignore the above and just twist the code to get it work and not give a fuck about OOP principles.\n" +
                "which is what i did when i wrote those code.  :)")
    return resultY
}

fun giveVisualText(string: String, visualTextView: TouchableView<TextView>) {
    visualTextView.touchView { textView ->
        textView.visibility = View.VISIBLE
        textView.text = string
        val visualEffectAnimation = AnimationUtils.loadAnimation(visualTextView.activity, R.anim.visual_text_effect)
        // Now Set your animation
        textView.startAnimation(visualEffectAnimation)
        textView.visibility = View.INVISIBLE
    }
}

class TouchableView<out V : View>(val view: V, val activity: Activity) {
    fun touchView(touch: (V) -> Unit) {
        activity.runOnUiThread { touch(view) }
    }
}

var pausedTime: Long = 0L
fun upTimeMillis(): Long {
    return SystemClock.uptimeMillis() - pausedTime
}
