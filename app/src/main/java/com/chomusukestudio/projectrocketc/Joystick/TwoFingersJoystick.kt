package com.chomusukestudio.projectrocketc.Joystick

class TwoFingersJoystick : Joystick() {
    override fun getRocketControl(currentRotation: Float): RocketControl {
        return RocketControl(if (actionDown) {
            if (nowX > 0)
                1f
            else if (nowX < -0)
                -1f
            else
                0f
        } else
            0f
        )
    }
}
