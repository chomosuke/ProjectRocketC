package com.chomusukestudio.projectrocketc.userInterface

//import com.google.firebase.analytics.FirebaseAnalytics
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chomusukestudio.prcandroid2dgameengine.runWithExceptionChecked
import com.chomusukestudio.prcandroid2dgameengine.threadClasses.ScheduledThread
import com.chomusukestudio.projectrocketc.MProcessingThread
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import kotlinx.android.synthetic.main.pre_game.*
import java.lang.Exception
import java.util.concurrent.Executors

enum class State { InGame, PreGame, Paused, Crashed }

class MainActivity : Activity() { // exception will be throw if you try to create any instance of this class on your own... i think.

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mProcessingThread: MProcessingThread
    private lateinit var myGLSurfaceView: MyGLSurfaceView

//    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    @Volatile var state: State = State.PreGame
        private set
    @Volatile var soundEffectsVolume = 100
        private set
    private lateinit var bgm: MediaPlayer
    @Volatile var musicVolume = 100
        private set

    public override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    
        // display splashScreen
        findViewById<View>(R.id.splashScreen).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_splash_image))
        
        bgm = MediaPlayer.create(this, R.raw.bgm)
        
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
		with(findViewById<SeekBar>(R.id.musicVolumeBar)) {
			progress = sharedPreferences.getInt(getString(R.string.musicVolume), 50)
            musicVolume = progress
			bgm.setVolume(musicVolume.toFloat()/100, musicVolume.toFloat()/100)
			setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
				override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    musicVolume = progress
					bgm.setVolume(musicVolume.toFloat()/100, musicVolume.toFloat()/100)
					with(sharedPreferences.edit()) {
						putInt(getString(R.string.musicVolume), musicVolume)
						apply()
					}
				}
				override fun onStartTrackingTouch(seekBar: SeekBar?) {}
				override fun onStopTrackingTouch(seekBar: SeekBar?) {}
			})
		}
        
        // update balance view
        findViewById<TextView>(R.id.balanceTextView).text = getString(R.string.add_dollar_symbol, sharedPreferences.getInt(getString(R.string.balance), 0))

        // see if this is the first time the game open
        if (sharedPreferences.getBoolean(getString(R.string.firstTimeOpen), true)) {
            showTutorial(findViewById(R.id.tutorialButton))

            // and set the firstTimeOpen to be false
            with(sharedPreferences.edit()) {
                putBoolean(getString(R.string.firstTimeOpen), false)
                apply()
            }
        }
        
        // update highest score
        findViewById<TextView>(R.id.highestScoreTextView).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())
        with(sharedPreferences.edit()) {
            putInt(getString(R.string.highestScoreRefresh), 0)
            apply()
        }

//        // Obtain the FirebaseAnalytics instance.
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // initialize surrounding
        Executors.newSingleThreadExecutor().submit {
            runWithExceptionChecked {
                mProcessingThread = MProcessingThread(
                        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate/*60f*/,
                        this) // we know that the context is MainActivity

                val size = Point() // updateBoundaries manually as SurfaceView won't be visible after initialization
                windowManager.defaultDisplay.getSize(size)
                mProcessingThread.updateBoundaries(size.x, size.y)

                myGLSurfaceView.initializeRenderer(mProcessingThread)

                mProcessingThread.waitForInit()

                // hide splash screen and show game
                this.runOnUiThread {

                    // cross fade them
                    findViewById<ConstraintLayout>(R.id.preGameLayout).visibility = View.VISIBLE
                    findViewById<ConstraintLayout>(R.id.preGameLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
                    findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.VISIBLE
                    findViewById<ConstraintLayout>(R.id.scoresLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
                    myGLSurfaceView.visibility = View.VISIBLE
                    myGLSurfaceView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_animation))
	
//					// start bgm
//					bgm.start()
//					bgm.isLooping = true
                    
                    // to the rocket last left off
                    swapRocket(sharedPreferences.getInt(getString(R.string.rocketIndex), 0))
					
                    showRocketQuirks()

                    findViewById<View>(R.id.splashScreen).animate()
                            .alpha(0f)
                            .setDuration(250)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    findViewById<View>(R.id.splashScreen).visibility = View.INVISIBLE
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
        findViewById<androidx.viewpager.widget.ViewPager>(R.id.tutorialPager).adapter = MyPagerAdapter(this)

        inTutorial = true
    }
    fun finishTutorial(view: View) {
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

        // start bgm
        bgmFadeOut.pauseAndWait()
        bgm.seekTo(2500)
        bgm.setVolume(musicVolume.toFloat()/100, musicVolume.toFloat()/100)
		bgm.start()
		while (!bgm.isPlaying) {
			// try to recover when MediaPlayer goes wrong
			try {
				bgm.release()
			} catch (e: Exception) {
				Log.e("bgm start", "$e")
			}
			try {
				bgm = MediaPlayer.create(this, R.raw.bgm)
				bgm.start()
			} catch (e: Exception) {
				Log.e("bgm start", "$e")
			}
		}
        bgm.isLooping = true

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
					// resume bgm
					bgm.start()
                    state = State.InGame
                }
                State.InGame -> {
                    pauseGame()
					// pause bgm
					bgm.pause()
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
				// restart bgm
				bgm.seekTo(2500)
				bgm.start()
            }
            else -> return // already in other state, could be lag so big that multi click check failed or pressed immediately after toHome
        }
    
        state = State.PreGame
        // change state before reset so next frame get the correct state
		// and also PreGame for reset to initialize correctly
        mProcessingThread.reset()
		// start the game
		state = State.InGame
    }
    
    private var volume = 1f
    private val bgmFadeOut: ScheduledThread = ScheduledThread(30) {
        volume /= 1.1f
        if (volume < 0.01f) {
            volume = 1f
            bgmFO.pause()
            bgm.pause()
            return@ScheduledThread
        }
        bgm.setVolume(volume*musicVolume/100, volume*musicVolume/100)
    }
    // bgmFO so bgmFadeOut can reference itself
    private val bgmFO = bgmFadeOut
    fun toHome(view: View) {
        when (state) {
            State.Crashed -> {// update highest score
                findViewById<TextView>(R.id.highestScoreTextView).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())

                findViewById<ConstraintLayout>(R.id.scoresLayout).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.scoresLayout).bringToFront()
                fadeOut(findViewById(R.id.onCrashLayout))
            }
            State.Paused -> {
				resumeGame()
                fadeOut(findViewById(R.id.inGameLayout))
            }
            else -> return
        } // already at home, must've been lag

        fadeIn(findViewById(R.id.preGameLayout))
	
		// fade out bgm
        bgmFadeOut.run()

		state = State.PreGame
		// change state before reset so next frame get the correct state
        mProcessingThread.reset()
    }

    fun swapRocketLeft(view: View) {
        swapRocket(-1)
    }
    fun swapRocketRight(view: View) {
        swapRocket(1)
    }
    private fun swapRocket(dIndex: Int) {
        mProcessingThread.swapRocket(dIndex)
        
        if (mProcessingThread.isOutOfBounds(1))
            findViewById<ImageButton>(R.id.swapRocketRightButton).visibility = View.INVISIBLE
        else
            findViewById<ImageButton>(R.id.swapRocketRightButton).visibility = View.VISIBLE
        if (mProcessingThread.isOutOfBounds(-1))
            findViewById<ImageButton>(R.id.swapRocketLeftButton).visibility = View.INVISIBLE
        else
            findViewById<ImageButton>(R.id.swapRocketLeftButton).visibility = View.VISIBLE
        
        val rocketQuirks = mProcessingThread.currentRocketQuirks
        val buyButton = findViewById<Button>(R.id.buyButton)
        val playButton = findViewById<ImageButton>(R.id.playButton)
        
        if (sharedPreferences.getBoolean(getString(R.string.bought, rocketQuirks.name), false)
                || mProcessingThread.rocketIndex == 0 // first rocket automatically brought
        ) {
            // if bought
            buyButton.visibility = View.INVISIBLE
            unlockLayout.visibility = View.INVISIBLE
            playButton.visibility = View.VISIBLE
        } else if (sharedPreferences.getInt(getString(R.string.highestScore), 0) < rocketQuirks.unlockScore) {
            // if not unlocked
            buyButton.visibility = View.INVISIBLE
            playButton.visibility = View.INVISIBLE
            unlockLayout.visibility = View.VISIBLE
            unlockTextView.text = getString(R.string.unlockScore, rocketQuirks.unlockScore)
        } else {
            // if not bought
            playButton.visibility = View.INVISIBLE
            unlockLayout.visibility = View.INVISIBLE
            buyButton.text = getString(R.string.add_dollar_symbol, rocketQuirks.price)
            buyButton.visibility = View.VISIBLE
        }
        showRocketQuirks()
//        with(sharedPreferences.edit()) {
//            putBoolean(getString(R.string.bought, rocketQuirks.name), false)
//            apply()
//        }
        
        // save rocket index
        with(sharedPreferences.edit()) {
            putInt(getString(R.string.rocketIndex), mProcessingThread.rocketIndex)
            apply()
        }
    
    }
    private fun showRocketQuirks() {
        val rocketQuirks = mProcessingThread.currentRocketQuirks
        findViewById<Button>(R.id.flyByDelta).text = getString(R.string.flybyDelta, formatQuirks(rocketQuirks.flybyDelta.toFloat()))
        findViewById<Button>(R.id.turningSpeed).text = getString(R.string.turningSpeed, formatQuirks(rocketQuirks.rotationSpeed * 1000))
        
        findViewById<Button>(R.id.name).text = rocketQuirks.name
    }
    private fun formatQuirks(value: Float) : String {
        val v = (value + 0.3f).toInt() // 0.3 is a random number it has no purpose except giving me the desire result without me doing more work
        val str = v.toString()
        return str + " ".repeat(2 - str.length)
    }

    fun showRocketDescription(view: View) {
        findViewById<TextView>(R.id.overlayMessage).typeface = Typeface.MONOSPACE
        findViewById<TextView>(R.id.overlayMessage).textSize = 21f
        displayMessage(mProcessingThread.currentRocketDescription)
    }
    fun flybyDeltaExplained(view: View) {
        findViewById<TextView>(R.id.overlayMessage).typeface = Typeface.MONOSPACE
        findViewById<TextView>(R.id.overlayMessage).textSize = 21f
        displayMessage(getString(R.string.flybyDeltaExplained))
    }
    fun turningSpeedExplained(view: View) {
        findViewById<TextView>(R.id.overlayMessage).typeface = Typeface.MONOSPACE
        findViewById<TextView>(R.id.overlayMessage).textSize = 21f
        displayMessage(getString(R.string.turningSpeedExplained))
    }
    
    fun buyRocket(view: View) {
        val balance = sharedPreferences.getInt(getString(R.string.balance), 0)
        val rocketQuirks = mProcessingThread.currentRocketQuirks
        
        if (rocketQuirks.price < balance) {
            val newBalance = balance - rocketQuirks.price
            with(sharedPreferences.edit()) {
                putBoolean(getString(R.string.bought, rocketQuirks.name), true)
                putInt(getString(R.string.balance), newBalance)
                apply()
            }
            
            findViewById<View>(R.id.buyButton).visibility = View.INVISIBLE
            findViewById<View>(R.id.playButton).visibility = View.VISIBLE
            findViewById<TextView>(R.id.balanceTextView).text = getString(R.string.add_dollar_symbol, newBalance)
        } else {
            findViewById<TextView>(R.id.overlayMessage).typeface = Typeface.DEFAULT
            findViewById<TextView>(R.id.overlayMessage).textSize = 27f
            displayMessage(getString(R.string.insufficient_funds))
        }
    }
    private fun displayMessage(msg: String) {
        findViewById<Button>(R.id.overlayMessage).text = msg
        overlay(findViewById(R.id.overlayMessage))
    }
    private fun overlay(view: View) {
        getCurrentLayout().visibility = View.INVISIBLE
        
        findViewById<View>(R.id.overlayBlack).visibility = View.VISIBLE
        findViewById<View>(R.id.overlayBlack).bringToFront()
        
        view.visibility = View.VISIBLE
        view.bringToFront()
    
    }
    fun removeOverlay(view: View) {
        findViewById<View>(R.id.overlayBlack).visibility = View.INVISIBLE
    
        getCurrentLayout().visibility = View.VISIBLE
        getCurrentLayout().bringToFront()
    
        view.visibility = View.INVISIBLE
    }
    private fun getCurrentLayout(): ConstraintLayout {
        return when (state) {
            State.Paused -> findViewById(R.id.onPausedLayout)
            State.PreGame -> findViewById(R.id.preGameLayout)
            State.Crashed -> findViewById(R.id.onCrashLayout)
            State.InGame -> findViewById(R.id.inGameLayout)
        }
    }

    private var inSetting = false
    fun openSetting(view: View) {
        overlay(findViewById(R.id.settingLayout))
        inSetting = true
    }
    fun closeSetting(view: View) {
        removeOverlay(findViewById(R.id.settingLayout))
        inSetting = false
    }

    fun onCrashed() {
        state = State.Crashed
    
        val score = LittleStar.score
        with(sharedPreferences) {
            with(edit()) {
                
                if (score > getInt(getString(R.string.highestScore), 0))
                    putInt(getString(R.string.highestScore), score) // update highest score
    
                // new balance
                putInt(getString(R.string.balance), getInt(getString(R.string.balance), 0) + score)
                
                apply()
            }
        }
        // firebase stuff that i don't understand
//            val bundle = Bundle()
//            bundle.putInt(FirebaseAnalytics.Param.SCORE, LittleStar.score)
//            bundle.putString("leaderboard_id", "mLeaderboard")
//            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle)
    
    
        runOnUiThread {
            findViewById<ConstraintLayout>(R.id.onCrashLayout).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.onCrashLayout).bringToFront()
        
            findViewById<TextView>(R.id.highestScoreOnCrash).text = /*putCommasInInt*/(sharedPreferences.getInt(getString(R.string.highestScore), 0).toString())
            findViewById<TextView>(R.id.previousScoreOnCrash).text = findViewById<TextView>(R.id.scoreTextView).text
            
            // update balance textView
            findViewById<TextView>(R.id.balanceTextView).text = getString(R.string.add_dollar_symbol, sharedPreferences.getInt(getString(R.string.balance), 0))
            findViewById<TextView>(R.id.gainedMoneyTextView).text = "+" + getString(R.string.add_dollar_symbol, score)
        
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
//						bgm.pause()
                    }
                }
                State.Paused -> {
//					if (hasFocus) {
//						bgm.start()
//					} else {
//                        bgm.pause()
//                    }
				}
                else -> {
                    if (!hasFocus) {
                        myGLSurfaceView.mRenderer.pauseGLRenderer()
                        bgm.pause()
                    } else {
                        myGLSurfaceView.mRenderer.resumeGLRenderer()
                        if (state == State.Crashed)
                            bgm.start()
                    }
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            // so the app is just starting in the first time... do nothing
        }
    }

    override fun onBackPressed() {
        if (inTutorial) {
            finishTutorial(findViewById(R.id.finishTutorialButton))
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
