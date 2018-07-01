package com.chomusukestudio.projectrocketc.Shape.point

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

fun distance(x1: Float, y1: Float, x2: Float, y2: Float) = Math.sqrt(square((x1 - x2).toDouble()) + square((y1 - y2).toDouble())).toFloat()

fun square(input: Double) = input * input