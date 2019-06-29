package com.chomusukestudio.projectrocketc.Rocket.rocketPhysics

import com.chomusukestudio.projectrocketc.Joystick.RocketControl
import com.chomusukestudio.projectrocketc.Rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.decelerateSpeedXY
import kotlin.math.cos
import kotlin.math.sin

class AccelerativeRocketPhysics : RocketPhysics() {
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
			speedX += rocketQuirks.acceleration * (now - previousFrameTime) * sin(currentRotation)
			speedY += rocketQuirks.acceleration * (now - previousFrameTime) * cos(currentRotation)
		}
		// friction
		if (speedX != 0f && speedY != 0f) {
			val speedXY = decelerateSpeedXY(speedX, speedY, rocketQuirks.deceleration, (now - previousFrameTime))
			speedX = speedXY[0]
			speedY = speedXY[1]
		}
		return RocketState(currentRotation, speedX, speedY)
	}
}