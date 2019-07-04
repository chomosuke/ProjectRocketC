package com.chomusukestudio.projectrocketc.Shape.LittleStar

import com.chomusukestudio.projectrocketc.Shape.*

class LittleStarShape(centerX: Float, centerY: Float, radius: Float,
                      starColor: Color,
                      circleColor: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override val isOverlapMethodLevel: Double = 2.0 // higher than circular shape
    override var componentShapes: Array<Shape> = arrayOf(CircularShape(centerX, centerY, radius, circleColor, buildShapeAttr)
            , NPointsStarShape(5, centerX, centerY, radius * 0.75f, starColor, buildShapeAttr.newAttrWithChangedZ(-0.01f)))
    
    
    public override fun isOverlapToOverride(anotherShape: Shape): Boolean {
        return componentShapes[0].isOverlap(anotherShape)
        // NPointsStarShape is irrelevant
    }
}

const val RADIUS_OF_LITTLE_STAR = 0.16f
