package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.projectrocketc.Rocket.Rocket

interface IFlybyable{
    fun checkFlyby(rocket: Rocket, frameDuration: Long): Boolean
}