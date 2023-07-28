package com.example.handrehab

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handrehab.databinding.FragmentCameraBinding
import com.google.android.material.math.MathUtils.dist
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class CameraFragment : Fragment(),
    GestureRecognizerHelper.GestureRecognizerListener {

    companion object {
        private const val TAG = "Hand gesture recognizer"
        private const val TAG2 = "open palm detector"
        private const val TAG3 = "little Finger"
        private const val TAG4 = "Thumb"
        private const val TAG5 = "Middle Finger"
        private const val TAG6 = "All Fingers spread"
        private const val TAG7 = "Thumb counter"


    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var defaultNumResults = 1
    private val gestureRecognizerResultAdapter: GestureRecognizerResultsAdapter by lazy {
        GestureRecognizerResultsAdapter().apply {
            updateAdapterSize(defaultNumResults)
        }
    }
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT
    private var closed = ArrayList<Int>()
    private var counterRepetition = 0
    private var fingerOneClosed = false
    private var fingerTwoClosed = false
    private var fingerThreeClosed = false
    private var fingerFourClosed = false
    private var allFingersClosed = false

    private var dist0812Before = 0f
    private var pointingFingerSpread = false
    private var distFingersSpreadMax = 0f
    private var distFingersSpreadMin = 10f
    private var fingersSpreadCounter = 0

    private var dist1620Before = 0f
    private var distLittleFingerSpreadMax = 0f
    private var distLittleFingerSpreadMin = 10f
    private var littleFingerSpread = false
    private var littleFingerCounter = 0

    private var dist0408Before = 0f
    private var distThumbSpreadMax = 0f
    private var distThumbSpreadMin = 10f
    private var thumbSpread = false
    private var thumbSpreadCounter = 0

    private var dist1216Before = 0f
    private var distMiddleFingerSpreadMax = 0f
    private var distMiddleFingerSpreadMin =0f
    private var middleFingerSpread = false
    private var middleFingerSpreadCounter = 0

    private var allFingersSpread = false

    private var thumbUp = false
    private var counterUpDown = 0


    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main).navigate(R.id.camera_to_permissons)
        }

        // Start the GestureRecognizerHelper again when users come back
        // to the foreground.
        backgroundExecutor.execute {
            if (gestureRecognizerHelper.isClosed()) {
                gestureRecognizerHelper.setupGestureRecognizer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::gestureRecognizerHelper.isInitialized) {
            viewModel.setMinHandDetectionConfidence(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(gestureRecognizerHelper.currentDelegate)

            // Close the Gesture Recognizer helper and release resources
            backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(fragmentCameraBinding.recyclerviewResults) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gestureRecognizerResultAdapter
        }

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Create the Hand Gesture Recognition Helper that will handle the
        // inference
        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                currentDelegate = viewModel.currentDelegate,
                gestureRecognizerListener = this
            )
        }

        // Attach listeners to UI control widgets
        initBottomSheetControls()
    }

    private fun initBottomSheetControls() {
        // init bottom sheet settings
        fragmentCameraBinding.bottomSheetLayout.detectionThresholdValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentMinHandDetectionConfidence
            )
        fragmentCameraBinding.bottomSheetLayout.trackingThresholdValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentMinHandTrackingConfidence
            )
        fragmentCameraBinding.bottomSheetLayout.presenceThresholdValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentMinHandPresenceConfidence
            )

        // When clicked, lower hand detection score threshold floor
        fragmentCameraBinding.bottomSheetLayout.detectionThresholdMinus.setOnClickListener {
            if (gestureRecognizerHelper.minHandDetectionConfidence >= 0.2) {
                gestureRecognizerHelper.minHandDetectionConfidence -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise hand detection score threshold floor
        fragmentCameraBinding.bottomSheetLayout.detectionThresholdPlus.setOnClickListener {
            if (gestureRecognizerHelper.minHandDetectionConfidence <= 0.8) {
                gestureRecognizerHelper.minHandDetectionConfidence += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, lower hand tracking score threshold floor
        fragmentCameraBinding.bottomSheetLayout.trackingThresholdMinus.setOnClickListener {
            if (gestureRecognizerHelper.minHandTrackingConfidence >= 0.2) {
                gestureRecognizerHelper.minHandTrackingConfidence -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise hand tracking score threshold floor
        fragmentCameraBinding.bottomSheetLayout.trackingThresholdPlus.setOnClickListener {
            if (gestureRecognizerHelper.minHandTrackingConfidence <= 0.8) {
                gestureRecognizerHelper.minHandTrackingConfidence += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, lower hand presence score threshold floor
        fragmentCameraBinding.bottomSheetLayout.presenceThresholdMinus.setOnClickListener {
            if (gestureRecognizerHelper.minHandPresenceConfidence >= 0.2) {
                gestureRecognizerHelper.minHandPresenceConfidence -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise hand presence score threshold floor
        fragmentCameraBinding.bottomSheetLayout.presenceThresholdPlus.setOnClickListener {
            if (gestureRecognizerHelper.minHandPresenceConfidence <= 0.8) {
                gestureRecognizerHelper.minHandPresenceConfidence += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, change the underlying hardware used for inference.
        // Current options are CPU and GPU
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
            viewModel.currentDelegate, false
        )
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {
                    try {
                        gestureRecognizerHelper.currentDelegate = p2
                        updateControlsUi()
                    } catch(e: UninitializedPropertyAccessException) {
                        Log.e(TAG, "GestureRecognizerHelper has not been initialized yet.")

                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }
    }

    // Update the values displayed in the bottom sheet. Reset recognition
    // helper.
    private fun updateControlsUi() {
        fragmentCameraBinding.bottomSheetLayout.detectionThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minHandDetectionConfidence
            )
        fragmentCameraBinding.bottomSheetLayout.trackingThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minHandTrackingConfidence
            )
        fragmentCameraBinding.bottomSheetLayout.presenceThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minHandPresenceConfidence
            )

        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when applicable
        backgroundExecutor.execute {
            gestureRecognizerHelper.clearGestureRecognizer()
            gestureRecognizerHelper.setupGestureRecognizer()
        }
        fragmentCameraBinding.overlay.clear()
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        recognizeHand(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun recognizeHand(imageProxy: ImageProxy) {
        gestureRecognizerHelper.recognizeLiveStream(
            imageProxy = imageProxy,
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after a hand gesture has been recognized. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView. Only one result is expected at a time. If two or more
    // hands are seen in the camera frame, only one will be processed.
    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {

        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                // Show result of recognized gesture
                val gestureCategories = resultBundle.results.first().gestures()


                if (gestureCategories.isNotEmpty()) {
                    gestureRecognizerResultAdapter.updateResults(gestureCategories.first())

                    calculateDistance(resultBundle.results.first())

                    thumbUpThumbDown(resultBundle.results.first())

                } else {
                    gestureRecognizerResultAdapter.updateResults(emptyList())
                    fingersSpreadCounter = 0
                    littleFingerCounter = 0
                    counterUpDown = 0
                }

                fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                    String.format("%d ms", resultBundle.inferenceTime)

                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    private fun detectSpreadPointingFinger(d0812: Float) {

        //Maximum
        if(d0812 > distFingersSpreadMax) {
            distFingersSpreadMax = d0812
            Log.i(TAG2, "Maximale Distanz: $distFingersSpreadMax")
        }

        //Minimum
        if(d0812 < distFingersSpreadMin) {
            distFingersSpreadMin = d0812
        }

        dist0812Before = d0812

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        val distMinMax = distFingersSpreadMax - distFingersSpreadMin
        Log.i(TAG2, "Maximale Distanz: $distMinMax")

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if(d0812 > (distFingersSpreadMin + distMinMax/2)) {
            if(!pointingFingerSpread) {
                fingersSpreadCounter++
                Log.i(TAG2, "Finger Spread counter: $fingersSpreadCounter")
                pointingFingerSpread = true
            }
        }

        if(d0812 < ((distFingersSpreadMin + distMinMax/2))) {
            if(pointingFingerSpread) {
                pointingFingerSpread = false
            }
        }

    }

    private fun detectClosedFingers (d07: Float, d08: Float, d011: Float, d012: Float, d015: Float, d016: Float,
    d19: Float, d20: Float, gestureRecognizer: GestureRecognizerResult) {

        //Geschlossene Finger detektieren
        //Zeigefinger geschlossen
        if(d07 > d08 && !fingerOneClosed) {
            closed.add(1)
            fingerOneClosed = true
            Log.i(TAG2, "FInger 1 closed")
        }

        //Mittelfinger geschlossen
        if(d011 > d012 && !fingerTwoClosed) {
            closed.add(2)
            fingerTwoClosed = true
            Log.i(TAG2, "FInger 2 closed")
        }

        //Ringfinger geschlossen
        if(d015 > d016 && !fingerThreeClosed) {
            closed.add(3)
            fingerThreeClosed = true
            Log.i(TAG2, "FInger 3 closed")
        }

        //Kleiner Finger geschlossen
        if(d19 > d20 && !fingerFourClosed) {
            closed.add(4)
            fingerFourClosed = true
            Log.i(TAG2, "FInger 4 closed")
        }

        // Wenn alle Finger geschlossen sind, wird der Counter für die Anzahl an Wiederholungen hochgezählt
        if(closed.size == 4 && !allFingersClosed) {
            counterRepetition++
            Log.i(TAG2, "Wiederholungen: $counterRepetition")
            allFingersClosed = true
            closed.clear()
        }

        //Wenn die Hand wieder geöffnet ist, werden die Werte zurückgesetzt
        if(gestureRecognizer.gestures().first()[0].categoryName() == "Open_Palm"){
            fingerOneClosed = false
            fingerTwoClosed = false
            fingerThreeClosed = false
            fingerFourClosed = false
            allFingersClosed = false
        }
    }

    private fun detectOrientation(x9: Float, x0: Float, y9: Float, y0: Float) {
        //Berechnung der Ausrichtung der offenen Hand
        val m: Float = if (abs(x9 - x0) < 0.05)
            1000000000f
        else
            abs((y9 - y0)/(x9 - x0))

        if (m in 0.0..1.0) {
            if (x9 > x0)
                Log.i(TAG2, "RIGHT")
            else
                Log.i(TAG2, "LEFT")
        }

        if (m>1) {
            if (y9 < y0)
                Log.i(TAG2, "UP")
            else
                Log.i(TAG2, "DOWN")
        }
    }

    private fun detectSpreadLittleFinger (d1620: Float) {

        //Maximum
        if(d1620 > distLittleFingerSpreadMax) {
            distLittleFingerSpreadMax = d1620
            Log.i(TAG3, "Maximale Distanz kleiner finger: $distLittleFingerSpreadMax")
        }

        //Minimum
        if(d1620 < distLittleFingerSpreadMin) {
            distLittleFingerSpreadMin = d1620
            Log.i(TAG3, "Minimale Distanz kleiner finger: $distLittleFingerSpreadMin")
        }

        dist1620Before = d1620

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        val distMinMax = distLittleFingerSpreadMax - distLittleFingerSpreadMin
        Log.i(TAG3, "Maximale Distanz: $distMinMax")

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if(d1620 > (distLittleFingerSpreadMin + distMinMax/2)) {
            if(!littleFingerSpread) {
                littleFingerCounter++
                Log.i(TAG3, "Finger Spread counter: $fingersSpreadCounter")
                littleFingerSpread = true
            }
        }

        if(d1620 < ((distLittleFingerSpreadMin + distMinMax/2))) {
            if(littleFingerSpread) {
                littleFingerSpread = false
            }
        }
    }

    private fun detectThumbSpread (d0408: Float) {

        //Maximum
        if(d0408 > distThumbSpreadMax) {
            distThumbSpreadMax = d0408
            Log.i(TAG4, "Maximale Distanz daumen: $distThumbSpreadMax")
        }

        //Minimum
        if(d0408 < distThumbSpreadMin) {
            distThumbSpreadMin = d0408
            Log.i(TAG4, "Minimale Distanz daumen: $distThumbSpreadMin")
        }

        dist0408Before = d0408

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        val distMinMax = distThumbSpreadMax - distThumbSpreadMin
        Log.i(TAG4, "Maximale Distanz: $distMinMax")

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if(d0408 > (distThumbSpreadMin + distMinMax/2)) {
            if(!thumbSpread) {
                thumbSpreadCounter++
                Log.i(TAG4, "Thumb Spread counter: $thumbSpreadCounter")
                thumbSpread = true
            }
        }

        if(d0408 < ((distThumbSpreadMin + distMinMax/2))) {
            if(thumbSpread) {
                thumbSpread = false
            }
        }
    }

    private fun detectMiddleFingerSpread (d1216: Float) {


        //Maximum
        if(d1216 > distMiddleFingerSpreadMax) {
            distMiddleFingerSpreadMax = d1216
            Log.i(TAG5, "Maximale Distanz Mittel-und Ringfinger: $distMiddleFingerSpreadMax")
        }

        //Minimum
        if(d1216 < distMiddleFingerSpreadMin) {
            distMiddleFingerSpreadMin = d1216
            Log.i(TAG5, "Minimale Distanz daumen: $distMiddleFingerSpreadMin")
        }

        dist1216Before = d1216

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        val distMinMax = distMiddleFingerSpreadMax - distMiddleFingerSpreadMin
        Log.i(TAG5, "Maximale Distanz: $distMinMax")

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if(d1216 > (distMiddleFingerSpreadMin + distMinMax/2)) {
            if(!middleFingerSpread) {
                Log.i(TAG5, "MiddleFinger Spread counter: $middleFingerSpreadCounter")
                middleFingerSpread = true
            }
        }

        if(d1216 < ((distMiddleFingerSpreadMin + distMinMax/2))) {
            if(middleFingerSpread) {
                middleFingerSpreadCounter++
                middleFingerSpread = false
            }
        }
    }

    private fun detectAllFingersSpread () {

        if(thumbSpread && middleFingerSpread && littleFingerSpread && pointingFingerSpread) {
            allFingersSpread = true
            Log.i(TAG6, "Alle Finger offen")
        }
        else {
            allFingersSpread = false
        }
    }

   private fun calculateDistance (gestureRecognizer: GestureRecognizerResult) {

       val x0 = gestureRecognizer.worldLandmarks()[0][0].x()
       val y0 = gestureRecognizer.worldLandmarks()[0][0].y()

       val x4 = gestureRecognizer.worldLandmarks()[0][4].x()
       val y4 = gestureRecognizer.worldLandmarks()[0][4].y()

       val x9 = gestureRecognizer.worldLandmarks()[0][9].x()
       val y9 = gestureRecognizer.worldLandmarks()[0][9].y()

       val x8 = gestureRecognizer.worldLandmarks()[0][8].x()
       val y8 = gestureRecognizer.worldLandmarks()[0][8].y()

       val x7 = gestureRecognizer.worldLandmarks()[0][7].x()
       val y7 = gestureRecognizer.worldLandmarks()[0][7].y()

       val x12 = gestureRecognizer.worldLandmarks()[0][12].x()
       val y12 = gestureRecognizer.worldLandmarks()[0][12].y()

       val x11 = gestureRecognizer.worldLandmarks()[0][11].x()
       val y11 = gestureRecognizer.worldLandmarks()[0][11].y()

       val x16 = gestureRecognizer.worldLandmarks()[0][16].x()
       val y16 = gestureRecognizer.worldLandmarks()[0][16].y()

       val x15 = gestureRecognizer.worldLandmarks()[0][15].x()
       val y15 = gestureRecognizer.worldLandmarks()[0][15].y()

       val x20 = gestureRecognizer.worldLandmarks()[0][20].x()
       val y20 = gestureRecognizer.worldLandmarks()[0][20].y()

       val x19 = gestureRecognizer.worldLandmarks()[0][19].x()
       val y19 = gestureRecognizer.worldLandmarks()[0][19].y()


       //Berechnung der Distanz Fingerspitze - Grundgelenk und Mittelgelenk Finger - Grundgelenk
       val d08 = dist(x0, y0, x8, y8)
       val d07 = dist(x0, y0, x7, y7)

       val d012 = dist(x0, y0, x12, y12)
       val d011 = dist(x0, y0, x11, y11)

       val d015 = dist(x0, y0, x15, y15)
       val d016 = dist(x0, y0, x16, y16)

       val d20 = dist(x0, y0, x20, y20)
       val d19 = dist(x0, y0, x19, y19)

       //Distanz zwischen Zeige- und Mittelfingerspitze
       var d0812 = dist(x8, y8, x12, y12)

       //Runden der Dezimalzahl auf 3 Nachkommastellen
       val bd0812 = BigDecimal(d0812.toDouble())
       d0812 = bd0812.setScale(4, RoundingMode.DOWN).toFloat()

       //Distanz zwischen Ringfingerspitze und der Spitze des kleinen Fingers
       var d1620 = dist(x16, y16, x20, y20)

       //Runden der Dezimalzahl auf 3 Nachkommastellen
       val bd1620 = BigDecimal(d1620.toDouble())
       d1620 = bd1620.setScale(4, RoundingMode.DOWN).toFloat()

       //Distanz zwischen Mittel- und Ringfingerspitze
       var d1216 = dist(x12, y12, x16, y16)

       //Runden der Dezimalzahl auf 3 Nachkommastellen
       val bd1216 = BigDecimal(d1216.toDouble())
       d1216 = bd1216.setScale(4, RoundingMode.DOWN).toFloat()

       //Distanz zwischen Daumen und Zeigefinger
       var d0408 = dist(x4, y4, x8, y8)

       //Runden der Dezimalzahl auf 3 Nachkommastellen
       val bd0408 = BigDecimal(d0408.toDouble())
       d0408 = bd0408.setScale(4, RoundingMode.DOWN).toFloat()


       detectSpreadPointingFinger(d0812)

       detectSpreadLittleFinger(d1620)

       detectThumbSpread(d0408)

       detectMiddleFingerSpread(d1216)

       detectAllFingersSpread()

       detectClosedFingers(d07, d08, d011, d012, d015, d016, d19, d20, gestureRecognizer)

       detectOrientation(x9, x0, y9, y0)

   }

    private fun thumbUpThumbDown (gestureRecognizer: GestureRecognizerResult) {

        if(gestureRecognizer.gestures().first()[0].categoryName() == "Thumb_Up") {
            thumbUp = true
        }

        if(gestureRecognizer.gestures().first()[0].categoryName() == "Thumb_Down" && thumbUp){
            thumbUp = false
            counterUpDown++
            Log.i(TAG7, "Thumb counter: $counterUpDown")
        }

    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            gestureRecognizerResultAdapter.updateResults(emptyList())

            if (errorCode == GestureRecognizerHelper.GPU_ERROR) {
                fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
                    GestureRecognizerHelper.DELEGATE_CPU, false
                )
            }
        }
    }
}
