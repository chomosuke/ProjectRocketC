package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr
import com.chomusukestudio.projectrocketc.Shape.TriangularShape
import com.chomusukestudio.projectrocketc.randFloat

import java.util.Arrays

import java.lang.Math.PI
import java.lang.Math.asin
import java.lang.Math.cos
import java.lang.Math.sin

class MarsShape(centerX: Float, centerY: Float, radius: Float, buildShapeAttr: BuildShapeAttr) : PlanetShape(centerX, centerY, radius) {
    override val isOverlapMethodLevel: Double = 2.0 // one level higher circularShape
    override lateinit var componentShapes: Array<Shape>
    
    init {
        
        val numberOfCrater = randFloat(4f, 6f).toInt()
        // 4 to 5 crater each planet
        val componentShapes = arrayOfNulls<Shape>(numberOfCrater + 1)
        
        // color of planet
        val mainColor = floatArrayOf(
                randFloat(0.2f, 1f),
                randFloat(0.2f, 1f),
                randFloat(0.2f, 1f))
        // color of crater
        val randomDarker = randFloat(0.6f, 0.8f)
        val darkerColor = floatArrayOf(randomDarker * mainColor[0], randomDarker * mainColor[1], randomDarker * mainColor[2])
        
        // the planet itself
        componentShapes[0] = CircularShape(centerX, centerY, radius, mainColor[0], mainColor[1], mainColor[2], 1f, buildShapeAttr)
        
        // generate some Crater on the planet
        for (i in 1 until componentShapes.size) {
            // (radius) of crater
            val sRadius = randFloat(0.35f, 0.65f) * PI / 6
            // (distance) of crater from center
            val offsetRadius = asin((i - 0.25) / numberOfCrater)
            
            // generate the crater
            val centerXOfBall = centerX
            val centerYOfBall = centerY
            val red = darkerColor[0]
            val green = darkerColor[1]
            val blue = darkerColor[2]
            val shapeAttributes1 = buildShapeAttr.newAttrWithChangedZ(-0.01f)
            componentShapes[i] = object : Shape() {
                override var componentShapes: Array<Shape>
                override val isOverlapMethodLevel: Double = 0.0
                init {
                    val r = sin(sRadius).toFloat() * radius
                    val centerXOfCrater = sin(offsetRadius).toFloat() * radius // for circularCrater
                    
                    val numberOfEdges = CircularShape.getNumberOfEdges(r)
                    val componentShapes = arrayOfNulls<Shape>(numberOfEdges - 2)
                    
                    // generate components triangularShape for EllipseShape isInUse center and a and b
                    for (i in 1 until componentShapes.size + 1) {
                        componentShapes[i - 1] = TriangularShape(mSin(offsetRadius).toFloat() * radius * cos(sRadius).toFloat(),
                                sin(sRadius).toFloat() * radius,
                                mSin(offsetRadius + sRadius * sin(2.0 * PI * i / numberOfEdges)).toFloat() * radius * cos(sRadius * cos(2.0 * PI * i / numberOfEdges)).toFloat(),
                                sin(sRadius * cos(2.0 * PI * i / numberOfEdges)).toFloat() * radius,
                                mSin(offsetRadius + sRadius * sin(2.0 * PI * (i + 1) / numberOfEdges)).toFloat() * radius * cos(sRadius * cos(2.0 * PI * (i + 1) / numberOfEdges)).toFloat(),
                                sin(sRadius * cos(2.0 * PI * (i + 1) / numberOfEdges)).toFloat() * radius,
                                red, green, blue, 1f, shapeAttributes1) // close for modification
                        // below is circular crater
//                        val x2 = centerXOfCrater + (r * sin(2 * PI * i / numberOfEdges)).toFloat()
//                        val y2 = (r * cos(2 * PI * i / numberOfEdges)).toFloat()
//                        val x3 = centerXOfCrater + (r * sin(2 * PI * (i + 1) / numberOfEdges)).toFloat()
//                        val y3 = (r * cos(2 * PI * (i + 1) / numberOfEdges)).toFloat()
//                        if (square(x2) >= square(radius) - square(y2) && square(x3) >= square(radius) - square(y3))
//                            componentShapes[i - 1] = TriangularShape(centerXOfCrater, r, sqrt(square(radius) - square(y2)), y2, sqrt(square(radius) - square(y3)), y3,
//                                    red, green, blue, 1f, z1, true);
//                        else if (square(x2) >= square(radius) - square(y2))
//                            componentShapes[i - 1] = TriangularShape(centerXOfCrater, r, sqrt(square(radius) - square(y2)), y2, x3, y3,
//                                    red, green, blue, 1f, z1, true);
//                        else if (square(x3) >= square(radius) - square(y3))
//                            componentShapes[i - 1] = TriangularShape(centerXOfCrater, r, x2, y2, sqrt(square(radius) - square(y3)), y3,
//                                    red, green, blue, 1f, z1, true);
//                        else
//                            componentShapes[i - 1] = TriangularShape(centerXOfCrater, r, x2, y2, x3, y3,
//                                    red, green, blue, 1f, z1, true); // close for modification
                    }
                    this.componentShapes = componentShapes as Array<Shape>

                    moveShape(centerXOfBall, centerYOfBall)
                }
                
                private fun mSin(a: Double): Double {
                    return if (a >= PI / 2)
                        1.0
                    else
                        sin(a)
                }
            }
            
            
        }
        val rComponentShapes = arrayOfNulls<Shape>(componentShapes.size - 1)
        System.arraycopy(componentShapes, 1, rComponentShapes, 0, rComponentShapes.size)
        Arrays.asList<Shape>(*rComponentShapes).shuffle()
        for (i in rComponentShapes.indices) {
            rComponentShapes[i]!!.rotateShape(centerX, centerY, (2 * PI / rComponentShapes.size * i).toFloat())
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }
} // close for modification!!! this thing is terrible
