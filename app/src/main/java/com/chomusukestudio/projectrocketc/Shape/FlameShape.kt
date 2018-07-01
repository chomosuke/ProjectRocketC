package com.chomusukestudio.projectrocketc.Shape

//import com.chomusukestudio.projectrocketc.Shape.point.distance
//import java.lang.Math.random
//
//class FlameShape(centerX: Float, centerTopY: Float, centerMiddleY: Float, centerBottomY: Float, z: Float) : Shape() {
//    override val isOverlapMethodLevel: Double = 3.0
//    override var componentShapes: Array<Shape>
//
//    init {
//        componentShapes = arrayOfNulls(4)
//        componentShapes[0] = TriangularShape(centerX, centerTopY,
//                centerX + distance(centerX, centerBottomY, centerX, centerTopY) / 8,
//                centerMiddleY, centerX, centerMiddleY, 1f, 1f, 1f, 1f, z)
//        componentShapes[1] = TriangularShape(centerX, centerTopY,
//                centerX - distance(centerX, centerBottomY, centerX, centerTopY) / 8,
//                centerMiddleY, centerX, centerMiddleY, 1f, 1f, 1f, 1f, z)
//        componentShapes[2] = TriangularShape(centerX, centerBottomY,
//                centerX + distance(centerX, centerBottomY, centerX, centerTopY) / 8,
//                centerMiddleY, centerX, centerMiddleY, 1f, 1f, 1f, 1f, z)
//        componentShapes[3] = TriangularShape(centerX, centerBottomY,
//                centerX - distance(centerX, centerBottomY, centerX, centerTopY) / 8,
//                centerMiddleY, centerX, centerMiddleY, 1f, 1f, 1f, 1f, z)
//        (componentShapes!![0] as TriangularShape).setColorForEachCoords(1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f)
//        (componentShapes!![1] as TriangularShape).setColorForEachCoords(1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f)
//        (componentShapes!![2] as TriangularShape).setColorForEachCoords(1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f)
//        (componentShapes!![3] as TriangularShape).setColorForEachCoords(1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f)
//    }
//
//    fun flameIt() {
//        val random = (random() * 0.5 + 0.5).toFloat()
//        (componentShapes!![0] as TriangularShape).setColorForEachCoords(1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, random)
//        (componentShapes!![1] as TriangularShape).setColorForEachCoords(1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, random)
//        (componentShapes!![2] as TriangularShape).setColorForEachCoords(1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, random)
//        (componentShapes!![3] as TriangularShape).setColorForEachCoords(1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, random)
//    }
//
//    public override fun isOverlapToOverride(anotherShape: Shape): Boolean {
//        return false // flame doesn't exist
//    }
//}
