package com.chomusukestudio.projectrocketc.littleStar

import com.chomusukestudio.prcandroid2dgameengine.shape.*
import kotlin.math.PI

class ArrowToLittleStarShape(radius: Float, arrowColor: Color, circleColor: Color, buildShapeAttr: BuildShapeAttr): Shape()/* can't overlap with the arrow */{
    override var componentShapes: Array<Shape> = arrayOf(CircularShape(Vector(0f, 0f), radius, circleColor, buildShapeAttr),
            RegularPolygonalShape(3, Vector(0f, 0f), radius * 0.75f, arrowColor, buildShapeAttr.newAttrWithChangedZ(-0.01f)))

    enum class Direction(val direction: Int) {
        UP(0), LEFT(1), DOWN(2), RIGHT(3)
    }
    private var direction = Direction.UP // because the three sided RegularPolygonalShape will be facing up when declared.
    fun setDirection(direction : Direction) {
        componentShapes[1].rotate((componentShapes[1] as RegularPolygonalShape).center,
				PI.toFloat()/2 * (direction.direction - this.direction.direction))
        this.direction = direction
    }
    
    fun setPosition(center: Vector) {
        val dCenter = center - (componentShapes[0] as CircularShape).center
        move(dCenter)
    }
}
