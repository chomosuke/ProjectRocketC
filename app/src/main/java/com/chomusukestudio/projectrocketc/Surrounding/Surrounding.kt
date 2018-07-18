package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.Shape

abstract class Surrounding {
    abstract val centerOfRotationX: Float
    abstract val centerOfRotationY: Float
    abstract val rotation: Float
    abstract var rocket: Rocket
    abstract fun initializeSurrounding(rocket: Rocket)
    abstract fun makeNewTriangleAndRemoveTheOldOne(now: Long, previousFrameTime: Long)
    abstract fun removeAllShape()
    abstract fun moveSurrounding(dx: Float, dy: Float, now: Long, previousFrameTime: Long)
    abstract fun anyLittleStar()
    abstract fun isCrashed(components: Array<Shape>): Shape? // null if no crash
    abstract fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long)
    abstract fun setLeftRightBottomTopEnd(leftEnd: Float, rightEnd: Float, bottomEnd: Float, topEnd: Float)
}
