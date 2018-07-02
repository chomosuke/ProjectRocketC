package com.chomusukestudio.projectrocketc.processingThread

import android.view.MotionEvent

interface ProcessingThread {
    var isStarted: Boolean
    fun onTouchEvent(e: MotionEvent): Boolean
    fun generateNextFrame(now: Long, previousFrameTime: Long)
    fun waitForLastFrame()
    fun shutDown()
}