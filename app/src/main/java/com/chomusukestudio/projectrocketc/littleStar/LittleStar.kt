package com.chomusukestudio.projectrocketc.littleStar

import android.widget.TextView
import com.chomusukestudio.projectrocketc.Shape.LittleStar.ArrowToLittleStarShape
import com.chomusukestudio.projectrocketc.Shape.LittleStar.LittleStarShape
import com.chomusukestudio.projectrocketc.Shape.LittleStar.RADIUS_OF_LITTLE_STAR
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.coordinate.distance
import com.chomusukestudio.projectrocketc.Shape.coordinate.rotatePoint
import com.chomusukestudio.projectrocketc.TouchableView
import com.chomusukestudio.projectrocketc.giveVisualText
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import android.media.SoundPool
import android.media.AudioManager
import android.util.Log
import com.chomusukestudio.projectrocketc.GLRenderer.*
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.Surrounding.Planet
import java.lang.Math.abs
import kotlin.math.pow


/**
 * Created by Shuang Li on 25/03/2018.
 */

class LittleStar(val COLOR: Color, private var centerX: Float, private var centerY: Float, private val range: Float, var duration: Long, now: Long, layers: Layers) {
    private var littleStarShape: LittleStarShape
    private var arrowToLittleStarShape: ArrowToLittleStarShape
    private var rangeCircleThingy: FullRingShape? = null
    private var inScreen: Boolean
    
    enum class Color(val color: com.chomusukestudio.projectrocketc.Shape.Color) {
        RED(Color(1f, 0f, 0f, 1f)), YELLOW(Color(242f/256f, 187f/256f, 26f/256f, 1f))
    }
    
    private val birthTime: Long = now

    init {
        // circle color for arrowToLittleStarShape is star color
        // arrow color is circle color for littleStarShape
        val buildShapeAttr = BuildShapeAttr(-10f, true, layers)
        littleStarShape = LittleStarShape(centerX, centerY, RADIUS_OF_LITTLE_STAR, COLOR.color, Color(1f, 1f, 1f, 1f), buildShapeAttr)
        arrowToLittleStarShape = ArrowToLittleStarShape(RADIUS_OF_LITTLE_STAR, Color(1f, 1f, 1f, 1f), COLOR.color, buildShapeAttr)

        if (centerX + RADIUS_OF_LITTLE_STAR + range > rightEnd &&
                        centerX - RADIUS_OF_LITTLE_STAR + range < leftEnd &&
                        centerY + RADIUS_OF_LITTLE_STAR + range > bottomEnd &&
                        centerY - RADIUS_OF_LITTLE_STAR + range < topEnd) { // if inScreen
            inScreen = true
            arrowToLittleStarShape.visibility = false
        } else {
            inScreen = false
            arrowToLittleStarShape.visibility = true
            positionArrow()
        }
        
//        val widthOfRangeCircleThingy = 0.05f
//        if (range > 0) {
//            rangeCircleThingy = FullRingShape(centerX, centerY, RADIUS_OF_LITTLE_STAR + range, RADIUS_OF_LITTLE_STAR + range,
//                    (RADIUS_OF_LITTLE_STAR + range - widthOfRangeCircleThingy) / (RADIUS_OF_LITTLE_STAR + range),
//                    COLOR.Red, COLOR.green, COLOR.blue, 0.5f, -10f)
//        }
    }
    
    private var lastFlash: Long = 0
    private val flashDuration = 100
    fun isTimeOut(now: Long): Boolean {
        if (now - birthTime > duration * 0.75f) {
            if (now - lastFlash > flashDuration) {
                // need to flash again
                if (inScreen) {
                    littleStarShape.visibility = !littleStarShape.visibility
                } else {
                    arrowToLittleStarShape.visibility = !arrowToLittleStarShape.visibility
                }
                lastFlash = now
            }
        }
        val isTimeOut = now - birthTime > duration
//        if (isTimeOut) {
//            if (dScore > 1 && COLOR == Color.RED) {
//                dScore /= 2
//                giveVisualText("×$dScore")
//            }
//        }
        return isTimeOut
    }
    
    fun isEaten(rocketComponentShapes: Array<Shape>): Boolean {
        for (rocketComponentShape in rocketComponentShapes)
            if (CircularShape.isOverlap(rocketComponentShape, centerX, centerY, RADIUS_OF_LITTLE_STAR))
                return true
        return false
    }
    
    //    static final MediaPlayer eatYellowStar =
    fun eatLittleStar(visualTextView: TouchableView<TextView>) {
        when (COLOR) {
            LittleStar.Color.YELLOW -> {
                score += dScore
                giveVisualText("+$dScore", visualTextView)

                // stop all streams
                while (starEatingStreamIds.isNotEmpty()) {
                    soundPool.stop(starEatingStreamIds[0])
                    starEatingStreamIds.removeAt(0)
                }

                if (dScore > 48) {
                    val playbackSpeed = 2f.pow((dScore % 12 + 48).toFloat() / 12) / 2

                    val baseVolume = 1 - abs((12f - dScore % 12) / 12f)

                    Log.v("eat star baseVolume", "" + baseVolume)

                    starEatingStreamIds.add(soundPool.play(soundId, baseVolume, baseVolume, 1, 0, playbackSpeed / 2f))
                    starEatingStreamIds.add(soundPool.play(soundId, 1 - baseVolume, 1 - baseVolume, 1, 0, playbackSpeed))
                } else {

                    val playbackSpeed = 2f.pow(dScore.toFloat() / 12) / 2

                    starEatingStreamIds.add(soundPool.play(soundId, 1f, 1f, 1, 0, playbackSpeed))
                }
            }
            LittleStar.Color.RED -> {
                dScore *= 2
                giveVisualText("×$dScore", visualTextView)
            }
        }
        littleStarShape.removeShape()
        arrowToLittleStarShape.removeShape()
        rangeCircleThingy?.removeShape()
    }
    
    fun isTooCloseToAPlanet(planet: Planet, margin: Float): Boolean {
        return distance(planet.centerX, planet.centerY, centerX, centerY) <= RADIUS_OF_LITTLE_STAR + margin + planet.radius
    }
    
    fun isTooFarFromAPlanet(planetShape: PlanetShape, margin: Float): Boolean {
        return distance(planetShape.centerX, planetShape.centerY, centerX, centerY) >= RADIUS_OF_LITTLE_STAR + margin + planetShape.radius
    }
    
    fun moveLittleStar(dx: Float, dy: Float) {
        if (dx == 0f && dy == 0f) {
            return
        }
        centerX += dx
        centerY += dy
        littleStarShape.moveShape(dx, dy)
        rangeCircleThingy?.moveShape(dx, dy)
        if (inScreen) {
            if (centerX + RADIUS_OF_LITTLE_STAR < rightEnd ||
                    centerX - RADIUS_OF_LITTLE_STAR > leftEnd ||
                    centerY + RADIUS_OF_LITTLE_STAR < bottomEnd ||
                    centerY - RADIUS_OF_LITTLE_STAR > topEnd) {
                // if now it's out of screen
                inScreen = false
                arrowToLittleStarShape.visibility = true
                // the next if statement will do the rest
            }
        }
        if (!inScreen) {
            if (centerX + RADIUS_OF_LITTLE_STAR > rightEnd &&
                    centerX - RADIUS_OF_LITTLE_STAR < leftEnd &&
                    centerY + RADIUS_OF_LITTLE_STAR > bottomEnd &&
                    centerY - RADIUS_OF_LITTLE_STAR < topEnd) {
                // if inScreen now
                inScreen = true
                arrowToLittleStarShape.visibility = false
            } else {
                positionArrow()
            }
        }
    }
    
    
    private fun positionArrow() {
        // still out of screen
        // point the arrow to the LittleStar
        val cLeftMax = leftEnd - RADIUS_OF_LITTLE_STAR
        val cRightMax = rightEnd + RADIUS_OF_LITTLE_STAR
        val cTopMax = topEnd - RADIUS_OF_LITTLE_STAR
        val cBottomMax = bottomEnd + RADIUS_OF_LITTLE_STAR
        var arrowX: Float
        var arrowY: Float
        val m = (centerY - centerOfRocketY) / (centerX - centerOfRocketX) // gradient of arrow
    
        if (centerY > centerOfRocketY) { // could be topEnd
            arrowY = cTopMax
            arrowX = (cTopMax - centerOfRocketY) / m + centerOfRocketX // put arrow on topEnd
            arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.UP)
        
            if (arrowX < cRightMax) { // if not topEnd but rightEnd
                arrowX = cRightMax
                arrowY = (cRightMax - centerOfRocketX) * m + centerOfRocketY// put arrow on rightEnd
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.RIGHT)
            
            } else if (arrowX > cLeftMax) { // if not topEnd but leftEnd
            
                arrowX = cLeftMax
                arrowY = (cLeftMax - centerOfRocketX) * m + centerOfRocketY // put arrow on leftEnd
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.LEFT)
            
            } // else arrow is on topEnd, don't have to change anything, already put it there
        } else { // could be bottomEnd
            arrowY = cBottomMax
            arrowX = (cBottomMax - centerOfRocketY) / m + centerOfRocketX // put arrow on bottomEnd
            arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.DOWN)
        
            if (arrowX < cRightMax) { // if not bottomEnd but rightEnd
            
                arrowX = cRightMax
                arrowY = (cRightMax - centerOfRocketX) * m + centerOfRocketY// put arrow on rightEnd
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.RIGHT)
            
            } else if (arrowX > cLeftMax) { // if not bottomEnd but leftEnd
            
                arrowX = cLeftMax
                arrowY = (cLeftMax - centerOfRocketX) * m + centerOfRocketY // put arrow on leftEnd
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.LEFT)
            
            } // else arrow is on bottomEnd, don't have to change anything, already put it there
        }
        arrowToLittleStarShape.setPosition(arrowX, arrowY)
    }
    
    fun rotateLittleStar(centerOfRotationX: Float, centerOfRotationY: Float, angle: Float) {
        val result = rotatePoint(centerX, centerY, centerOfRotationX, centerOfRotationY, angle)
        centerX = result[0]
        centerY = result[1]
        littleStarShape.rotateShape(centerOfRotationX, centerOfRotationY, angle)
        rangeCircleThingy?.rotateShape(centerOfRotationX, centerOfRotationY, angle)
    }
    
    fun resetPosition(centerX: Float, centerY: Float) {
        val dx = centerX - this.centerX
        val dy = centerY - this.centerY
        moveLittleStar(dx, dy)
    }
    
    fun removeLittleStarShape() {
        littleStarShape.removeShape()
        arrowToLittleStarShape.removeShape()
        rangeCircleThingy?.removeShape()
    }
    
    fun isOverlap(shape: Shape): Boolean {
        return littleStarShape.isOverlap(shape)
    }
    
    private var speed = 0f
    fun attractLittleStar(centerOfRotationX: Float, centerOfRotationY: Float, now: Long, previousFrameTime: Long, acceleration: Float) {
        val distance = distance(centerX, centerY, centerOfRotationX, centerOfRotationY)
        if (distance < RADIUS_OF_LITTLE_STAR + range){
            // accelerate it
            speed += acceleration * (now -  previousFrameTime)
            // so people can see if they had it or not
            littleStarShape.visibility = true
        } else {
            // slow down
            if (speed > 0f)
                speed -= acceleration * (now - previousFrameTime)
            else
                speed = 0f
        }
        if (speed != 0f) {
            val angle = atan2(centerOfRotationY - centerY, centerOfRotationX - centerX)
            moveLittleStar(speed * cos(angle) * (now - previousFrameTime), speed * sin(angle) * (now - previousFrameTime))
        }
    }
    
    companion object {
        
        var score = 0
            private set
        var dScore = 1
        
        fun cleanScore() {
            dScore = 1
            score = 0
        }
        
        private var centerOfRocketX = 0f
        private var centerOfRocketY = 0f
        fun setCenterOfRocket(centerOfRocketX: Float, centerOfRocketY: Float) {
            LittleStar.centerOfRocketX = centerOfRocketX
            LittleStar.centerOfRocketY = centerOfRocketY
        }

        var soundId: Int = 0
        var soundPool: SoundPool = SoundPool(4, AudioManager.STREAM_MUSIC, 100)
        var starEatingStreamIds = ArrayList<Int>()
    }
}


