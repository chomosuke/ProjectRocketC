package com.chomusukestudio.projectrocketc.Surrounding

import android.util.Log
import com.chomusukestudio.projectrocketc.GLRenderer.bottomEnd
import com.chomusukestudio.projectrocketc.GLRenderer.leftEnd
import com.chomusukestudio.projectrocketc.GLRenderer.rightEnd
import com.chomusukestudio.projectrocketc.GLRenderer.topEnd
import com.chomusukestudio.projectrocketc.IReusable
import com.chomusukestudio.projectrocketc.PlanetShape.PlanetShape
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.Overlapper
import com.chomusukestudio.projectrocketc.Shape.Vector
import com.chomusukestudio.projectrocketc.distance

// this class takes a planetShape and manage it, make it flybyable and reusable.
// this is done to weaken the coupling between PlanetShapes and Surrounding
class Planet(private val planetShape: PlanetShape): IReusable, IFlybyable {

    var visibility: Boolean
        get() = planetShape.visibility
        set(value) { planetShape.visibility = value }

    // at first the planet haven't been flybyed and the close time is zero
    private var flybyable = true
    private var closeTime = 0L
    override fun checkFlyby(rocket: Rocket, frameDuration: Long, flybyDistance: Float, timeLimit: Long): Boolean {
        if (distance(rocket.centerOfRotation, center) <= radius + (rocket.width/2) + flybyDistance)
            closeTime += frameDuration
        if (flybyable) {
            if (closeTime > timeLimit) {
                flybyable = false
                // can't flyby the same planet twice
                Log.v("flyby time limit", "" + timeLimit)
                return true
            }
        }
        return false
    }


    var center = planetShape.center
        private set // Planet's center is different from actual center while planet go beyond the screen

    val radius: Float
        get() = planetShape.radius

    private var angleRotated: Float = 0f

    override var isInUse: Boolean = false
        set(value) {
            field = value
            if (!value) {
                visibility = false
                flybyable = true
                closeTime = 0L
            }
        }

    private fun setActual(actualCenter: Vector) {
        val dCenter = actualCenter - planetShape.center
        planetShape.move(dCenter)
        if (angleRotated != 0f) {
            planetShape.rotate(center, angleRotated)
            angleRotated = 0f // reset angleRotated
        }
    }


    fun resetPosition(center: Vector) {
        planetShape.resetPosition(center)
        this.center = center
    }

    fun rotatePlanet(centerOfRotation: Vector, angle: Float) {
        if (angle == 0f) {
            return
        }
        angleRotated += angle
        center = center.rotateVector(centerOfRotation, angle)
        visibility = canBeSeen()
        if (visibility)
            setActual(center)
    }

    private fun canBeSeen(): Boolean {
        return canBeSeenIf(center) || canBeSeenIf(planetShape.center)
    }

    private fun canBeSeenIf(center: Vector): Boolean {
        val maxWidth = planetShape.maxWidth
        return center.x < rightEnd + maxWidth &&
                center.x > leftEnd - maxWidth &&
                center.y < topEnd + maxWidth &&
                center.y > bottomEnd - maxWidth
    }

    fun movePlanet(vector: Vector) {
        center += vector
        visibility = canBeSeen()
        if (visibility)
            setActual(center)
    }

    fun isOverlap(overlapper: Overlapper): Boolean {
        return planetShape.overlapper overlap overlapper
    }

    fun isTooClose(anotherPlanet: Planet, distance: Float): Boolean {
        // if circle and circle are too close
        return distance(anotherPlanet.center, this.center) <= anotherPlanet.planetShape.radius + planetShape.radius + distance
        // testing all pointsOutside is impractical because performance, subclass may override this method.
    }
}