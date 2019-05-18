package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.projectrocketc.GLRenderer.bottomEnd
import com.chomusukestudio.projectrocketc.GLRenderer.leftEnd
import com.chomusukestudio.projectrocketc.GLRenderer.rightEnd
import com.chomusukestudio.projectrocketc.GLRenderer.topEnd
import com.chomusukestudio.projectrocketc.IReusable
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.PlanetShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.coordinate.distance
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.Shape.coordinate.square

// this class takes a planetShape and manage it, make it flybyable and reusable.
// this is done to weaken the coupling between PlanetShapes and BasicSurrounding
class Planet(private val planetShape: PlanetShape): IReusable, IFlybyable {

    var visibility: Boolean
        get() = planetShape.visibility
        set(value) { planetShape.visibility = value }

    // at first the planet haven't been flybyed and the close time is zero
    override var flybyable = true
    override var closeTime = 0L
    override fun checkFlyby(rocket: Rocket, frameDuration: Long): Boolean {
        if (distance(rocket.centerOfRotationX, rocket.centerOfRotationY, centerX, centerY) <= radius + (rocket.width/2) + 0.5)
            closeTime += frameDuration
        if (flybyable) {
            if (closeTime > 125) {
                flybyable = false
                // can't flyby the same planet twice
                return true
            }
        }
        return false
    }


    var centerX: Float = planetShape.centerX
        private set
    var centerY: Float = planetShape.centerY
        private set

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

    private fun setActual(actualCenterX: Float, actualCenterY: Float) {
        val dx = actualCenterX - planetShape.centerX
        val dy = actualCenterY - planetShape.centerY
        planetShape.moveShape(dx, dy)
        if (angleRotated != 0f) {
            planetShape.rotateShape(centerX, centerY, angleRotated)
            angleRotated = 0f // reset angleRotated
        }
    }


    fun resetPosition(centerX: Float, centerY: Float) {
        planetShape.resetPosition(centerX, centerY)
        this.centerX = centerX
        this.centerY = centerY
    }

    fun rotatePlanet(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        if (angle == 0f) {
            return
        }
        angleRotated += angle
        val result = rotatePoint(centerX, centerY, centerOfRotationX, centerOfRotationY, angle)
        centerX = result[0]
        centerY = result[1]
        visibility = canBeSeen()
        if (visibility)
            setActual(centerX, centerY)
    }

    private fun canBeSeen(): Boolean {
        return canBeSeenIf(centerX, centerY) || canBeSeenIf(planetShape.centerX, planetShape.centerY)
    }

    fun canBeSeenIf(centerX: Float, centerY: Float): Boolean {
        val maxWidth = planetShape.maxWidth
        return centerX < leftEnd + maxWidth &&
                centerX > rightEnd - maxWidth &&
                centerY < topEnd + maxWidth &&
                centerY > bottomEnd - maxWidth
    }

    fun movePlanet(dx: Float, dy: Float) {
        centerX += dx
        centerY += dy
        visibility = canBeSeen()
        if (visibility)
            setActual(centerX, centerY)
    }

    fun isOverlap(anotherShape: Shape): Boolean {
        return planetShape.isOverlap(anotherShape)
    }

    fun isTooClose(anotherPlanet: Planet, distance: Float): Boolean {
        // if circle and circle are too close
        return square(anotherPlanet.centerX - this.centerX) + square(anotherPlanet.centerY - this.centerY) <= square(anotherPlanet.planetShape.radius + planetShape.radius + distance)
        // testing all pointsOutside is impractical because performance, subclass may override this method.
    }
}