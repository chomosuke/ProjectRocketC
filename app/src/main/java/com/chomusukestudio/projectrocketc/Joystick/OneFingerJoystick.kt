package com.chomusukestudio.projectrocketc.Joystick

import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape

class OneFingerJoystick(layers: Layers) : Joystick() {
    private val circularShape = CircularShape(0f, -4f, 1f, 1f, 1f, 1f, 0.4f, BuildShapeAttr(-10f, true, layers))
    private val quadrilateralShape = QuadrilateralShape(-1f, -4.1f, -1f, -3.9f, 1f, -3.9f, 1f, -4.1f, 1f, 1f, 1f, 0.4f, BuildShapeAttr(-10f, true, layers))

    override fun drawJoystick() {
        if (actionDown)
            circularShape.resetParameter(
                    if (nowX < -1f)
                        -1f
                    else if (nowX > 1f)
                        1f
                    else
                        nowX
                    , -4f, 1f)
        else
            circularShape.resetParameter(0f, -4f, 1f)
    }
    
    override fun removeAllShape() {
        circularShape.removeShape()
        quadrilateralShape.removeShape()
    }

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
