package com.chomusukestudio.projectrocketc.joystick

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.*

class OneFingerJoystick(drawData: DrawData) : Joystick(drawData) {
    private val circularShape = CircularShape(Vector(0f, -4f), 1f, Color(1f, 1f, 1f, 0.4f), BuildShapeAttr(-10f, true, drawData))
    private val quadrilateralShape = QuadrilateralShape(Vector(-1f, -4.1f), Vector(-1f, -3.9f), Vector(1f, -3.9f), Vector(1f, -4.1f),
            Color(1f, 1f, 1f, 0.4f), BuildShapeAttr(-10f, true, drawData))

    override fun drawJoystick() {
        if (actionDown)
            circularShape.resetParameter(Vector(
                    if (nowXY.x < -1f)
                        -1f
                    else if (nowXY.x > 1f)
                        1f
                    else
                        nowXY.x
                    , -4f), 1f)
        else
            circularShape.resetParameter(Vector(0f, -4f), 1f)
    }
    
    override fun removeAllShape() {
        circularShape.remove()
        quadrilateralShape.remove()
    }

    override fun getRocketControl(currentRotation: Float): RocketControl {
        return RocketControl(if (actionDown) {
            if (nowXY.x > 0)
                1f
            else if (nowXY.y < -0)
                -1f
            else
                0f
        } else
            0f
        )
    }
}
