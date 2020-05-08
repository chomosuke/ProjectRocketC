package com.chomusukestudio.projectrocketc.rocket

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.media.MediaPlayer
import androidx.annotation.CallSuper
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.BuildShapeAttr
import com.chomusukestudio.prcandroid2dgameengine.shape.IRemovable
import com.chomusukestudio.prcandroid2dgameengine.shape.ISolid
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.projectrocketc.joystick.RocketControl
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.rocket.rocketRelated.ExplosionShape
import com.chomusukestudio.projectrocketc.rocket.rocketRelated.RedExplosionShape
import com.chomusukestudio.projectrocketc.rocket.rocketPhysics.RocketPhysics
import com.chomusukestudio.projectrocketc.rocket.trace.Trace
import com.chomusukestudio.projectrocketc.surrounding.Surrounding
import com.chomusukestudio.projectrocketc.userInterface.MainActivity
import com.chomusukestudio.projectrocketc.userInterface.State
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import kotlin.math.PI
import kotlin.math.log

abstract class Rocket(protected val surrounding: Surrounding, private val mainActivity: MainActivity, var rocketPhysics: RocketPhysics, private val drawData: DrawData) {
    protected open val crashSound: MediaPlayer = MediaPlayer.create(mainActivity, R.raw.fx22)
    
    fun setRotationAndCenter(centerOfRotation: Vector, rotation: Float) {
        // called before initialize trace
        // set rotation
        this.centerOfRotation = centerOfRotation
        currentRotation = rotation // this setter already rotate the rocket
    }
    
    protected open var explosionShape: ExplosionShape? = null
    
    protected open val crashOverlappers get() = Array(components.size) { components[it].overlapper }
    
    protected abstract val traces: Array<Trace>
    var currentRotation = 0f
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

    abstract val description: String

    protected abstract val components: Array<ISolid>
    var centerOfRotation = surrounding.centerOfRotation
        protected set(value) {
            val offset = value - field
            field = value
            for (component in components)
                component.move(offset)
        }
    // surrounding have to define center of rotation
    // constructor of subclasses need to reset components with its center of rotation at centerOfRotationY and centerOfRotationX and defined it's velocity
    
    open fun isCrashed(surrounding: Surrounding, timePassed: Long): Boolean {
        // surrounding will handle this
        val crashedOverlapper = surrounding.isCrashed(crashOverlappers)
        if (crashedOverlapper.isNotEmpty()) {
            crashSound.setVolume(mainActivity.soundEffectsVolume.toFloat()/100,
                    mainActivity.soundEffectsVolume.toFloat()/100)
            crashSound.start()
            return true
        }
        return false
    }
    
    open val explosionCoordinate = centerOfRotation
    fun drawExplosion(now: Long, previousFrameTime: Long) {
        if (explosionShape == null) {
            explosionShape = RedExplosionShape(explosionCoordinate, 0.75f, 1000, BuildShapeAttr(-11f, true, drawData))
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
    open fun moveRocket(rocketControl: RocketControl, now: Long, previousFrameTime: Long, state: State) {
        val rocketState = rocketPhysics.getRocketState(rocketQuirks, RocketState(currentRotation, velocity), rocketControl, now, previousFrameTime)
        
        this.velocity = rocketState.velocity
        this.currentRotation = rocketState.currentRotation
        
        val displacement = -velocity * (now - previousFrameTime).toFloat()

        for (trace in traces) {
            trace.moveTrace(displacement)
            trace.fadeTrace(now, previousFrameTime)
        }
        if (rocketControl.throttleOn/* && state == State.InGame*/) {
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

data class RocketQuirks(val name: String, val initialSpeed: Float, val rotationSpeed: Float, val price: Int,
                        val flybyDistance: Float, val flybyDelta: Int, val eatLittleStarDelta: Int)
val v2RocketQuirks           = RocketQuirks("V-2 rocket",    0.003f, 0.0026f, 0,      0.5f, 1, 1)
val sputnikRocketQuirks      = RocketQuirks("Sputnik",       0.003f, 0.0024f, 5000,   0.36f, 2, 1)
val vostokKRocketQuirks      = RocketQuirks("Vostok-K",      0.003f, 0.0033f, 10000,  0.36f, 2, 1)
val saturnVRocketQuirks      = RocketQuirks("Saturn V",      0.003f, 0.003f, 20000,  0.26f, 5, 1)
val spaceShuttleRocketQuirks = RocketQuirks("Space Shuttle", 0.003f, 0.0042f, 50000,  0.19f, 8, 1)
val falcon9RocketQuirks      = RocketQuirks("Falcon 9",      0.003f, 0.0038f, 100000, 0.14f, 15, 1)
val falconHeavyRocketQuirks  = RocketQuirks("Falcon Heavy",  0.003f, 0.005f, 200000, 0.10f, 25, 1)

fun convertPointsOnRocket(pR: Array<Vector>, center: Vector, scale: Vector): Array<Vector> {
    for (i in pR.indices) {
        pR[i] -= center  // bring it to center
        pR[i] *= scale
        pR[i] = pR[i].rotateVector(PI.toFloat() / 2) // point to right which is rotation 0
    }
    val pL = Array(pR.size) { pR[it].mirrorXAxis() }
    return pL
}