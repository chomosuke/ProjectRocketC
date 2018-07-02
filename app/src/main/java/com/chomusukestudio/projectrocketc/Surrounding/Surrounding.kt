package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.Shape

interface Surrounding {
    var isStarted: Boolean
    val centerOfRotationX: Float
    val centerOfRotationY: Float
    val rotation: Float
    var rocket: Rocket
    fun initializeSurrounding(rocket: Rocket)
    fun makeNewTriangleAndRemoveTheOldOne(now: Long, previousFrameTime: Long)
    fun removeAllShape()
    fun moveSurrounding(dx: Float, dy: Float, now: Long, previousFrameTime: Long)
    fun anyLittleStar()
    fun isCrashed(components: Array<Shape>): Boolean
    fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long)
    fun setLeftRightBottomTopEnd(leftEnd: Float, rightEnd: Float, bottomEnd: Float, topEnd: Float)
}
