package com.chomusukestudio.projectrocketc.rocket.rocketPhysics

import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.projectrocketc.joystick.RocketControl
import com.chomusukestudio.projectrocketc.rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.rocket.RocketState
import com.chomusukestudio.projectrocketc.rocket.speedFormula
import com.chomusukestudio.projectrocketc.littleStar.LittleStar


class DirectionalRocketPhysics: RocketPhysics() {
    override fun getRocketState(rocketQuirks: RocketQuirks, rocketState: RocketState, rocketControl: RocketControl, now: Long, previousFrameTime: Long): RocketState {
        var currentRotation = rocketState.currentRotation

        val speed = speedFormula(rocketQuirks.initialSpeed, LittleStar.score)

        val rotationNeeded = rocketControl.rotationNeeded
        val rotationSpeed = speed / rocketQuirks.turningRadius
        val dr = rotationSpeed * (now - previousFrameTime) // dr/dt * dt
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

        val velocity = Vector(speed, 0f).rotateVector(currentRotation)
        return RocketState(currentRotation, velocity)
    }
}