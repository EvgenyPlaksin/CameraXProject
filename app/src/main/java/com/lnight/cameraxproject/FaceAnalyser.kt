package com.lnight.cameraxproject

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class FaceAnalyser: ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image
        image?.close()
    }
}