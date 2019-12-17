package com.chomusukestudio.projectrocketc

import android.media.MediaPlayer
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Joystick.TwoFingersJoystick
import com.chomusukestudio.projectrocketc.Rocket.*
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.DragRocketPhysics
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.Color
import com.chomusukestudio.projectrocketc.Shape.TriangularShape
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import java.lang.IndexOutOfBoundsException
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

class ProcessingThread(val refreshRate: Float, private val mainActivity: MainActivity, private val layers: Layers) {

    private val state
            get() = mainActivity.state

    var joystick =
            TwoFingersJoystick()
//            OneFingerJoystick()
//            InertiaJoystick()
    private var surrounding = Surrounding(TouchableView(mainActivity.findViewById(R.id.visualText), mainActivity), layers)
    private var rocketIndex = 0
    private var rocket = getRocket(rocketIndex)
    init {
        // load sound for eat little star soundPool
        LittleStar.soundId = LittleStar.soundPool.load(mainActivity, R.raw.eat_little_star, 1)
//        LittleStar.soundId = LittleStar.soundPool.load("res/raw/eat_little_star.m4a", 1) // this is not working

        surrounding.initializeSurrounding(rocket, mainActivity.state)
    }

    private fun getRocket(rocketIndex: Int): Rocket {
        val crashSound = MediaPlayer.create(mainActivity, R.raw.fx22)
        crashSound.setVolume(0.66f, 0.66f)
        return when (rocketIndex) {
			0 -> V2InstantDeath(surrounding, crashSound, DragRocketPhysics(), layers)
            1 -> V2(surrounding, crashSound, DragRocketPhysics(), layers)
            2 -> SaturnV(surrounding, crashSound, DragRocketPhysics(), layers)
            3 -> Falcon9(surrounding, crashSound, DragRocketPhysics(), layers)
            4 -> FalconHeavy(surrounding, crashSound, mainActivity, DragRocketPhysics(), layers)
            else -> throw IndexOutOfBoundsException("rocketIndex out of bounds")
        }
    }
    fun swapRocket(dIndex: Int) {
        pauseForChanges()
        rocket.removeAllShape()
        rocketIndex += dIndex
        rocket = getRocket(rocketIndex)
        surrounding.rocket = rocket
        resumeWithChanges()
    }
    fun isOutOfBounds(dIndex: Int): Boolean {
        val index = rocketIndex + dIndex
        return index !in 0..4
    }

    private fun updateScore() {
        mainActivity.runOnUiThread {
            mainActivity.findViewById<TextView>(R.id.scoreTextView).text = /*putCommasInInt*/(LittleStar.score.toString())
            mainActivity.findViewById<TextView>(R.id.deltaTextView).text = "δ " + (LittleStar.dScore).toString()
        }
    }

    fun updateHighestScore(updateHighestScore: (Int) -> Unit ) {
        updateHighestScore(LittleStar.score)
    }

    fun onTouchEvent(e: MotionEvent): Boolean {
        return if (state == State.InGame || state == State.Paused) {
            joystick.onTouchEvent(e)
            true
        } else
            false
    }

    fun reset() {
        pauseForChanges()
        removeAllShapes() // remove all previous shapes
        val surroundingResources = surrounding.trashAndGetResources()
        surrounding = Surrounding(TouchableView(mainActivity.findViewById(R.id.visualText), mainActivity), layers, surroundingResources)
        rocket = getRocket(rocketIndex)
        surrounding.initializeSurrounding(rocket, mainActivity.state)
            joystick = TwoFingersJoystick()
//            joystick = OneFingerJoystick()
//        joystick = InertiaJoystick()
        LittleStar.cleanScore()
        resumeWithChanges()
    }

    private fun removeAllShapes() {
        surrounding.removeAllShape()
        rocket.removeAllShape()
        joystick.removeAllShape()
    } // for onStop() and onDestroy() to remove Shapes
    // and when crashed

    private var pausedForChanges = false
    private fun pauseForChanges() {
        if (!pausedForChanges) {
            pausedForChanges = true
            waitForLastFrame()
        }
    }
    private fun resumeWithChanges() {
        pausedForChanges = false
    }

    fun shutDown() {
        removeAllShapes()
    }

    private val nextFrameThread = Executors.newSingleThreadExecutor { r -> Thread(r, "nextFrameThread") }
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private val warningRed = TriangularShape(Vector(0f, 100f), Vector(100f, -100f), Vector(-100f, -100f), Color(1f, 0f, 0f, 0.5f), BuildShapeAttr(-100f, false, layers))
    
    fun generateNextFrame(now: Long, previousFrameTime: Long) {
        if (!pausedForChanges) { // if aren't pausing for changes
            finished = false // haven't started
            nextFrameThread.submit {
                runWithExceptionChecked {
                    val startTime = SystemClock.uptimeMillis()

                    if (state == State.InGame) {

                        // see if crashed
                        if (rocket.isCrashed(surrounding, now - previousFrameTime)) {
                            mainActivity.onCrashed()
                        }
                        surrounding.checkAndAddLittleStar(now)
                    }
                    if (/*state == State.PreGame || */state == State.InGame) {
                        rocket.moveRocket(joystick.getRocketControl(rocket.currentRotation), now, previousFrameTime)
                        surrounding.makeNewTriangleAndRemoveTheOldOne(now, previousFrameTime, state)
                        joystick.drawJoystick()
                    }
                    if (state == State.Crashed) {
                        rocket.fadeTrace(now, previousFrameTime)
                        rocket.drawExplosion(now, previousFrameTime)
                    }
                    if (state == State.InGame)
                        updateScore() // only update score when InGame

////                if (SystemClock.uptimeMillis() - startTime > 1000 / refreshRate) {
//                if (SystemClock.uptimeMillis() - startTime > 16) { // target 60 fps
//                    if (CircularShape.performanceIndex > 0.3) {
//                        CircularShape.performanceIndex /= 1.001
//                    }
//                }
//
////                if (SystemClock.uptimeMillis() - startTime < 1000 / refreshRate) {
//                if (SystemClock.uptimeMillis() - startTime < 16) { // increase imageQuality by increasing number of edges
//                    if (CircularShape.performanceIndex < 1) {
//                        CircularShape.performanceIndex *= 1.001
//                    }
//                }
                    // let's do this at the end

                    if (SystemClock.uptimeMillis() - startTime > 16) {
                        Log.i("processing thread", "" + (SystemClock.uptimeMillis() - startTime))
//                        warningRed.visibility = true
                    }
                    else warningRed.visibility = false

                    // finished
                    finished = true
                    // notify waitForLastFrame
                    lock.lock()
                    condition.signal() // wakes up GLThread
                    //                Log.v("Thread", "nextFrameThread notified lockObject");
                    lock.unlock()
                }
            }
        }
    }

    fun waitForLastFrame() {
        // wait for the last nextFrameThread
        lock.lock()
        // synchronized outside the loop so other thread can't notify when it's not waiting
        while (!finished) {
            //                Log.v("Thread", "nextFrameThread wait() called");
            try {
                condition.await() // wait, last nextFrameThread will wake this Thread up
            } catch (e: InterruptedException) {
                Log.e("lock", "Who would interrupt lock? They don't even have the reference.", e)
            }

            //                Log.v("Thread", "nextFrameThread finished waiting");
        }
        lock.unlock()
    }

    @Volatile var finished = true // last frame that doesn't exist has finish
}