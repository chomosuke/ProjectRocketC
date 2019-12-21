package com.chomusukestudio.projectrocketc.Rocket.rocketPhysics

import com.chomusukestudio.projectrocketc.Joystick.RocketControl
import com.chomusukestudio.projectrocketc.Rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Rocket.speedFormula
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.decelerateVelocity
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.square

class DragRocketPhysics : RocketPhysics() {
    override fun getRocketState(rocketQuirks: RocketQuirks, rocketState: RocketState, rocketControl: RocketControl, now: Long, previousFrameTime: Long): RocketState {
        val rotationNeeded = rocketControl.rotationNeeded
        val dr = rocketQuirks.rotationSpeed * (now - previousFrameTime) // dr/dt * dt
        var currentRotation = rocketState.currentRotation
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
        // friction
        val speed = rocketState.velocity.abs
        var velocity = if (speed != 0f)
            decelerateVelocity(rocketState.velocity, square(speed), (now - previousFrameTime))
        else
            rocketState.velocity
        
        if (rocketControl.throttleOn) {
            val targetSpeed = speedFormula(rocketQuirks.initialSpeed, LittleStar.score)
            val acceleration = square(targetSpeed)
            val ds = acceleration * (now - previousFrameTime)
            velocity += Vector(ds, 0f).rotateVector(currentRotation)
        }
        return RocketState(currentRotation, velocity)
    }

}