package com.chomusukestudio.projectrocketc.Surrounding


import android.support.annotation.CallSuper
import com.chomusukestudio.projectrocketc.GLRenderer.*

import com.chomusukestudio.projectrocketc.Rocket.Rocket
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.QuadrilateralShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.TriangularShape
import com.chomusukestudio.projectrocketc.State
import com.chomusukestudio.projectrocketc.ThreadClasses.ParallelForI
import com.chomusukestudio.projectrocketc.littleStar.LittleStar

import java.util.ArrayList

import java.lang.Math.random

class TestSurrounding(private val layers: Layers): Surrounding() {

    override fun initializeSurrounding(rocket: Rocket, state: State) {
        this.rocket = rocket
        startingPathOfRocket = QuadrilateralShape(centerOfRotationX - rocket.width / 2f, java.lang.Float.MAX_VALUE / 100f,
                centerOfRotationX + rocket.width / 2f, java.lang.Float.MAX_VALUE / 100f, // / 100 to prevent overflow
                centerOfRotationX + rocket.width / 2f, centerOfRotationY,
                centerOfRotationX - rocket.width / 2f, centerOfRotationY,
                0f, 1f, 0f, 1f, BuildShapeAttr(10f, true, layers)) // z is 10 because this is the most common use of z therefore are least likely to create a new layer.
        startingPathOfRocket.rotateShape(centerOfRotationX, centerOfRotationY, rotation)
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
    
    override val centerOfRotationX: Float = 0f
    override val centerOfRotationY: Float = 0f
    override val rotation: Float = 0f
    
    private val numberOfTriangleOnScreen = 5
    
    // reference to the previous topEnd triangles
    private val previousTriangles = arrayOf(TriangularShape(6f, (8 + 16 / numberOfTriangleOnScreen).toFloat(), //topEnd leftEnd
            2f, (8 + 16 / numberOfTriangleOnScreen).toFloat(), //topEnd rightEnd
            2f, -13f, //bottomEnd rightEnd
            random().toFloat(), 1f, 1f, 1f, BuildShapeAttr(10f, true, layers)),//leftEnd topEnd
            TriangularShape(6f, (8 + 16 / numberOfTriangleOnScreen).toFloat(), //topEnd leftEnd
                    6f, -13f, // bottomEnd leftEnd
                    2f, -13f, //bottomEnd rightEnd
                    random().toFloat(), 1f, 1f, 1f, BuildShapeAttr(10f, true, layers)))//leftEnd bottomEnd
    
    init {
        LittleStar.setCenterOfRocket(centerOfRotationX, centerOfRotationY)
    
        // so that previousTriangle can be not nullable
        boundaries.add(previousTriangles[0])
        boundaries.add(previousTriangles[1])
    }
    
    override fun makeNewTriangleAndRemoveTheOldOne(now: Long, previousFrameTime: Long, state: State) {
        
        if (8.1 >= previousTriangles[0].getTriangularShapeCoords(Y1)) {// only go though the entire thing when need to
            // to remove
            var i = 0
            while (i < boundaries.size) {
                if ((boundaries[i] as TriangularShape).getTriangularShapeCoords(Y1) < -13 && //y1
                        
                        (boundaries[i] as TriangularShape).getTriangularShapeCoords(Y2) < -13 && //y2
                        
                        (boundaries[i] as TriangularShape).getTriangularShapeCoords(Y3) < -13) { //y3
                    boundaries.removeAt(i).removeShape()
                    i--
                }
                i++
            }
            // to create
            val randomPoint = (random() * 5 - 2.5).toFloat()

            val buildBoundryShapesAttr = BuildShapeAttr(10f, true, layers)
            // complicated calculation done to ensure triangle meet up with each other and there ain't any overlapping or gap
            boundaries.add(TriangularShape(6f, 8.1f + 16f / numberOfTriangleOnScreen, //topEnd leftEnd
                    randomPoint + 1.5f, 8.1f + 16f / numberOfTriangleOnScreen, //topEnd rightEnd
                    previousTriangles[0].getTriangularShapeCoords(X2), previousTriangles[0].getTriangularShapeCoords(Y2), //bottomEnd rightEnd (previous topEnd rightEnd)
                    random().toFloat(), 1f, 1f, 1f, buildBoundryShapesAttr))//leftEnd topEnd
            // refresh previous triangle so other can follow
            boundaries.add(TriangularShape(6f, 8.1f + 16f / numberOfTriangleOnScreen, //topEnd leftEnd
                    previousTriangles[0].getTriangularShapeCoords(X1), previousTriangles[0].getTriangularShapeCoords(Y1), // bottomEnd leftEnd (previous topEnd leftEnd)
                    previousTriangles[0].getTriangularShapeCoords(X2), previousTriangles[0].getTriangularShapeCoords(Y2), //bottomEnd rightEnd (previous topEnd rightEnd)
                    random().toFloat(), 1f, 1f, 1f, buildBoundryShapesAttr))//leftEnd bottomEnd
            previousTriangles[0] = boundaries[boundaries.size - 2] as TriangularShape
            
            boundaries.add(TriangularShape(-6f, 8.1f + 16f / numberOfTriangleOnScreen, //topEnd rightEnd
                    randomPoint - 1.5f, 8.1f + 16f / numberOfTriangleOnScreen, //topEnd leftEnd
                    previousTriangles[1].getTriangularShapeCoords(X2), previousTriangles[1].getTriangularShapeCoords(Y2), //bottomEnd leftEnd (previous topEnd leftEnd)
                    random().toFloat(), 1f, 1f, 1f, buildBoundryShapesAttr))//rightEnd topEnd
            // refresh previous triangle so other can follow
            boundaries.add(TriangularShape(-6f, 8.1f + 16f / numberOfTriangleOnScreen, //topEnd rightEnd
                    previousTriangles[1].getTriangularShapeCoords(X1), previousTriangles[1].getTriangularShapeCoords(Y1), // bottomEnd rightEnd (previous topEnd rightEnd)
                    previousTriangles[1].getTriangularShapeCoords(X2), previousTriangles[1].getTriangularShapeCoords(Y2), //bottomEnd leftEnd (previous topEnd leftEnd)
                    random().toFloat(), 1f, 1f, 1f, buildBoundryShapesAttr))//rightEnd bottomEnd
            previousTriangles[1] = boundaries[boundaries.size - 2] as TriangularShape
        }
    }
    
    @CallSuper
    override fun moveSurrounding(dx: Float, dy: Float, now: Long, previousFrameTime: Long) {
        // move the boundaries down by y (y is decided by plane.movePlane())
        
        for (i in boundaries.indices)
            boundaries[i].moveShape(dx, dy)
        
        for (littleStar in littleStars)
            littleStar.moveLittleStar(dx, dy)
    }
    
    override fun rotateSurrounding(angle: Float, now: Long, previousFrameTime: Long) {
        // move the boundaries down by y (y is decided by plane.movePlane())
        for (boundary in boundaries)
            boundary.rotateShape(centerOfRotationX, centerOfRotationY, angle)
        for (littleStar in littleStars)
            littleStar.rotateLittleStar(centerOfRotationX, centerOfRotationY, angle)
    }
    
    override fun isCrashed(components: Array<Shape>): Shape? {
        crashedShape = null
        val boundariesNeedToBeChecked = ArrayList<Shape>(100)
        for (boundary in boundaries) {
            if (boundary.visibility) { // rocket can only hit on visibility stuff
                boundariesNeedToBeChecked.add(boundary)
            }
        }
        parallelForIForIsCrashed.run({ i ->
            val boundary = boundariesNeedToBeChecked[i]
            for (component in components) {
                if (boundary.isOverlap(component)) { // if does overlap
                    crashedShape = component
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
