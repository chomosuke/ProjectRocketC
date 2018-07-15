package com.chomusukestudio.projectrocketc.Shape

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin

class TopHalfRingShape// this is really redundant
(centerX: Float, centerY: Float, a: Float, b: Float, factor: Float, red: Float, green: Float, blue: Float, alpha: Float, z: Float, visibility: Boolean) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    private val numberOfEdges = CircularShape.getNumberOfEdges((a + b + 1f) / 2) / 2
    override var componentShapes: Array<Shape> = Array(numberOfEdges) // + 1 is for the rounding.
    { i -> QuadrilateralShape(centerX + a * sin(PI * i / numberOfEdges - PI / 2).toFloat(),
            centerY + b * cos(PI * i / numberOfEdges - PI / 2).toFloat(),
            centerX + a * sin(PI * (i + 1) / numberOfEdges - PI / 2).toFloat(),
            centerY + b * cos(PI * (i + 1) / numberOfEdges - PI / 2).toFloat(),
            centerX + factor * a * sin(PI * (i + 1) / numberOfEdges - PI / 2).toFloat(),
            centerY + factor * b * cos(PI * (i + 1) / numberOfEdges - PI / 2).toFloat(),
            centerX + factor * a * sin(PI * i / numberOfEdges - PI / 2).toFloat(),
            centerY + factor * b * cos(PI * i / numberOfEdges - PI / 2).toFloat(),
            red, green, blue, alpha, z, visibility)}
    //        super(centerX, centerY, b, a, factor, Red, green, blue, alpha, z);
    //        rotateTriangle((float) centerX, (float) centerY, PI / 2);
}
