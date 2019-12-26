package com.chomusukestudio.projectrocketc

import android.content.Context
import com.chomusukestudio.projectrocketc.GLRenderer.GLImage
import com.chomusukestudio.projectrocketc.GLRenderer.Layers
import com.chomusukestudio.projectrocketc.Shape.*

class Image(context: Context, resourceId: Int, var vertex1: Vector, var vertex2: Vector, var vertex3: Vector, var vertex4: Vector,
            overlapperVertexes: Array<Vector>, showOverlapper: Boolean,
            z: Float, layers: Layers): ISolid, IRemovable {
    private val glImage = GLImage(context, resourceId, vertex1, vertex2, vertex3, vertex4, z, layers)
    override fun move(displacement: Vector) {
        glImage.move(displacement)
        (overlapper as PolygonalOverlapper).move(displacement)
        overlapperShape?.move(displacement)
        vertex1 += displacement
        vertex2 += displacement
        vertex3 += displacement
        vertex4 += displacement
    }

    override fun rotate(centerOfRotation: Vector, angle: Float) {
        glImage.rotate(centerOfRotation, angle)
        (overlapper as PolygonalOverlapper).rotate(centerOfRotation, angle)
        overlapperShape?.rotate(centerOfRotation, angle)
        vertex1 = vertex1.rotateVector(centerOfRotation, angle)
        vertex2 = vertex2.rotateVector(centerOfRotation, angle)
        vertex3 = vertex3.rotateVector(centerOfRotation, angle)
        vertex4 = vertex4.rotateVector(centerOfRotation, angle)
    }

    override val overlapper: Overlapper = PolygonalOverlapper(overlapperVertexes)

    private val overlapperShape = if (showOverlapper)
        EarClipPolygonalShape(
                overlapperVertexes,
                Color(0f, 1f, 0f, 0.4f),
                BuildShapeAttr(-100f, true, layers))
    else null

    override fun remove() {
        glImage.remove()
        overlapperShape?.remove()
    }

    override val removed: Boolean
        get() = glImage.removed
}