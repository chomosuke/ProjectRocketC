package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.distance
import com.chomusukestudio.projectrocketc.square
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class EllipseShape(center: Vector, a: Float, b: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override lateinit var componentShapes: Array<Shape>
    
    init {
        val numberOfEdges = CircularShape.getNumberOfEdges((a + b) / 2)
        // just initialize it.
        val componentShapes = arrayOfNulls<TriangularShape>(numberOfEdges - 2)
    
        // generate components triangularShape for EllipseShape isInUse center and a and b
        for (i in 1 until numberOfEdges - 1)
            componentShapes[i - 1] = TriangularShape(Vector(center.x, center.y + b),
                    Vector(center.x + a * sin(2f * PI.toFloat() * i / numberOfEdges),
                            center.y + b * cos(2f * PI.toFloat() * i / numberOfEdges)),
                    Vector(center.x + a * sin(2f * PI.toFloat() * (i + 1) / numberOfEdges),
                            center.y + b * cos(2f * PI.toFloat() * (i + 1) / numberOfEdges)),
                    color, buildShapeAttr) // close for modification
    
        this.componentShapes = componentShapes as Array<Shape>
    }
}

class EllipseOverlapper(val center: Vector, val a: Float, val b: Float, val rotation: Float): Overlapper() {
    override fun overlap(anotherOverlapper: Overlapper): Boolean {
        if (a == b) // convert to circle
            return CircularOverlapper(center, a) overlap anotherOverlapper
        when (anotherOverlapper) {
            is PointOverlapper -> {
                val point = transformToEllipseCoordinate(anotherOverlapper.point)
                
                val focus1: Vector
                val focus2: Vector
                val d: Float
                if (a > b) {
                    d = 2 * a
                    val c = sqrt(square(a) - square(b))
                    focus1 = Vector(c, 0f)
                    focus2 = Vector(-c, 0f)
                } else {
                    d = 2 * b
                    val c = sqrt(square(b) - square(a))
                    focus1 = Vector(0f, c)
                    focus2 = Vector(0f, -c)
                }
                return distance(focus1, point) + distance(focus2, point) <= d
            }
            is TriangularOverlapper -> {
                return this overlap PointOverlapper(anotherOverlapper.vertex1) ||
                        this overlap PointOverlapper(anotherOverlapper.vertex2) ||
                        this overlap PointOverlapper(anotherOverlapper.vertex3)
                // TODO: not completely implemented
            }
            is CircularOverlapper -> { TODO("not implemented")
//                if (this overlap PointOverlapper(anotherOverlapper.center) ||
//                        anotherOverlapper overlap PointOverlapper(this.center)) // one inside another
//                    return true
//                val center = transformToEllipseCoordinate(anotherOverlapper.center)
//
            }
            else -> return super.overlap(anotherOverlapper)
        }
    }
    private fun transformToEllipseCoordinate(coordinate: Vector) = (coordinate - center).rotateVector(-rotation)
}
