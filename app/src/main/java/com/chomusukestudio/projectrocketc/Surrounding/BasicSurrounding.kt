package com.chomusukestudio.projectrocketc.Surrounding

import android.util.Log
import android.widget.TextView
import com.chomusukestudio.projectrocketc.*
import com.chomusukestudio.projectrocketc.GLRenderer.*

import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Rocket.speedFormula
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.JupiterShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.MarsShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.PlanetShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.SaturnShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.StarShape

import java.util.ArrayList

import com.chomusukestudio.projectrocketc.distance
import com.chomusukestudio.projectrocketc.square
import com.chomusukestudio.projectrocketc.littleStar.LittleStar.Color.YELLOW
import java.lang.Math.random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Created by Shuang Li on 31/03/2018.
 */

class BasicSurrounding(private val visualTextView: TouchableView<TextView>, private val layers: Layers, resources: SurroundingResources? = null) {
    private val planets = ArrayList<Planet>() // this defines where the rocket can't go
    // rockets should have z value of 10 while background should have a z value higher than 10, like 11.
    private val backgrounds = // backGrounds doesn't effect plane
            if (resources is SurroundingResources)
                resources.background
            else {
                // initialize all those stars in the backgrounds
                Array<Shape>(6000) { // 6000 stars in the background
                    return@Array StarShape(Vector(randFloat(leftEnd, rightEnd), randFloat(topEnd, bottomEnd)),
                            (random() * random() * random()).toFloat() * 255f / 256f + 1f / 256f, random().toFloat() * 0.3f, BuildShapeAttr(11f, true, layers))
                }
            }
    private val littleStars = ArrayList<LittleStar>()
    private lateinit var startingPathOfRocket: Shape
    lateinit var rocket: Rocket

    val centerOfRotation = Vector(0f, 0f)
    val rotation = PI.toFloat() / 2

    private val minDistantBetweenPlanet = 4f

    private lateinit var newPlanet: Planet
    private var numberOfRedStar = 0

    private var displacement = Vector(0f, 0f) // displacement since last makeNewTriangleAndRemoveTheOldOne()
    private val parallelForIForBackgroundStars = ParallelForI(8, "move background")

    private val planetsStore =
            if(resources is SurroundingResources)
                resources.planetsStore
            else
                Array(1000) {// 1000 planet in store waiting for use
                    val planetShape = generateRandomPlanet(Vector(100f, 100f), generateRadius(), 10f, layers)
                    planetShape.isInUse = false
                    return@Array planetShape
                }

    private fun generateRandomPlanet(center: Vector, radius: Float, z: Float, layers: Layers): Planet {
        val randomPlanetShape: PlanetShape
        if (radius < averageRadius) {
//            val timeStarted = SystemClock.uptimeMillis()
            randomPlanetShape = MarsShape(center, radius, BuildShapeAttr(z, false, layers))
//            Log.v("time take for newPlanet", "mars " + (SystemClock.uptimeMillis() - timeStarted))
        } else if (radius < averageRadius + radiusMargin * 0.6) {
//            val timeStarted = SystemClock.uptimeMillis()
            val ringA = randFloat(1.5f, 1.7f) * radius
            randomPlanetShape = SaturnShape(ringA, randFloat(0.1f, 0.6f) * ringA, 1.2f * radius, randFloat(3f, 6f).toInt(), center, radius, BuildShapeAttr(z, false, layers))
            //            randomPlanetShape = new SaturnShape(ringA, (float) (0.1 + 0.5 * random()) * ringA, (0.67f + 0.2f*(float)random()) * ringA, (int) (3 * random() + 3), centerX, centerY, radius, z);
//            Log.v("time take for newPlanet", "saturn " + (SystemClock.uptimeMillis() - timeStarted))
        } else {
//            val timeStarted = SystemClock.uptimeMillis()
            randomPlanetShape = JupiterShape(center, radius, BuildShapeAttr(z, false, layers))
//            Log.v("time take for newPlanet", "jupiter " + (SystemClock.uptimeMillis() - timeStarted))
        }
        randomPlanetShape.rotateShape(center, (random() * 2.0 * PI).toFloat())
        return Planet(randomPlanetShape)
    }

    init {
        LittleStar.setCenterOfRocket(centerOfRotation)
    }

    private val topPlanetBoundary = topEnd * (1.5f/* + topMarginForLittleStar*/)
    private val bottomPlanetBoundary = bottomEnd * 1.5f
    private val leftPlanetBoundary = rightEnd * 1.5f
    private val rightPlanetBoundary = leftEnd * 1.5f

    fun initializeSurrounding(rocket: Rocket, state: State) {
        // pass the rocket to the surrounding so surrounding can do stuff such as setCenterOfRotation

        this.rocket = rocket

        val minCloseDist = 0.004f * timeLimit
        val initialFlybyDistance = sqrt(square(flybyDistance + averageRadius) - square(minCloseDist))/*rocket.width / 2 + flybyDistance*/
//        if (rocket.initialSpeed != 0f) {
            startingPathOfRocket = QuadrilateralShape(Vector(centerOfRotation.x - initialFlybyDistance, 100000000f),
                    Vector(centerOfRotation.x + initialFlybyDistance, 100000000f), // max value is bad because it causes overflow... twice
                            Vector(centerOfRotation.x + initialFlybyDistance, centerOfRotation.y - initialFlybyDistance),
                                    Vector(centerOfRotation.x - initialFlybyDistance, centerOfRotation.y - initialFlybyDistance),
                    Color(0f, 1f, 0f, 1f), BuildShapeAttr(0f, false, layers))
            startingPathOfRocket.rotateShape(centerOfRotation, rotation - PI.toFloat()/2)
//        } else {
//            startingPathOfRocket = QuadrilateralShape(centerOfRotationX - initialFlybyDistance, centerOfRotationY + initialFlybyDistance + 1f,
//                    centerOfRotationX + initialFlybyDistance, centerOfRotationY + initialFlybyDistance + 1f,
//                    centerOfRotationX + initialFlybyDistance, centerOfRotationY - initialFlybyDistance,
//                    centerOfRotationX - initialFlybyDistance, centerOfRotationY - initialFlybyDistance,
//                    0f, 1f, 0f, 1f, BuildShapeAttr(0f, false, layers))
//        }


        val avoidDistance = Vector(110f, 0f) // to avoid constant change of visibility
        startingPathOfRocket.moveShape(avoidDistance) // i'll move it back later
        newPlanet = getRandomPlanet()
        // initialize surrounding
        val iMax = 256
        for (i in 0..iMax) {
            // randomly reposition the new planet
            val center = Vector(randFloat(rightPlanetBoundary, leftPlanetBoundary),
            (topPlanetBoundary - bottomPlanetBoundary) / iMax * i + bottomPlanetBoundary) + avoidDistance // + avoidDistanceX as to avoid constant change of visibility
            newPlanet.resetPosition(center)

            if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet

                planets.add(newPlanet)// use the random new planet

                // create another random new planet for next use
                newPlanet = getRandomPlanet()
            }
        }
        for (boundary in planets)
            boundary.movePlanet(-avoidDistance) // move planets back
        startingPathOfRocket.moveShape(-avoidDistance) // move startingPathOfRocket back
    }

    private fun isGoodPlanet(planet: Planet, state: State): Boolean {
        // find out if this planet is too close to other planet
        for (planet2 in planets) {
            if (planet2.isTooClose(planet, minDistantBetweenPlanet))
            // it is too close
                return false
        }
        // it is not too close to any other planet
        if (state != State.InGame)
            if (planet.isOverlap(startingPathOfRocket.overlapper))
                return false// if it blocks the rocket before start
        return true
    }

    fun makeNewTriangleAndRemoveTheOldOne(now: Long, previousFrameTime: Long, state: State) {
        //        if ( ! (-1 < displacementX && displacementX < 1) || ! (-1 < displacementY && displacementY < 1)) {
        //            // only go through the whole thing when displacementX or displacementY is bigger than 1
        // or not so all these work don't get to pile up to one frame

        //        minDistantBetweenPlanet = (float) (4f - log(LittleStar.Companion.getScore() + 1)/log(65536));

        // to delete
        var i = 0
        while (i < planets.size) { // planets
            if (planets[i].center.y < bottomEnd * 30f ||
                    planets[i].center.y > topEnd * 30f ||
                    planets[i].center.x < leftEnd * 30f ||
                    planets[i].center.x > rightEnd * 30f) {
                planets.removeAt(i).isInUse = false
                i--// this one is removed so the next one would have a index of i
            }
            i++
        }

        // to create
        val newAreaLR = abs(displacement.x * (topPlanetBoundary - bottomPlanetBoundary))
        val newAreaTB = abs(displacement.y * (leftPlanetBoundary - rightPlanetBoundary))
        val rand = random()
        if (rand * newAreaLR > (1 - rand) * newAreaTB) {

            if (displacement.x < 0) {
                // if need to create on positive side
                // put the random planet on the positive side
                newPlanet.resetPosition(Vector(randFloat(leftPlanetBoundary, leftPlanetBoundary + displacement.x), randFloat(bottomPlanetBoundary, topPlanetBoundary)))

                if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet
                    planets.add(newPlanet)// use the random new planet
                    // create another random new planet for next use
                    newPlanet = getRandomPlanet()
                }
            } else {
                // if need to create on negative side
                // put the random planet on the negative side
                newPlanet.resetPosition(Vector(randFloat(rightPlanetBoundary, rightPlanetBoundary + displacement.x), randFloat(bottomPlanetBoundary, topPlanetBoundary)))

                if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet
                    planets.add(newPlanet)// use the random new planet
                    // create another random new planet for next use
                    newPlanet = getRandomPlanet()
                }
            }
        } else {
            if (displacement.y < 0) {
                // if need to create on positive side
                // put the random planet on the positive side
                newPlanet.resetPosition(Vector(randFloat(rightPlanetBoundary, leftPlanetBoundary),
                        randFloat(topPlanetBoundary, topPlanetBoundary + displacement.y)))

                if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet
                    planets.add(newPlanet) // use the random new planet
                    // create another random new planet for next use
                    newPlanet = getRandomPlanet()
                }
            } else {
                // if need to create on negative side
                // put the random planet on the positive side
                newPlanet.resetPosition(Vector(randFloat(rightPlanetBoundary, leftPlanetBoundary), randFloat(bottomPlanetBoundary, bottomPlanetBoundary + displacement.y)))

                if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet
                    planets.add(newPlanet)// use the random new planet
                    // create another random new planet for next use
                    newPlanet = getRandomPlanet()
                }
            }
        }
        // reset displacement
        displacement = Vector(0f, 0f)

    }

    private fun attractLittleStar(now: Long, previousFrameTime: Long) {
        for (littleStar in littleStars)
            littleStar.attractLittleStar(centerOfRotation, now, previousFrameTime, 0.00002f)
    }

    fun removeAllShape() {

        for (planet in planets) {
            planet.isInUse = false
        }
        newPlanet.isInUse = false

        // move backgrounds so people can't recognize it's the same stars parallelForIForBackgroundStars.waitForLastRun()
        parallelForIForBackgroundStars.run({ i ->
            val starShape = backgrounds[i] as StarShape

            starShape.moveStarShape(Vector(3.1415926535f, 3.1415926535897932354626f))
            // those that got out of screen, make them appear on the opposite side
            if (starShape.center.y < bottomEnd)
                starShape.resetPosition(Vector(starShape.center.x, starShape.center.y + (topEnd - bottomEnd)))
            if (starShape.center.y > topEnd)
                starShape.resetPosition(Vector(starShape.center.x, starShape.center.y - (topEnd - bottomEnd)))
            if (starShape.center.x < leftEnd)
                starShape.resetPosition(Vector(starShape.center.x + (rightEnd - leftEnd), starShape.center.y))
            if (starShape.center.x > rightEnd)
                starShape.resetPosition(Vector(starShape.center.x - (rightEnd - leftEnd), starShape.center.y))
        }, backgrounds.size)
        parallelForIForBackgroundStars.waitForLastRun()
        // leave background for next use
//        for (background in backgrounds) {
//            background.removeShape()
//        }

        for (littleStar in littleStars) {
            littleStar.removeLittleStarShape()
        }
    }

    fun trashAndGetResources(): SurroundingResources? {
        return SurroundingResources(backgrounds, planetsStore)
    }

    private fun sparkleStar(starShape: StarShape, now: Long, previousFrameTime: Long) {
        if (starShape.isSparkling) {
            if (random() < 1.0 / 50) {
                starShape.isSparkling = false
            } else if (starShape.isSparkleBrighter) {
                if (starShape.brightness >= 1) {
                    starShape.isSparkleBrighter = false
                } else {
                    starShape.changeBrightness((now - previousFrameTime) / 500.0)
                }
            } else {
                if (starShape.brightness <= 0) {
                    starShape.isSparkleBrighter = true
                } else {
                    starShape.changeBrightness(-(now - previousFrameTime) / 500.0)
                }
            }
        } else if (random() < 1.0 / 20) {
            starShape.isSparkling = true
        }
    }

    //    private val parallelForIForMoveBoundaries = ParallelForI(8, "parallelForIForMoveBoundaries")
    fun moveSurrounding(vector: Vector, now: Long, previousFrameTime: Long) {
        parallelForIForBackgroundStars.waitForLastRun()

        // move background
        parallelForIForBackgroundStars.run({ i ->
            val starShape = backgrounds[i] as StarShape

            starShape.moveStarShape(vector)
            // those that got out of screen, make them appear on the opposite side
            if (starShape.center.y < bottomEnd)
                starShape.resetPosition(Vector(starShape.center.x, starShape.center.y + (topEnd - bottomEnd)))
            if (starShape.center.y > topEnd)
                starShape.resetPosition(Vector(starShape.center.x, starShape.center.y - (topEnd - bottomEnd)))
            if (starShape.center.x < leftEnd)
                starShape.resetPosition(Vector(starShape.center.x + (rightEnd - leftEnd), starShape.center.y))
            if (starShape.center.x > rightEnd)
                starShape.resetPosition(Vector(starShape.center.x - (rightEnd - leftEnd), starShape.center.y))
        }, backgrounds.size)

//        val visibleBoundaries = ArrayList<Shape>()
//        for (planet in planets) {
//            if (planet.visibility)
//                visibleBoundaries.offset(planet)
//            else
//                planet.moveStarShape(dx, dy)
//        }
//        parallelForIForMoveBoundaries.run({ i ->
//            visibleBoundaries[i].moveStarShape(dx, dy)
//        }, visibleBoundaries.size)
//        parallelForIForBackgroundStars.waitForLastRun()

        for (i in planets.indices)
            planets[i].movePlanet(vector)
        for (littleStar in littleStars)
            littleStar.moveLittleStar(vector)

        displacement += vector
        // refresh displacement since last makeNewTriangleAndRemoveTheOldOne()

        attractLittleStar(now, previousFrameTime)
        checkFlyby(now - previousFrameTime)
    }

    private fun checkFlyby(frameDuration: Long) {
        for (planet in planets) {
            if (planet.visibility) // only check visible plane for flyby
            if (planet.checkFlyby(rocket, frameDuration)) {
                flybysInThisYellowStar++

                LittleStar.dScore = (LittleStar.dScore + (flybysInThisYellowStar * 5))
                //            LittleStar.Companion.setDScore(1000000);
//            if ((1 + flybysInThisYellowStar * 0.5) % 1 == 0.0) { // display an integer
                giveVisualText("δ+" + (flybysInThisYellowStar * 5), visualTextView)
//            } else {
//                giveVisualText("×" + (1 + flybysInThisYellowStar * 0.5), visualTextView)
//            }
                when (flybysInThisYellowStar) {
                    1 -> {
                    }
                    2 -> {
                    }
                    3 -> {
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun checkAndAddLittleStar(now: Long) { // this get called every frame
        // you trust state will be inGame or processing thread won't check
        if (littleStars.size == 0) {
            littleStars.add(oneNewLittleStar(now)) // if started and there is no little star in surrounding, then offset one.
        }
        for (i in littleStars.indices) {
            if (rocket.isEaten(littleStars[i])) {
                littleStars[i].eatLittleStar(visualTextView)
                when (littleStars[i].COLOR) {
                    YELLOW -> {
                        LittleStar.dScore = LittleStar.dScore + 1
                    }
                    LittleStar.Color.RED -> numberOfRedStar--
                }
                littleStars.removeAt(i)
            } else if (littleStars[i].isTimeOut(now)) {
                when (littleStars[i].COLOR) {
                    YELLOW -> {
                        littleStars.add(oneNewLittleStar(now))
                        LittleStar.dScore = 1
                    }
                    LittleStar.Color.RED -> numberOfRedStar--
                }// can't be Red to prevent cheating the system
                littleStars[i].removeLittleStarShape()
                littleStars.removeAt(i)
            }
        }
    }

    private fun oneNewLittleStar(now: Long): LittleStar {
        val center = Vector(randFloat(rightEnd, leftEnd), topEnd/*/* + topMarginForLittleStar*/ * topEnd * (float) random()*/)
        val littleStar = /*if (rocket.initialSpeed == 0f)
            LittleStar(YELLOW, centerX, centerY, 1f,
                (distance(centerX, centerY, centerOfRotationX, centerOfRotationY) / speedFormula(1f / 1000f, LittleStar.score) * 2).toLong(), nowXY, layers)
        else*/
            LittleStar(YELLOW, center, 1f,
                    (distance(center, centerOfRotation) / speedFormula(2f/ 1000f, LittleStar.score) * 2).toLong(), now, layers)

        var finished: Boolean
        while (true) {
            finished = true
            for (planet in planets) {
                if (littleStar.isTooCloseToAPlanet(planet, minDistantBetweenPlanet / 4)) {
                    finished = false
                    break
                }
            }
            if (finished) {
                flybysInThisYellowStar = 0
                return littleStar
            } else {
                littleStar.resetPosition(Vector(randFloat(rightEnd, leftEnd), topEnd/* + topMarginForLittleStar * topEnd * random().toFloat()*/))
            }
        }
    }
    
    private var flybysInThisYellowStar = 0

    fun isCrashed(overlappers: Array<Overlapper>): Overlapper? {
        // find the closest planet
        // initialize to the first planet
        var closestPlanet = planets[0]
        var minDistance = distance(planets[0].center, centerOfRotation)
        for (i in 1 until planets.size) {
            val d = distance(planets[i].center, centerOfRotation)
            if (d < minDistance) {
                closestPlanet = planets[i]
                minDistance = d
            }
        }
    
        for (overlapper in overlappers) {
            if (closestPlanet.isOverlap(overlapper)) { // if does overlap
                return overlapper
            }
        }
        return null
    }

    fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long) {
        // move the planets down by y (y is decided by plane.movePlane())
        for (planet in planets)
            planet.rotatePlanet(centerOfRotation, angle)
        for (littleStar in littleStars)
            littleStar.rotateLittleStar(centerOfRotation, angle)
    }


    private var lastUsedPlanet: Int = 0
    private fun getRandomPlanet(): Planet {
        for (i in planetsStore.indices) {
            lastUsedPlanet++// get the next random planet
            lastUsedPlanet %= planetsStore.size
            val planet = planetsStore[lastUsedPlanet]
            if (!planet.isInUse) {
                // if it's not in use then use it
                planet.isInUse = true
                return planet
            }
        }
        // actually run out of planets....
        throw IndexOutOfBoundsException("run out of planet?!")
//            return generateRandomPlanet(0f, 0f, generateRadius(), 10f)
    }

    private fun generateRadius(): Float {
        return randFloat(averageRadius + radiusMargin, averageRadius - radiusMargin)
    }
}

class SurroundingResources(val background: Array<Shape>, val planetsStore: Array<Planet>)

private const val radiusMargin = 0.25f
private const val averageRadius = 0.75f // for planet shape to determent which type of planet suits the size best.
private const val flybyDistance = 0.5f
private val maxCloseDist = sqrt(square(flybyDistance) + 2 * (averageRadius + radiusMargin) * flybyDistance) * 2
private val maxFlybySpeed = speedFormula(0.004f, 200)
private val timeLimit = maxCloseDist / maxFlybySpeed

// this class takes a planetShape and manage it, make it flybyable and reusable.
// this is done to weaken the coupling between PlanetShapes and BasicSurrounding
class Planet(private val planetShape: PlanetShape): IReusable, IFlybyable {

    var visibility: Boolean
        get() = planetShape.visibility
        set(value) { planetShape.visibility = value }

    // at first the planet haven't been flybyed and the close time is zero
    private var flybyable = true
    private var closeTime = 0L
    override fun checkFlyby(rocket: Rocket, frameDuration: Long): Boolean {
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
        planetShape.moveShape(dCenter)
        if (angleRotated != 0f) {
            planetShape.rotateShape(center, angleRotated)
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