package com.chomusukestudio.projectrocketc.processingThread

import android.view.MotionEvent
import com.chomusukestudio.projectrocketc.GLRenderer.GLTriangle

class TestingProcessingThread : ProcessingThread() {
    override fun shutDown() {
        // do nothing
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        // do nothing
        return true
    }

    val triangle = GLTriangle(1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f, 1f, 1f, 1f)
    override fun generateNextFrame(now: Long, previousFrameTime: Long) {

    }

    override fun waitForLastFrame() {
        // do nothing
    }

    override var isStarted: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
}