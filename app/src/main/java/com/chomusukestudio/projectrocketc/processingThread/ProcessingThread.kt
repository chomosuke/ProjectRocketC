package com.chomusukestudio.projectrocketc.processingThread

import android.util.Log
import android.view.MotionEvent
import com.chomusukestudio.projectrocketc.Joystick.Joystick
import com.chomusukestudio.projectrocketc.MainActivity
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.Logger
import com.chomusukestudio.projectrocketc.transformToMatrixX
import com.chomusukestudio.projectrocketc.transformToMatrixY
import com.chomusukestudio.projectrocketc.upTimeMillis

class ProcessingThread(var joystick: Joystick, var surrounding: Surrounding, var rocket: Rocket, val refreshRate: Float, val mainActivity: MainActivity) {
    fun onTouchEvent(e: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        val x = transformToMatrixX(e.x)
        val y = transformToMatrixY(e.y)

        if (isStarted) {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    joystick.updateTouchPosition(x, y)
                }
                MotionEvent.ACTION_UP -> joystick.actionUp()
                MotionEvent.ACTION_MOVE -> joystick.updateTouchPosition(x, y)
            }
        }
        return true
    }

    fun shutDown() {
        removeAllShapes()
    }

    private fun removeAllShapes() {
        surrounding.removeAllShape()
        rocket.removeAllShape()
        joystick.removeAllShape()
    } // for onStop() and onDestroy() to remove Shapes
    // and when crashed

    private val nextFrameThread = Executors.newSingleThreadExecutor { r -> Thread(r, "nextFrameThread") }
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    fun generateNextFrame(now: Long, previousFrameTime: Long) {
        nextFrameThread.submit {
            try {
                finished = false

                if (isStarted) {

                    // see if crashed
                    if (rocket.isCrashed(surrounding)) {
                        removeAllShapes()// removing shapes
                        mainActivity.onCrashed()
                    }
                    surrounding.anyLittleStar()
                }
                rocket.moveRocket(joystick.getTurningDirection(rocket.currentRotation), now, previousFrameTime)
                surrounding.makeNewTriangleAndRemoveTheOldOne(now, previousFrameTime)

                joystick.drawJoystick()

                if (upTimeMillis() - now > 1000 / refreshRate) {
                    if (CircularShape.dynamicPerformanceIndex > 0.3) {
                        CircularShape.dynamicPerformanceIndex /= 1.001
                    }
                }

                if (upTimeMillis() - now < 1000 / refreshRate) { // increase imageQuality by increasing number of edges
                    if (CircularShape.dynamicPerformanceIndex < 1) {
                        CircularShape.dynamicPerformanceIndex *= 1.001
                    }
                }

                if (upTimeMillis() - now > 1000 / refreshRate) {
                    Log.i("processing thread", "" + (upTimeMillis() - now))
                }
                //            Log.v("processing thread", "" + (upTimeMillis() - now));

                finished = true

                lock.lock()
                condition.signal()
                //                Log.v("Thread", "nextFrameThread notified lockObject");
                lock.unlock() // wakes up GLThread
            } catch (e: Exception) {
                val logger = Logger.getAnonymousLogger()
                logger.log(Level.SEVERE, "an exception was thrown in nextFrameThread", e)
                throw e
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

    var finished = true // last frame that doesn't exist has finish

    var isStarted
        get() = surrounding.isStarted
        set(isStarted) {
            surrounding.isStarted = isStarted
            if (isStarted)
                LittleStar.cleanScore()
        }
}