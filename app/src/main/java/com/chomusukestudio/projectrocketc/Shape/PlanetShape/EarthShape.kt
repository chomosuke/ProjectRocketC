package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.Shape

class EarthShape(centerX: Float, centerY: Float, radius: Float, z: Float)// one level higher than circularShape
    : PlanetShape(centerX, centerY, radius, 2.0) {
    override var componentShapes: Array<Shape>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
}
