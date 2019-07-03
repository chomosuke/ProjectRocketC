package com.chomusukestudio.projectrocketc.Rocket.rocketPhysics

import com.chomusukestudio.projectrocketc.Joystick.RocketControl
import com.chomusukestudio.projectrocketc.Rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Rocket.speedFormula
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import kotlin.math.cos
import kotlin.math.sin


class DirectionalRocketPhysics: RocketPhysics() {
	override fun getRocketState(rocketQuirks: RocketQuirks, rocketState: RocketState, rocketControl: RocketControl, now: Long, previousFrameTime: Long): RocketState {
		var currentRotation = rocketState.currentRotation

		val speed = speedFormula(rocketQuirks.initialSpeed, LittleStar.score)
		val speedX = speed * sin(currentRotation)
		val speedY = speed * cos(currentRotation)

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
		return RocketState(currentRotation, speedX, speedY)
	}
}