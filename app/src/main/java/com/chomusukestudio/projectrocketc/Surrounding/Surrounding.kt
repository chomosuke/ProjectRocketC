package com.chomusukestudio.projectrocketc.Surrounding

import com.chomusukestudio.prcandroid2dgameengine.distance
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.randFloat
import com.chomusukestudio.prcandroid2dgameengine.shape.*
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.prcandroid2dgameengine.square
import com.chomusukestudio.prcandroid2dgameengine.threadClasses.ParallelForI
import com.chomusukestudio.projectrocketc.PlanetShape.*
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Rocket.speedFormula
import com.chomusukestudio.projectrocketc.TouchableView
import com.chomusukestudio.projectrocketc.UI.MainActivity
import com.chomusukestudio.projectrocketc.UI.State
import com.chomusukestudio.projectrocketc.giveVisualText
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.littleStar.LittleStar.Color.YELLOW
import java.lang.Math.random
import java.util.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Created by Shuang Li on 31/03/2018.
 */

class Surrounding(private val mainActivity: MainActivity, private val drawData: DrawData, resources: SurroundingResources? = null) {
    private val planets = ArrayList<Planet>() // this defines where the rocket can't go
    // rockets should have z value of 10 while background should have a z value higher than 10, like 11.
    private val backgrounds = // backGrounds doesn't effect plane
            if (resources is SurroundingResources)
                resources.background
            else {
                // initialize all those stars in the backgrounds
                Array<Shape>(6000) { // 6000 stars in the background
                    return@Array StarShape(Vector(randFloat(leftEnd, rightEnd), randFloat(topEnd, bottomEnd)),
                            (random() * random() * random()).toFloat() * 255f / 256f + 1f / 256f,
                            random().toFloat() * 0.3f, BuildShapeAttr(1f, true, drawData))
                }
            }
    private val littleStars = ArrayList<LittleStar>()
    private lateinit var startingPathOfRocket: Shape

    private val leftEnd inline get() = drawData.leftEnd
    private val rightEnd inline get() = drawData.rightEnd
    private val topEnd inline get() = drawData.topEnd
    private val bottomEnd inline get() = drawData.bottomEnd

    private var rocketField: Rocket? = null
    var rocket: Rocket
        set(value) {
            rocketField = value
            value.setRotationAndCenter(centerOfRotation, rotation)
        }
        get() = rocketField!!

    val centerOfRotation = Vector(0f, 0f)
    val rotation = PI.toFloat() / 2

    private val minDistantBetweenPlanet = 3.8f

    private lateinit var newPlanet: Planet
    private var numberOfRedStar = 0

    private var displacement = Vector(0f, 0f) // displacement since last makeNewTriangleAndRemoveTheOldOne()
    private val parallelForIForBackgroundStars = ParallelForI(8, "move background")

    private val planetsStore: Array<Planet> =
            if(resources is SurroundingResources)
                resources.planetsStore
            else {
                // 1000 planet in store waiting for use
                val planets = arrayOfNulls<Planet>(1000)
                
                val parallelForI = ParallelForI(16, "fill planetsStore")
                parallelForI.run({
                    planets[it] = generateRandomPlanet(Vector(100f, 100f), generateRadius(), 0f, drawData)
                    planets[it]!!.isInUse = false
                }, 1000)
                parallelForI.waitForLastRun()
                
                planets as Array<Planet>
            }

    private fun generateRandomPlanet(center: Vector, radius: Float, z: Float, drawData: DrawData): Planet {
        val randomPlanetShape: PlanetShape
        if (radius < averageRadius) {
//            val timeStarted = SystemClock.uptimeMillis()
            randomPlanetShape = MarsShape(center, radius, BuildShapeAttr(z, false, drawData))
//            Log.v("time take for newPlanet", "mars " + (SystemClock.uptimeMillis() - timeStarted))
        } else if (radius < averageRadius + radiusMargin * 0.6) {
//            val timeStarted = SystemClock.uptimeMillis()
            val ringA = randFloat(1.5f, 1.7f) * radius
            randomPlanetShape = SaturnShape(ringA, randFloat(0.1f, 0.6f) * ringA,
                    1.2f * radius, randFloat(3f, 6f).toInt(), center, radius, BuildShapeAttr(z, false, drawData))
            //            randomPlanetShape = new SaturnShape(ringA, (float) (0.1 + 0.5 * random()) * ringA, (0.67f + 0.2f*(float)random()) * ringA, (int) (3 * random() + 3), centerX, centerY, radius, z);
//            Log.v("time take for newPlanet", "saturn " + (SystemClock.uptimeMillis() - timeStarted))
        } else {
//            val timeStarted = SystemClock.uptimeMillis()
            randomPlanetShape = JupiterShape(center, radius, BuildShapeAttr(z, false, drawData))
//            Log.v("time take for newPlanet", "jupiter " + (SystemClock.uptimeMillis() - timeStarted))
        }
        randomPlanetShape.rotate(center, (random() * 2.0 * PI).toFloat())
        return Planet(randomPlanetShape, drawData)
    }

    init {
        LittleStar.setCenterOfRocket(centerOfRotation)
    }

    private val topPlanetBoundary = topEnd * (1.5f/* + topMarginForLittleStar*/)
    private val bottomPlanetBoundary = bottomEnd * 1.5f
    private val leftPlanetBoundary = leftEnd * 1.5f
    private val rightPlanetBoundary = rightEnd * 1.5f

    fun initializeSurrounding(rocket: Rocket, state: State) {
        // pass the rocket to the surrounding so surrounding can do stuff such as setCenterOfRotation

        this.rocket = rocket

//        val minCloseDist = 0.004f * timeLimit
        val initialFlybyDistance = 1f//sqrt(square(flybyDistance + averageRadius) - square(minCloseDist))/*rocket.width / 2 + flybyDistance*/
//        if (rocket.initialSpeed != 0f) {
            startingPathOfRocket = QuadrilateralShape(Vector(centerOfRotation.x - initialFlybyDistance, 100000000f),
                    Vector(centerOfRotation.x + initialFlybyDistance, 100000000f), // max value is bad because it causes overflow... twice
                            Vector(centerOfRotation.x + initialFlybyDistance, centerOfRotation.y - initialFlybyDistance),
                                    Vector(centerOfRotation.x - initialFlybyDistance, centerOfRotation.y - initialFlybyDistance),
                    Color(0f, 1f, 0f, 1f), BuildShapeAttr(0f, false, drawData))
            startingPathOfRocket.rotate(centerOfRotation, rotation - PI.toFloat()/2)
//        } else {
//            startingPathOfRocket = QuadrilateralShape(centerOfRotationX - initialFlybyDistance, centerOfRotationY + initialFlybyDistance + 1f,
//                    centerOfRotationX + initialFlybyDistance, centerOfRotationY + initialFlybyDistance + 1f,
//                    centerOfRotationX + initialFlybyDistance, centerOfRotationY - initialFlybyDistance,
//                    centerOfRotationX - initialFlybyDistance, centerOfRotationY - initialFlybyDistance,
//                    0f, 1f, 0f, 1f, BuildShapeAttr(0f, false, allLayers.shapeLayers))
//        }


        val avoidDistance = Vector(110f, 0f) // to avoid constant change of visibility
        startingPathOfRocket.move(avoidDistance) // i'll move it back later
        newPlanet = getRandomPlanet()
        // initialize surrounding
        val iMax = ((topPlanetBoundary - bottomPlanetBoundary) * (leftPlanetBoundary - rightPlanetBoundary)).toInt().absoluteValue
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
        startingPathOfRocket.move(-avoidDistance) // move startingPathOfRocket back
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

        val newAreaLR = abs(displacement.x * (topPlanetBoundary - bottomPlanetBoundary))
        val newAreaTB = abs(displacement.y * (leftPlanetBoundary - rightPlanetBoundary))
        if (newAreaLR + newAreaTB > 1) {
            // only place more planets when new ground covered is bigger than 1
            // to make planet position look more random, and consistency for different frame rates

            // to create
            val rand = random()
            if (rand * newAreaLR > (1 - rand) * newAreaTB) {

                if (displacement.x.sign == (rightPlanetBoundary - leftPlanetBoundary).sign) {
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
                if (displacement.y.sign == (bottomPlanetBoundary - topPlanetBoundary).sign) {
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
                if (planet.
                        checkFlyby(rocket, frameDuration)) {
                    flybysInThisYellowStar++
            
                    LittleStar.dScore = (LittleStar.dScore + (flybysInThisYellowStar * rocket.rocketQuirks.flybyDelta))
                    
                    giveVisualText("δ+" + (flybysInThisYellowStar * rocket.rocketQuirks.flybyDelta),
                            TouchableView(mainActivity.findViewById(R.id.visualText), mainActivity))
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
                littleStars[i].eatLittleStar(mainActivity)
                when (littleStars[i].COLOR) {
                    YELLOW -> {
                        LittleStar.dScore = LittleStar.dScore + rocket.rocketQuirks.eatLittleStarDelta
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
                (distance(centerX, centerY, centerOfRotationX, centerOfRotationY) / speedFormula(1f / 1000f, LittleStar.score) * 2).toLong(), nowXY, allLayers)
        else*/
            LittleStar(YELLOW, center, 1f,
                    (distance(center, centerOfRotation) / speedFormula(2f/ 1000f, LittleStar.score) * 2).toLong(), now, drawData)

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

    fun isCrashed(overlappers: Array<Overlapper>): ArrayList<Overlapper> {
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
    
        val crashedOverlappers = ArrayList<Overlapper>()
        for (overlapper in overlappers) {
            if (closestPlanet.isOverlap(overlapper)) { // if does overlap
                crashedOverlappers.add(overlapper)
            }
        }
        return crashedOverlappers
    }

    fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long) {
        // move the planets down by y (y is decided by plane.movePlane())
        for (planet in planets)
            planet.rotatePlanet(centerOfRotation, angle)
        for (littleStar in littleStars)
            littleStar.rotateLittleStar(centerOfRotation, angle)
    }


    private var lastUsedPlanet = Random.nextInt(planetsStore.size)
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
//private const val flybyDistance = 0.25f//0.5f
//private val maxCloseDist = sqrt(square(flybyDistance) + 2 * (averageRadius + radiusMargin) * flybyDistance) * 2
//private val maxFlybySpeed = speedFormula(0.003f, 500)
//private val timeLimit = 1L//(maxCloseDist / maxFlybySpeed).toLong()