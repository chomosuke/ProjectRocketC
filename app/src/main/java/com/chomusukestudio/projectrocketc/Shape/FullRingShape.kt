package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI

class FullRingShape(center: Vector, a: Float, b: Float, innerPercentage: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override var componentShapes: Array<Shape> = arrayOf(TopHalfRingShape(center, a, b, innerPercentage, color, buildShapeAttr),
            TopHalfRingShape(center, a, b, innerPercentage, color, buildShapeAttr))
    
    init {
        componentShapes[1].rotateShape(center, PI.toFloat())
    }
}
