package com.chomusukestudio.projectrocketc.Joystick

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData

class TwoFingersJoystick(drawData: DrawData) : Joystick(drawData) {
    override fun getRocketControl(currentRotation: Float): RocketControl {
        return RocketControl(if (actionDown) {
            when {
                nowXY.x > 0 -> -1f
                nowXY.x < -0 -> 1f
                else -> 0f
            }
        } else
            0f
        )
    }
}