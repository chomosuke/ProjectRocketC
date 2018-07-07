package com.chomusukestudio.projectrocketc.Shape.PlanetShape

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.TriangularShape

import java.util.Arrays
import java.util.Collections

import java.lang.Math.PI
import java.lang.Math.asin
import java.lang.Math.cos
import java.lang.Math.random
import java.lang.Math.sin

class MarsShape(centerX: Float, centerY: Float, radius: Float, z: Float) : PlanetShape(centerX, centerY, radius) {
    override val isOverlapMethodLevel: Double = 2.0
    override lateinit var componentShapes: Array<Shape>
    
    init {
        
        val numberOfCrater = (4 + random() * 2).toInt()
        // 4 to 6 crater each planet
        val componentShapes = arrayOfNulls<Shape>(numberOfCrater + 1)
        
        // color of planet
        val mainColor = floatArrayOf((random() * 0.8 + 0.2).toFloat(), (random() * 0.8 + 0.2).toFloat(), (random() * 0.8 + 0.2).toFloat())
        // color of crater
        val randomDarker = (random() * 0.2 + 0.6).toFloat()
        val darkerColor = floatArrayOf(randomDarker * mainColor[0], randomDarker * mainColor[1], randomDarker * mainColor[2])
        
        // the planet itself
        componentShapes[0] = CircularShape(centerX, centerY, radius, mainColor[0], mainColor[1], mainColor[2], 1f, z)
        
        // generate some Crater on the planet
        for (i in 1 until componentShapes.size) {
            // (radius) of crater
            val sRadius = (random() * 0.3 + 0.35) * PI / 6
            // (distance) of crater from center
            val offsetRadius = asin((i - 0.25) / numberOfCrater)
            
            // generate the crater
            val centerXOfBall = centerX.toDouble()
            val centerYOfBall = centerY.toDouble()
            val red = darkerColor[0]
            val green = darkerColor[1]
            val blue = darkerColor[2]
            val z1 = z - 0.01f
            componentShapes[i] = object : Shape() {
                override var componentShapes: Array<Shape>
                override val isOverlapMethodLevel: Double = 0.0
                init {
                    val r = sin(sRadius).toFloat() * radius
                    
                    val numberOfEdges = CircularShape.getNumberOfEdges(r)
                    val componentShapes = arrayOfNulls<Shape>(numberOfEdges - 2)
                    
                    // generate components triangularShape for EllipseShape using center and a and b
                    for (i in 1 until componentShapes.size + 1) {
                        componentShapes[i - 1] = TriangularShape(mSin(offsetRadius).toFloat() * radius * cos(sRadius).toFloat(),
                                sin(sRadius).toFloat() * radius,
                                mSin(offsetRadius + sRadius * sin(2.0 * PI * i.toDouble() / numberOfEdges)).toFloat() * radius * cos(sRadius * cos(2.0 * PI * i.toDouble() / numberOfEdges)).toFloat(),
                                sin(sRadius * cos(2.0 * PI * i.toDouble() / numberOfEdges)).toFloat() * radius,
                                mSin(offsetRadius + sRadius * sin(2.0 * PI * (i + 1).toDouble() / numberOfEdges)).toFloat() * radius * cos(sRadius * cos(2.0 * PI * (i + 1).toDouble() / numberOfEdges)).toFloat(),
                                sin(sRadius * cos(2.0 * PI * (i + 1).toDouble() / numberOfEdges)).toFloat() * radius,
                                red, green, blue, 1f, z1) // close for modification
                        // screw that low efficiency thingy
                        //                    double x2 = centerX + (r * sin(2*PI*i/numberOfEdges));
                        //                    double y2 = (r * cos(2*PI*i/numberOfEdges));
                        //                    double x3 = centerX + (r * sin(2*PI*(i+1)/numberOfEdges));
                        //                    double y3 = (r * cos(2*PI*(i+1)/numberOfEdges));
                        //                    if (square(x2) >= square(radiusOfBall) - square(y2) && square(x3) >= square(radiusOfBall) - square(y3))
                        //                        componentShapes[i - 1] = new TriangularShape(centerX, r, sqrt(square(radiusOfBall) - square(y2)), y2, sqrt(square(radiusOfBall) - square(y3)), y3,
                        //                                Red, green, blue, alpha, z);
                        //                    else if (square(x2) >= square(radiusOfBall) - square(y2))
                        //                        componentShapes[i - 1] = new TriangularShape(centerX, r, sqrt(square(radiusOfBall) - square(y2)), y2, x3, y3,
                        //                                Red, green, blue, alpha, z);
                        //                    else if (square(x3) >= square(radiusOfBall) - square(y3))
                        //                        componentShapes[i - 1] = new TriangularShape(centerX, r, x2, y2, sqrt(square(radiusOfBall) - square(y3)), y3,
                        //                                Red, green, blue, alpha, z);
                        //                    else
                        //                        componentShapes[i - 1] = new TriangularShape(centerX, r, x2, y2, x3, y3,
                        //                            Red, green, blue, alpha, z); // close for modification
                    }
                    this.componentShapes = componentShapes as Array<Shape>

                    moveShape(centerXOfBall.toFloat(), centerYOfBall.toFloat())
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
        Collections.shuffle(Arrays.asList<Shape>(*rComponentShapes))
        for (i in rComponentShapes.indices) {
            rComponentShapes[i]!!.rotateShape(centerX, centerY, (2 * PI / rComponentShapes.size * i).toFloat())
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }// one level higher circularShape
}// close for modification!!! this thing is terrible
