package com.chomusukestudio.projectrocketc.Joystick

import android.view.MotionEvent
import com.chomusukestudio.projectrocketc.Shape.Vector
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
				if (!pointers.contains(e.getPointerId(e.actionIndex))) // offset the new pointer
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
		var pointerExist: Boolean
		do {
			pointerExist = true
			try {
				nowXY = Vector(transformToMatrixX(e.getX(e.findPointerIndex(pointers.last()))),
						transformToMatrixY(e.getY(e.findPointerIndex(pointers.last()))))
			} catch (e: IllegalArgumentException) {
				pointerExist = false
			}
		} while (!pointerExist)
		
		
		if (pointers.size > 1) {
			nowX2 = transformToMatrixX(e.getX(e.findPointerIndex(pointers[pointers.lastIndex - 1])))
			nowY2 = transformToMatrixY(e.getY(e.findPointerIndex(pointers[pointers.lastIndex - 1])))
		} else {
			nowX2 = 0f
			nowY2 = 0f
		}
	}
	override fun getRocketControl(currentRotation: Float): RocketControl {
		return RocketControl(
				if (actionDown && nowX2 == 0f) {
					when {
						nowXY.x > 0 -> -1f
						nowXY.x < 0 -> 1f
						else -> 0f
					}
				} else 0f
				, !((nowX2 > 0 && nowXY.x < 0) || (nowXY.x > 0 && nowX2 < 0)))
	}
}