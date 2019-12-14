package com.chomusukestudio.projectrocketc.Joystick

/**
 * Created by Shuang Li on 1/04/2018.
 */

class TraditionalJoystick : Joystick() {
    override fun getRocketControl(currentRotation: Float): RocketControl {
        return RocketControl(0f)
    }

    override fun drawJoystick() {

    }

    override fun removeAllShape() {

    }
}