package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.*

import java.lang.Math.random

class StarShape(center: Vector, brightness: Float, private val SPEED: Float, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(4, // four seems like a sensible number
            center, 16f / 720f, Color(1f, 1f, 1f, brightness), buildShapeAttr))
    var brightness: Float = brightness
        private set
    
    var isSparkling = false
    var isSparkleBrighter = 0.5 < random()
    val center: Vector
        get() = (componentShapes[0] as RegularPolygonalShape).center
    
    fun moveStarShape(displacement: Vector) {
        super.moveShape(displacement * SPEED)
    }
    
    fun resetPosition(center: Vector) {
        val dCenter = center - this.center
        super.moveShape(dCenter)
    }
    
    fun changeBrightness(db: Double) {
        brightness += db.toFloat()
        componentShapes[0].resetAlpha(brightness)
    }
}
