package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.projectrocketc.Rocket.Rocket

interface IFlybyable{
    var flybyable: Boolean
    var closeTime: Long
    fun checkFlyby(rocket: Rocket): Boolean {
        if (flybyable) {

            if (closeTime > 125) {
                flybyable = false
                // can't flyby the same planet twice
                return true
            }
        }
        return false
    }
}