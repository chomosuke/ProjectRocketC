package com.chomusukestudio.projectrocketc.Shape

import com.chomusukestudio.projectrocketc.GLRenderer.Layers

class BuildShapeAttr(val z: Float, val visibility: Boolean, val layers: Layers) { // never set this as a property
    fun newAttrWithChangedZ(dz: Float) = BuildShapeAttr(z + dz, visibility, layers)
    fun newAttrWithNewVisibility(visibility: Boolean) = BuildShapeAttr(z, visibility, layers)
}