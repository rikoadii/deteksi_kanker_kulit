package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.IOException

class ImageClassifierHelper(private val context: Context) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        try {
            // Creating an instance of ImageClassifier from the model file
            imageClassifier = ImageClassifier.createFromFile(context, "cancer_classification.tflite")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun classifyStaticImage(imageUri: Uri): Pair<String, Float>? {
        return try {
            // Convert Uri to Bitmap
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }.copy(Bitmap.Config.ARGB_8888, true)

            // Convert Bitmap to TensorImage
            val tensorImage = TensorImage.fromBitmap(bitmap)

            // Classify the image
            val results = imageClassifier?.classify(tensorImage)

            // Extracting the top result
            results?.firstOrNull()?.categories?.maxByOrNull { it.score }?.let { category ->
                Pair(category.label, category.score)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
