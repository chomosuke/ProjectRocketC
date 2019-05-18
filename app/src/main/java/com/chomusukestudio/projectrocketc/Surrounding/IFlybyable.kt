package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.projectrocketc.Rocket.Rocket

interface IFlybyable{
    var flybyable: Boolean
    var closeTime: Long
    fun checkFlyby(rocket: Rocket, frameDuration: Long): Boolean
}