package com.chomusukestudio.projectrocketc.Rocket.rocketPhysics

import com.chomusukestudio.projectrocketc.Joystick.RocketControl
import com.chomusukestudio.projectrocketc.Rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Rocket.speedFormula
import com.chomusukestudio.projectrocketc.decelerateSpeedXY
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
        var speedX = rocketState.speedX
        var speedY = rocketState.speedY
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
            speedX += ds * sin(currentRotation)
            speedY += ds * cos(currentRotation)
        }
        // friction
        if (!(speedX == 0f && speedY == 0f)) {
            val speed = sqrt(square(speedX) + square(speedY))
            val speedXY = decelerateSpeedXY(speedX, speedY, square(speed), (now - previousFrameTime))
            speedX = speedXY[0]
            speedY = speedXY[1]
        }
        return RocketState(currentRotation, speedX, speedY)
    }

}