package com.chomusukestudio.projectrocketc.Shape

interface IMovable {
    fun move(displacement: Vector)
    fun rotate(centerOfRotation: Vector, angle: Float)
}

interface IOverlapable {
    val overlapper: Overlapper
}

interface ISolid: IMovable, IOverlapable