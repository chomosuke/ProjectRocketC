package com.chomusukestudio.projectrocketc.Rocket

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.support.annotation.CallSuper
import com.chomusukestudio.projectrocketc.GLRenderer.Layer
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.ExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.RedExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.coordinate.Coordinate
import com.chomusukestudio.projectrocketc.State
import java.lang.Math.*

abstract class Rocket(protected val surrounding: Surrounding, private val layers: Layers) {

    protected open var explosionShape: ExplosionShape? = null

    protected abstract val trace: Trace
    var currentRotation = surrounding.rotation
        protected set/* angle of rocket's current heading in radians
    angle between up and rocket current heading, positive is clockwise. */
    abstract var speed: Float
        protected set // ds/dt
    abstract val radiusOfRotation: Float
    
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
    open fun moveRocket(rotationNeeded: Float, now: Long, previousFrameTime: Long, state: State) {
        
        if (state == State.InGame) { // only make it faster if it's already started
            // when 0 score, 1 times as fast, when 1024 score, 2 times as fast
            speed = initialSpeed * (log((LittleStar.score + 1).toDouble()) / log(32.0) + 1).toFloat()
//                        speed = initialSpeed * (LittleStar.score / 64f + 1);
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

        val dx = -ds * sin(currentRotation.toDouble()).toFloat()
        val dy = -ds * cos(currentRotation.toDouble()).toFloat()

        if (state == State.InGame) { // only generate trace when in game
            trace.moveTrace(dx, dy)
            generateTrace(now, previousFrameTime)
            fadeTrace(now, previousFrameTime)
        }

        surrounding.moveSurrounding(dx, dy , now, previousFrameTime)
    }

    protected abstract fun generateTrace(now: Long, previousFrameTime: Long)
    open fun fadeTrace(now: Long, previousFrameTime: Long) {
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

