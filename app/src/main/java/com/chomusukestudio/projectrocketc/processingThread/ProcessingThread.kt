package com.chomusukestudio.projectrocketc.processingThread

import android.view.MotionEvent

abstract class ProcessingThread {
    abstract var isStarted: Boolean
    abstract fun onTouchEvent(e: MotionEvent): Boolean
    abstract fun generateNextFrame(now: Long, previousFrameTime: Long)
    abstract fun waitForLastFrame()
    abstract fun shutDown()
}