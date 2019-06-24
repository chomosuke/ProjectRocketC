package com.chomusukestudio.projectrocketc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Point
import android.opengl.GLES20
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.chomusukestudio.projectrocketc.GLRenderer.*

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay
import com.chomusukestudio.projectrocketc.processingThread.ProcessingThread
import android.view.*
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import com.chomusukestudio.projectrocketc.Shape.CircularShape
//import com.google.firebase.analytics.FirebaseAnalytics
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.logging.Logger

enum class State { InGame, PreGame, Paused, Crashed }

class MainActivity : Activity() { // exception will be throw if you try to create any instance of this class on your own... i think.

    private lateinit var sharedPreferences: SharedPreferences

//    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    @Volatile var state: State = State.PreGame
        private set

    public override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // initialize width and height in pixel
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        widthInPixel = size.x.toFloat()
        heightInPixel = size.y.toFloat()

        setContentView(R.layout.activity_main)

        // initialize sharedPreference
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

//        with(sharedPreferences.edit()) {
//            putInt(getString(R.string.highestScore), 0)
//            apply()
//        }

        // a safer way to set listener
        findViewById<ImageButton>(R.id.playButton).setOnClickListener { view -> startGame(view) }
        findViewById<ImageButton>(R.id.pauseButton).setOnClickListener { view -> onPause(view) }
        findViewById<ImageButton>(R.id.restartButton).setOnClickListener { view -> restartGame(view) }
        findViewById<ImageButton>(R.id.toHomeButton).setOnClickListener { view -> toHome(view) }

        // display splashScreen
        findViewById<ImageView>(R.id.chomusukeView).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_splash_image))

        // update highest score
        findViewById<TextView>(R.id.highestScoreTextView).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())
        with(sharedPreferences.edit()) {
            putInt(getString(R.string.highestScoreRefresh), 0)
            apply()
        }

//        // Obtain the FirebaseAnalytics instance.
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        CircularShape.performanceIndex = sharedPreferences.getFloat(getString(R.string.performanceIndex), 1f)

        // initialize surrounding
        Executors.newSingleThreadExecutor().submit {
            runWithExceptionChecked {

                findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).initializeRenderer()

                // hide splash screen and show game
                this.runOnUiThread {

                    // cross fade them
                    findViewById<ConstraintLayout>(R.id.preGameLayout).visibility = View.VISIBLE
                    findViewById<ConstraintLayout>(R.id.preGameLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
                    findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.VISIBLE
                    findViewById<ConstraintLayout>(R.id.scoresLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
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

                    if (state != State.PreGame) {
                        Log.e("game launching", "state not in PreGame but $state")
                        state = State.PreGame // this is pregame
                    }
//                    // see if this is the first time the game open
//                    if (sharedPreferences.getBoolean(getString(R.string.firstTimeOpen), true)) {
//                        // if it is show the tutorial
//                        findViewById<ConstraintLayout>(R.id.tutorialLayout).visibility = View.VISIBLE
//                        findViewById<ConstraintLayout>(R.id.tutorialLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
//
//                        // and set the firstTimeOpen to be false
//                        with(sharedPreferences.edit()) {
//                            putBoolean(getString(R.string.firstTimeOpen), false)
//                            apply()
//                        }
//                    }
                }
            }
        }
    }

    fun startGame(view: View) {
        if (state == State.InGame) return // already started, must've been lag

        if (state != State.PreGame)
            throw IllegalStateException("Starting Game while not in PreGame")
        state = State.InGame // start game

        // fade away pregame layout with animation
        fadeOut(findViewById(R.id.preGameLayout))

        findViewById<ConstraintLayout>(R.id.inGameLayout).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.inGameLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
    }

    private var lastClickOnPause = 0L
    fun onPause(view: View) {
        if (if (SystemClock.uptimeMillis() - lastClickOnPause < 500) {
                    true
                } else {
                    lastClickOnPause = SystemClock.uptimeMillis()
                    false
                })
            return // prevent cheating the game

        try {
            when (state) {
                State.Paused -> {
                    findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.resumeGLRenderer()
                    findViewById<ImageButton>(R.id.pauseButton).setImageDrawable(resources.getDrawable(R.drawable.pause_button))
                    state = State.InGame
                }
                State.InGame -> {
                    findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.pauseGLRenderer()
                    findViewById<ImageButton>(R.id.pauseButton).setImageDrawable(resources.getDrawable(R.drawable.resume_button))
                    state = State.Paused
                }
                State.Crashed -> {
                    // Crashed, do nothing
                }
                else -> throw IllegalStateException("Trying to pause while not InGame.")
            }
        } catch (e: UninitializedPropertyAccessException) {
            // TODO: we should do something here
        }
    }

    private var lastClickRestartGame = 0L
    fun restartGame(view: View) {
        if (if (SystemClock.uptimeMillis() - lastClickRestartGame < 1000) {
                    true
                } else {
                    lastClickRestartGame = SystemClock.uptimeMillis()
                    false
                })
            return // multi click check

        if (state != State.Crashed) // already in other state, could be lag so big that multi click check failed or pressed immediately after toHome
            return

        // update highest score
        findViewById<TextView>(R.id.highestScoreTextView).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())

        findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.scoresLayout).bringToFront()

        findViewById<ConstraintLayout>(R.id.inGameLayout).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.inGameLayout).bringToFront()

        fadeOut(findViewById(R.id.onCrashLayout))

        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).resetGame()

        state = State.InGame
    }

    fun toHome(view: View) {
        if (state != State.Crashed && state != State.Paused) return // already at home, must've been lag

        // update highest score
        findViewById<TextView>(R.id.highestScoreTextView).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())

        findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.scoresLayout).bringToFront()

        fadeOut(findViewById(R.id.onCrashLayout))
        fadeIn(findViewById(R.id.preGameLayout))

        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).resetGame()

        state = State.PreGame
    }

    fun openSetting(view: View) {

    }

    fun closeSetting(view: View) {

    }

    fun onCrashed() {
        state = State.Crashed

        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.processingThread.updateHighestScore { score ->
            if (score > sharedPreferences.getInt(getString(R.string.highestScore), 0)) {
                // update highest score
                with(sharedPreferences.edit()) {
                    putInt(getString(R.string.highestScore), score)
                    apply()
                }
                // firebase stuff that i don't understand
//            val bundle = Bundle()
//            bundle.putInt(FirebaseAnalytics.Param.SCORE, LittleStar.score)
//            bundle.putString("leaderboard_id", "mLeaderboard")
//            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle)

            }
        }
        runOnUiThread {
            findViewById<ConstraintLayout>(R.id.onCrashLayout).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.onCrashLayout).bringToFront()

            findViewById<TextView>(R.id.highestScoreOnCrash).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())
            findViewById<TextView>(R.id.previousScoreOnCrash).text = findViewById<TextView>(R.id.scoreTextView).text

            findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.INVISIBLE

            findViewById<ConstraintLayout>(R.id.inGameLayout).visibility = View.INVISIBLE
            // prevent any uncleaned visual effect
            findViewById<TextView>(R.id.visualText).text = ""
        }
    }

    private fun fadeOut(view: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out_animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.INVISIBLE
            }
        })
        view.startAnimation(animation)
    }

    private fun fadeIn(view: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) { view.visibility = View.VISIBLE }
            override fun onAnimationEnd(animation: Animation?) {}
        })
        view.startAnimation(animation)
        view.bringToFront()
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
        // onDestroy will be called after onDrawFrame() returns so no worry of removing stuff twice :)
        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).shutDown()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

//        // update performanceIndex
//        val allFrameRate = findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.allFrameRate
//        allFrameRate.sort()
//        val ninetyPercentFrameRate = allFrameRate[allFrameRate.size / 10]
//        if (ninetyPercentFrameRate < 60) {
//            // target %90 frame rate at 60
//            if (CircularShape.performanceIndex > 0.3)
//            // make it smaller
//                CircularShape.performanceIndex /= 1.1f
//        } else {
//            if (CircularShape.performanceIndex < 1f)
//            // make it bigger
//                CircularShape.performanceIndex *= 1.1f
//        }
//        // update sharedPreference
//        with(sharedPreferences.edit()) {
//            putFloat(getString(R.string.performanceIndex), CircularShape.performanceIndex)
//            apply()
//        }
        // I will put this in the setting
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        try {
            when (state) {
                State.InGame -> {
                    if (!hasFocus) {
                        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.pauseGLRenderer()
                        findViewById<ImageButton>(R.id.pauseButton).setImageDrawable(resources.getDrawable(R.drawable.resume_button))
                        state = State.Paused
                    }
                }
                State.Paused -> { /*nothing, there is nothing can be done.*/ }
                else -> {
                    if (!hasFocus)
                        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.pauseGLRenderer()
                    else
                        findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.resumeGLRenderer()
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            // so the app is just starting in the first time... do nothing
        }
    }

    override fun onBackPressed() {
        when (state) {
            State.PreGame -> {
                super.onBackPressed()
            }
            State.InGame, State.Paused ->
                onPause(findViewById<Button>(R.id.pauseButton))
            State.Crashed ->
                toHome(findViewById<Button>(R.id.toHomeButton))
        }
    }
    
    class MyGLSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {

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

        fun initializeRenderer() {

            setEGLConfigChooser(MyConfigChooser())// antialiasing

            val layers = Layers()
            val processingThread = ProcessingThread(
                    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate/*60f*/,
                    scanForActivity(context) as MainActivity, layers) // we know that the context is MainActivity
//            processingThread = TestingProcessingThread()
            mRenderer = TheGLRenderer(processingThread, this, layers)

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
            findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.pauseGLRenderer()
            mRenderer.processingThread.reset()
            findViewById<MyGLSurfaceView>(R.id.MyGLSurfaceView).mRenderer.resumeGLRenderer()
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            return mRenderer.processingThread.onTouchEvent(e) // we know that the context is MainActivity
        }
    }
}
