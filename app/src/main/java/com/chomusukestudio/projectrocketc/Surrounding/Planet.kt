package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.prcandroid2dgameengine.distance
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.Overlapper
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.projectrocketc.IReusable
import com.chomusukestudio.projectrocketc.PlanetShape.PlanetShape
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.max
import com.chomusukestudio.projectrocketc.min

// this class takes a planetShape and manage it, make it flybyable and reusable.
// this is done to weaken the coupling between PlanetShapes and Surrounding
class Planet(private val planetShape: PlanetShape, private val drawData: DrawData/*for bourndaries*/): IReusable {

    var visibility: Boolean
        get() = planetShape.visibility
        set(value) { planetShape.visibility = value }

    // at first the planet haven't been flybyed and the close time is zero
    private var flybyable = true
    fun checkFlyby(rocket: Rocket, frameDuration: Long): Boolean {
        if (flybyable &&
                distance(rocket.centerOfRotation, center) <= radius + (rocket.width/2) + rocket.rocketQuirks.flybyDistance) {
                flybyable = false
                // can't flyby the same planet twice
                return true
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
        val xMax = max(drawData.leftEnd, drawData.rightEnd)
        val xMin = min(drawData.leftEnd, drawData.rightEnd)
        val yMax = max(drawData.topEnd, drawData.bottomEnd)
        val yMin = min(drawData.topEnd, drawData.bottomEnd)
        return center.x < xMax + maxWidth &&
                center.x > xMin - maxWidth &&
                center.y < yMax + maxWidth &&
                center.y > yMin - maxWidth
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