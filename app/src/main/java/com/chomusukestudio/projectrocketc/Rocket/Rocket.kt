package com.chomusukestudio.projectrocketc.Rocket

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.support.annotation.CallSuper
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Joystick.RocketControl
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.ExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.RedExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.coordinate.Coordinate
import kotlin.math.*

abstract class Rocket(protected val surrounding: Surrounding, var rocketPhysics: RocketPhysics, private val layers: Layers) {
    
    protected fun setRotation(centerOfRotationX: Float, centerOfRotationY: Float, rotation: Float) {
        // called before initialize trace
        // set rotation
        this.centerOfRotationX = centerOfRotationX
        this.centerOfRotationY = centerOfRotationY
        for (component in components)
            component.rotateShape(centerOfRotationX, centerOfRotationY, rotation)
        currentRotation = rotation
    }
    
    protected open var explosionShape: ExplosionShape? = null
    
    protected abstract val shapeForCrashAppro: Shape
    fun cloneShapeForCrashAppro(): Shape {
        return shapeForCrashAppro.cloneShape()
    }
    
    protected abstract val trace: Trace
    var currentRotation = surrounding.rotation
        /* angle of rocket's current heading in radians
    angle between up and rocket current heading, positive is clockwise. */
        protected set(value) {
            val angle = value - field
            field = value
            for (component in components)
                component.rotateShape(centerOfRotationX, centerOfRotationY, angle)
            //            surrounding.rotateSurrounding(dr, now, previousFrameTime);
            shapeForCrashAppro.rotateShape(centerOfRotationX, centerOfRotationY, angle)
        }
    protected var speedX = 0f
        private set
    protected var speedY = 0f
        private set
    
    abstract val rocketQuirks: RocketQuirks
    protected abstract val components: Array<Shape>
    
    var centerOfRotationX: Float = surrounding.centerOfRotationX
        protected set
    var centerOfRotationY: Float = surrounding.centerOfRotationY
        protected set
    // surrounding have to define center of rotation
    // constructor of subclasses need to reset components with its center of rotation at centerOfRotationY and centerOfRotationX and defined it's speed
    
    protected var crashedComponent: Shape? = null
    open fun isCrashed(surrounding: Surrounding): Boolean {
        // surrounding will handle this
        crashedComponent = surrounding.isCrashed(shapeForCrashAppro, components)
        if (crashedComponent != null) {
            return true
        }
        return false
    }
    
    open val explosionCoordinate = Coordinate(centerOfRotationX, centerOfRotationY)
    fun drawExplosion(now: Long, previousFrameTime: Long) {
        if (explosionShape == null) {
            explosionShape = RedExplosionShape(explosionCoordinate.x, explosionCoordinate.y, 0.75f, 1000, BuildShapeAttr(-11f, true, layers))
        } else {
            // rocket already blown up
            for (component in components)
                if (!component.removed)
                    component.removeShape()
            
            explosionShape!!.drawExplosion(now - previousFrameTime)
        }
    }
    
    abstract val width: Float
    
    @CallSuper // allow rocket to have moving component
    open fun moveRocket(rocketControl: RocketControl, now: Long, previousFrameTime: Long) {
        val rocketState = rocketPhysics.getRocketState(rocketQuirks, RocketState(currentRotation, speedX, speedY), rocketControl, now, previousFrameTime)
        
        this.speedX = rocketState.speedX
        this.speedY = rocketState.speedY
        this.currentRotation = rocketState.currentRotation
        
        val dx = -speedX * (now - previousFrameTime)
        val dy = -speedY * (now - previousFrameTime)
        
        if (rocketControl.throttleOn) {
            generateTrace(now, previousFrameTime)
        }
        trace.moveTrace(dx, dy)
        trace.fadeTrace(now, previousFrameTime)
        
        surrounding.moveSurrounding(dx, dy, now, previousFrameTime)
    }
    
    protected abstract fun generateTrace(now: Long, previousFrameTime: Long)

    fun fadeTrace(now: Long, previousFrameTime: Long) {
        trace.fadeTrace(now, previousFrameTime)
    }
    
    open fun removeAllShape() {
        for (component in components)
            if (!component.removed)
                component.removeShape()
        trace.removeTrace()
        explosionShape?.removeShape()
    }
    
    fun isEaten(littleStar: LittleStar): Boolean {
        return littleStar.isEaten(components)
    }
}

fun speedFormula(initialSpeed: Float, score: Int): Float {
    // when 0 score, 1 times as fast, when speedLogBase^n score, (n+1) times as fast
    return initialSpeed * (log(score + 1f, 32f) + 1)
//                        speed = initialSpeed * (LittleStar.score / 64f + 1);
}

data class RocketState(val currentRotation: Float, val speedX: Float, val speedY: Float)

data class RocketQuirks(val turningRadius: Float, val initialSpeed: Float, val rotationSpeed: Float, val acceleration: Float, val deceleration: Float)