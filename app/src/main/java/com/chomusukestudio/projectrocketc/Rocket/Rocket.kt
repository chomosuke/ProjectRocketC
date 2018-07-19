package com.chomusukestudio.projectrocketc.Rocket

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.support.annotation.CallSuper
import com.chomusukestudio.projectrocketc.Shape.CircularShape

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.TraceShape.TraceShape
import com.chomusukestudio.projectrocketc.state
import com.chomusukestudio.projectrocketc.State
import com.chomusukestudio.projectrocketc.upTimeMillis
import java.lang.Math.*

import java.util.ArrayList

// TODO: both rocket and trace needs clean up, except I have no intention to do so
abstract class Rocket(protected val surrounding: Surrounding) {
    
    protected var traces = ArrayList<TraceShape>()
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
    fun isCrashed(surrounding: Surrounding): Boolean {
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

        fadeMoveAndRemoveTraces(now, previousFrameTime, ds)

        surrounding.moveSurrounding(-ds * sin(currentRotation.toDouble()).toFloat(), -ds * cos(currentRotation.toDouble()).toFloat(), now, previousFrameTime)
//        surrounding.moveSurrounding(0f,0f, now, previousFrameTime)
        //        surrounding.moveSurrounding(0, -ds, now, previousFrameTime);

//        // move rocket components
//        for (component in components)
//            component.moveShape(ds * sin(currentRotation.toDouble()).toFloat(), ds * cos(currentRotation.toDouble()).toFloat())
//
//        offsetCamera(ds * sin(currentRotation.toDouble()).toFloat(), ds * cos(currentRotation.toDouble()).toFloat())
//
//        // refresh ends of surrounding
//        val leftRightBottomTopEnd = generateLeftRightBottomTop(widthInPixel / heightInPixel)
//        surrounding.setLeftRightBottomTopEnd(leftRightBottomTopEnd[0], leftRightBottomTopEnd[1], leftRightBottomTopEnd[2], leftRightBottomTopEnd[3])

        waitForFadeMoveAndRemoveTraces()
        
        generateTraces(previousFrameTime, now, ds)
        
    }
    
    open fun waitForFadeMoveAndRemoveTraces() {
        // do nothing
    }

    
    open fun fadeMoveAndRemoveTraces(now: Long, previousFrameTime: Long, ds: Float) {
        for (trace in traces) {
            // fade traces
            trace.changeShapeColor(0f, 0f, 0f, -(now - previousFrameTime) / 5000f)
        }
        moveTraceWithSurrounding(ds)
        removeTrace()
    }
    
    protected fun moveTraceWithSurrounding(ds: Float) {
        for (trace in traces)
        // move traces
            trace.moveShape(-ds * sin(currentRotation.toDouble()).toFloat(), -ds * cos(currentRotation.toDouble()).toFloat())
    }
    
    protected open fun removeTrace() {
        // remove faded traces
        var i = 0
        while (i < traces.size) {
            if (1.0 / 256 >= traces[i].shapeColor[3]) {
                traces.removeAt(i).removeShape()
                i--
            }
            i++
        }
    }
    
    protected abstract fun generateTraces(previousFrameTime: Long, now: Long, ds: Float)

    open fun removeAllShape() {
        for (component in components)
            component.removeShape()
        for (trace in traces)
            trace.removeShape()
    }
    
    fun isEaten(littleStar: LittleStar): Boolean {
        return littleStar.isEaten(components)
    }
}

