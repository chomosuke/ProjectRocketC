package com.chomusukestudio.projectrocketc.Shape.LittleStar

import com.chomusukestudio.projectrocketc.Shape.*

class LittleStarShape(center: Vector, radius: Float, starColor: Color,
                      circleColor: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
	override var componentShapes: Array<Shape> = arrayOf(CircularShape(center, radius, circleColor, buildShapeAttr)
            , NPointsStarShape(5, center, radius * 0.75f, starColor, buildShapeAttr.newAttrWithChangedZ(-0.01f)))
	override val overlapper get() = componentShapes[0].overlapper
	// NPointsStarShape is irrelevant
}

const val RADIUS_OF_LITTLE_STAR = 0.16f
