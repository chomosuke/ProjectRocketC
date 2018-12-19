package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.RegularPolygonalShape
import com.chomusukestudio.projectrocketc.Shape.Shape

import java.lang.Math.random

class StarShape(centerX: Float, centerY: Float, brightness: Float, private val SPEED: Float, z: Float, visibility: Boolean) : Shape() {
    override val isOverlapMethodLevel: Double = 0.0
    override var componentShapes: Array<Shape> = arrayOf(RegularPolygonalShape(4, // four seems like a sensible number
            centerX, centerY, 16f / 720f, 1f, 1f, 1f, brightness, z, visibility))
    var brightness: Float = 0.toFloat()
        private set
    
    var isSparkling = false
    var isSparkleBrighter = 0.5 < random()
    val centerX: Float
        get() = (componentShapes[0] as RegularPolygonalShape).centerX
    val centerY: Float
        get() = (componentShapes[0] as RegularPolygonalShape).centerY
    
    init { this.brightness = brightness }
    
    override fun moveShape(dx: Float, dy: Float) {
        super.moveShape(SPEED * dx, SPEED * dy)
    }
    
    fun resetPosition(centerX: Float, centerY: Float) {
        val dx = centerX - this.centerX
        val dy = centerY - this.centerY
        super.moveShape(dx, dy)
    }
    
    fun changeBrightness(db: Double) {
        brightness += db.toFloat()
        componentShapes[0].resetShapeColor(brightness, brightness, brightness, 1f)
    }
}
