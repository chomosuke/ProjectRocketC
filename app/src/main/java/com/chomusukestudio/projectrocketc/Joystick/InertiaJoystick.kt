package com.chomusukestudio.projectrocketc.Joystick

import android.view.MotionEvent
import com.chomusukestudio.projectrocketc.transformToMatrixX
import com.chomusukestudio.projectrocketc.transformToMatrixY

class InertiaJoystick: Joystick() {
	private var nowX2 = 0f
	private var nowY2 = 0f
	override fun onTouchEvent(e: MotionEvent) {
		// MotionEvent reports input details from the touch screen
		// and other input controls. In this case, you are only
		// interested in events where the touch position changed.
		when (e.actionMasked) {
			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
				if (!pointers.contains(e.getPointerId(e.actionIndex))) // add the new pointer
					pointers.add(e.getPointerId(e.actionIndex))
//                else // this actually does happen for some reason
//                    throw RuntimeException("New pointer is already in pointers.") // interesting
			
			MotionEvent.ACTION_UP -> {
				// last pointer is up
				if (pointers.isNotEmpty()) // pointers.isEmpty() if pointer is from before the game start
					pointers.remove(e.getPointerId(e.actionIndex))
				
				if (pointers.isNotEmpty())
					throw RuntimeException("There still pointers after action up.")
				return
			}
			MotionEvent.ACTION_POINTER_UP ->
				if (pointers.isNotEmpty()) // pointers.isEmpty() if pointer is from before the game start
					pointers.remove(e.getPointerId(e.actionIndex))
		}
		nowX = transformToMatrixX(e.getX(e.findPointerIndex(pointers.last())))
		nowY = transformToMatrixY(e.getY(e.findPointerIndex(pointers.last())))
		
		if (pointers.size > 1) {
			nowX2 = transformToMatrixX(e.getX(e.findPointerIndex(pointers[pointers.lastIndex - 1])))
			nowY2 = transformToMatrixY(e.getY(e.findPointerIndex(pointers[pointers.lastIndex - 1])))
		} else {
			nowX2 = 0f
			nowY2 = 0f
		}
		if (nowX2 > nowX) { // nowX is always right
			val tempX = nowX2
			val tempY = nowY2
			nowX2 = nowX
			nowY2 = nowY
			nowX = tempX
			nowY = tempY
		}
	}
	override fun getRocketMotion(currentRotation: Float): RocketMotion {
		return RocketMotion((if (actionDown) {
			if (nowX > 2 && nowX < 4)
				1f
			else if (nowX < 2 && nowX > 0)
				-1f
			else
				0f
		} else
			0f
		), (actionDown && nowX2 < 0))
	}
}