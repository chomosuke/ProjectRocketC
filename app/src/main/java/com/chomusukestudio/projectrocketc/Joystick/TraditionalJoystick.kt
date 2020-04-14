package com.chomusukestudio.projectrocketc.Joystick

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData

/**
 * Created by Shuang Li on 1/04/2018.
 */

class TraditionalJoystick(drawData: DrawData) : Joystick(drawData) {
    override fun getRocketControl(currentRotation: Float): RocketControl {
        return RocketControl(0f)
    }

    override fun drawJoystick() {

    }

    override fun removeAllShape() {

    }
}