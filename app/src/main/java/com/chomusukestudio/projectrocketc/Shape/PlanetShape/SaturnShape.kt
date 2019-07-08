package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.distance
import com.chomusukestudio.projectrocketc.randFloat
import java.lang.Math.*

import java.util.ArrayList
import kotlin.math.pow

class SaturnShape(ringA: Float, ringB: Float, innerA: Float, numberOfRings: Int, center: Vector, radius: Float, buildShapeAttr: BuildShapeAttr) : PlanetShape(center, radius) {
    override val isOverlapMethodLevel: Double = 3.0 // one level higher than other PlanetShape cause the ring
    override lateinit var componentShapes: Array<Shape>

    override val maxWidth: Float = ringA
    
    init {
        var ringA = ringA
        var ringB = ringB
        
        val mainColor = Color(
                randFloat(0.2f, 0.8f),
                randFloat(0.2f, 0.8f),
                randFloat(0.2f, 0.8f), 1f)
        
        val centerComponentShapes = arrayOfNulls<Shape>(1)
        centerComponentShapes[0] = CircularShape(center, radius, mainColor, buildShapeAttr)
        
        val theRatio = ringB / ringA
        
        val aForPointsOutside = ringA
        val bForPointsOutside = ringB
        
        val factor = (innerA / ringA).pow(1f / numberOfRings)
        val ringComponentShapes = arrayOfNulls<TopHalfRingShape>(numberOfRings * 2) // number of rings times two
        var ringColor: Color? = null // just for the compiler to not talk about not initializing stuff
        for (i in ringComponentShapes.indices) {
            if (i % 2 == 0) {
                // new color
                
                // the ratio also happens to be the ratio of actual thickness to thickness to camera
                var alpha = randFloat(0.5f, 1f).pow(theRatio)
                
                if (alpha > 1) alpha = 1f // alpha can't be bigger than 1.
                ringColor = Color(
                        randFloat(0.7f, 1.3f) * mainColor.red,
                        randFloat(0.7f, 1.3f) * mainColor.green,
                        randFloat(0.7f, 1.3f) * mainColor.blue, alpha)
                
                // topEnd half ring
                ringComponentShapes[i] = TopHalfRingShape(center, ringA, ringB, factor,
                        ringColor, buildShapeAttr.newAttrWithChangedZ(0.01f))
                
            } else {
                // bottomEnd half ring
                ringComponentShapes[i] = TopHalfRingShape(center, ringA, ringB, factor,
                        ringColor!!, buildShapeAttr.newAttrWithChangedZ(-0.01f))
                ringComponentShapes[i]!!.rotateShape(center, PI.toFloat())
                
                // smaller a & b
                ringA *= factor
                ringB = theRatio * ringA
            }
        }
        componentShapes = Array(ringComponentShapes.size + centerComponentShapes.size) // throw those Shape into component shapes
        { i ->
            if (i < centerComponentShapes.size)
                centerComponentShapes[i]!!
            else
                ringComponentShapes[i - centerComponentShapes.size]!!
        }
        
        
        
        // for isOverlapToOverride
        val numberOfPointsOnRing = 2 * CircularShape.getNumberOfEdges(aForPointsOutside)
        val pointsOutside = ArrayList<Vector>()
        
        for (i in 0 until numberOfPointsOnRing) {
            val point = Vector(center.x + aForPointsOutside * sin(PI * 2 * i / numberOfPointsOnRing).toFloat(),
                    center.y + bForPointsOutside * cos(PI * 2 * i / numberOfPointsOnRing).toFloat())
            if (distance(point, center) > radius) { // point is out side planet itself
                pointsOutside.add(point)
            }
        }
        
        // put it in the array for superclass and isOverlapToOverride
        this.pointsOutside = pointsOutside.toArray(arrayOfNulls<Vector>(pointsOutside.size))
        /*float dA = (aForPointsOutside - innerA);
        int numberOfPointsOutside = (int) (dA / (0.15) + 2);
        pointsOutsideX = new float[numberOfPointsOutside];
        pointsOutsideY = new float[numberOfPointsOutside];
        for (int i = 0; i < numberOfPointsOutside; i++) {
            pointsOutsideX[i] = centerX + innerA + (float) i / (numberOfPointsOutside - 1) * dA;
            pointsOutsideY[i] = centerY;
        }*/
    }
    
    override fun isOverlapToOverride(anotherShape: Shape): Boolean {
        if (super.isOverlapToOverride(anotherShape))
            return true
        for (i in pointsOutside!!.indices)
            if (anotherShape.isInside(pointsOutside!![i]))
                return true
        return false
    }
}
