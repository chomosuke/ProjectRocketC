package com.chomusukestudio.projectrocketc.Rocket

import android.media.MediaPlayer
import android.util.Log
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Joystick.RocketMotion
import com.chomusukestudio.projectrocketc.Rocket.trace.RegularPolygonalTrace
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Shape.coordinate.square
import com.chomusukestudio.projectrocketc.State

import com.chomusukestudio.projectrocketc.Surrounding.Surrounding
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Created by Shuang Li on 11/03/2018.
 */

open class Rocket1(surrounding: Surrounding, private val crashSound: MediaPlayer, layers: Layers) : Rocket(surrounding, layers) {
    override val trace = RegularPolygonalTrace(6, 1.01f, 0.24f,  0.4f, 1000, 1f, 1f, 0f, 3f, layers)

    override fun generateTrace(now: Long, previousFrameTime: Long) {
        val x1 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX4)
        val y1 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY4)
        val x2 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QX3)
        val y2 = (components[3] as QuadrilateralShape).getQuadrilateralShapeCoords(QY3)
        val originX = (x1 + x2) / 2
        val originY = (y1 + y2) / 2
        trace.generateTrace(now, previousFrameTime, originX, originY)
    }

    override var radiusOfRotation = 2f
    final override val initialSpeed = 0f / 1000f
    override var speed = initialSpeed

    override val width = 0.3f

    final override val components: Array<Shape> = Array(4) { i ->
        when (i) {
            // defined components of rocket around centerOfRotation set by surrounding
            0 ->
                TriangularShape(centerOfRotationX, centerOfRotationY + 0.5f,
                        centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f,
                        1f, 1f, 1f, 1f, BuildShapeAttr(1f, true, layers))
            1 ->
                QuadrilateralShape(centerOfRotationX + 0.15f, centerOfRotationY + 0.3f,
                        centerOfRotationX - 0.15f, centerOfRotationY + 0.3f, centerOfRotationX - 0.15f, centerOfRotationY - 0.3f,
                        centerOfRotationX + 0.15f, centerOfRotationY - 0.3f, 1f, 1f, 1f, 1f, BuildShapeAttr(1f, true, layers))
            2 ->
                CircularShape(centerOfRotationX, centerOfRotationY /*+ 0.38*/, 0.07f,
                        0.1f, 0.1f, 0.1f, 1f, BuildShapeAttr(0.9999f, true, layers))
            3 ->
                QuadrilateralShape(centerOfRotationX + 0.1f, centerOfRotationY - 0.3f,
                        centerOfRotationX - 0.1f, centerOfRotationY - 0.3f, centerOfRotationX - 0.12f, centerOfRotationY - 0.4f,
                        centerOfRotationX + 0.12f, centerOfRotationY - 0.4f, 1f, 1f, 1f, 1f, BuildShapeAttr(1f, true, layers))
            else -> {
                throw IndexOutOfBoundsException()
            }
        }
    }
    override val shapeForCrashAppro = QuadrilateralShape(centerOfRotationX + 0.15f, centerOfRotationY + 0.5f,
            centerOfRotationX - 0.15f, centerOfRotationY + 0.5f, centerOfRotationX - 0.15f, centerOfRotationY - 0.4f,
            centerOfRotationX + 0.15f, centerOfRotationY - 0.4f, 1f, 1f, 1f, 1f, BuildShapeAttr(1f, false, layers))

    override fun isCrashed(surrounding: Surrounding): Boolean {
        return if (super.isCrashed(surrounding)) {
            crashSound.start()
            true
        } else false
    }
    
    private var speedX = 0f
    private var speedY = 0f
    private val acce = 0.000001f
    override fun moveRocket(rocketMotion: RocketMotion, now: Long, previousFrameTime: Long, state: State) {
        val rotationNeeded = rocketMotion.rotationNeeded
        val speedOfRotation = 0.003f
        val dr = speedOfRotation * (now - previousFrameTime) // dr/dt * dt
        when {
            rotationNeeded < -dr -> {
                rotateRocket(-dr)
            }
            rotationNeeded > dr -> {
                rotateRocket(dr)
            }
            else -> {
                rotateRocket(rotationNeeded)
            }
        }
        if (rocketMotion.throttleOn) {
            speedX += acce * (now - previousFrameTime) * -sin(currentRotation)
            speedY += acce * (now - previousFrameTime) * -cos(currentRotation)
            val dx = speedX * (now - previousFrameTime)
            val dy = speedY * (now - previousFrameTime)
            surrounding.moveSurrounding(dx, dy, now, previousFrameTime)
            speed = sqrt(square(speedX) + square(speedY))
            if (state == State.InGame) { // only generate trace when in game
                trace.moveTrace(dx, dy)
                generateTrace(now, previousFrameTime)
                fadeTrace(now, previousFrameTime)
            }
            Log.d("throttle", "on")
        }
        // friction
        if (speed != 0f) {
        
            val dSpeed = acce * (now - previousFrameTime) / 2
            if (speed > dSpeed) speed -= dSpeed
            else speed = 0f
            when {
                speedX == 0f -> // calculation can be simplified
                    speedY = if (speedY >= 0) speed else -speed
                speedY == 0f ->
                    speedX = if (speedX >= 0) speed else -speed
                else -> {
                    val ratio = speedY / speedX
                    speedX = if (speedX >= 0) // preserve sign of speeds
                        speed / sqrt(square(ratio) + 1)
                    else
                        -speed / sqrt(square(ratio) + 1)
                    speedY = ratio * speedX
                }
            }
            val dx = speedX * (now - previousFrameTime)
            val dy = speedY * (now - previousFrameTime)
            surrounding.moveSurrounding(dx, dy, now, previousFrameTime)
        }
    
    }
}