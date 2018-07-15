package com.chomusukestudio.projectrocketc.Joystick

class TwoFingersJoystick : Joystick() {
    override fun drawJoystick() {
        // no need to draw anything
    }
    
    override fun removeAllShape() {
        // no need to remove anything
    }

    override fun getTurningDirection(currentRotation: Float): Float {
        return if (actionDown) {
            if (nowX > 0)
                1f
            else if (nowX < -0)
                -1f
            else
                0f
        } else
            0f
    }
}
