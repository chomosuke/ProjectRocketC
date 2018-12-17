package com.chomusukestudio.projectrocketc.Rocket

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.support.annotation.CallSuper
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.ExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.state
import com.chomusukestudio.projectrocketc.State
import java.lang.Math.*

abstract class Rocket(protected val surrounding: Surrounding) {

    protected open var explosionShape: ExplosionShape? = null

    protected abstract val trace: Trace
    var currentRotation = surrounding.rotation
        protected set/* angle of rocket's current heading in radians
    angle between up and rocket current heading, positive is clockwise. */
    abstract var speed: Float
        protected set // ds/dt
    abstract var radiusOfRotation: Float
        protected set
    
    protected abstract val initialSpeed: Float
    protected abstract val components: Array<Shape>
    
    protected var centerOfRotationX: Float = surrounding.centerOfRotationX
    protected var centerOfRotationY: Float = surrounding.centerOfRotationY
    // surrounding have to define center of rotation
    // constructor of subclasses need to reset components with its center of rotation at centerOfRotationY and centerOfRotationX and defined it's speed

    protected var crashedComponent: Shape? = null
    open fun isCrashed(surrounding: Surrounding): Boolean {
        // surrounding will handle this
        crashedComponent = surrounding.isCrashed(components)
        if (crashedComponent != null) {
            return true
        }
            return false
    }

    abstract fun drawExplosion(now: Long, previousFrameTime: Long)

    abstract val width: Float
    
    protected fun setRotation(centerOfRotationX: Float, centerOfRotationY: Float, rotation: Float) {
        // called before initialize trace
        // set rotation
        this.centerOfRotationX = centerOfRotationX
        this.centerOfRotationY = centerOfRotationY
        for (component in components)
            component.rotateShape(centerOfRotationX, centerOfRotationY, rotation)
        currentRotation = rotation
    }
    
    @CallSuper // allow rocket to have moving component
    open fun moveRocket(rotationNeeded: Float, now: Long, previousFrameTime: Long) {
        
        if (state == State.InGame) { // only make it faster if it's already started
            // when 0 score, 1 times as fast, when 1024 score, 2 times as fast
            speed = initialSpeed * (log((LittleStar.score + 1).toDouble()) / log(64.0) + 1).toFloat()
            //            speed = initialSpeed * (LittleStar.Companion.getScore() / 64f + 1);
        }
        val speedOfRotation = speed / radiusOfRotation // dr/dt = ds/dt / radiusOfRotation
        val ds = speed * (now - previousFrameTime) // ds/dt * dt
        val dr = speedOfRotation * (now - previousFrameTime) // dr/dt * dt
    
        when {
            rotationNeeded < -dr -> {
                currentRotation -= dr
                for (component in components)
                    component.rotateShape(centerOfRotationX, centerOfRotationY, -dr)
                //            surrounding.rotateSurrounding(-dr, now, previousFrameTime);
            }
            rotationNeeded > dr -> {
                currentRotation += dr
                for (component in components)
                    component.rotateShape(centerOfRotationX, centerOfRotationY, dr)
                //            surrounding.rotateSurrounding(dr, now, previousFrameTime);
            }
            else -> {
                currentRotation += rotationNeeded
                for (component in components)
                    component.rotateShape(centerOfRotationX, centerOfRotationY, rotationNeeded)
                //            surrounding.rotateSurrounding(rotationNeeded, now, previousFrameTime);
            }
        }

        generateTrace(now, previousFrameTime)
        fadeTrace(now, previousFrameTime)

        // move trace with surrounding
        val dx = -ds * sin(currentRotation.toDouble()).toFloat()
        val dy = -ds * cos(currentRotation.toDouble()).toFloat()
        surrounding.moveSurrounding(dx, dy , now, previousFrameTime)
        trace.moveTrace(dx, dy)
    }

    protected abstract fun generateTrace(now: Long, previousFrameTime: Long)
    abstract fun fadeTrace(now: Long, previousFrameTime: Long)

    open fun removeAllShape() {
        for (component in components)
            component.removeShape()
        trace.removeTrace()
    }
    
    fun isEaten(littleStar: LittleStar): Boolean {
        return littleStar.isEaten(components)
    }
}

