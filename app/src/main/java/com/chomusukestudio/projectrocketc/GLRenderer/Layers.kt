package com.chomusukestudio.projectrocketc.GLRenderer

import java.util.ArrayList
import java.util.concurrent.locks.ReentrantLock

class Layers<T : Layer> { // a group of arrayList
    val arrayList = ArrayList<T>()

//        fun offsetAllLayers(dOffsetX: Float, dOffsetY: Float) {
//            for (layer in arrayList)
//                layer.offsetLayer(dOffsetX, dOffsetY)
//        }

    val lockOnArrayList = ReentrantLock()

    fun drawAllTriangles() {
        // no need to sort, already in order
        lockOnArrayList.lock() // for preventing concurrent modification
        for (layer in arrayList) { // draw arrayList in order
            layer.drawLayer()
        }
        lockOnArrayList.unlock()
    }

    //        private val parallelForIForPassArraysToBuffers = ParallelForI(20, "passArraysToBuffers")
    fun passArraysToBuffers() {
        lockOnArrayList.lock()
//            parallelForIForPassArraysToBuffers.run({ i ->
//                arrayList[i].passArraysToBuffers()
//            }, arrayList.size)
//            parallelForIForPassArraysToBuffers.waitForLastRun()
        for (layer in arrayList)
            layer.passArraysToBuffers()
        lockOnArrayList.unlock()
    }
}

data class AllLayers(val shapeLayers: Layers<ShapeLayer>, val textureLayers: Layers<TextureLayer>) {
    fun drawAll() {
        shapeLayers.drawAllTriangles()
    }

    fun passArraysToBuffers() {
        shapeLayers.passArraysToBuffers()
    }
}