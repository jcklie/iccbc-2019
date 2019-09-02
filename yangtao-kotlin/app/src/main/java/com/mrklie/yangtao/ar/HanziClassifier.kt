package com.mrklie.yangtao.ar

import android.content.Context
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.Size
import org.opencv.objdetect.HOGDescriptor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class HanziClassifier(val context: Context, val modelPath: String, val labelPath: String) {

    val interpreter: Interpreter
    val labels: List<String>
    val hog: HOGDescriptor
    val descriptors: MatOfFloat
    val outputArray: FloatArray

    init {
        val model = loadModel()
        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)

        labels = loadLabels()

        hog = buildHog()
        descriptors = MatOfFloat()
        descriptors.alloc(hog.descriptorSize.toInt())

        outputArray = FloatArray(labels.size)
    }

    private fun loadModel(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)

        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels(): List<String> {
        return context.assets.open(labelPath)
            .bufferedReader(Charsets.UTF_8)
            .readLines()
    }

    private fun buildHog(): HOGDescriptor {
        val windowSize = Size(64.0, 64.0)
        val blockSize = Size(8.0, 8.0)
        val blockStride = Size(8.0, 8.0)
        val cellSize = Size(8.0, 8.0)
        val nbins = 9

        return HOGDescriptor(windowSize, blockSize, blockStride, cellSize, nbins)
    }

    fun predict(image: Mat): String {
        hog.compute(image, descriptors)

        interpreter.run(descriptors.toArray(), outputArray)

        val idx = maxIndex(outputArray)
        return labels[idx]
    }

    private fun maxIndex(probabilities: FloatArray): Int {
        return probabilities.zip(1..probabilities.size).fold(0, { bestIndex, (probability, index) ->
            if (probability > probabilities[bestIndex])
                index
            else
                bestIndex
        })
    }
}