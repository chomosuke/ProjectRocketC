package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI

class FullRingShape(centerX: Float, centerY: Float, a: Float, b: Float, innerPercentage: Float, red: Float, green: Float, blue: Float, alpha: Float, z: Float, visibility: Boolean) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override var componentShapes: Array<Shape> = arrayOf(HalfRingShape(centerX, centerY, a, b, innerPercentage, red, green, blue, alpha, z, visibility),
            HalfRingShape(centerX, centerY, a, b, innerPercentage, red, green, blue, alpha, z, visibility))
    
    init {
        componentShapes[1].rotateShape(centerX, centerY, PI.toFloat())
    }
}
