package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.square
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Vector(val x: Float, val y: Float) {
    fun rotateVector(centerOfRotation: Vector, angle: Float): Vector {
        if (angle == 0f)
        // yeah, I do zero angle a lot
            return this
        
        // translate point back to origin:
        var result = this.minus(centerOfRotation)
        
        // rotate point around origin
        result = result.rotateVector(angle)
        
        // translate point back:
        return result + centerOfRotation
    }
    
    fun rotateVector(angle: Float): Vector {
        val sinAngle = sin(angle)
        val cosAngle = cos(angle)
        val xNew = x * cosAngle - y * sinAngle
        val yNew = x * sinAngle + y * cosAngle
        return Vector(xNew, yNew)
    }
    
    val direction get() = atan2(x, y)
    val abs get() = sqrt(square(x) + square(y))
    
    fun scale(pivot: Vector, factor: Float) = Vector(pivot.x + factor * (x - pivot.x), pivot.y + factor * (y - pivot.y))
    fun offset(dx: Float, dy: Float) = Vector(x + dx, y + dy)
    fun mirrorXAxis() = Vector(x, -y)
    
    operator fun plus(anotherVector: Vector) = Vector(x + anotherVector.x, y + anotherVector.y)
    operator fun minus(anotherVector: Vector) = Vector(x - anotherVector.x, y - anotherVector.y)
    operator fun unaryMinus() = Vector(-x, -y)
    operator fun times(factor: Float) = Vector(x * factor, y * factor)
}