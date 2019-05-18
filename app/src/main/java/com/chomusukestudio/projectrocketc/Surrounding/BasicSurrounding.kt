package com.chomusukestudio.projectrocketc.Surrounding

import android.widget.TextView
import com.chomusukestudio.projectrocketc.GLRenderer.*

import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.JupiterShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.MarsShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.PlanetShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.SaturnShape
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.StarShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr

import java.util.ArrayList

import com.chomusukestudio.projectrocketc.Shape.coordinate.distance
import com.chomusukestudio.projectrocketc.State
import com.chomusukestudio.projectrocketc.TouchableView
import com.chomusukestudio.projectrocketc.giveVisualText
import com.chomusukestudio.projectrocketc.littleStar.LittleStar.Color.YELLOW
import java.lang.Math.*

/**
 * Created by Shuang Li on 31/03/2018.
 */

class BasicSurrounding(private val visualTextView: TouchableView<TextView>, private val layers: Layers, resources: SurroundingResources?) : Surrounding() {
    private val planets = ArrayList<Planet>() // this defines where the plane can't go
    // planets should have z value of 10 while background should have a z value higher than 10, like 11.
    private var backgrounds: ArrayList<Shape> // backGrounds doesn't effect plane
    private val littleStars = ArrayList<LittleStar>()
    private lateinit var startingPathOfRocket: QuadrilateralShape
    override lateinit var rocket: Rocket

    override val centerOfRotationX: Float = 0f
    override val centerOfRotationY: Float = 0f
    override val rotation: Float = 0f

    private val minDistantBetweenPlanet = 4f

    private val topMarginForLittleStar = 1f
    private lateinit var newPlanet: Planet
    private var numberOfRedStar = 0

    private var displacementX: Double = 0.0
    private var displacementY: Double = 0.0 // displacement since last makeNewTriangleAndRemoveTheOldOne()
    private val parallelForIForBackgroundStars = ParallelForI(8, "move background")

    private val NUMBER_OF_STARS = 6000

    private val NUMBER_OF_PLANET = 1000
    private lateinit var planetsStore: Array<Planet>
    private fun fillUpPlanets(layers: Layers) {
        planetsStore = Array(NUMBER_OF_PLANET) {
            val planetShape = generateRandomPlanet(100f, 100f, generateRadius(), 10f, layers)
            planetShape.isInUse = false
            return@Array planetShape
        }
    }

    private val RADIUS_MARGIN = 0.5f
    private val AVERAGE_RADIUS = 0.75f// for planet shape to determent which type of planet suits the size best.

    private fun generateRandomPlanet(centerX: Float, centerY: Float, radius: Float, z: Float, layers: Layers): Planet {
        val randomPlanetShape: PlanetShape
        if (radius < AVERAGE_RADIUS) {
//            val timeStarted = SystemClock.uptimeMillis()
            randomPlanetShape = MarsShape(centerX, centerY, radius, BuildShapeAttr(z, false, layers))
//            Log.v("time take for newPlanet", "mars " + (SystemClock.uptimeMillis() - timeStarted))
        } else if (radius < AVERAGE_RADIUS + RADIUS_MARGIN / 3) {
//            val timeStarted = SystemClock.uptimeMillis()
            val ringA = ((1.5 + random() * 0.2) * radius).toFloat()
            randomPlanetShape = SaturnShape(ringA, (0.1 + 0.5 * random()).toFloat() * ringA, 1.2f * radius, (3 * random() + 3).toInt(), centerX, centerY, radius, BuildShapeAttr(z, false, layers))
            //            randomPlanetShape = new SaturnShape(ringA, (float) (0.1 + 0.5 * random()) * ringA, (0.67f + 0.2f*(float)random()) * ringA, (int) (3 * random() + 3), centerX, centerY, radius, z);
//            Log.v("time take for newPlanet", "saturn " + (SystemClock.uptimeMillis() - timeStarted))
        } else {
//            val timeStarted = SystemClock.uptimeMillis()
            randomPlanetShape = JupiterShape(centerX, centerY, radius, BuildShapeAttr(z, false, layers))
//            Log.v("time take for newPlanet", "jupiter " + (SystemClock.uptimeMillis() - timeStarted))
        }
        randomPlanetShape.rotateShape(centerX, centerY, (random() * 2.0 * PI).toFloat())
        return Planet(randomPlanetShape)
    }

    init {
        if(resources is BasicSurroundingResources) {
            planetsStore = resources.planetsStore
            backgrounds = resources.background
        } else {
            fillUpPlanets(layers)
            // initialize all those stars in the backgrounds
            backgrounds = ArrayList(NUMBER_OF_STARS)
            for (i in 0 until NUMBER_OF_STARS) {
                backgrounds.add(StarShape(random().toFloat() * (leftEnd - rightEnd) + rightEnd,
                        random().toFloat() * (topEnd - bottomEnd) + bottomEnd,
                        (random() * random() * random()).toFloat() * 255f / 256f + 1f / 256f, random().toFloat() * 0.3f, BuildShapeAttr(11f, true, layers)))
            }
        }

        LittleStar.setCenterOfRocket(centerOfRotationX, centerOfRotationY)
    }

    override fun initializeSurrounding(rocket: Rocket, state: State) {
        // pass the rocket to the surrounding so surrounding can do stuff such as setCenterOfRotation

        this.rocket = rocket
        startingPathOfRocket = QuadrilateralShape(centerOfRotationX - (rocket.width / 2 + flybyDistance), 1000000000f,
                centerOfRotationX + (rocket.width / 2 + flybyDistance), 1000000000f, // max value is bad because it causes overflow... twice
                centerOfRotationX + (rocket.width / 2 + flybyDistance), centerOfRotationY,
                centerOfRotationX - (rocket.width / 2 + flybyDistance), centerOfRotationY,
                0f, 1f, 0f, 1f, BuildShapeAttr(0f, false, layers))
        startingPathOfRocket.rotateShape(centerOfRotationX, centerOfRotationY, rotation)


        val avoidDistanceX = 110f // to avoid constant change of visibility
        startingPathOfRocket.moveShape(avoidDistanceX, 0f) // i'll move it back later
        newPlanet = getRandomPlanet()
        // initialize surrounding
        for (i in 0..256) {
            // randomly reposition the new planet
            val centerX = (random() * (leftEnd * 1.5f - rightEnd * 1.5f) + rightEnd * 1.5f).toFloat() + avoidDistanceX // + avoidDistanceX as to avoid constant change of visibility
            val centerY = (random() * (topEnd * (1.5f/*/* + topMarginForLittleStar*/*/) - bottomEnd * 1.5f) + bottomEnd * 1.5f).toFloat()
            newPlanet.resetPosition(centerX, centerY)

            if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet

                planets.add(newPlanet)// use the random new planet

                // create another random new planet for next use
                newPlanet = getRandomPlanet()
            }
        }
        for (boundary in planets)
            boundary.movePlanet(-avoidDistanceX, 0f) // move planets back
        startingPathOfRocket.moveShape(-avoidDistanceX, 0f) // move startingPathOfRocket back
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
            if (planet.isOverlap(startingPathOfRocket))
                return false// if it blocks the rocket before start
        return true
    }

    override fun makeNewTriangleAndRemoveTheOldOne(now: Long, previousFrameTime: Long, state: State) {
        //        if ( ! (-1 < displacementX && displacementX < 1) || ! (-1 < displacementY && displacementY < 1)) {
        //            // only go through the whole thing when displacementX or displacementY is bigger than 1
        // or not so all these work don't get to pile up to one frame

        //        minDistantBetweenPlanet = (float) (4f - log(LittleStar.Companion.getScore() + 1)/log(65536));

        // to delete
        var i = 0
        while (i < planets.size) { // planets
            if (planets[i].centerY < bottomEnd * 30f ||
                    planets[i].centerY > topEnd * 30f ||
                    planets[i].centerX < rightEnd * 30f ||
                    planets[i].centerX > leftEnd * 30f) {
                planets.removeAt(i).isInUse = false
                i--// this one is removed so the next one would have a index of i
            }
            i++
        }

        // to create
        if (random() < abs(displacementX * (topEnd * (1.5f/*/* + topMarginForLittleStar*/*/) - bottomEnd * 1.5f) / (displacementY * (leftEnd * 1.5f - rightEnd * 1.5f))) /
                (1 + abs(displacementX * (topEnd * (1.5f/* + topMarginForLittleStar*/) - bottomEnd * 1.5f) / (displacementY * (leftEnd * 1.5f - rightEnd * 1.5f))))) {

            if (displacementX < 0) {
                // if need to create on positive side
                // put the random planet on the positive side
                newPlanet.resetPosition((leftEnd * 1.5f + random() * displacementX).toFloat(), (random() * (topEnd * (1.5f/* + topMarginForLittleStar*/) - bottomEnd * 1.5f) + bottomEnd * 1.5f).toFloat())

                if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet
                    planets.add(newPlanet)// use the random new planet
                    // create another random new planet for next use
                    newPlanet = getRandomPlanet()
                }
            } else {
                // if need to create on negative side
                // put the random planet on the negative side
                newPlanet.resetPosition((rightEnd * 1.5f + random() * displacementX).toFloat(), (random() * (topEnd * (1.5f/* + topMarginForLittleStar*/) - bottomEnd * 1.5f) + bottomEnd * 1.5f).toFloat())

                if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet
                    planets.add(newPlanet)// use the random new planet
                    // create another random new planet for next use
                    newPlanet = getRandomPlanet()
                }
            }
        } else {
            if (displacementY < 0) {
                // if need to create on positive side
                // put the random planet on the positive side
                newPlanet.resetPosition((random() * (leftEnd * 1.5f - rightEnd * 1.5f) + rightEnd * 1.5f).toFloat(), (topEnd * (1.5f/* + topMarginForLittleStar*/) + random() * displacementY).toFloat())

                if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet
                    planets.add(newPlanet) // use the random new planet
                    // create another random new planet for next use
                    newPlanet = getRandomPlanet()
                }
            } else {
                // if need to create on negative side
                // put the random planet on the positive side
                newPlanet.resetPosition((random() * (leftEnd * 1.5f - rightEnd * 1.5f) + rightEnd * 1.5f).toFloat(), (bottomEnd * 1.5f + random() * displacementY).toFloat())

                if (isGoodPlanet(newPlanet, state)) {// it is not too close to any other planet
                    planets.add(newPlanet)// use the random new planet
                    // create another random new planet for next use
                    newPlanet = getRandomPlanet()
                }
            }
        }
        // reset displacement
        displacementX = 0.0
        displacementY = 0.0

    }

    private fun attractLittleStar(now: Long, previousFrameTime: Long) {
        for (littleStar in littleStars)
            littleStar.attractLittleStar(centerOfRotationX, centerOfRotationY, now, previousFrameTime, 0.00002f)
    }

    override fun removeAllShape() {

        for (planet in planets) {
            planet.isInUse = false
        }
        newPlanet.isInUse = false

        // move backgrounds so people can't recognize it's the same stars parallelForIForBackgroundStars.waitForLastRun()
        parallelForIForBackgroundStars.run({ i ->
            val starShape = backgrounds[i] as StarShape

            starShape.moveStarShape(3.1415926535f, 3.1415926535897932354626f)
            // those that got out of screen, make them appear on the opposite side
            if (starShape.centerY < bottomEnd)
                starShape.resetPosition(starShape.centerX, starShape.centerY + (topEnd - bottomEnd))
            if (starShape.centerY > topEnd)
                starShape.resetPosition(starShape.centerX, starShape.centerY - (topEnd - bottomEnd))
            if (starShape.centerX < rightEnd)
                starShape.resetPosition(starShape.centerX + (leftEnd - rightEnd), starShape.centerY)
            if (starShape.centerX > leftEnd)
                starShape.resetPosition(starShape.centerX - (leftEnd - rightEnd), starShape.centerY)
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

    override fun trashAndGetResources(): SurroundingResources? {
        return BasicSurroundingResources(backgrounds, planetsStore)
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
    override fun moveSurrounding(dx: Float, dy: Float, now: Long, previousFrameTime: Long) {
        parallelForIForBackgroundStars.waitForLastRun()

        // move background
        parallelForIForBackgroundStars.run({ i ->
            val starShape = backgrounds[i] as StarShape

            starShape.moveStarShape(dx, dy)
            // those that got out of screen, make them appear on the opposite side
            if (starShape.centerY < bottomEnd)
                starShape.resetPosition(starShape.centerX, starShape.centerY + (topEnd - bottomEnd))
            if (starShape.centerY > topEnd)
                starShape.resetPosition(starShape.centerX, starShape.centerY - (topEnd - bottomEnd))
            if (starShape.centerX < rightEnd)
                starShape.resetPosition(starShape.centerX + (leftEnd - rightEnd), starShape.centerY)
            if (starShape.centerX > leftEnd)
                starShape.resetPosition(starShape.centerX - (leftEnd - rightEnd), starShape.centerY)
        }, backgrounds.size)

//        val visibleBoundaries = ArrayList<Shape>()
//        for (planet in planets) {
//            if (planet.visibility)
//                visibleBoundaries.add(planet)
//            else
//                planet.moveStarShape(dx, dy)
//        }
//        parallelForIForMoveBoundaries.run({ i ->
//            visibleBoundaries[i].moveStarShape(dx, dy)
//        }, visibleBoundaries.size)
//        parallelForIForBackgroundStars.waitForLastRun()

        for (i in planets.indices)
            planets[i].movePlanet(dx, dy)
        for (littleStar in littleStars)
            littleStar.moveLittleStar(dx, dy)

        displacementX += dx.toDouble()
        displacementY += dy.toDouble()
        // refresh displacement since last makeNewTriangleAndRemoveTheOldOne()

        attractLittleStar(now, previousFrameTime)

    }

    override fun checkAndAddLittleStar(now: Long) { // this get called every frame
        // you trust state will be inGame or processing thread won't check
        if (littleStars.size == 0) {
            littleStars.add(oneNewLittleStar(now)) // if started and there is no little star in surrounding, then add one.
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
        val centerX = random().toFloat() * (leftEnd - rightEnd) + rightEnd
        val centerY = topEnd/*/* + topMarginForLittleStar*/ * topEnd * (float) random()*/
        val littleStar = LittleStar(YELLOW, centerX, centerY, 1f,
                (distance(centerX, centerY, centerOfRotationX, centerOfRotationY) / rocket.speed * 2).toLong(), now, layers)

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
                littleStar.resetPosition(random().toFloat() * (leftEnd - rightEnd) + rightEnd, topEnd/* + topMarginForLittleStar * topEnd * random().toFloat()*/)
            }
        }
    }

    private val parallelForIForIsCrashed = ParallelForI(8, "is crashed")
//    private var closeLastFrame = false
//    private var distanceLastFrame: Float = 0f
    @Volatile
//    private var closeThisFrame = false
//    private var distanceThisFrame: Float = 0f
    private var flybyDistance = 0.5f
    private var flybysInThisYellowStar = 0
    private var flybyPlanet: Planet? = null

    @Volatile
    private var crashedShape: Shape? = null

    override fun isCrashed(shapeForCrashAppro:Shape, components: Array<Shape>): Shape? {
        crashedShape = null
        val planetsNeedToBeChecked = ArrayList<Planet>(100)
        for (planet in planets) {
            if (planet.visibility) { // rocket can only hit on visibility stuff
                planetsNeedToBeChecked.add(planet)
            }
        }
        parallelForIForIsCrashed.run({ i ->
            val planetShape = planetsNeedToBeChecked[i] as Planet
            if (planetShape.isOverlap(shapeForCrashAppro)) {
                // only check it when it's close
                for (component in components) {
                    if (planetShape.isOverlap(component)) { // if does overlap
                        crashedShape = component
                    }
                }
            }
        }, planetsNeedToBeChecked.size)
        // no need for improvement for immediate return true, most of the time there will not be any overlap.
        parallelForIForIsCrashed.waitForLastRun()

        // see if precision flyby is complete
//        if (closeLastFrame && !flybyPlanet!!.flybyed && distanceThisFrame > distanceLastFrame) {
//        if (flybyPlanet!!.checkFlyby(rocket)) {
        if (false) {
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
//        closeLastFrame = closeThisFrame
//        distanceLastFrame = distanceThisFrame

        return crashedShape
    }

    override fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long) {
        // move the planets down by y (y is decided by plane.movePlane())
        for (planet in planets)
            planet.rotatePlanet(centerOfRotationX, centerOfRotationY, angle)
        for (littleStar in littleStars)
            littleStar.rotateLittleStar(centerOfRotationX, centerOfRotationY, angle)
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
        return random().toFloat() * RADIUS_MARGIN + AVERAGE_RADIUS - RADIUS_MARGIN / 2
    }
}

class BasicSurroundingResources(val background: ArrayList<Shape>, val planetsStore: Array<Planet>): SurroundingResources()
