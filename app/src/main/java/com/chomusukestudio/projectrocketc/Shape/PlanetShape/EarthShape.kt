package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.Vector

class EarthShape(center: Vector, radius: Float, buildShapeAttr: BuildShapeAttr)
    : PlanetShape(center, radius) {
	override var componentShapes: Array<Shape>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
}