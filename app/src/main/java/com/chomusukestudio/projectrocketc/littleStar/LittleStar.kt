package com.chomusukestudio.projectrocketc.littleStar

import android.widget.TextView
import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.FullRingShape
import com.chomusukestudio.projectrocketc.Shape.LittleStar.ArrowToLittleStarShape
import com.chomusukestudio.projectrocketc.Shape.LittleStar.LittleStarShape
import com.chomusukestudio.projectrocketc.Shape.LittleStar.RADIUS_OF_LITTLE_STAR
import com.chomusukestudio.projectrocketc.Shape.PlanetShape.PlanetShape
import com.chomusukestudio.projectrocketc.Shape.Shape

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
import java.lang.Math.abs
import java.lang.Math.pow
import kotlin.math.sqrt


/**
 * Created by Shuang Li on 25/03/2018.
 */

class LittleStar(val COLOR: Color, private var centerX: Float, private var centerY: Float, private val range: Float, var duration: Long, now: Long) {
    private var littleStarShape: LittleStarShape
    private var arrowToLittleStarShape: ArrowToLittleStarShape
    private var rangeCircleThingy: FullRingShape? = null
    private var inScreen: Boolean
    
    enum class Color(val red: Float, val green: Float, val blue: Float) {
        RED(1f, 0f, 0f), YELLOW(242f/256f, 187f/256f, 26f/256f)
    }
    
    private val birthTime: Long = now

    init {
        // circle color for arrowToLittleStarShape is star color
        // arrow color is circle color for littleStarShape
        littleStarShape = LittleStarShape(centerX, centerY, RADIUS_OF_LITTLE_STAR, COLOR.red, COLOR.green, COLOR.blue, 1f, 1f, 1f, -10f, true)
        arrowToLittleStarShape = ArrowToLittleStarShape(RADIUS_OF_LITTLE_STAR, 1f, 1f, 1f, COLOR.red, COLOR.green, COLOR.blue, -10f, true)

        if (centerX + RADIUS_OF_LITTLE_STAR + range > RIGHT_END &&
                        centerX - RADIUS_OF_LITTLE_STAR + range < LEFT_END &&
                        centerY + RADIUS_OF_LITTLE_STAR + range > BOTTOM_END &&
                        centerY - RADIUS_OF_LITTLE_STAR + range < TOP_END) { // if inScreen
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
                    val playbackSpeed = pow(2.0, (dScore % 12 + 48).toDouble() / 12).toFloat() / 2

                    val baseVolume = 1 - abs((12f - dScore % 12) / 12f)

                    Log.v("eat star baseVolume", "" + baseVolume)

                    starEatingStreamIds.add(soundPool.play(soundId, baseVolume, baseVolume, 1, 0, playbackSpeed / 2f))
                    starEatingStreamIds.add(soundPool.play(soundId, 1 - baseVolume, 1 - baseVolume, 1, 0, playbackSpeed))
                } else {

                    val playbackSpeed = pow(2.0, dScore.toDouble() / 12).toFloat() / 2

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
    
    fun isTooCloseToAPlanet(planetShape: PlanetShape, margin: Float): Boolean {
        return distance(planetShape.centerX, planetShape.centerY, centerX, centerY) <= RADIUS_OF_LITTLE_STAR + margin + planetShape.radius
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
            if (centerX + RADIUS_OF_LITTLE_STAR < RIGHT_END ||
                    centerX - RADIUS_OF_LITTLE_STAR > LEFT_END ||
                    centerY + RADIUS_OF_LITTLE_STAR < BOTTOM_END ||
                    centerY - RADIUS_OF_LITTLE_STAR > TOP_END) {
                // if now it's out of screen
                inScreen = false
                arrowToLittleStarShape.visibility = true
                // the next if statement will do the rest
            }
        }
        if (!inScreen) {
            if (centerX + RADIUS_OF_LITTLE_STAR > RIGHT_END &&
                    centerX - RADIUS_OF_LITTLE_STAR < LEFT_END &&
                    centerY + RADIUS_OF_LITTLE_STAR > BOTTOM_END &&
                    centerY - RADIUS_OF_LITTLE_STAR < TOP_END) {
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
        val leftEnd = LEFT_END - RADIUS_OF_LITTLE_STAR
        val rightEnd = RIGHT_END + RADIUS_OF_LITTLE_STAR
        val topEnd = TOP_END - RADIUS_OF_LITTLE_STAR
        val bottomEnd = BOTTOM_END + RADIUS_OF_LITTLE_STAR
        var arrowX: Float
        var arrowY: Float
        val m = (centerY - centerOfRocketY) / (centerX - centerOfRocketX) // gradient of arrow
    
        if (centerY > centerOfRocketY) { // could be top
            arrowY = topEnd
            arrowX = (topEnd - centerOfRocketY) / m + centerOfRocketX // put arrow on top
            arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.UP)
        
            if (arrowX < rightEnd) { // if not top but right
                arrowX = rightEnd
                arrowY = (rightEnd - centerOfRocketX) * m + centerOfRocketY// put arrow on right
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.RIGHT)
            
            } else if (arrowX > leftEnd) { // if not top but left
            
                arrowX = leftEnd
                arrowY = (leftEnd - centerOfRocketX) * m + centerOfRocketY // put arrow on left
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.LEFT)
            
            } // else arrow is on top, don't have to change anything, already put it there
        } else { // could be bottom
            arrowY = bottomEnd
            arrowX = (bottomEnd - centerOfRocketY) / m + centerOfRocketX // put arrow on bottom
            arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.DOWN)
        
            if (arrowX < rightEnd) { // if not bottom but right
            
                arrowX = rightEnd
                arrowY = (rightEnd - centerOfRocketX) * m + centerOfRocketY// put arrow on right
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.RIGHT)
            
            } else if (arrowX > leftEnd) { // if not bottom but left
            
                arrowX = leftEnd
                arrowY = (leftEnd - centerOfRocketX) * m + centerOfRocketY // put arrow on left
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.LEFT)
            
            } // else arrow is on bottom, don't have to change anything, already put it there
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
            val angle = atan2(centerOfRotationY - centerY, centerOfRotationX - centerX)
            speed += acceleration * (now -  previousFrameTime)
            moveLittleStar(speed * cos(angle) * (now -  previousFrameTime), speed * sin(angle) * (now - previousFrameTime))
            littleStarShape.visibility = true;
        } else {
            // reset speed
            speed = 0f
        }
    }
    
    companion object {
        
        var score = 0
        var dScore = 1
        
        fun cleanScore() {
            dScore = 1
            score = 0
        }
        
        private var LEFT_END = 0f
        private var RIGHT_END = 0f
        private var TOP_END = 0f
        private var BOTTOM_END = 0f
        fun setENDs(LEFT_END: Float, RIGHT_END: Float, BOTTOM_END: Float, TOP_END: Float) {
            LittleStar.LEFT_END = LEFT_END
            LittleStar.RIGHT_END = RIGHT_END
            LittleStar.TOP_END = TOP_END
            LittleStar.BOTTOM_END = BOTTOM_END
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


