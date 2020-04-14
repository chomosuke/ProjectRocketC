package com.chomusukestudio.projectrocketc.PlanetShape

import com.chomusukestudio.prcandroid2dgameengine.shape.BuildShapeAttr
import com.chomusukestudio.prcandroid2dgameengine.shape.Shape
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector

class EarthShape(center: Vector, radius: Float, buildShapeAttr: BuildShapeAttr)
    : PlanetShape(center, radius) {
    override var componentShapes: Array<Shape>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
}