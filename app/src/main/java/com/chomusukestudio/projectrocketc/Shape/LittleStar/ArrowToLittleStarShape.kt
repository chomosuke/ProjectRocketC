package com.chomusukestudio.projectrocketc.Shape.LittleStar

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.RegularPolygonalShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import kotlin.math.PI

class ArrowToLittleStarShape(radius: Float, redArrow: Float, greenArrow: Float, blueArrow: Float,
                             redCircle: Float, greenCircle: Float, blueCircle: Float, z: Float): Shape()/* can't overlap with the arrow */{
    override val isOverlapMethodLevel: Double = 0.0
    override var componentShapes: Array<Shape> = arrayOf(CircularShape(0f, 0f, radius, redCircle, greenCircle, blueCircle, 1f, z)
            , RegularPolygonalShape(3, 0f, 0f, radius * 0.75f, redArrow, greenArrow, blueArrow, 1f, z - 0.01f))

    enum class Direction(val direction: Int) {
        UP(0), LEFT(1), DOWN(2), RIGHT(3)
    }
    private var direction = Direction.UP // because the three sided RegularPolygonalShape will be facing up when declared.
    fun setDirection(direction : Direction) {
        componentShapes[1].rotateShape((componentShapes[1] as RegularPolygonalShape).centerX,
                (componentShapes[1] as RegularPolygonalShape).centerY, PI.toFloat()/2 * (direction.direction - this.direction.direction))
        this.direction = direction
    }

    fun setPosition(centerX : Float, centerY : Float) {
        val dx = centerX - (componentShapes[0] as CircularShape).centerX
        val dy = centerY - (componentShapes[0] as CircularShape).centerY
        moveShape(dx, dy)
    }
}
