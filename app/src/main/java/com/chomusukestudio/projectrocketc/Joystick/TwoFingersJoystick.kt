package com.chomusukestudio.projectrocketc.Joystick

class TwoFingersJoystick : Joystick() {
    override fun getRocketMotion(currentRotation: Float): RocketMotion {
        return RocketMotion(if (actionDown) {
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
