package com.chomusukestudio.projectrocketc.Shape.coordinate

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun rotatePoint(pointX: Float, pointY: Float, centerOfRotationX: Float, centerOfRotationY: Float, angle: Float): FloatArray {
    if (angle == 0f) {
        return floatArrayOf(pointX, pointY)
        // yeah, I do zero angle a lot
    }
    var resultX = pointX
    var resultY = pointY
    
    val sinAngle = Math.sin(-angle.toDouble()).toFloat()
    val cosAngle = Math.cos(-angle.toDouble()).toFloat()
    
    // translate score back to origin:
    resultX -= centerOfRotationX
    resultY -= centerOfRotationY
    
    // rotate score
    val xNew = resultX * cosAngle - resultY * sinAngle
    val yNew = resultX * sinAngle + resultY * cosAngle
    
    // translate score back:
    resultX = xNew + centerOfRotationX
    resultY = yNew + centerOfRotationY
    
    return floatArrayOf(resultX, resultY)
}

fun distance(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt(square(x1 - x2) + square(y1 - y2))

fun square(input: Float) = input * input

class Coordinate(var x: Float, var y: Float) {
    fun rotateCoordinate(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        if (angle == 0f)
            // yeah, I do zero angle a lot
            return

        val sinAngle = sin(-angle)
        val cosAngle = cos(-angle)

        // translate score back to origin:
        x -= centerOfRotationX
        y -= centerOfRotationY

        // rotate score
        val xNew = x * cosAngle - y * sinAngle
        val yNew = x * sinAngle + y * cosAngle

        // translate score back:
        x = xNew + centerOfRotationX
        y = yNew + centerOfRotationY
    }
}