package com.chomusukestudio.projectrocketc.UI

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Point
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.animation.AnimationUtils

import android.view.*
import android.view.animation.Animation
import android.widget.*
import com.chomusukestudio.projectrocketc.*
import com.chomusukestudio.projectrocketc.Shape.CircularShape
//import com.google.firebase.analytics.FirebaseAnalytics
import java.util.concurrent.Executors

enum class State { InGame, PreGame, Paused, Crashed }

class MainActivity : Activity() { // exception will be throw if you try to create any instance of this class on your own... i think.

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var processingThread: ProcessingThread
    private lateinit var myGLSurfaceView: MyGLSurfaceView

//    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    @Volatile var state: State = State.PreGame
        private set
    @Volatile var soundEffectsVolume = 100
        private set
    @Volatile var musicVolume = 100
        private set

    public override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // initialize width and height in pixel
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        widthInPixel = size.x.toFloat()
        heightInPixel = size.y.toFloat()

        setContentView(R.layout.activity_main)

        // initialize convenient variable
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        myGLSurfaceView = findViewById(R.id.MyGLSurfaceView)

//        with(sharedPreferences.edit()) {
//            putInt(getString(R.string.highestScore), 0)
//            apply()
//        }

        // setting
        with(findViewById<SeekBar>(R.id.soundEffectsVolumeBar)) {
            soundEffectsVolume = sharedPreferences.getInt(getString(R.string.soundEffectsVolume), 100)
            progress = soundEffectsVolume
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    soundEffectsVolume = progress
                    with(sharedPreferences.edit()) {
                        putInt(getString(R.string.soundEffectsVolume), soundEffectsVolume)
                        apply()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // see if this is the first time the game open
        if (sharedPreferences.getBoolean(getString(R.string.firstTimeOpen), true)) {
            showTutorial(findViewById(R.id.tutorialButton))

            // and set the firstTimeOpen to be false
            with(sharedPreferences.edit()) {
                putBoolean(getString(R.string.firstTimeOpen), false)
                apply()
            }
        }

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

                myGLSurfaceView.initializeRenderer()
                processingThread = myGLSurfaceView.mRenderer.processingThread

                // hide splash screen and show game
                this.runOnUiThread {

                    // cross fade them
                    findViewById<ConstraintLayout>(R.id.preGameLayout).visibility = View.VISIBLE
                    findViewById<ConstraintLayout>(R.id.preGameLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
                    findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.VISIBLE
                    findViewById<ConstraintLayout>(R.id.scoresLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
                    myGLSurfaceView.visibility = View.VISIBLE
                    myGLSurfaceView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))

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
                }
            }
        }
    }

    private var inTutorial = false
    fun showTutorial(view: View) {
        Log.v("tutorial", "showing")
        // if it is show the tutorial
        findViewById<ConstraintLayout>(R.id.tutorialGroup).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.tutorialGroup).bringToFront()
        findViewById<ViewPager>(R.id.tutorialPager).adapter = MyPagerAdapter(this)

        inTutorial = true
    }
    fun finishTutorial() {
        fadeOut(findViewById(R.id.tutorialGroup))

        inTutorial = false
    }

    fun onTouchMyGLSurface(e: MotionEvent) {

    }

    fun startGame(view: View) {
        if (state == State.InGame) return // already started, must've been lag

        if (state != State.PreGame)
            throw IllegalStateException("Starting Game while not in PreGame")
        state = State.InGame // start game

        // fade away pregame layout with animation
        fadeOut(findViewById(R.id.preGameLayout))

        fadeIn(findViewById(R.id.inGameLayout))
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
                    resumeGame()
                    state = State.InGame
                }
                State.InGame -> {
                    pauseGame()
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
    private fun pauseGame() {
        myGLSurfaceView.mRenderer.pauseGLRenderer()
        findViewById<ConstraintLayout>(R.id.onPausedLayout).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.onPausedLayout).bringToFront()
//        fadeIn(findViewById(R.id.onPausedLayout))
        findViewById<ImageButton>(R.id.pauseButton).visibility = View.INVISIBLE
    }
    private fun resumeGame() {
        myGLSurfaceView.mRenderer.resumeGLRenderer()
//        findViewById<ConstraintLayout>(R.id.onPausedLayout).visibility = View.INVISIBLE
        fadeOut(findViewById(R.id.onPausedLayout))
        fadeIn(findViewById(R.id.pauseButton))
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

        when (state) {
            State.Crashed -> {
                // update highest score
                findViewById<TextView>(R.id.highestScoreTextView).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())

                findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.VISIBLE

                findViewById<ConstraintLayout>(R.id.inGameLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.inGameLayout).bringToFront()

                findViewById<ConstraintLayout>(R.id.scoresLayout).bringToFront()

                fadeOut(findViewById(R.id.onCrashLayout))
            }
            State.Paused -> {
                resumeGame()
            }
            else -> return // already in other state, could be lag so big that multi click check failed or pressed immediately after toHome
        }

        processingThread.reset()

        state = State.InGame
    }

    fun toHome(view: View) {
        when (state) {
            State.Crashed -> {// update highest score
                findViewById<TextView>(R.id.highestScoreTextView).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())

                findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.scoresLayout).bringToFront()
                fadeOut(findViewById(R.id.onCrashLayout))
            }
            State.Paused -> {
                fadeOut(findViewById(R.id.inGameLayout))
                resumeGame()
            }
            else -> return
        } // already at home, must've been lag

        fadeIn(findViewById(R.id.preGameLayout))

        processingThread.reset()

        state = State.PreGame
    }

    fun swapRocketLeft(view: View) {
        if (processingThread.isOutOfBounds(-2))
            findViewById<ImageButton>(R.id.swapRocketLeftButton).visibility = View.INVISIBLE
        processingThread.swapRocket(-1)
        findViewById<ImageButton>(R.id.swapRocketRightButton).visibility = View.VISIBLE
    }
    fun swapRocketRight(view: View) {
        if (processingThread.isOutOfBounds(2))
            findViewById<ImageButton>(R.id.swapRocketRightButton).visibility = View.INVISIBLE
        processingThread.swapRocket(1)
        findViewById<ImageButton>(R.id.swapRocketLeftButton).visibility = View.VISIBLE
    }

    private var inSetting = false
    fun openSetting(view: View) {
        val currentLayout =
                getCurrentLayoutForSetting()

        findViewById<ConstraintLayout>(R.id.settingLayout).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.settingLayout).bringToFront()

        currentLayout.visibility = View.INVISIBLE

        inSetting = true
    }
    fun closeSetting(view: View) {

        val currentLayout =
                getCurrentLayoutForSetting()

        currentLayout.visibility = View.VISIBLE
        currentLayout.bringToFront()

        findViewById<ConstraintLayout>(R.id.settingLayout).visibility = View.INVISIBLE

        inSetting = false
    }
    private fun getCurrentLayoutForSetting(): ConstraintLayout {
        return when (state) {
            State.Paused -> findViewById(R.id.onPausedLayout)
            State.PreGame -> findViewById(R.id.preGameLayout)
            State.Crashed -> findViewById(R.id.onCrashLayout)
            else -> throw IllegalStateException()
        }
    }

    fun onCrashed() {
        state = State.Crashed

        processingThread.updateHighestScore { score ->
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
        myGLSurfaceView.shutDown()
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
                        onPause(findViewById<Button>(R.id.pauseButton))
                    }
                }
                State.Paused -> { /*nothing, there is nothing can be done.*/ }
                else -> {
                    if (!hasFocus)
                        myGLSurfaceView.mRenderer.pauseGLRenderer()
                    else
                        myGLSurfaceView.mRenderer.resumeGLRenderer()
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            // so the app is just starting in the first time... do nothing
        }
    }

    override fun onBackPressed() {
        if (inTutorial) {
            finishTutorial()
        } else if (inSetting) {
            closeSetting(findViewById(R.id.closeSettingButton))
        } else {
            when (state) {
                State.PreGame -> {
                    super.onBackPressed()
                }
                State.InGame, State.Paused ->
                    onPause(findViewById<ImageButton>(R.id.pauseButton))
                State.Crashed ->
                    toHome(findViewById<ImageButton>(R.id.toHomeButton))
            }
        }
    }
}