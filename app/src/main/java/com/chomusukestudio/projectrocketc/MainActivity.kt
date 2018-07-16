package com.chomusukestudio.projectrocketc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.app.Activity
import android.content.SharedPreferences
import android.opengl.GLES20
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
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
import android.widget.Button
import android.widget.ImageView
import com.chomusukestudio.projectrocketc.Joystick.OneFingerJoystick
import com.chomusukestudio.projectrocketc.littleStar.putCommasInInt
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.logging.Logger

@Volatile var state: State = State.PreGame
enum class State { InGame, PreGame, Paused, Crashed }

class MainActivity : Activity() { // exception will be throw if you try to create any instance of this class on your own... i think.
    private lateinit var sharedPreferences: SharedPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // initialize sharedPreference
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        // set height and width of the screen
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        heightInPixel = displayMetrics.heightPixels.toFloat()
        widthInPixel = displayMetrics.widthPixels.toFloat()

        // display splashScreen
        findViewById<ImageView>(R.id.chomusukeView).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_splash_image))

        // update highest score
        findViewById<TextView>(R.id.highestScoreTextView).text = putCommasInInt(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())

        // initialize surrounding
        Executors.newSingleThreadExecutor().submit {
            BasicSurrounding.fillUpPlanetShapes()

            findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).initializeRenderer()

            // hide splash screen and show game
            this.runOnUiThread {
                findViewById<ConstraintLayout>(R.id.preGameLayout).visibility = View.VISIBLE
                findViewById<TextView>(R.id.pointTextView).visibility = View.VISIBLE
                findViewById<TextView>(R.id.highestScoreTextView).visibility = View.VISIBLE
                findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).visibility = View.VISIBLE
                findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))

                findViewById<ImageView>(R.id.chomusukeView).bringToFront()
                findViewById<ImageView>(R.id.chomusukeView).animate()
                        .alpha(0f)
                        .setDuration(250)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                findViewById<ImageView>(R.id.chomusukeView).visibility = View.INVISIBLE
                            }
                        })
                findViewById<ImageView>(R.id.chomusukeView).visibility = View.INVISIBLE
            }
        }
    }

    private fun disableClipOnParents(v: View) {
        if (v.parent == null)
            return
        if (v is ViewGroup)
            v.clipChildren = false
        if (v.parent is View)
            disableClipOnParents(v.parent as View)
    }

    private val updateScoreThread = ScheduledThread(16) { // 16 millisecond should be good
        this.runOnUiThread { findViewById<TextView>(R.id.pointTextView).text = putCommasInInt(LittleStar.score.toString()) }
    }

    fun startGame(view: View) {
        state = State.InGame // start game
        LittleStar.cleanScore()

        // start refresh score regularly
        updateScoreThread.run()

        // Now Set your animation
        findViewById<ConstraintLayout>(R.id.preGameLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_animation))

        findViewById<ConstraintLayout>(R.id.inGameLayout).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.inGameLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))

        findViewById<ConstraintLayout>(R.id.preGameLayout).visibility = View.INVISIBLE
    }

    fun onPause(view: View) {
        try {
            if (state == State.Paused) {
                findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.resumeGLRenderer()
                findViewById<Button>(R.id.pauseButton).text = "PauseMe"
                state = State.InGame
            } else if (state == State.InGame) {
                findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.pauseGLRenderer()
                findViewById<Button>(R.id.pauseButton).text = "ResumeMe"
                state = State.Paused
            } else {
                throw IllegalStateException("Trying to pause while not InGame.")
            }
        } catch (e: UninitializedPropertyAccessException) {
            // TODO: we should do something here
        }
    }

    fun openSetting(view: View) {

    }

    fun onCrashed() {
        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.pauseGLRenderer()
        updateScoreThread.pause()
        runOnUiThread {

            if (LittleStar.score > sharedPreferences.getInt(getString(R.string.highestScore), 0)) {
                // update highest score
                with(sharedPreferences.edit()) {
                    putInt(getString(R.string.highestScore), LittleStar.score)
                    apply()
                }
                findViewById<TextView>(R.id.highestScoreTextView).text = putCommasInInt(LittleStar.score.toString())
            }
            findViewById<ConstraintLayout>(R.id.preGameLayout).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.preGameLayout).bringToFront()

            findViewById<ConstraintLayout>(R.id.inGameLayout).visibility = View.INVISIBLE
        }
        state = State.Crashed
        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).resetGame()
        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.resumeGLRenderer()
        state = State.PreGame
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
        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).shutDown()
    }// onDestroy will be called after onDrawFrame() returns so no worry of removing stuff twice :)

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        try {
            when (state) {
                State.InGame -> {
                    if (!hasFocus) {
                        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.pauseGLRenderer()
                        findViewById<Button>(R.id.pauseButton).text = "ResumeMe"
                    }
                }
                State.PreGame -> {
                    if (hasFocus)
                        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.resumeGLRenderer()
                    else
                        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.pauseGLRenderer()
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            // so the app is just starting in the first time... do nothing
        }
    }

    override fun onBackPressed() {
        when (state) {
            State.PreGame ->
                super.onBackPressed()
            else ->
                onPause(findViewById<Button>(R.id.pauseButton))
        }
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
//                    OneFingerJoystick(),
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
//            processingThread.joystick = OneFingerJoystick()
        }
        
        override fun onTouchEvent(e: MotionEvent): Boolean {
            return if (state == State.Paused)
                false
            else
                processingThread.onTouchEvent(e)
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

@Volatile var pausedTime: Long = 0L
fun upTimeMillis(): Long {
    return SystemClock.uptimeMillis() - pausedTime
}
