package com.chomusukestudio.projectrocketc.Surrounding

import android.media.MediaPlayer
import android.util.Log
import android.widget.TextView

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

import java.util.ArrayList

import com.chomusukestudio.projectrocketc.Shape.coordinate.distance
import com.chomusukestudio.projectrocketc.State
import com.chomusukestudio.projectrocketc.TouchableView
import com.chomusukestudio.projectrocketc.giveVisualText
import com.chomusukestudio.projectrocketc.littleStar.LittleStar.Color.YELLOW
import com.chomusukestudio.projectrocketc.state
import com.chomusukestudio.projectrocketc.upTimeMillis
import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.random

/**
 * Created by Shuang Li on 31/03/2018.
 */

class BasicSurrounding(private var leftEnd: Float, private var rightEnd: Float,
                       private var bottomEnd: Float, private var topEnd: Float,
                       private val visualTextView: TouchableView<TextView>) : Surrounding() {
    override fun setLeftRightBottomTopEnd(leftEnd: Float, rightEnd: Float, bottomEnd: Float, topEnd: Float) {
        this.leftEnd = leftEnd
        this.rightEnd = rightEnd
        this.bottomEnd = bottomEnd
        this.topEnd = topEnd
        if (this.rightEnd > this.leftEnd) {
            val temp = this.rightEnd
            this.rightEnd = this.leftEnd
            this.leftEnd = temp
        } // rightEnd smaller than leftEnd, topEnd bigger than bottomEnd
        if (bottomEnd > topEnd) {
            val temp = topEnd
            this.topEnd = bottomEnd
            this.bottomEnd = temp
        } // basic surrounding (and maybe others) need it

        LittleStar.setENDs(this.leftEnd, this.rightEnd, this.bottomEnd, this.topEnd)

//        PlanetShape.setENDs(this.leftEnd * 1.5f - 0.1f, this.rightEnd * 1.5f + 0.1f, this.bottomEnd * 1.5f + 0.1f, this.topEnd * 1.5f - 0.1f)
////         0.1f so newPlanet will not keep changing visibility
        PlanetShape.setENDs(this.leftEnd, this.rightEnd, this.bottomEnd, this.topEnd)
    }

    private val boundaries = ArrayList<PlanetShape>() // this defines where the plane can't go
    // boundaries should have z value of 10 while background should have a z value higher than 10, like 11.
    private lateinit var backgrounds: ArrayList<Shape> // backGrounds doesn't effect plane
    private val littleStars = ArrayList<LittleStar>()
    private lateinit var startingPathOfRocket: QuadrilateralShape
    override lateinit var rocket: Rocket
    
    override val centerOfRotationX: Float = 0f
    override val centerOfRotationY: Float = 0f
    override val rotation: Float = 0f
    
    private val minDistantBetweenPlanet = 4f
    
    private val topMarginForLittleStar = 1f
    private lateinit var newPlanet: PlanetShape
    private var numberOfRedStar = 0
    
    private var displacementX: Double = 0.0
    private var displacementY: Double = 0.0 // displacement since last makeNewTriangleAndRemoveTheOldOne()
    private val parallelForIForBackgroundStars = ParallelForI(8, "move background")
    
    private var numberOfYellowStarEatenSinceLastRedStar = 0
    
    init {
        setLeftRightBottomTopEnd(leftEnd, rightEnd, bottomEnd, topEnd)
        
        LittleStar.setCenterOfRocket(centerOfRotationX, centerOfRotationY)
    }
    
    override fun initializeSurrounding(rocket: Rocket) {
        // pass the rocket to the surrounding so surrounding can do stuff such as setCenterOfRotation

        this.rocket = rocket
        startingPathOfRocket = QuadrilateralShape(centerOfRotationX - (rocket.width / 2 + flybyDistance), 1000000000f,
                centerOfRotationX + (rocket.width / 2 + flybyDistance), 1000000000f, // max value is bad because it causes overflow... twice
                centerOfRotationX + (rocket.width / 2 + flybyDistance), centerOfRotationY,
                centerOfRotationX - (rocket.width / 2 + flybyDistance), centerOfRotationY,
                0f, 1f, 0f, 1f, 10f, false) // z is 10 because this is the most common use of z therefore are least likely to create a new layer.
        startingPathOfRocket.rotateShape(centerOfRotationX, centerOfRotationY, rotation)

        // initialize all those stars in the backgrounds
        if (starsBackground == null) { // first time initialize
            backgrounds = ArrayList(NUMBER_OF_STARS)
            for (i in 0 until NUMBER_OF_STARS) {
                backgrounds.add(StarShape(random().toFloat() * (leftEnd - rightEnd) + rightEnd,
                        random().toFloat() * (topEnd - bottomEnd) + bottomEnd,
                        (random() * random() * random()).toFloat() * 255f / 256f + 1f / 256f, random().toFloat() * 0.3f, 11f, true))
            }
            starsBackground = backgrounds
        } else {
            backgrounds = starsBackground as ArrayList<Shape>
        }

        val avoidDistanceX = 110f // to avoid constant change of visibility
        startingPathOfRocket.moveShape(avoidDistanceX, 0f) // i'll move it back later
        newPlanet = getRandomPlanetShape()
        // initialize surrounding
        for (i in 0..256) {
            // randomly reposition the new planet
            val centerX = (random() * (leftEnd * 1.5f - rightEnd * 1.5f) + rightEnd * 1.5f).toFloat() + avoidDistanceX // + avoidDistanceX as to avoid constant change of visibility
            val centerY = (random() * (topEnd * (1.5f/*/* + topMarginForLittleStar*/*/) - bottomEnd * 1.5f) + bottomEnd * 1.5f).toFloat()
            newPlanet.resetPosition(centerX, centerY)

            if (isGoodPlanet(newPlanet)) {// it is not too close to any other planet

                boundaries.add(newPlanet)// use the random new planet

                // create another random new planet for next use
                newPlanet = getRandomPlanetShape()
            }
        }
        for (boundary in boundaries)
            boundary.moveShape(-avoidDistanceX, 0f) // move planets back
        startingPathOfRocket.moveShape(-avoidDistanceX, 0f) // move startingPathOfRocket back

        // initialize this for flyby
        rectangleForFlyby = QuadrilateralShape(centerOfRotationX + (rocket.width / 2 + flybyDistance),
                centerOfRotationY + 0.4f,
                centerOfRotationX + (rocket.width / 2 + flybyDistance),
                centerOfRotationY - 0.4f,
                centerOfRotationX - (rocket.width / 2 + flybyDistance),
                centerOfRotationY - 0.4f,
                centerOfRotationX - (rocket.width / 2 + flybyDistance),
                centerOfRotationY + 0.4f, 0f, 1f, 0f, 1f, 0f, false)
    }
    
    private fun isGoodPlanet(planetShape: PlanetShape): Boolean {
        // find out if this planet is too close to other planet
        for (boundary in boundaries) {
            if (planetShape.isTooClose(boundary, minDistantBetweenPlanet))
            // it is too close
                return false
        }
        // it is not too close to any other planet
        if (state != State.InGame)
            if (planetShape.isOverlap(startingPathOfRocket))
                return false// if it blocks the rocket before start
        return true
    }
    
    override fun makeNewTriangleAndRemoveTheOldOne(now: Long, previousFrameTime: Long) {
        //        if ( ! (-1 < displacementX && displacementX < 1) || ! (-1 < displacementY && displacementY < 1)) {
        //            // only go through the whole thing when displacementX or displacementY is bigger than 1
        // or not so all these work don't get to pile up to one frame
        
        //        minDistantBetweenPlanet = (float) (4f - log(LittleStar.Companion.getScore() + 1)/log(65536));
        
        // to delete
        var i = 0
        while (i < boundaries.size) { // boundaries
            if (boundaries[i].centerY < bottomEnd * 30f ||
                    boundaries[i].centerY > topEnd * 30f ||
                    boundaries[i].centerX < rightEnd * 30f ||
                    boundaries[i].centerX > leftEnd * 30f) {
                boundaries.removeAt(i).removePlanet()
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
                    
                    if (isGoodPlanet(newPlanet)) {// it is not too close to any other planet
                        boundaries.add(newPlanet)// use the random new planet
                        // create another random new planet for next use
                        newPlanet = getRandomPlanetShape()
                    }
                } else {
                    // if need to create on negative side
                    // put the random planet on the negative side
                    newPlanet.resetPosition((rightEnd * 1.5f + random() * displacementX).toFloat(), (random() * (topEnd * (1.5f/* + topMarginForLittleStar*/) - bottomEnd * 1.5f) + bottomEnd * 1.5f).toFloat())
                    
                    if (isGoodPlanet(newPlanet)) {// it is not too close to any other planet
                        boundaries.add(newPlanet)// use the random new planet
                        // create another random new planet for next use
                        newPlanet = getRandomPlanetShape()
                    }
                }
            } else {
                if (displacementY < 0) {
                    // if need to create on positive side
                    // put the random planet on the positive side
                    newPlanet.resetPosition((random() * (leftEnd * 1.5f - rightEnd * 1.5f) + rightEnd * 1.5f).toFloat(), (topEnd * (1.5f/* + topMarginForLittleStar*/) + random() * displacementY).toFloat())
        
                    if (isGoodPlanet(newPlanet)) {// it is not too close to any other planet
                        boundaries.add(newPlanet) // use the random new planet
                        // create another random new planet for next use
                        newPlanet = getRandomPlanetShape()
                    }
                } else {
                    // if need to create on negative side
                    // put the random planet on the positive side
                    newPlanet.resetPosition((random() * (leftEnd * 1.5f - rightEnd * 1.5f) + rightEnd * 1.5f).toFloat(), (bottomEnd * 1.5f + random() * displacementY).toFloat())
        
                    if (isGoodPlanet(newPlanet)) {// it is not too close to any other planet
                        boundaries.add(newPlanet)// use the random new planet
                        // create another random new planet for next use
                        newPlanet = getRandomPlanetShape()
                    }
                }
            }
        // reset displacement
        displacementX = 0.0
        displacementY = 0.0
        
    }
    
    private fun attractLittleStar(now: Long, previousFrameTime: Long) {
        for (littleStar in littleStars)
            littleStar.attractLittleStar(centerOfRotationX, centerOfRotationY, now, previousFrameTime, 0.008f * rocket.speed)
    }
    
    override fun removeAllShape() {

        for (boundary in boundaries) {
            boundary.removePlanet()
        }
        newPlanet.removePlanet()

        // move backgrounds so people can't recognize it's the same stars parallelForIForBackgroundStars.waitForLastRun()
        parallelForIForBackgroundStars.run({ i ->
            val starShape = backgrounds[i] as StarShape

            starShape.moveShape(0f, 2f)
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

            starShape.moveShape(dx, dy)
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
//        for (boundary in boundaries) {
//            if (boundary.visibility)
//                visibleBoundaries.add(boundary)
//            else
//                boundary.moveShape(dx, dy)
//        }
//        parallelForIForMoveBoundaries.run({ i ->
//            visibleBoundaries[i].moveShape(dx, dy)
//        }, visibleBoundaries.size)
//        parallelForIForBackgroundStars.waitForLastRun()

    for (i in boundaries.indices)
        boundaries[i].moveShape(dx, dy)
    for (littleStar in littleStars)
        littleStar.moveLittleStar(dx, dy)
//    for (i in boundaries.indices)
//        boundaries[i].moveShape(0f, 0f)
//    for (littleStar in littleStars)
//        littleStar.moveLittleStar(0f, 0f)
        
        displacementX += dx.toDouble()
        displacementY += dy.toDouble()
        // refresh displacement since last makeNewTriangleAndRemoveTheOldOne()
        
        attractLittleStar(now, previousFrameTime)
        
    }
    
    override fun anyLittleStar() { // this get called every frame
        if (littleStars.size == 0 && state == State.InGame) { // can't do this from the UI thread.
            littleStars.add(oneNewLittleStar(true)) // if started and there is no little star in surrounding, then add one.
        }
        for (i in littleStars.indices) {
            if (rocket.isEaten(littleStars[i])) {
                littleStars[i].eatLittleStar(visualTextView)
                when (littleStars[i].COLOR) {
                    YELLOW -> {
                        numberOfYellowStarEatenSinceLastRedStar++
                        LittleStar.dScore = LittleStar.dScore + 1
                    }
                    LittleStar.Color.RED -> numberOfRedStar--
                }
                littleStars.removeAt(i)
            } else if (littleStars[i].timeOut()) {
                when (littleStars[i].COLOR) {
                    YELLOW -> {
                        littleStars.add(oneNewLittleStar(false))
                        LittleStar.dScore = 1
                    }
                    LittleStar.Color.RED -> numberOfRedStar--
                }// can't be Red to prevent cheating the system
                littleStars[i].removeLittleStarShape()
                littleStars.removeAt(i)
            }
        }
    }
    
    private fun oneNewLittleStar(canBeRed: Boolean): LittleStar {
        /* if (random() < (numberOfYellowStarEatenSinceLastRedStar + 1f) / 11f && canBeRed) {
            numberOfYellowStarEatenSinceLastRedStar = 0;
            
           *//* float centerX = (float) random() * (leftEnd - rightEnd) + rightEnd;
            float centerY = topEnd/* + topMarginForLittleStar*/ * topEnd * (float) random();
            LittleStar littleStar = new LittleStar(RED, centerX, centerY, 0f, (long) (distance(centerX, centerY, getCenterOfRotationX(), getCenterOfRotationY()) / rocket.getSpeed() * 1.33));
    
            boolean finished;
            while (true) {
                finished = false;
                for (Shape boundary : boundaries) {
                    if (!littleStar.isTooFarFromAPlanet((PlanetShape) boundary, minDistantBetweenPlanet / 8) &&
                            !littleStar.isOverlap(boundary) &&
                            !littleStar.isTooCloseToAPlanet((PlanetShape) boundary, LittleStarShape.RADIUS_OF_LITTLE_STAR)) {
                        finished = true;
                        break;
                    }
                    if (boundary instanceof SaturnShape) {
                        float[] pointsOutsideX = ((SaturnShape) boundary).getPointsOutsideX();
                        float[] pointsOutsideY = ((SaturnShape) boundary).getPointsOutsideY();
                        for (int i = 0; i < pointsOutsideX.length; i++) {
                            if (distance(pointsOutsideX[i], pointsOutsideY[i], centerX, centerY) < 0.25f + LittleStarShape.RADIUS_OF_LITTLE_STAR) {
                                finished = false;
                                break;
                            }
                        }
                    }
                }
                if (finished) {
                    return littleStar;
                } else {
                    centerX = (float) random() * (leftEnd - rightEnd) + rightEnd;
                    centerY = topEnd/* + topMarginForLittleStar*/ * topEnd * (float) random();
                    littleStar.resetPosition(centerX, centerY);
                    littleStar.setDuration((long) (distance(centerX, centerY, getCenterOfRotationX(), getCenterOfRotationY()) / rocket.getSpeed() * 1.33));
                }
            }*//*
            
            for (Shape boundary : boundaries) {
                PlanetShape pBoundary = (PlanetShape) boundary;
                if (pBoundary.getCenterX() < leftEnd && pBoundary.getCenterX() > rightEnd &&
                        pBoundary.getCenterY() > topEnd) {
                    float centerX;
                    float centerY;
                    
                    // have to put this outside of the loop...
                    boolean tooCloseToPointsOutside;
                    do {
                        float angle = (float) (random() * 2 * PI);
                        final float minMargin = 0*//*minDistantBetweenPlanet / 40f*//*;
                        float margin = (float) random() * minDistantBetweenPlanet / 8f + minMargin;
                        centerX = (float) cos(angle) * (pBoundary.getRadius() + margin + LittleStarShape.RADIUS_OF_LITTLE_STAR) + pBoundary.getCenterX();
                        centerY = (float) sin(angle) * (pBoundary.getRadius() + margin + LittleStarShape.RADIUS_OF_LITTLE_STAR) + pBoundary.getCenterY();
    
                        while(true) {
                            tooCloseToPointsOutside = false;
                            if (pBoundary instanceof SaturnShape) {
                                float[] pointsOutsideX = pBoundary.getPointsOutsideX();
                                float[] pointsOutsideY = pBoundary.getPointsOutsideY();
                                for (int i = 0; i < pointsOutsideX.length; i++) {
                                    if (distance(pointsOutsideX[i], pointsOutsideY[i], centerX, centerY) < ((SaturnShape) pBoundary).getMaxWidth() - pBoundary.getRadius() - margin + LittleStarShape.RADIUS_OF_LITTLE_STAR) {
                                        tooCloseToPointsOutside = true;
                                        break;
                                    }
                                }
                            }
                            if (tooCloseToPointsOutside) {
                                // if too close to ring or whatever
                                // make it further away
                                margin *= 1.01f;
    
                                centerX = (float) cos(angle) * (pBoundary.getRadius() + margin + LittleStarShape.RADIUS_OF_LITTLE_STAR) + pBoundary.getCenterX();
                                centerY = (float) sin(angle) * (pBoundary.getRadius() + margin + LittleStarShape.RADIUS_OF_LITTLE_STAR) + pBoundary.getCenterY();
                            } else {
                                break;
                            }
                        }
    
                    } while (!(centerX < leftEnd && centerX > rightEnd && centerY > topEnd));
                    return new LittleStar(RED, centerX, centerY, 0.1f, (long) (distance(centerX, centerY, getCenterOfRotationX(), getCenterOfRotationY()) / rocket.getSpeed() * 1.33));
                }
            }
            throw new IndexOutOfBoundsException("out of boundary while trying to add Red little star");
        } else */
        val centerX = random().toFloat() * (leftEnd - rightEnd) + rightEnd
        val centerY = topEnd/*/* + topMarginForLittleStar*/ * topEnd * (float) random()*/
        val littleStar = LittleStar(YELLOW, centerX, centerY, minDistantBetweenPlanet / 8f,
                (distance(centerX, centerY, centerOfRotationX, centerOfRotationY) / rocket.speed * 2).toLong())
    
        var finished: Boolean
        while (true) {
            finished = true
            for (boundary in boundaries) {
                if (littleStar.isTooCloseToAPlanet(boundary, minDistantBetweenPlanet / 4)) {
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
    private var closeLastFrame = false
    private var distanceLastFrame: Float = 0f
    @Volatile private var closeThisFrame = false
    private var distanceThisFrame: Float = 0f
    private val flybyDistance = 0.35f
    private lateinit var rectangleForFlyby: QuadrilateralShape
    private var flybysInThisYellowStar = 0
    private var flybyPlanetShape: PlanetShape? = null

    @Volatile private var crashedShape: Shape? = null
    override fun isCrashed(components: Array<Shape>): Shape? {
        crashedShape = null
        val boundariesNeedToBeChecked = ArrayList<Shape>(100)
        for (boundary in boundaries) {
            if (boundary.visibility) { // rocket can only hit on visibility stuff
                boundariesNeedToBeChecked.add(boundary)
            }
        }
//        flybyDistance = minDistantBetweenPlanet / 10f
        rectangleForFlyby.setQuadrilateralShapeCoords(centerOfRotationX + (rocket.width / 2 + flybyDistance),
                centerOfRotationY + 0.5f,
                centerOfRotationX + (rocket.width / 2 + flybyDistance),
                centerOfRotationY - 0.4f,
                centerOfRotationX - (rocket.width / 2 + flybyDistance),
                centerOfRotationY - 0.4f,
                centerOfRotationX - (rocket.width / 2 + flybyDistance),
                centerOfRotationY + 0.5f)
        rectangleForFlyby.rotateShape(centerOfRotationX, centerOfRotationY, rocket.currentRotation)
        closeThisFrame = false
        parallelForIForIsCrashed.run({ i ->
            val planetShape = boundariesNeedToBeChecked[i] as PlanetShape
            
            if (rectangleForFlyby.isOverlap(planetShape)) {
                this.closeThisFrame = true
                distanceThisFrame = distance(centerOfRotationX, centerOfRotationY, planetShape.centerX, planetShape.centerY)
                flybyPlanetShape = planetShape
                for (component in components) {
                    if (planetShape.isOverlap(component)) { // if does overlap
                        crashedShape = component
                    }
                }
            }
        }, boundariesNeedToBeChecked.size)
        // no need for improvement for immediate return true, most of the time there will not be any overlap.
        parallelForIForIsCrashed.waitForLastRun()
        
        // see if precision flyby is complete
        if (closeLastFrame && !flybyPlanetShape!!.flybyed && distanceThisFrame > distanceLastFrame) {
            // can't flyby the same planet twice
            flybyPlanetShape!!.flybyed = true
            
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
        closeLastFrame = closeThisFrame
        distanceLastFrame = distanceThisFrame

        return crashedShape
    }
    
    override fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long) {
        // move the boundaries down by y (y is decided by plane.movePlane())
        for (boundary in boundaries)
            boundary.rotateShape(centerOfRotationX, centerOfRotationY, angle)
        for (littleStar in littleStars)
            littleStar.rotateLittleStar(centerOfRotationX, centerOfRotationY, angle)
    }

    companion object {
        
        private val NUMBER_OF_STARS = 6000
        var starsBackground: ArrayList<Shape>? = null // so it can be reinitialized in MainActivity
        
        private val NUMBER_OF_PLANET = 1000
        private var planetShapes: Array<PlanetShape>? = null
        private lateinit var planetShapesZs: ArrayList<Float>
        fun fillUpPlanetShapes() {
            planetShapes = Array(NUMBER_OF_PLANET) {
                val planetShape = generateRandomPlanetShape(100f, 100f, generateRadius(), 10f)
                planetShape.removePlanet()
                return@Array planetShape
            }
            planetShapesZs = getAllPlanetZs()
        }
        
        private val RADIUS_MARGIN = 0.5f
        private val AVERAGE_RADIUS = 0.75f// for planet shape to determent which type of planet suits the size best.
    
        private fun generateRandomPlanetShape(centerX: Float, centerY: Float, radius: Float, z: Float): PlanetShape {
            val randomPlanetShape: PlanetShape
            if (radius < AVERAGE_RADIUS) {
                val timeStarted = upTimeMillis()
                randomPlanetShape = MarsShape(centerX, centerY, radius, z, false)
                Log.v("time take for newPlanet", "mars " + (upTimeMillis() - timeStarted))
            } else if (radius < AVERAGE_RADIUS + RADIUS_MARGIN / 3) {
                val timeStarted = upTimeMillis()
                val ringA = ((1.5 + random() * 0.2) * radius).toFloat()
                randomPlanetShape = SaturnShape(ringA, (0.1 + 0.5 * random()).toFloat() * ringA, 1.2f * radius, (3 * random() + 3).toInt(), centerX, centerY, radius, z, false)
                //            randomPlanetShape = new SaturnShape(ringA, (float) (0.1 + 0.5 * random()) * ringA, (0.67f + 0.2f*(float)random()) * ringA, (int) (3 * random() + 3), centerX, centerY, radius, z);
                Log.v("time take for newPlanet", "saturn " + (upTimeMillis() - timeStarted))
            } else {
                val timeStarted = upTimeMillis()
                randomPlanetShape = JupiterShape(centerX, centerY, radius, z, false)
                Log.v("time take for newPlanet", "jupiter " + (upTimeMillis() - timeStarted))
            }
            randomPlanetShape.rotateShape(centerX, centerY, (random() * 2.0 * PI).toFloat())
            return randomPlanetShape
        }
    
        private var lastUsedPlanet: Int = 0
        private fun getRandomPlanetShape(): PlanetShape {
            for (i in planetShapes!!.indices) {
                lastUsedPlanet++// get the next random planet
                lastUsedPlanet %= planetShapes!!.size
                val planetShape = planetShapes!![lastUsedPlanet]
                if (!planetShape.isInUse) {
                    // if it's not in use then use it
                    planetShape.usePlanet()
                    return planetShape
                }
            }
            // actually run out of planets....
            throw IndexOutOfBoundsException("run out of planet?!")
//            return generateRandomPlanetShape(0f, 0f, generateRadius(), 10f)
        }

        fun getAllPlanetZs(): ArrayList<Float> {
            val zs = ArrayList<Float>()
            for (planetShape in planetShapes!!) {
                val planetShapeZs = planetShape.getZs()
                var isDifferentZ = true
                for (planetShapeZ in planetShapeZs) {
                    for (z in zs) {
                        if (z == planetShapeZ) {
                            isDifferentZ = false
                            break
                        }
                    }
                    if (isDifferentZ)
                        zs.add(planetShapeZ)
                }
            }
            return zs
        }
    
        private fun generateRadius(): Float {
            return random().toFloat() * RADIUS_MARGIN + AVERAGE_RADIUS - RADIUS_MARGIN / 2
        }
    }
}
