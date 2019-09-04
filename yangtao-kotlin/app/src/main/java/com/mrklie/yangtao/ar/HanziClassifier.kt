package com.mrklie.yangtao.ar

import android.content.Context
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc.INTER_NEAREST
import org.opencv.imgproc.Imgproc.resize
import org.opencv.objdetect.HOGDescriptor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

typealias Prediction = Pair<Int, Float>

class HanziClassifier(val context: Context, val modelPath: String, val labelPath: String) {


    val interpreter: Interpreter
    val labels: List<String>
    val hog: HOGDescriptor
    val descriptors: MatOfFloat
    val outputArray: Array<FloatArray>

    init {
        val model = loadModel()
        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)

        labels = loadLabels()

        hog = buildHog()
        descriptors = MatOfFloat()
        descriptors.alloc(hog.descriptorSize.toInt())

        outputArray = arrayOf(FloatArray(labels.size))
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
        val blockSize = Size(16.0, 16.0)
        val blockStride = Size(8.0, 8.0)
        val cellSize = Size(8.0, 8.0)
        val nbins = 9

        return HOGDescriptor(windowSize, blockSize, blockStride, cellSize, nbins)
    }

    fun predict(image: Mat): List<String> {
        val resizedImage = Mat(64, 64, image.type())
        resize(image, resizedImage, resizedImage.size(), 0.0, 0.0, INTER_NEAREST)

        hog.compute(resizedImage, descriptors)

        val input = descriptors.toArray()
        interpreter.run(input, outputArray)


        return getTop10Predictions(outputArray[0])
    }

    fun getTop10Predictions(probabilities: FloatArray): List<String> {
        val predictions = mutableListOf<Prediction>()

        probabilities.forEachIndexed {index, score ->
            val prediction = index to score

            if (predictions.size < 10) {
                predictions.add(prediction)
            } else {
                val shouldReplace = predictions.find {
                    val (label, likelihood) = it
                    likelihood < score
                }

                if (shouldReplace != null) {
                    predictions[predictions.indexOf(shouldReplace)] = prediction
                }
            }
        }


        return predictions.map { labels[it.first] }
    }
}