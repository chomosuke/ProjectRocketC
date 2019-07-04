package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.Layers


/**
 * Created by Shuang Li on 28/02/2018.
 */

abstract class Shape: Cloneable{
    fun cloneShape(): Shape {
        return super.clone() as Shape
    }

    abstract val isOverlapMethodLevel: Double // numerical measurement of how considerate is isOverlapToOverride method.
    /* IMPORTANT
     inheriting note:
     subclass need to defined the constructor and other that might be worth providing
     set isOverlapMethodLevel
      */
    
    // TriangularShape is the end of all recursion.
    // hahahahaha it sounds so cool!
    
    abstract var componentShapes: Array<Shape>
        protected set
    
    open val shapeColor: Color
        get() = componentShapes[0].shapeColor
    
    protected open val size: Int
        get() {
            var numberOfTriangularShape = 0
            for (componentShape in componentShapes)
                numberOfTriangularShape += componentShape.size
            return numberOfTriangularShape
        }

    open var visibility: Boolean
        set(value) {
            if (value != visibility) {
                for (componentShape in componentShapes)
                    componentShape.visibility = value
            }
        }
        get() = componentShapes[0].visibility
    
    open fun moveShape(dx: Float, dy: Float) {
        if (dx == 0f && dy == 0f) {
            return // yeah i do that a lot
        }
        for (componentShape in componentShapes)
            componentShape.moveShape(dx, dy)
    }
    
    open fun rotateShape(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        if (angle == 0f) {
            return  // as mind blowing as it is people like me do zero angle a lot
        }
        // positive is clockwise
        for (componentShape in componentShapes)
            componentShape.rotateShape(centerOfRotationX, centerOfRotationY, angle)
    }
    
    open fun resetShapeColor(color: Color) {
        for (componentShape in componentShapes)
            componentShape.resetShapeColor(color)
    }
    
    open fun resetAlpha(alpha: Float) {
        for (componentShape in componentShapes) {
            componentShape.resetAlpha(alpha)
        }
    }
    
    open fun changeShapeColor(dRed: Float, dGreen: Float, dBlue: Float, dAlpha: Float) {
        for (componentShape in componentShapes)
            componentShape.changeShapeColor(dRed, dGreen, dBlue, dAlpha)
    }
    
    fun isOverlap(anotherShape: Shape): Boolean {
        return if (anotherShape.isOverlapMethodLevel > this.isOverlapMethodLevel) {
            // if anotherShape has a more considerate isOverlapToOverride method then of course use it
            anotherShape.isOverlapToOverride(this)
        } else {
            this.isOverlapToOverride(anotherShape)
        }
    }
    
    protected open fun isOverlapToOverride(anotherShape: Shape): Boolean {
        for (componentShape in componentShapes)
            for (componentShapeOfAnotherShape in anotherShape.componentShapes)
                if (componentShape.isOverlap(componentShapeOfAnotherShape))
                    return true
        return false
    }
    
    open fun isInside(x: Float, y: Float): Boolean {
        for (componentShape in componentShapes)
            if (componentShape.isInside(x, y))
                return true
        return false
    }

    open var removed = false
        protected set
    open fun removeShape() {
        for (componentShape in componentShapes)
            componentShape.removeShape()
        removed = true
    }
}

data class Color (val red: Float, val green: Float, val blue: Float, val alpha: Float)

data class BuildShapeAttr(val z: Float, val visibility: Boolean, val layers: Layers) { // never set this as a property
    fun newAttrWithChangedZ(dz: Float) = BuildShapeAttr(z + dz, visibility, layers)
    fun newAttrWithNewVisibility(visibility: Boolean) = BuildShapeAttr(z, visibility, layers)
}