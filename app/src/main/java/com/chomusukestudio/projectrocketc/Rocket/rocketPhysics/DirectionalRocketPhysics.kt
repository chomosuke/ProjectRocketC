package com.chomusukestudio.projectrocketc.Rocket.rocketPhysics

import com.chomusukestudio.projectrocketc.Joystick.RocketControl
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.Rocket.RocketState
import com.chomusukestudio.projectrocketc.Rocket.speedFormula
import com.chomusukestudio.projectrocketc.State
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import kotlin.math.cos
import kotlin.math.sin


class DirectionalRocketPhysics: RocketPhysics() {
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
		val speed = speedFormula(rocketQuirks.initialSpeed, LittleStar.score)
		val speedX = speed * sin(currentRotation)
		val speedY = speed * cos(currentRotation)
		return RocketState(currentRotation, speedX, speedY)
	}
}