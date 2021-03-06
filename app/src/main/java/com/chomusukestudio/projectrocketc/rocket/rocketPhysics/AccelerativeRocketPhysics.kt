package com.chomusukestudio.projectrocketc.rocket.rocketPhysics

import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.projectrocketc.joystick.RocketControl
import com.chomusukestudio.projectrocketc.rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.rocket.RocketState
import com.chomusukestudio.projectrocketc.decelerateVelocity
//
//class AccelerativeRocketPhysics : RocketPhysics() {
//    override fun getRocketState(rocketQuirks: RocketQuirks, rocketState: RocketState, rocketControl: RocketControl, now: Long, previousFrameTime: Long): RocketState {
//        val rotationNeeded = rocketControl.rotationNeeded
//        val dr = rocketQuirks.rotationSpeed * (now - previousFrameTime) // dr/dt * dt
//        var currentRotation = rocketState.currentRotation
//        var velocity = rocketState.velocity
//        when {
//            rotationNeeded < -dr -> {
//                currentRotation -= dr
//            }
//            rotationNeeded > dr -> {
//                currentRotation += dr
//            }
//            else -> {
//                currentRotation += rotationNeeded
//            }
//        }
//        if (rocketControl.throttleOn) {
//            velocity += Vector(rocketQuirks.acceleration * (now - previousFrameTime), 0f).rotateVector(currentRotation)
//        }
//        // friction
//        if (velocity.abs != 0f) {
//            velocity = decelerateVelocity(velocity, rocketQuirks.deceleration, (now - previousFrameTime))
//        }
//        return RocketState(currentRotation, velocity)
//    }
//}