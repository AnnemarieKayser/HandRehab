/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.handrehab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: GestureRecognizerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var numberPaint = Paint()
    private var counterPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private var counterFinger = 0
    private var repetitions: Int? = 0
    private var sets: Int? = 0
    private var setsCompleted = 0
    private var exerciseCompleted = false



    init {
        initPaints()
    }

    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        var i = 0
        counterPaint.color = Color.WHITE
        counterPaint.setTextSize(50F)
        if(exerciseCompleted){
            canvas.drawText("Übung abgeschlossen", 100F, 100F, counterPaint)
        } else {
            canvas.drawText("Wiederholungen: $counterFinger / $repetitions", 100F, 100F, counterPaint)
            canvas.drawText("Sets: $setsCompleted / $sets", 100F, 200F, counterPaint)
        }
        results?.let { gestureRecognizerResult ->
            for(landmark in gestureRecognizerResult.landmarks()) {
                for(normalizedLandmark in landmark) {
                    numberPaint.setColor(Color.WHITE)
                    numberPaint.setTextSize(20F)
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint)
                    canvas.drawText(
                        "$i",
                        normalizedLandmark.x() * imageWidth * scaleFactor + 2,
                        normalizedLandmark.y() * imageHeight * scaleFactor + 2,
                        numberPaint
                    )
                    i++
                }
                HandLandmarker.HAND_CONNECTIONS.forEach {
                    canvas.drawLine(
                        gestureRecognizerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                        gestureRecognizerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                        gestureRecognizerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
                        gestureRecognizerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                        linePaint)
                }
                /*HandLandmarker.HAND_CONNECTIONS.forEach {
                    canvas.drawLine(
                        gestureRecognizerResult.landmarks().get(0).get(8).x() * imageWidth * scaleFactor,
                        gestureRecognizerResult.landmarks().get(0).get(8).y() * imageHeight * scaleFactor,
                        gestureRecognizerResult.landmarks().get(0).get(12).x() * imageWidth * scaleFactor,
                        gestureRecognizerResult.landmarks().get(0).get(12).y() * imageHeight * scaleFactor,
                        linePaint)
                }*/
            }
            i = 0
        }
    }

    fun setResults(
        gestureRecognizerResult: GestureRecognizerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
        counter: Int,
        setsCompleted1: Int,
        repetitions1: Int?,
        sets1: Int?,
        statusExercise: Boolean
    ) {

        repetitions = repetitions1
        sets = sets1
        counterFinger = counter
        results = gestureRecognizerResult
        setsCompleted = setsCompleted1
        exerciseCompleted = statusExercise

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
    }
}
