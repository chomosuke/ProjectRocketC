package com.chomusukestudio.projectrocketc.Joystick

import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.TriangularShape

import java.lang.Math.PI
import java.lang.Math.atan2

class ArrowJoystick(private val centerOfRotationX: Float, private val centerOfRotationY: Float, buildShapeAttr: BuildShapeAttr) : Joystick() {
    @Volatile
    private var centerOfJoystickX: Float = 0f
    @Volatile
    private var centerOfJoystickY: Float = 0f
    
    private// range from -pi to pi;
    val intendedDirection: Float
        get() = atan2((nowX - centerOfJoystickX).toDouble(), (nowY - centerOfJoystickY).toDouble()).toFloat()
    
    private val triangularShape = TriangularShape(0f, 0f, 0f, 0f, 0f, 0f, 0.7f, 0.3f, 0.3f, 0.9f, buildShapeAttr)
    private val quadrilateralShape = QuadrilateralShape(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.7f, 0.3f, 0.3f, 0.9f, buildShapeAttr)
    private fun actionDown() {
        this.centerOfJoystickX = nowX
        this.centerOfJoystickY = nowY
    }

    override fun updateTouchPosition(nowX: Float, nowY: Float) {
        actionDown()
        super.updateTouchPosition(nowX, nowY)
    }
    
    override fun getTurningDirection(currentRotation: Float): Float { // compare currentRotation with intended direction to determine turning direction
        if (actionDown) { // only rotate rocket when the screen is touched
            var rotationNeeded = intendedDirection - currentRotation
            
            while (true) { // get rotationNeeded in range of -pi and pi
                if (rotationNeeded > PI)
                    rotationNeeded -= (2 * PI).toFloat()
                else if (rotationNeeded < -PI)
                    rotationNeeded += (2 * PI).toFloat()
                else
                    break
            }
            return rotationNeeded
        } // it works, but why
        else
            return 0f
    }
    
    override fun drawJoystick() {
        if (actionDown) { // only display arrow if the screen is touched
            triangularShape.setTriangleCoords(centerOfRotationX + nowX - centerOfJoystickX,
                    centerOfRotationY + nowY - centerOfJoystickY + 0.5f,
                    centerOfRotationX + nowX - centerOfJoystickX + 0.15f,
                    centerOfRotationY + nowY - centerOfJoystickY + 0.3f,
                    centerOfRotationX + nowX - centerOfJoystickX - 0.15f,
                    centerOfRotationY + nowY - centerOfJoystickY + 0.3f)
            quadrilateralShape.setQuadrilateralShapeCoords(centerOfRotationX + nowX - centerOfJoystickX - 0.05f,
                    centerOfRotationY + nowY - centerOfJoystickY + 0.3f,
                    centerOfRotationX + nowX - centerOfJoystickX + 0.05f,
                    centerOfRotationY + nowY - centerOfJoystickY + 0.3f,
                    centerOfRotationX + nowX - centerOfJoystickX + 0.05f,
                    centerOfRotationY + nowY - centerOfJoystickY,
                    centerOfRotationX + nowX - centerOfJoystickX - 0.05f,
                    centerOfRotationY + nowY - centerOfJoystickY) // update arrow position
            
            // rotate arrow
            triangularShape.rotateShape(centerOfRotationX + nowX - centerOfJoystickX, centerOfRotationY + nowY - centerOfJoystickY, intendedDirection)
            quadrilateralShape.rotateShape(centerOfRotationX + nowX - centerOfJoystickX, centerOfRotationY + nowY - centerOfJoystickY, intendedDirection)
        } else { // if the screen is not touched
            triangularShape.setTriangleCoords(0f, 0f, 0f, 0f, 0f, 0f) // hide arrow
            quadrilateralShape.setQuadrilateralShapeCoords(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        }
    }
    
    override fun removeAllShape() {
        triangularShape.removeShape()
        quadrilateralShape.removeShape()
    }
}
