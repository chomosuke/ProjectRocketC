package com.chomusukestudio.projectrocketc.planetShape

import com.chomusukestudio.prcandroid2dgameengine.shape.CircularOverlapper
import com.chomusukestudio.prcandroid2dgameengine.shape.Overlapper
import com.chomusukestudio.prcandroid2dgameengine.shape.Shape
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector

abstract class PlanetShape internal constructor(center: Vector, val radius: Float) : Shape() {
    var center = center
        private set

    open val maxWidth: Float = radius
    
    override fun move(displacement: Vector) {
        super.move(displacement)
        center += (displacement)
    }

    fun resetPosition(center: Vector) {
        val dCenter = center - this.center
        move(dCenter)
    }
    
    override fun rotate(centerOfRotation: Vector, angle: Float) {
        super.rotate(centerOfRotation, angle)
        center = center.rotateVector(centerOfRotation, angle)
    }
    
    override val overlapper: Overlapper
        get() = CircularOverlapper(center, radius)
}
