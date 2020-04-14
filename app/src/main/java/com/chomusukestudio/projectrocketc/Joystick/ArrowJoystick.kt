package com.chomusukestudio.projectrocketc.Joystick

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.*

import java.lang.Math.PI

class ArrowJoystick(private val centerOfRotation: Vector, buildShapeAttr: BuildShapeAttr, drawData: DrawData) : Joystick(drawData) {
    @Volatile
    private var centerOfJoystick = Vector(0f, 0f)
    
    private// range from -pi to pi;
    val intendedDirection: Float
        get() = (nowXY - centerOfJoystick).direction
    
    private val triangularShape = TriangularShape(Vector(0f, 0f), Vector(0f, 0f), Vector(0f, 0f), Color(0.7f, 0.3f, 0.3f, 0.9f), buildShapeAttr)
    private val quadrilateralShape = QuadrilateralShape(Vector(0f, 0f), Vector(0f, 0f), Vector(0f, 0f), Vector(0f, 0f), Color(0.7f, 0.3f, 0.3f, 0.9f), buildShapeAttr)
    private fun actionDown() {
        this.centerOfJoystick = nowXY
    }

    override fun updateTouchPosition(nowXY: Vector) {
        actionDown()
        super.updateTouchPosition(nowXY)
    }
    
    override fun getRocketControl(currentRotation: Float): RocketControl { // compare currentRotation with intended direction to determine turning direction
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
            return RocketControl(rotationNeeded)
        } // it works, but why
        else
            return RocketControl(0f)
    }
    
    override fun drawJoystick() {
        if (actionDown) { // only display arrow if the screen is touched
            val centerOfArrow = centerOfRotation + nowXY - centerOfJoystick
            triangularShape.setTriangleCoords(centerOfArrow.offset(0f, 0.5f),
                    centerOfArrow.offset(0.15f, 0.3f),
                    centerOfArrow.offset(-0.15f, 0.3f))
            quadrilateralShape.setQuadrilateralShapeCoords(centerOfArrow.offset(-0.05f, 0.3f),
                    centerOfArrow.offset(0.05f, 0.3f),
                    centerOfArrow.offset(0.05f, 0f),
                    centerOfArrow.offset(-0.05f, 0f)) // update arrow position
            
            // rotate arrow
            triangularShape.rotate(centerOfArrow, intendedDirection)
            quadrilateralShape.rotate(centerOfArrow, intendedDirection)
        } else { // if the screen is not touched
            triangularShape.setTriangleCoords(Vector(0f, 0f), Vector(0f, 0f), Vector(0f, 0f)) // hide arrow
            quadrilateralShape.setQuadrilateralShapeCoords(Vector(0f, 0f), Vector(0f, 0f), Vector(0f, 0f), Vector(0f, 0f))
        }
    }
    
    override fun removeAllShape() {
        triangularShape.remove()
        quadrilateralShape.remove()
    }
}
