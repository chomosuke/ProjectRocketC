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
import com.chomusukestudio.projectrocketc.Shape.Overlapper

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.Vector
import kotlin.math.*

abstract class Rocket(protected val surrounding: Surrounding, var rocketPhysics: RocketPhysics, private val layers: Layers) {
    
    protected fun setRotation(centerOfRotation: Vector, rotation: Float) {
        // called before initialize trace
        // set rotation
        this.centerOfRotation = centerOfRotation
        for (component in components)
            component.rotateShape(centerOfRotation, rotation)
        currentRotation = rotation
    }
    
    protected open var explosionShape: ExplosionShape? = null
    
    protected open val crashOverlappers get() = Array(components.size) { components[it].overlapper }
    
    protected abstract val trace: Trace
    var currentRotation = surrounding.rotation
        /* angle of rocket's current heading in radians
    angle between up and rocket current heading, positive is clockwise. */
        protected set(value) {
            val angle = value - field
            field = value
            for (component in components)
                component.rotateShape(centerOfRotation, angle)
            //            surrounding.rotateSurrounding(dr, nowXY, previousFrameTime);
        }
    protected var velocity = Vector(0f, 0f)
        private set
    
    abstract val rocketQuirks: RocketQuirks
    protected abstract val components: Array<Shape>
    
    var centerOfRotation = surrounding.centerOfRotation
        protected set
    // surrounding have to define center of rotation
    // constructor of subclasses need to reset components with its center of rotation at centerOfRotationY and centerOfRotationX and defined it's velocity
    
    protected var crashedOverlapper: Overlapper? = null
    open fun isCrashed(surrounding: Surrounding, timePassed: Long): Boolean {
        // surrounding will handle this
        crashedOverlapper = surrounding.isCrashed(crashOverlappers)
        if (crashedOverlapper != null) {
            return true
        }
        return false
    }
    
    open val explosionCoordinate = centerOfRotation
    fun drawExplosion(now: Long, previousFrameTime: Long) {
        if (explosionShape == null) {
            explosionShape = RedExplosionShape(explosionCoordinate, 0.75f, 1000, BuildShapeAttr(-11f, true, layers))
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
        val rocketState = rocketPhysics.getRocketState(rocketQuirks, RocketState(currentRotation, velocity), rocketControl, now, previousFrameTime)
        
        this.velocity = rocketState.velocity
        this.currentRotation = rocketState.currentRotation
        
        val displacement = -velocity * (now - previousFrameTime).toFloat()
        
        if (rocketControl.throttleOn) {
            generateTrace(now, previousFrameTime)
        }
        trace.moveTrace(displacement)
        trace.fadeTrace(now, previousFrameTime)
        
        surrounding.moveSurrounding(displacement, now, previousFrameTime)
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
//                        velocity = initialSpeed * (LittleStar.score / 64f + 1);
}

data class RocketState(val currentRotation: Float, val velocity: Vector)

data class RocketQuirks(val turningRadius: Float, val initialSpeed: Float, val rotationSpeed: Float, val acceleration: Float, val deceleration: Float)