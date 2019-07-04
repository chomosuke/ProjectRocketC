package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI

class FullRingShape(centerX: Float, centerY: Float, a: Float, b: Float, innerPercentage: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override var componentShapes: Array<Shape> = arrayOf(HalfRingShape(centerX, centerY, a, b, innerPercentage, color, buildShapeAttr),
            HalfRingShape(centerX, centerY, a, b, innerPercentage, color, buildShapeAttr))
    
    init {
        componentShapes[1].rotateShape(centerX, centerY, PI.toFloat())
    }
}
