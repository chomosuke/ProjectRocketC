package com.chomusukestudio.projectrocketc.Joystick

class OneFingerJoystick : Joystick() {
    override fun drawJoystick() {
        // do nothing
    }
    
    override fun removeAllShape() {
        // do nothing
    }
    
    override fun getTurningDirection(currentRotation: Float): Float {
        return if (actionDown)
        // turn right
            1f
        else
        // turn left
            -1f
    }
}
