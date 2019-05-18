package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.State

abstract class Surrounding {
    abstract val centerOfRotationX: Float
    abstract val centerOfRotationY: Float
    abstract val rotation: Float
    abstract var rocket: Rocket
    abstract fun initializeSurrounding(rocket: Rocket, state: State)
    abstract fun makeNewTriangleAndRemoveTheOldOne(now: Long, previousFrameTime: Long, state: State)
    abstract fun removeAllShape()
    abstract fun moveSurrounding(dx: Float, dy: Float, now: Long, previousFrameTime: Long)
    abstract fun checkAndAddLittleStar(now: Long)
    abstract fun isCrashed(shapeForCrashAppro: Shape, components: Array<Shape>): Shape? // null if no crash
    abstract fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long)
    open fun trashAndGetResources(): SurroundingResources? = null
}

abstract class SurroundingResources
