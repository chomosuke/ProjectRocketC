package com.chomusukestudio.projectrocketc.Rocket.rocketPhysics

import com.chomusukestudio.projectrocketc.Joystick.RocketControl
import com.chomusukestudio.projectrocketc.Rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Rocket.speedFormula
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.decelerateVelocity
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.square
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DragRocketPhysics : RocketPhysics() {
    override fun getRocketState(rocketQuirks: RocketQuirks, rocketState: RocketState, rocketControl: RocketControl, now: Long, previousFrameTime: Long): RocketState {
        val rotationNeeded = rocketControl.rotationNeeded
        val dr = rocketQuirks.rotationSpeed * (now - previousFrameTime) // dr/dt * dt
        var currentRotation = rocketState.currentRotation
        var velocity = rocketState.velocity
        when {
            rotationNeeded < -dr -> {
                currentRotation -= dr
            }
            rotationNeeded > dr -> {
                currentRotation += dr
            }
            else -> {
                currentRotation += rotationNeeded
            }
        }
        if (rocketControl.throttleOn) {
            val targetSpeed = speedFormula(rocketQuirks.initialSpeed, LittleStar.score)
            val acceleration = square(targetSpeed)
            val ds = acceleration * (now - previousFrameTime)
            velocity += Vector(ds, 0f).rotateVector(currentRotation)
        }
        // friction
        val speed = velocity.abs
        if (speed != 0f)
            velocity = decelerateVelocity(velocity, square(speed), (now - previousFrameTime))
        return RocketState(currentRotation, velocity)
    }

}