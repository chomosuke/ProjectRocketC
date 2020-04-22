package com.chomusukestudio.projectrocketc.rocket.rocketPhysics

import com.chomusukestudio.projectrocketc.joystick.RocketControl
import com.chomusukestudio.projectrocketc.rocket.RocketQuirks
import com.chomusukestudio.projectrocketc.rocket.RocketState

abstract class RocketPhysics{
  abstract fun getRocketState(rocketQuirks: RocketQuirks, rocketState: RocketState, rocketControl: RocketControl, now: Long, previousFrameTime: Long): RocketState
}