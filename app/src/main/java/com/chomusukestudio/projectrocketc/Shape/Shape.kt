package com.chomusukestudio.projectrocketc.Shape


/**
 * Created by Shuang Li on 28/02/2018.
 */

abstract class Shape{
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
    
    open val shapeColor: FloatArray
        get() = componentShapes[0].shapeColor
    
    protected open val size: Int
        get() {
            var numberOfTriangularShape = 0
            for (componentShape in componentShapes)
                numberOfTriangularShape += componentShape.size
            return numberOfTriangularShape
        }
    
    open var visibility: Boolean = true
        set(visibility) {
            if (field != visibility) {
                field = visibility
                for (componentShape in componentShapes)
                    componentShape.visibility = visibility
            }
        }
    
    open fun moveShape(dx: Float, dy: Float) {
        if (dx == 0f && dy == 0f) {
            return
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
    
    open fun resetShapeColor(red: Float, green: Float, blue: Float, alpha: Float) {
        for (componentShape in componentShapes)
            componentShape.resetShapeColor(red, green, blue, alpha)
    }
    
    open fun resetAlpha(alpha: Float) {
        for (componentShape in componentShapes) {
            componentShape.resetAlpha(alpha)
        }
    }
    
    open fun changeShapeColor(red: Float, green: Float, blue: Float, alpha: Float) {
        for (componentShape in componentShapes)
            componentShape.changeShapeColor(red, green, blue, alpha)
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
    
    open fun removeShape() {
        for (componentShape in componentShapes)
            componentShape.removeShape()
    }

    open fun getZs(): ArrayList<Float> {
        val zs = ArrayList<Float>()
        for (componentShape in componentShapes) {
            val componentShapeZs = componentShape.getZs()
            var isDifferentZ = true
            for (componentShapeZ in componentShapeZs) {
                for (z in zs) {
                    if (z == componentShapeZ) {
                        isDifferentZ = false
                        break
                    }
                }
                if (isDifferentZ)
                    zs.add(componentShapeZ)
            }
        }
        return zs
    }
}
