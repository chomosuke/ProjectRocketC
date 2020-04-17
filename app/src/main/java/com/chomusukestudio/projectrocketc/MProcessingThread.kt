package com.chomusukestudio.projectrocketc

import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import com.chomusukestudio.prcandroid2dgameengine.ProcessingThread
import com.chomusukestudio.prcandroid2dgameengine.shape.*
import com.chomusukestudio.projectrocketc.Joystick.TwoFingersJoystick
import com.chomusukestudio.projectrocketc.Rocket.*
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.DragRocketPhysics
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.UI.MainActivity
import com.chomusukestudio.projectrocketc.UI.State
import com.chomusukestudio.projectrocketc.littleStar.LittleStar

class MProcessingThread(val refreshRate: Float, private val mainActivity: MainActivity): ProcessingThread(mainActivity) {

    private val state
            get() = mainActivity.state

    var joystick =
            TwoFingersJoystick(drawData)
//            OneFingerJoystick()
//            InertiaJoystick()
    private lateinit var surrounding: Surrounding
    var rocketIndex = 0
        private set
    private lateinit var rocket: Rocket
    init {
        // load sound for eat little star soundPool
        LittleStar.soundId = LittleStar.soundPool.load(mainActivity, R.raw.eat_little_star, 1)
//        LittleStar.soundId = LittleStar.soundPool.load("res/raw/eat_little_star.m4a", 1) // this is not working
    }

    override fun initializeWithBoundaries() {
        CircularShape.pixelPerLength = (1 / drawData.pixelSize.x).toInt()
        surrounding = Surrounding(mainActivity, drawData)
        rocket = getRocket(rocketIndex)
        surrounding.initializeSurrounding(rocket, mainActivity.state)
    }
    
    val currentRocketQuirks get() = rocket.rocketQuirks

    private fun getRocket(rocketIndex: Int): Rocket {
        return when (rocketIndex) {
			0 -> V2(surrounding, mainActivity, DragRocketPhysics(), drawData)
            1 -> SaturnV(surrounding, mainActivity, DragRocketPhysics(), drawData)
            2 -> Falcon9(surrounding, mainActivity, DragRocketPhysics(), drawData)
            3 -> FalconHeavy(surrounding, mainActivity, DragRocketPhysics(), drawData)
            4 -> SpaceShuttle(surrounding, mainActivity, DragRocketPhysics(), drawData)
            else -> throw IndexOutOfBoundsException("rocketIndex out of bounds")
        }
    }
    fun swapRocket(dIndex: Int) {
        pause() // to prevent removing the removed
        rocket.removeAllShape()
        rocketIndex += dIndex
        rocket = getRocket(rocketIndex)
        surrounding.rocket = rocket
        resume()
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

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return if (state == State.InGame || state == State.Paused) {
            joystick.onTouchEvent(e)
            true
        } else
            false
    }

    override fun getLeftRightBottomTopBoundaries(width: Int, height: Int): FloatArray {
        val widthOverHeight = width.toFloat() / height.toFloat()
        return when {
            widthOverHeight > 9f / 16f -> // if the screen is wider than a 16:9 screen
                floatArrayOf(widthOverHeight * -8f, widthOverHeight * 8f, -8f, 8f)

            1 / widthOverHeight > 16f / 9f -> // if the screen is taller than a 16:9 screen
                floatArrayOf(-4.5f, 4.5f, 1 / widthOverHeight * -4.5f, 1 / widthOverHeight * 4.5f)

            else -> // if the screen is 16;9
                floatArrayOf(-4.5f, 4.5f, -8f, 8f)
        }
    }

    fun reset() {
        pause() // to prevent removing the removed
        removeAllShapes() // remove all previous shapes
        val surroundingResources = surrounding.trashAndGetResources()
        surrounding = Surrounding(mainActivity, drawData, surroundingResources)
        rocket = getRocket(rocketIndex)
        surrounding.initializeSurrounding(rocket, mainActivity.state)
            joystick = TwoFingersJoystick(drawData)
//            joystick = OneFingerJoystick()
//        joystick = InertiaJoystick()
        LittleStar.cleanScore()
        resume()
    }

    private fun removeAllShapes() {
        surrounding.removeAllShape()
        rocket.removeAllShape()
        joystick.removeAllShape()
    } // for onStop() and onDestroy() to remove Shapes
    // and when crashed

    private val warningRed = TriangularShape(Vector(0f, 100f), Vector(100f, -100f), Vector(-100f, -100f), Color(1f, 0f, 0f, 0.5f), BuildShapeAttr(-100f, false, drawData))

    private var previousFrameTime: Long = 0
    override fun generateNextFrame(timeInMillis: Long) {
        val startTime = SystemClock.uptimeMillis()

        if (previousFrameTime == 0L)
            previousFrameTime = timeInMillis
        if (state == State.InGame) {
            inGame(timeInMillis, previousFrameTime)
        }
        if (state == State.Crashed) {
            crashed(timeInMillis, previousFrameTime)
        }
//                    if (state == State.PreGame) {
//                        rocket.moveRocket(joystick.getRocketControl(rocket.currentRotation), now, previousFrameTime, state)
//                        surrounding.makeNewTriangleAndRemoveTheOldOne(now, previousFrameTime, state)
//                    }

        if (SystemClock.uptimeMillis() - startTime > 16) {
            Log.i("processing thread", "" + (SystemClock.uptimeMillis() - startTime))
//            warningRed.visibility = true
        } else warningRed.visibility = false

        previousFrameTime = timeInMillis
    }
    private fun inGame(now: Long, previousFrameTime: Long) {

        // see if crashed
        if (rocket.isCrashed(surrounding, now - previousFrameTime)) {
            mainActivity.onCrashed()
        }

        surrounding.checkAndAddLittleStar(now)

        rocket.moveRocket(joystick.getRocketControl(rocket.currentRotation), now, previousFrameTime, state)
        surrounding.makeNewTriangleAndRemoveTheOldOne(now, previousFrameTime, state)

        joystick.drawJoystick()

        updateScore()
    }
    private fun crashed(now: Long, previousFrameTime: Long) {
        rocket.fadeTrace(now, previousFrameTime)
        rocket.drawExplosion(now, previousFrameTime)
    }
}