package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.*
import com.chomusukestudio.projectrocketc.randFloat
import java.lang.Math.*

import kotlin.math.pow

class SaturnShape(private val ringA: Float, private val ringB: Float, innerA: Float, numberOfRings: Int,
                  center: Vector, radius: Float, buildShapeAttr: BuildShapeAttr) : PlanetShape(center, radius) {
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
                ringComponentShapes[i]!!.rotate(center, PI.toFloat())
                
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
    }
    
    override val overlapper: Overlapper
        get() = object : Overlapper() {
            override val components: Array<Overlapper>
                get() = arrayOf(CircularOverlapper(center, radius), EllipseOverlapper(center, ringA, ringB, rotation))
        }
    private var rotation = 0f
    override fun rotate(centerOfRotation: Vector, angle: Float) {
        super.rotate(centerOfRotation, angle)
        rotation += angle
    }
}
