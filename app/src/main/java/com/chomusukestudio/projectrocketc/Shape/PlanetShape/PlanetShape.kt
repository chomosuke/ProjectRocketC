package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import android.util.Log
import com.chomusukestudio.projectrocketc.GLRenderer.bottomEnd
import com.chomusukestudio.projectrocketc.GLRenderer.leftEnd
import com.chomusukestudio.projectrocketc.GLRenderer.rightEnd
import com.chomusukestudio.projectrocketc.GLRenderer.topEnd
import com.chomusukestudio.projectrocketc.IReusable
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.TriangularShape
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.Shape.coordinate.square
import com.chomusukestudio.projectrocketc.Surrounding.IFlybyable

abstract class PlanetShape internal constructor(centerX: Float, centerY: Float, val radius: Float) : Shape() {
    var centerX: Float = centerX
        private set
    var centerY: Float = centerY
        private set

    // part of isOverlap
    var pointsOutsideX: FloatArray? = null
        protected set
    var pointsOutsideY: FloatArray? = null
        protected set

    open val maxWidth: Float = radius

    // for debugging
    override var visibility: Boolean
        get() = super.visibility
        set(value) {
            if (visibility != value)
                Log.d("PlanetShape", "visibility changed to $value")
            super.visibility = value
        }
    
    override fun moveShape(dx: Float, dy: Float) {
        super.moveShape(dx, dy)
        if (pointsOutsideX != null) { // update special score as well
            for (i in pointsOutsideX!!.indices) {
                pointsOutsideX!![i] += dx
                pointsOutsideY!![i] += dy
            }
        }

        centerX += dx
        centerY += dy
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

    fun resetPosition(centerX: Float, centerY: Float) {
        val dx = centerX - this.centerX
        val dy = centerY - this.centerY
        moveShape(dx, dy)
    }
    
    override fun rotateShape(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        super.rotateShape(centerOfRotationX, centerOfRotationY, angle)
        if (pointsOutsideX != null) { // update special score as well
            for (i in pointsOutsideX!!.indices) {
                val result = rotatePoint(pointsOutsideX!![i], pointsOutsideY!![i], centerOfRotationX, centerOfRotationY, angle)
                pointsOutsideX!![i] = result[0]
                pointsOutsideY!![i] = result[1]
            }
        }
        val result = rotatePoint(centerX, centerY, centerOfRotationX, centerOfRotationY, angle)
        centerX = result[0]
        centerY = result[1]
    }
    
    public override fun isOverlapToOverride(anotherShape: Shape): Boolean {
        return CircularShape.isOverlap(anotherShape, centerX, centerY, radius)
    }
    
    override fun isInside(x: Float, y: Float): Boolean {
        return square(x - centerX) + square(y - centerY) <= square(radius)
    }
}
