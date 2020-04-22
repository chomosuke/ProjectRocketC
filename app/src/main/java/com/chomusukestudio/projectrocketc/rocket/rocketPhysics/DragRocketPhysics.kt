package com.chomusukestudio.projectrocketc.rocket.rocketPhysics

import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.prcandroid2dgameengine.square
import com.chomusukestudio.projectrocketc.joystick.RocketControl
import com.chomusukestudio.projectrocketc.rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.rocket.RocketState
import com.chomusukestudio.projectrocketc.rocket.speedFormula
import com.chomusukestudio.projectrocketc.decelerateVelocity
import com.chomusukestudio.projectrocketc.littleStar.LittleStar

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