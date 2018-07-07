package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.point.rotatePoint
import com.chomusukestudio.projectrocketc.Shape.point.square

abstract class PlanetShape internal constructor(centerX: Float, centerY: Float, val radius: Float) : Shape() {
    var centerX: Float = 0.toFloat()
        private set
    var centerY: Float = 0.toFloat()
        private set
    
    var flybyed = false
    
    var pointsOutsideX: FloatArray? = null
        protected set
    var pointsOutsideY: FloatArray? = null
        protected set
    
    var isInUse: Boolean = false
        private set
    
    private var angleRotated: Float = 0.toFloat()
    
    private var actualCenterX: Float = 0.toFloat()
    private var actualCenterY: Float = 0.toFloat()
    
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
        if (centerX < LEFT_END &&
                centerX > RIGHT_END &&
                centerY < TOP_END &&
                centerY > BOTTOM_END ||  // if can be seen
                actualCenterX < LEFT_END &&
                actualCenterX > RIGHT_END &&
                actualCenterY < TOP_END &&
                actualCenterY > BOTTOM_END)
            setActual(centerX, centerY)
        else {
            // if can't be seen
            visibility = false
        }
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
        super.moveShape(dx, dy)
        this.actualCenterX = actualCenterX
        this.actualCenterY = actualCenterY
        if (angleRotated != 0f) {
            super.rotateShape(centerX, centerY, angleRotated)
            angleRotated = 0f // reset angleRotated
        }
        visibility = true
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
        if (centerX < LEFT_END &&
                centerX > RIGHT_END &&
                centerY < TOP_END &&
                centerY > BOTTOM_END ||  // if can be seen
                actualCenterX < LEFT_END &&
                actualCenterX > RIGHT_END &&
                actualCenterY < TOP_END &&
                actualCenterY > BOTTOM_END)
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
