package com.chomusukestudio.projectrocketc.processingThread

import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import com.chomusukestudio.projectrocketc.*
import com.chomusukestudio.projectrocketc.GLRenderer.Layer
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Joystick.Joystick
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

class ProcessingThread(var joystick: Joystick, var surrounding: Surrounding, var rocket: Rocket, val refreshRate: Float, val mainActivity: MainActivity) {
    fun onTouchEvent(e: MotionEvent, state: State): Boolean {
        return if (state == State.InGame || state == State.Paused) {
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

    fun generateNextFrame(now: Long, previousFrameTime: Long, state: State) {
        finished = false // haven't started
        nextFrameThread.submit {
            runWithExceptionChecked {
                val startTime = SystemClock.uptimeMillis()

                if (state == State.InGame) {

                    // see if crashed
                    if (rocket.isCrashed(surrounding)) {
                        mainActivity.onCrashed()
                    }
                    surrounding.checkAndAddLittleStar(now)
                }
                if (state == State.PreGame || state == State.InGame) {
                    rocket.moveRocket(joystick.getTurningDirection(rocket.currentRotation), now, previousFrameTime, state)
                    surrounding.makeNewTriangleAndRemoveTheOldOne(now, previousFrameTime, state)
                    joystick.drawJoystick()
                }
                if (state == State.Crashed) {
                    rocket.fadeTrace(now, previousFrameTime)
                    rocket.drawExplosion(now, previousFrameTime)
                }

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
                }

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