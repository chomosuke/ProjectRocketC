package com.chomusukestudio.projectrocketc.littleStar

import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import com.chomusukestudio.prcandroid2dgameengine.distance
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.shape.*
import com.chomusukestudio.projectrocketc.PlanetShape.PlanetShape
import com.chomusukestudio.projectrocketc.R
import com.chomusukestudio.projectrocketc.Surrounding.Planet
import com.chomusukestudio.projectrocketc.TouchableView
import com.chomusukestudio.projectrocketc.UI.MainActivity
import com.chomusukestudio.projectrocketc.giveVisualText
import java.lang.Math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


/**
 * Created by Shuang Li on 25/03/2018.
 */

class LittleStar(val COLOR: Color, private var center: Vector, private val range: Float, var duration: Long, now: Long, private val drawData: DrawData) {
    private var littleStarShape: LittleStarShape
    private var arrowToLittleStarShape: ArrowToLittleStarShape
    private var rangeCircleThingy: FullRingShape? = null
    private var inScreen: Boolean
    
    enum class Color(val color: com.chomusukestudio.prcandroid2dgameengine.shape.Color) {
        RED(Color(1f, 0f, 0f, 1f)), YELLOW(Color(242f/256f, 187f/256f, 26f/256f, 1f))
    }
    
    private val birthTime: Long = now

    private val leftEnd inline get() = drawData.leftEnd
    private val rightEnd inline get() = drawData.rightEnd
    private val topEnd inline get() = drawData.topEnd
    private val bottomEnd inline get() = drawData.bottomEnd

    init {
        // circle color for arrowToLittleStarShape is star color
        // arrow color is circle color for littleStarShape
        val buildShapeAttr = BuildShapeAttr(-10f, true, drawData)
        littleStarShape = LittleStarShape(center, RADIUS_OF_LITTLE_STAR, COLOR.color, Color(1f, 1f, 1f, 1f), buildShapeAttr)
        arrowToLittleStarShape = ArrowToLittleStarShape(RADIUS_OF_LITTLE_STAR, Color(1f, 1f, 1f, 1f), COLOR.color, buildShapeAttr)

        if (center.x + RADIUS_OF_LITTLE_STAR + range > leftEnd &&
                        center.x - RADIUS_OF_LITTLE_STAR + range < rightEnd &&
                        center.y + RADIUS_OF_LITTLE_STAR + range > bottomEnd &&
                        center.y - RADIUS_OF_LITTLE_STAR + range < topEnd) { // if inScreen
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
    
    fun isEaten(rocketOverlappers: Array<Overlapper>): Boolean {
        for (rocketOverlapper in rocketOverlappers)
            if (CircularOverlapper(center, RADIUS_OF_LITTLE_STAR) overlap rocketOverlapper)
                return true
        return false
    }
    
    //    static final MediaPlayer eatYellowStar =
    fun eatLittleStar(mainActivity: MainActivity) {
        when (COLOR) {
            LittleStar.Color.YELLOW -> {
                score += dScore
                giveVisualText("+$dScore", TouchableView(mainActivity.findViewById(R.id.visualText), mainActivity))

                // stop all streams
                while (starEatingStreamIds.isNotEmpty()) {
                    soundPool.stop(starEatingStreamIds[0])
                    starEatingStreamIds.removeAt(0)
                }

                val volume = mainActivity.soundEffectsVolume.toFloat()/100
                if (dScore > 48) {
                    val playbackSpeed = 2f.pow((dScore % 12 + 48).toFloat() / 12) / 2

                    val baseVolume = 1 - abs((12f - dScore % 12) / 12f)

                    Log.v("eat star baseVolume", "" + baseVolume)

                    starEatingStreamIds.add(soundPool.play(soundId, baseVolume*volume, baseVolume*volume,
                            1, 0, playbackSpeed / 2f))
                    starEatingStreamIds.add(soundPool.play(soundId, (1 - baseVolume)*volume, (1 - baseVolume)*volume,
                            1, 0, playbackSpeed))
                } else {

                    val playbackSpeed = 2f.pow(dScore.toFloat() / 12) / 2

                    starEatingStreamIds.add(soundPool.play(soundId, volume, volume, 1, 0, playbackSpeed))
                }
            }
            LittleStar.Color.RED -> {
                dScore *= 2
                giveVisualText("×$dScore", TouchableView(mainActivity.findViewById(R.id.visualText), mainActivity))
            }
        }
        littleStarShape.remove()
        arrowToLittleStarShape.remove()
        rangeCircleThingy?.remove()
    }
    
    fun isTooCloseToAPlanet(planet: Planet, margin: Float): Boolean {
        return distance(planet.center, center) <= RADIUS_OF_LITTLE_STAR + margin + planet.radius
    }
    
    fun isTooFarFromAPlanet(planetShape: PlanetShape, margin: Float): Boolean {
        return distance(planetShape.center, center) >= RADIUS_OF_LITTLE_STAR + margin + planetShape.radius
    }
    
    fun moveLittleStar(displacement: Vector) {
        if (displacement.x == 0f && displacement.y == 0f) {
            return
        }
        center += displacement
        littleStarShape.move(displacement)
        rangeCircleThingy?.move(displacement)
        if (inScreen) {
            if (center.x + RADIUS_OF_LITTLE_STAR < leftEnd ||
                    center.x - RADIUS_OF_LITTLE_STAR > rightEnd ||
                    center.y + RADIUS_OF_LITTLE_STAR < bottomEnd ||
                    center.y - RADIUS_OF_LITTLE_STAR > topEnd) {
                // if nowXY it's out of screen
                inScreen = false
                arrowToLittleStarShape.visibility = true
                // the next if statement will do the rest
            }
        }
        if (!inScreen) {
            if (center.x + RADIUS_OF_LITTLE_STAR > leftEnd &&
                    center.x - RADIUS_OF_LITTLE_STAR < rightEnd &&
                    center.y + RADIUS_OF_LITTLE_STAR > bottomEnd &&
                    center.y - RADIUS_OF_LITTLE_STAR < topEnd) {
                // if inScreen nowXY
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
        val cLeftMax = rightEnd - RADIUS_OF_LITTLE_STAR
        val cRightMax = leftEnd + RADIUS_OF_LITTLE_STAR
        val cTopMax = topEnd - RADIUS_OF_LITTLE_STAR
        val cBottomMax = bottomEnd + RADIUS_OF_LITTLE_STAR
        var arrow: Vector
        val m = (center.y - centerOfRocket.y) / (center.x - centerOfRocket.x) // gradient of arrow
    
        if (center.y > centerOfRocket.y) { // could be topEnd
           
            arrow = Vector((cTopMax - centerOfRocket.y) / m + centerOfRocket.x, cTopMax) // put arrow on topEnd
            arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.UP)
        
            if (arrow.x < cRightMax) { // if not topEnd but leftEnd
                
                arrow = Vector(cRightMax, (cRightMax - centerOfRocket.x) * m + centerOfRocket.y) // put arrow on leftEnd
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.RIGHT)
            
            } else if (arrow.x > cLeftMax) { // if not topEnd but rightEnd
                
                arrow = Vector(cLeftMax, (cLeftMax - centerOfRocket.x) * m + centerOfRocket.y) // put arrow on rightEnd
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.LEFT)
            
            } // else arrow is on topEnd, don't have to change anything, already put it there
        } else { // could be bottomEnd
            
            arrow = Vector((cBottomMax - centerOfRocket.y) / m + centerOfRocket.x, cBottomMax) // put arrow on bottomEnd
            arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.DOWN)
        
            if (arrow.x < cRightMax) { // if not bottomEnd but leftEnd
            
                arrow = Vector(cRightMax, (cRightMax - centerOfRocket.x) * m + centerOfRocket.y) // put arrow on leftEnd
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.RIGHT)
            
            } else if (arrow.x > cLeftMax) { // if not bottomEnd but rightEnd
            
                arrow = Vector(cLeftMax, (cLeftMax - centerOfRocket.x) * m + centerOfRocket.y) // put arrow on rightEnd
                arrowToLittleStarShape.setDirection(ArrowToLittleStarShape.Direction.LEFT)
            
            } // else arrow is on bottomEnd, don't have to change anything, already put it there
        }
        arrowToLittleStarShape.setPosition(arrow)
    }
    
    fun rotateLittleStar(centerOfRotation: Vector, angle: Float) {
        center = center.rotateVector(centerOfRotation, angle)
        littleStarShape.rotate(centerOfRotation, angle)
        rangeCircleThingy?.rotate(centerOfRotation, angle)
    }
    
    fun resetPosition(center: Vector) {
        val dCenter = center - this.center
        moveLittleStar(dCenter)
    }
    
    fun removeLittleStarShape() {
        littleStarShape.remove()
        arrowToLittleStarShape.remove()
        rangeCircleThingy?.remove()
    }
    
    fun isOverlap(shape: Shape): Boolean {
        return littleStarShape.overlapper overlap shape.overlapper
    }
    
    private var speed = 0f
    fun attractLittleStar(centerOfRotation: Vector, now: Long, previousFrameTime: Long, acceleration: Float) {
        val distance = distance(center, centerOfRotation)
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
            val angle = atan2(centerOfRotation.y - center.y, centerOfRotation.x - center.x)
            moveLittleStar(Vector(speed * cos(angle), speed * sin(angle)) * (now - previousFrameTime).toFloat())
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
        
        private var centerOfRocket = Vector(0f, 0f)
        fun setCenterOfRocket(centerOfRocket: Vector) {
            LittleStar.centerOfRocket = centerOfRocket
        }

        var soundId: Int = 0
        var soundPool: SoundPool = SoundPool(4, AudioManager.STREAM_MUSIC, 100)
        var starEatingStreamIds = ArrayList<Int>()
    }
}


