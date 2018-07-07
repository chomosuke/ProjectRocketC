package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.Shape

class EarthShape(centerX: Float, centerY: Float, radius: Float, z: Float)
    : PlanetShape(centerX, centerY, radius) {
    override val isOverlapMethodLevel: Double = 2.0// one level higher than circularShape
    override var componentShapes: Array<Shape>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
}
