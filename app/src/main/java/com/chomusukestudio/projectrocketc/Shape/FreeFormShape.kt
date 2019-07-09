package com.chomusukestudio.projectrocketc.Shape

class FreeFormShape(points: Array<Vector>, color: Color, buildShapeAttr: BuildShapeAttr): Shape() {
	override val isOverlapMethodLevel = 0.0
	override var componentShapes = generateComponents(points, color, buildShapeAttr)
	private fun generateComponents(points: Array<Vector>, color: Color, buildShapeAttr: BuildShapeAttr): Array<Shape> {
		val componentShapes = arrayOfNulls<Shape>(points.size - 2)
		for (i in componentShapes.indices)
			componentShapes[i] = TriangularShape(points[0], points[i + 1], points[i + 2], color, buildShapeAttr)
		return componentShapes as Array<Shape>
	}
}