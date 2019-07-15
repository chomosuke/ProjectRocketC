package com.chomusukestudio.projectrocketc.Shape

import kotlin.math.PI

class BarShape(topLeft: Vector, bottomRight: Vector, sideThickness: Float, barColor: Color, sideColor: Color, buildShapeAttr: BuildShapeAttr): Shape() {
	override var componentShapes: Array<Shape> = run {
		val topLeftInner = topLeft.offset(sideThickness, -sideThickness)
		val bottomRightInner = bottomRight.offset(-sideThickness, sideThickness)
		arrayOf(
				QuadrilateralShape(topLeftInner, Vector(bottomRightInner.x, topLeftInner.y),
						bottomRightInner, Vector(topLeftInner.x, bottomRightInner.y),
						barColor, buildShapeAttr), // bar itself
				QuadrilateralShape(topLeft, Vector(topLeftInner.x, topLeft.y),
						Vector(topLeftInner.x, bottomRight.y), Vector(topLeft.x, bottomRight.y),
						sideColor, buildShapeAttr), // left side wide
				QuadrilateralShape(Vector(bottomRightInner.x, topLeft.y), Vector(bottomRight.x, topLeft.y),
						bottomRight, Vector(bottomRightInner.x, bottomRight.y),
						sideColor, buildShapeAttr), // right side wide
				QuadrilateralShape(Vector(topLeftInner.x, topLeft.y), Vector(bottomRightInner.x, topLeft.y),
						Vector(bottomRightInner.x, topLeftInner.y), topLeftInner,
						sideColor, buildShapeAttr), // top side not extend
				QuadrilateralShape(Vector(topLeftInner.x, bottomRightInner.y), bottomRightInner,
						Vector(bottomRightInner.x, bottomRight.y), Vector(topLeftInner.x, bottomRight.y),
						sideColor, buildShapeAttr) // bottom side not extend
		)
	}
	
	private val barLength = bottomRight.x - topLeft.x
	var fullness = 1f
		set(value) {
			field = if (value >= 0f) value else 0f
			
			val bar = componentShapes[0] as QuadrilateralShape
			val orientation = (bar.vertex4 - bar.vertex1).direction + PI.toFloat()/2
			val dVector = Vector(fullness*barLength, 0f).rotateVector(orientation)
			
			bar.setQuadrilateralShapeCoords(bar.vertex1, bar.vertex1 + dVector,
					bar.vertex4 + dVector, bar.vertex4)
		}
}