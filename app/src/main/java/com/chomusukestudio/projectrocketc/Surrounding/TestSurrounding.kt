package com.chomusukestudio.projectrocketc.Surrounding


import android.support.annotation.CallSuper
import com.chomusukestudio.projectrocketc.GLRenderer.*

import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.State
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI
import com.chomusukestudio.projectrocketc.littleStar.LittleStar
import com.chomusukestudio.projectrocketc.randFloat

import java.util.ArrayList

import java.lang.Math.random
import kotlin.math.PI

class TestSurrounding(private val layers: Layers): Surrounding() {
    
    override fun initializeSurrounding(rocket: Rocket, state: State) {
        this.rocket = rocket
        startingPathOfRocket = QuadrilateralShape(Vector(centerOfRotation.x - rocket.width / 2f, java.lang.Float.MAX_VALUE / 100f),
                Vector(centerOfRotation.x + rocket.width / 2f, java.lang.Float.MAX_VALUE / 100f), // / 100 to prevent overflow
                Vector(centerOfRotation.x + rocket.width / 2f, centerOfRotation.y),
                Vector(centerOfRotation.x - rocket.width / 2f, centerOfRotation.y),
                Color(0f, 1f, 0f, 1f), BuildShapeAttr(10f, true, layers)) // z is 10 because this is the most common use of z therefore are least likely to create a new layer.
        startingPathOfRocket.rotateShape(centerOfRotation, rotation)
        startingPathOfRocket.visibility = false //  this shape will only be used in isOverlapToOverride.
    } // pass the rocket to the surrounding so surrounding can do stuff such as setCenterOfRotation
    
    
    private var boundaries = ArrayList<Shape>() // this defines where the plane can't go
    // boundaries should have z value of 10 while background should have a z value higher than 10, like 11.
    private var backgrounds = ArrayList<Shape>() // backGrounds doesn't effect plane
    private var littleStars = ArrayList<LittleStar>()
    private lateinit var startingPathOfRocket: QuadrilateralShape
    override lateinit var rocket: Rocket
    @Volatile
    private var crashedShape: Shape? = null
    private val parallelForIForIsCrashed = ParallelForI(8, "is crashed")
    
    override val centerOfRotation = Vector(0f, 0f)
    override val rotation = PI.toFloat() / 2
    
    private val numberOfTriangleOnScreen = 5
    
    // reference to the previous topEnd triangles
    private val previousTriangles = arrayOf(TriangularShape(Vector(6f, 8 + 16f / numberOfTriangleOnScreen), //topEnd rightEnd
            Vector(2f, 8 + 16f / numberOfTriangleOnScreen), //topEnd leftEnd
            Vector(2f, -13f), //bottomEnd leftEnd
            Color(random().toFloat(), 1f, 1f, 1f), BuildShapeAttr(10f, true, layers)),//rightEnd topEnd
            TriangularShape(Vector(6f, 8 + 16f / numberOfTriangleOnScreen), //topEnd rightEnd
                    Vector(6f, -13f), // bottomEnd rightEnd
                    Vector(2f, -13f), //bottomEnd leftEnd
                    Color(random().toFloat(), 1f, 1f, 1f), BuildShapeAttr(10f, true, layers)))//rightEnd bottomEnd
    
    init {
        LittleStar.setCenterOfRocket(centerOfRotation)
        
        // so that previousTriangle can be not nullable
        boundaries.add(previousTriangles[0])
        boundaries.add(previousTriangles[1])
    }
    
    override fun makeNewTriangleAndRemoveTheOldOne(now: Long, previousFrameTime: Long, state: State) {
        
        if (8.1 >= previousTriangles[0].vertex1.y) {// only go though the entire thing when need to
            // to remove
            var i = 0
            while (i < boundaries.size) {
                if ((boundaries[i] as TriangularShape).vertex1.y < -13 && //y1
                        
                        (boundaries[i] as TriangularShape).vertex2.y < -13 && //y2
                        
                        (boundaries[i] as TriangularShape).vertex3.y < -13) { //y3
                    boundaries.removeAt(i).removeShape()
                    i--
                }
                i++
            }
            // to create
            val randomPoint = randFloat(-2.5f, 2.5f)
            
            val buildBoundryShapesAttr = BuildShapeAttr(10f, true, layers)
            // complicated calculation done to ensure triangle meet up with each other and there ain't any overlapping or gap
            boundaries.add(TriangularShape(Vector(6f, 8.1f + 16f / numberOfTriangleOnScreen), //topEnd rightEnd
                    Vector(randomPoint + 1.5f, 8.1f + 16f / numberOfTriangleOnScreen), //topEnd leftEnd
                    previousTriangles[0].vertex2, //bottomEnd leftEnd (previous topEnd leftEnd)
                    Color(random().toFloat(), 1f, 1f, 1f), buildBoundryShapesAttr))//rightEnd topEnd
            // refresh previous triangle so other can follow
            boundaries.add(TriangularShape(Vector(6f, 8.1f + 16f / numberOfTriangleOnScreen), //topEnd rightEnd
                    previousTriangles[0].vertex1, // bottomEnd rightEnd (previous topEnd rightEnd)
                    previousTriangles[0].vertex2, //bottomEnd leftEnd (previous topEnd leftEnd)
                    Color(random().toFloat(), 1f, 1f, 1f), buildBoundryShapesAttr))//rightEnd bottomEnd
            previousTriangles[0] = boundaries[boundaries.size - 2] as TriangularShape
            
            boundaries.add(TriangularShape(Vector(-6f, 8.1f + 16f / numberOfTriangleOnScreen), //topEnd leftEnd
                    Vector(randomPoint - 1.5f, 8.1f + 16f / numberOfTriangleOnScreen), //topEnd rightEnd
                    previousTriangles[1].vertex2, //bottomEnd rightEnd (previous topEnd rightEnd)
                    Color(random().toFloat(), 1f, 1f, 1f), buildBoundryShapesAttr))//leftEnd topEnd
            // refresh previous triangle so other can follow
            boundaries.add(TriangularShape(Vector(-6f, 8.1f + 16f / numberOfTriangleOnScreen), //topEnd leftEnd
                    previousTriangles[1].vertex1, // bottomEnd leftEnd (previous topEnd leftEnd)
                    previousTriangles[1].vertex2, //bottomEnd rightEnd (previous topEnd rightEnd)
                    Color(random().toFloat(), 1f, 1f, 1f), buildBoundryShapesAttr))//leftEnd bottomEnd
            previousTriangles[1] = boundaries[boundaries.size - 2] as TriangularShape
        }
    }
    
    @CallSuper
    override fun moveSurrounding(vector: Vector, now: Long, previousFrameTime: Long) {
        // move the boundaries down by y (y is decided by plane.movePlane())
        
        for (i in boundaries.indices)
            boundaries[i].moveShape(vector)
        
        for (littleStar in littleStars)
            littleStar.moveLittleStar(vector)
    }
    
    override fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long) {
        // move the boundaries down by y (y is decided by plane.movePlane())
        for (boundary in boundaries)
            boundary.rotateShape(centerOfRotation, angle)
        for (littleStar in littleStars)
            littleStar.rotateLittleStar(centerOfRotation, angle)
    }
    
    override fun isCrashed(shapeForCrashAppro: Shape, components: Array<Shape>): Shape? {
        crashedShape = null
        val boundariesNeedToBeChecked = ArrayList<Shape>(100)
        for (boundary in boundaries) {
            if (boundary.visibility) { // rocket can only hit on visibility stuff
                boundariesNeedToBeChecked.add(boundary)
            }
        }
        parallelForIForIsCrashed.run({ i ->
            val boundary = boundariesNeedToBeChecked[i]
            if (boundary.overlapper overlap shapeForCrashAppro.overlapper) {
                // only check it when it's close
                for (component in components) {
                    if (boundary.overlapper overlap component.overlapper) { // if does overlap
                        crashedShape = component
                    }
                }
            }
        }, boundariesNeedToBeChecked.size)
        parallelForIForIsCrashed.waitForLastRun()
        // no need for improvement for immediate return true, most of the time there will not be any overlap.
        return crashedShape
    }
    
    override fun removeAllShape() {
        for (boundary in boundaries) {
            boundary.removeShape()
        }
        for (background in backgrounds) {
            background.removeShape()
        }
        for (littleStar in littleStars) {
            littleStar.removeLittleStarShape()
        }
    }
    
    override fun checkAndAddLittleStar(now: Long) {
        for (littleStar in littleStars) {
            if (rocket.isEaten(littleStar));
//                littleStar.eatLittleStar(TODO("put some ViewWithActivity<TextView> here"))
        }
    }
}
