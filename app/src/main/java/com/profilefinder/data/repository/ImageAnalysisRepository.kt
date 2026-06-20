package com.profilefinder.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.profilefinder.data.models.ImageAnalysisResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.profilefinder.data.models.DetectedObject as AppDetectedObject

@Singleton
class ImageAnalysisRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )

    suspend fun analyzeImage(uri: Uri): ImageAnalysisResult {
        val image = InputImage.fromFilePath(context, uri)

        val ocrText = recognizeText(image)
        val objects = detectObjects(image)

        val description = buildDescription(objects, ocrText)

        return ImageAnalysisResult(
            detectedObjects = objects,
            ocrText = ocrText,
            description = description,
            confidence = if (objects.isNotEmpty()) objects.map { it.confidence }.average().toFloat() else 0f
        )
    }

    private suspend fun recognizeText(image: InputImage): String =
        suspendCancellableCoroutine { cont ->
            textRecognizer.process(image)
                .addOnSuccessListener { result ->
                    cont.resume(result.text)
                }
                .addOnFailureListener { e ->
                    cont.resume("") // graceful fallback
                }
        }

    private suspend fun detectObjects(image: InputImage): List<AppDetectedObject> =
        suspendCancellableCoroutine { cont ->
            objectDetector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    val appObjects = detectedObjects.flatMap { obj ->
                        obj.labels.map { label ->
                            AppDetectedObject(
                                label = label.text,
                                confidence = label.confidence
                            )
                        }
                    }.distinctBy { it.label }
                    cont.resume(appObjects)
                }
                .addOnFailureListener { e ->
                    cont.resume(emptyList())
                }
        }

    private fun buildDescription(objects: List<AppDetectedObject>, ocrText: String): String {
        val sb = StringBuilder()

        if (objects.isNotEmpty()) {
            val topObjects = objects.sortedByDescending { it.confidence }.take(5)
            sb.append("Detected: ${topObjects.joinToString(", ") { it.label }}.")
        }

        if (ocrText.isNotBlank()) {
            sb.append(" Text found in image.")
        }

        if (sb.isEmpty()) {
            sb.append("No specific objects or text detected.")
        }

        return sb.toString()
    }
}
