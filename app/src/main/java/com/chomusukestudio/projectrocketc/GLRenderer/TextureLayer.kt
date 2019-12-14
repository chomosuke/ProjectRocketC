package com.chomusukestudio.projectrocketc.GLRenderer

class TextureLayer(z: Float) : Layer(z, 12) {
    override val mProgram: Int
        get() = glProgram
    companion object {
        private var glProgram = -100
        fun createGLProgram() {
            glProgram = createGLProgram(vertexShaderCode, fragmentShaderCode)
        }
        private const val vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec3 tData;" +
                        "varying vec3 tPositionAndIndex;" +
                        "void main() {" +
                        "  tPositionAndIndex = tData;" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}"

        private const val fragmentShaderCode =
                "precision mediump float;" +
                        "uniform sampler2DArray textureArray;" +
                        "varying vec3 tPositionAndIndex;" +
                        "void main() {" +
                        " gl_FragColor = texture(textureArray, tPositionAndIndex);" +
                        "}"
    }
}