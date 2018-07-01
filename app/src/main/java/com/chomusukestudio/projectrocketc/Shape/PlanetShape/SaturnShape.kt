package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.TopHalfRingShape

import java.util.ArrayList

import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.pow
import java.lang.Math.random
import java.lang.Math.sin

class SaturnShape(ringA: Float, ringB: Float, innerA: Float, numberOfRings: Int, centerX: Float, centerY: Float, radius: Float, z: Float) : PlanetShape(centerX, centerY, radius, 3.0) {
    override lateinit var componentShapes: Array<Shape>
    
    val ringWidth: Float
    
    init {
        var ringA = ringA
        var ringB = ringB
        
        ringWidth = ringA
        
        val mainColor = floatArrayOf((random() * 0.6 + 0.2).toFloat(), (random() * 0.6 + 0.2).toFloat(), (random() * 0.6 + 0.2).toFloat())
        
        val centerComponentShapes = arrayOfNulls<Shape>(1)
        centerComponentShapes[0] = CircularShape(centerX, centerY, radius, mainColor[0], mainColor[1], mainColor[2], 1f, z)
        
        val theRatio = ringB / ringA
        
        val aForPointsOutside = ringA
        val bForPointsOutside = ringB
        
        val factor = pow((innerA / ringA).toDouble(), (1f / numberOfRings).toDouble()).toFloat()
        val ringComponentShapes = arrayOfNulls<TopHalfRingShape>(numberOfRings * 2) // number of rings times two
        var ringColor = FloatArray(0) // just for the compiler to not talk about not initializing stuff
        for (i in ringComponentShapes.indices) {
            if (i % 2 == 0) {
                // new color
                
                // the ratio also happens to be the ratio of actual thickness to thickness to camera
                var alpha = pow(random() * 0.5 + 0.5, theRatio.toDouble()).toFloat()
                
                if (alpha > 1) alpha = 1f // alpha can't be bigger than 1.
                ringColor = floatArrayOf(((random() * 0.6 + 0.7) * mainColor[0]).toFloat(), ((random() * 0.6 + 0.7) * mainColor[1]).toFloat(), ((random() * 0.6 + 0.7) * mainColor[2]).toFloat(), alpha)
                
                // top half ring
                ringComponentShapes[i] = TopHalfRingShape(centerX, centerY, ringA, ringB, factor,
                        ringColor[0], ringColor[1], ringColor[2],
                        ringColor[3], z + 0.01f)
                
            } else {
                // bottom half ring
                ringComponentShapes[i] = TopHalfRingShape(centerX, centerY, ringA, ringB, factor,
                        ringColor[0], ringColor[1], ringColor[2],
                        ringColor[3], z - 0.01f)
                ringComponentShapes[i]!!.rotateShape(centerX, centerY, PI.toFloat())
                
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
        val pointsOutsideX = ArrayList<Float>()
        val pointsOutsideY = ArrayList<Float>()
        
        for (i in 0 until numberOfPointsOnRing)
        
        // put it in the array for superclass and isOverlapToOverride
        this.pointsOutsideX = FloatArray(pointsOutsideX.size)
        this.pointsOutsideY = FloatArray(pointsOutsideY.size)
        for (i in this.pointsOutsideX!!.indices) {
            this.pointsOutsideX!![i] = pointsOutsideX[i]
            this.pointsOutsideY!![i] = pointsOutsideY[i]
            // cast from float to float.
        }
        /*float dA = (aForPointsOutside - innerA);
        int numberOfPointsOutside = (int) (dA / (0.15) + 2);
        pointsOutsideX = new float[numberOfPointsOutside];
        pointsOutsideY = new float[numberOfPointsOutside];
        for (int i = 0; i < numberOfPointsOutside; i++) {
            pointsOutsideX[i] = centerX + innerA + (float) i / (numberOfPointsOutside - 1) * dA;
            pointsOutsideY[i] = centerY;
        }*/
    }// one level higher than other PlanetShape cause the ring
    
    override fun isOverlapToOverride(anotherShape: Shape): Boolean {
        if (super.isOverlapToOverride(anotherShape))
            return true
        for (i in pointsOutsideX!!.indices)
            if (anotherShape.isInside(pointsOutsideX!![i], pointsOutsideY!![i]))
                return true
        return false
    }
}
