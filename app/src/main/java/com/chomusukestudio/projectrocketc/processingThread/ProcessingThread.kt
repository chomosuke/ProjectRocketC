package com.chomusukestudio.projectrocketc.processingThread

import android.util.Log
import android.view.MotionEvent
import com.chomusukestudio.projectrocketc.*
import com.chomusukestudio.projectrocketc.Joystick.Joystick
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.Logger

class ProcessingThread(var joystick: Joystick, var surrounding: Surrounding, var rocket: Rocket, val refreshRate: Float, val mainActivity: MainActivity) {
    fun onTouchEvent(e: MotionEvent): Boolean {
        return if (state == State.InGame) {
            joystick.onTouchEvent(e)
            true
        } else
            false
    }

    fun shutDown() {
        removeAllShapes()
    }

    fun removeAllShapes() {
        surrounding.removeAllShape()
        rocket.removeAllShape()
        joystick.removeAllShape()
    } // for onStop() and onDestroy() to remove Shapes
    // and when crashed

    private val nextFrameThread = Executors.newSingleThreadExecutor { r -> Thread(r, "nextFrameThread") }
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    fun generateNextFrame(now: Long, previousFrameTime: Long) {
        finished = false // haven't started
        nextFrameThread.submit {
            try {

                if (state == State.InGame) {

                    // see if crashed
                    if (rocket.isCrashed(surrounding)) {
                        state = State.Crashed
                        mainActivity.onCrashed()
                    }
                    surrounding.anyLittleStar()
                }
                if (state == State.PreGame || state == State.InGame) {
                    rocket.moveRocket(joystick.getTurningDirection(rocket.currentRotation), now, previousFrameTime)
                    surrounding.makeNewTriangleAndRemoveTheOldOne(now, previousFrameTime)
                    joystick.drawJoystick()
                }
                if (state == State.Crashed) {
                    rocket.drawExplosion()
                }

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

                // finished
                finished = true
                // notify waitForLastFrame
                lock.lock()
                condition.signal()
                //                Log.v("Thread", "nextFrameThread notified lockObject");
                lock.unlock() // wakes up GLThread
            } catch (e: Exception) {
                val logger = Logger.getAnonymousLogger()
                logger.log(Level.SEVERE, "an exception was thrown in nextFrameThread", e)
                Log.e("exception", "in processingThread" + e)
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

    @Volatile var finished = true // last frame that doesn't exist has finish
}