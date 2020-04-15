package com.chomusukestudio.projectrocketc.UI

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.chomusukestudio.prcandroid2dgameengine.PRCGLSurfaceView
import com.chomusukestudio.prcandroid2dgameengine.scanForActivity

class MyGLSurfaceView(context: Context, attributeSet: AttributeSet) : PRCGLSurfaceView(context, attributeSet) {
    override fun onTouchEvent(e: MotionEvent): Boolean {
        (scanForActivity(context) as MainActivity).onTouchMyGLSurface(e) // we know that the context is MainActivity
        return super.onTouchEvent(e)
    }
}