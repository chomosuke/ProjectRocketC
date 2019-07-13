package com.chomusukestudio.projectrocketc.Shape

class PolygonalShape(vertexes: Array<Vector>, color: Color, buildShapeAttr: BuildShapeAttr): Shape() {
	override var componentShapes = Array<Shape>(vertexes.size - 2) {
		TriangularShape(vertexes[0], vertexes[it + 1], vertexes[it + 2], color, buildShapeAttr)
	}
	
	fun getVertex(index: Int): Vector {
		return when (index) {
			0 -> (componentShapes[0] as TriangularShape).vertex1
			1 -> (componentShapes[0] as TriangularShape).vertex2
			else -> (componentShapes[index - 2] as TriangularShape).vertex3
		}
	}
}