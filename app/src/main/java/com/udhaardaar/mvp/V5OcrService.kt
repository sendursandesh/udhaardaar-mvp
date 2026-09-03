package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/** Offline OCR boundary. UI must show extracted text for human review before committing values. */
class V5OcrService(private val context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    fun extract(bitmap: Bitmap, onResult: (String, Float) -> Unit, onError: (Exception) -> Unit) {
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result ->
                val blocks = result.textBlocks
                val confidenceValues = blocks.flatMap { it.lines }.flatMap { it.elements }.mapNotNull { it.confidence }
                val confidence = if (confidenceValues.isEmpty()) 0f else confidenceValues.average().toFloat()
                onResult(result.text, confidence)
            }
            .addOnFailureListener { onError(it) }
    }
    fun close() { recognizer.close() }
}
