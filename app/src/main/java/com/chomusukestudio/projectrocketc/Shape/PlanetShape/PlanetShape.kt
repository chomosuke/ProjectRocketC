package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.*

abstract class PlanetShape internal constructor(center: Vector, val radius: Float) : Shape() {
    var center = center
        private set

    open val maxWidth: Float = radius

//    // for debugging
//    override var visibility: Boolean
//        get() = super.visibility
//        set(value) {
//            if (visibility != value)
//                Log.d("PlanetShape", "visibility changed to $value")
//            super.visibility = value
//        }
    
    override fun move(displacement: Vector) {
        super.move(displacement)
        center += (displacement)
    }

    private fun getComponentTriangularShapes(shape: Shape): Array<TriangularShape> {
        val componentTriangularShapes = arrayOfNulls<TriangularShape>(size)
        if (shape is TriangularShape) {
            componentTriangularShapes[0] = shape
            return componentTriangularShapes as Array<TriangularShape>
        } else {
            var i = 0
            for (componentShape in shape.componentShapes) {
                val componentShapeComponentTriangularShapes = getComponentTriangularShapes(componentShape)
                System.arraycopy(componentShapeComponentTriangularShapes, 0, componentTriangularShapes, i, componentShapeComponentTriangularShapes.size)
                i += componentShapeComponentTriangularShapes.size
            }
            return componentTriangularShapes as Array<TriangularShape>
        }
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
