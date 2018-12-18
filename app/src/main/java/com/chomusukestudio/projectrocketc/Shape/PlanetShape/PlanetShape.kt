package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import android.util.Log
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.TriangularShape
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.Shape.coordinate.square

abstract class PlanetShape internal constructor(centerX: Float, centerY: Float, val radius: Float) : Shape() {
    var centerX: Float = 0f
        private set
    var centerY: Float = 0f
        private set
    
    var flybyed = false
    
    var pointsOutsideX: FloatArray? = null
        protected set
    var pointsOutsideY: FloatArray? = null
        protected set
    
    var isInUse: Boolean = false
        private set
    
    private var angleRotated: Float = 0f
    
    private var actualCenterX: Float = 0f
    private var actualCenterY: Float = 0f

    open val maxWidth: Float = radius

    // for debugging
    override var visibility: Boolean
        get() = super.visibility
        set(value) {
            if (visibility != value)
                Log.d("PlanetShape", "visibility changed to $value")
            super.visibility = value
        }

    init {
        this.centerX = centerX
        actualCenterX = this.centerX
        this.centerY = centerY
        actualCenterY = this.centerY
    }
    
    override fun moveShape(dx: Float, dy: Float) {
        if (pointsOutsideX != null) { // update special score as well
            for (i in pointsOutsideX!!.indices) {
                pointsOutsideX!![i] += dx
                pointsOutsideY!![i] += dy
            }
        }
        centerX += dx
        centerY += dy
        if (canBeSeen()) {
            setActual(centerX, centerY)
            visibility = true
        }
        else {
            // if can't be seen
            visibility = false
        }
    }

    private fun canBeSeen(): Boolean {
        return canBeSeenIf(centerX, centerY) || canBeSeenIf(actualCenterX, actualCenterY)
    }

    fun canBeSeenIf(centerX: Float, centerY: Float): Boolean {
        return centerX < LEFT_END + maxWidth &&
                centerX > RIGHT_END - maxWidth &&
                centerY < TOP_END + maxWidth &&
                centerY > BOTTOM_END - maxWidth
    }
    
    fun removePlanet() {
        visibility = false
        isInUse = false
        flybyed = false
    }
    
    fun usePlanet() {
        isInUse = true
    }
    
    private fun setActual(actualCenterX: Float, actualCenterY: Float) {
        val dx = actualCenterX - this.actualCenterX
        val dy = actualCenterY - this.actualCenterY
//        moveShapeMultiThreaded(dx, dy)
        super.moveShape(dx, dy)
        this.actualCenterX = actualCenterX
        this.actualCenterY = actualCenterY
        if (angleRotated != 0f) {
            super.rotateShape(centerX, centerY, angleRotated)
            angleRotated = 0f // reset angleRotated
        }
    }

//    private fun moveShapeMultiThreaded(dx: Float, dy: Float) {
//        val componentTriangularShapes = getComponentTriangularShapes(this)
//        // turns out i don't really need this
//    }

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
        if (angle == 0f) {
            return
        }
        if (pointsOutsideX != null) { // update special score as well
            for (i in pointsOutsideX!!.indices) {
                val result = rotatePoint(pointsOutsideX!![i], pointsOutsideY!![i], centerOfRotationX, centerOfRotationY, angle)
                pointsOutsideX!![i] = result[0]
                pointsOutsideY!![i] = result[1]
            }
        }
        angleRotated += angle
        val result = rotatePoint(centerX, centerY, centerOfRotationX, centerOfRotationY, angle)
        centerX = result[0]
        centerY = result[1]
        if (canBeSeen())
            setActual(centerX, centerY)
        else {
            // if can't be seen
            visibility = false
        }
    }
    
    public override fun isOverlapToOverride(anotherShape: Shape): Boolean {
        return CircularShape.isOverlap(anotherShape, centerX, centerY, radius)
    }
    
    fun isTooClose(anotherPlanetShape: PlanetShape, distance: Float): Boolean {
        // if circle and circle are too close
        return square((anotherPlanetShape.centerX - this.centerX).toDouble()) + square((anotherPlanetShape.centerY - this.centerY).toDouble()) <= square((anotherPlanetShape.radius + this.radius + distance).toDouble())
        // testing all pointsOutside is impractical because performance, subclass may override this method.
    }
    
    override fun isInside(x: Float, y: Float): Boolean {
        return square((x - centerX).toDouble()) + square((y - centerY).toDouble()) <= square(radius.toDouble())
    }
    
    companion object {
        private var LEFT_END: Float = 0.toFloat()
        private var RIGHT_END: Float = 0.toFloat()
        private var BOTTOM_END: Float = 0.toFloat()
        private var TOP_END: Float = 0.toFloat()
        fun setENDs(leftEnd: Float, rightEnd: Float, bottomEnd: Float, topEnd: Float) {
            LEFT_END = leftEnd
            RIGHT_END = rightEnd
            BOTTOM_END = bottomEnd
            TOP_END = topEnd
        }
    }
}
