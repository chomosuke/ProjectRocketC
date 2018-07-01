package com.chomusukestudio.projectrocketc.Joystick

/**
 * Created by Shuang Li on 1/04/2018.
 */

class TraditionalJoystick : Joystick() {
    override fun getTurningDirection(currentRotation: Float): Float {
        return 0f
    }
    
    override fun drawJoystick() {
    
    }
    
    override fun removeAllShape() {
    
    }
}
