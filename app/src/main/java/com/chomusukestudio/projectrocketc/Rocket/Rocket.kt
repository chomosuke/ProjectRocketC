package com.chomusukestudio.projectrocketc.Rocket

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.media.MediaPlayer
import android.support.annotation.CallSuper
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Joystick.RocketControl
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.ExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.RocketRelated.RedExplosionShape
import com.chomusukestudio.projectrocketc.Rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.Rocket.trace.Trace
import com.chomusukestudio.projectrocketc.Shape.*

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import com.chomusukestudio.projectrocketc.UI.MainActivity
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import kotlin.math.*

abstract class Rocket(protected val surrounding: Surrounding, private val mainActivity: MainActivity, var rocketPhysics: RocketPhysics, private val layers: Layers) {
    protected open val crashSound: MediaPlayer = MediaPlayer.create(mainActivity, R.raw.fx22)
    
    protected fun setRotation(centerOfRotation: Vector, rotation: Float) {
        // called before initialize trace
        // set rotation
        this.centerOfRotation = centerOfRotation
        for (component in components) {
            component.move(centerOfRotation)
            component.rotate(centerOfRotation, rotation)
        }
        currentRotation = rotation
    }
    
    protected open var explosionShape: ExplosionShape? = null
    
    protected open val crashOverlappers get() = Array(components.size) { components[it].overlapper }
    
    protected abstract val traces: Array<Trace>
    var currentRotation = surrounding.rotation
        /* angle of rocket's current heading in radians
    angle between up and rocket current heading, positive is clockwise. */
        protected set(value) {
            val angle = value - field
            field = value
            for (component in components)
                component.rotate(centerOfRotation, angle)
            //            surrounding.rotateSurrounding(dr, nowXY, previousFrameTime);
        }
    protected var velocity = Vector(0f, 0f)
        protected set
    
    abstract val rocketQuirks: RocketQuirks
    protected abstract val components: Array<ISolid>
    var centerOfRotation = surrounding.centerOfRotation
        protected set
    // surrounding have to define center of rotation
    // constructor of subclasses need to reset components with its center of rotation at centerOfRotationY and centerOfRotationX and defined it's velocity
    
    open fun isCrashed(surrounding: Surrounding, timePassed: Long): Boolean {
        // surrounding will handle this
        val crashedOverlapper = surrounding.isCrashed(crashOverlappers)
        if (crashedOverlapper.isNotEmpty()) {
            crashSound.setVolume(0.66f * mainActivity.soundEffectsVolume/100,
                    0.66f * mainActivity.soundEffectsVolume/100)
            crashSound.start()
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
                if (component is IRemovable)
                    if (!component.removed)
                        component.remove()
            
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

        for (trace in traces) {
            trace.moveTrace(displacement)
            trace.fadeTrace(now, previousFrameTime)
        }
        if (rocketControl.throttleOn) {
            generateTrace(now, previousFrameTime)
        }
        
        surrounding.moveSurrounding(displacement, now, previousFrameTime)
    }
    
    protected abstract fun generateTrace(now: Long, previousFrameTime: Long)

    fun fadeTrace(now: Long, previousFrameTime: Long) {
        for (trace in traces)
            trace.fadeTrace(now, previousFrameTime)
    }
    
    open fun removeAllShape() {
        for (component in components)
            if (component is IRemovable)
                if (!component.removed)
                    component.remove()
        for (trace in traces)
            trace.removeTrace()
        explosionShape?.remove()
        crashSound.release()
    }
    
    fun isEaten(littleStar: LittleStar): Boolean {
        return littleStar.isEaten(crashOverlappers)
    }
}

fun speedFormula(initialSpeed: Float, score: Int): Float {
    // when 0 score, 1 times as fast, when speedLogBase^n score, (n+1) times as fast
    return initialSpeed * (log(score + 1f, 32f) + 1)
//                        velocity = initialSpeed * (LittleStar.score / 64f + 1);
}

data class RocketState(val currentRotation: Float, val velocity: Vector)

data class RocketQuirks(val turningRadius: Float, val initialSpeed: Float, val rotationSpeed: Float, val acceleration: Float, val deceleration: Float)

fun convertPointsOnRocket(pR: Array<Vector>, center: Vector, scale: Vector): Array<Vector> {
    for (i in pR.indices) {
        pR[i] -= center  // bring it to center
        pR[i] = pR[i].scaleXY(scale)
        pR[i] = pR[i].rotateVector(PI.toFloat() / 2) // point to right which is rotation 0
    }
    val pL = Array(pR.size) { pR[it].mirrorXAxis() }
    return pL
}