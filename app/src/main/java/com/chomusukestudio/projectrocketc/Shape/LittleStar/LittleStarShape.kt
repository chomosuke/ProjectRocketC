package com.chomusukestudio.projectrocketc.Shape.LittleStar

import com.chomusukestudio.projectrocketc.Shape.CircularShape
import com.chomusukestudio.projectrocketc.Shape.NPointsStarShape
import com.chomusukestudio.projectrocketc.Shape.Shape
import com.chomusukestudio.projectrocketc.Shape.BuildShapeAttr

class LittleStarShape(centerX: Float, centerY: Float, radius: Float,
                      redStar: Float, greenStar: Float, blueStar: Float,
                      redCircle: Float, greenCircle: Float, blueCircle: Float, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 2.0 // higher than circular shape
    override var componentShapes: Array<Shape> = arrayOf(CircularShape(centerX, centerY, radius, redCircle, greenCircle, blueCircle, 1f, buildShapeAttr)
            , NPointsStarShape(5, centerX, centerY, radius * 0.75f, redStar, greenStar, blueStar, 1f, buildShapeAttr.newAttrWithChangedZ(-0.01f)))
    
    
    public override fun isOverlapToOverride(anotherShape: Shape): Boolean {
        return componentShapes[0].isOverlap(anotherShape)
        // NPointsStarShape is irrelevant
    }
}

const val RADIUS_OF_LITTLE_STAR = 0.16f
