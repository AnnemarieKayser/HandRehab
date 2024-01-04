package com.example.handrehab.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.ekn.gruzer.gaugelibrary.Range
import com.example.handrehab.Data
import com.example.handrehab.DataMinMax
import com.example.handrehab.MainViewModel
import com.example.handrehab.PermissionsFragment
import com.example.handrehab.databinding.FragmentCameraBinding
import com.example.handrehab.item.Exercises
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.math.MathUtils.dist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import splitties.toast.toast
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import com.example.handrehab.R


class CameraFragment : Fragment(),
    GestureRecognizerHelper.GestureRecognizerListener {

    companion object {
        private const val TAG = "test123Hand gesture recognizer"
        private const val TAG2 = "test123open palm detector"
        const val TAG3 = "test123little Finger"
        private const val TAG4 = "test123Thumb"
        private const val TAG5 = "test123Middle Finger"
        private const val TAG6 = "test123All Fingers spread"
        private const val TAG7 = "test123Thumb counter"
        private const val TAG8 = "test123Pointing Finger"
        private const val TAG9 = "test123 xKoordinaten"

    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    // === Firebase database === //
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }


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


    // min und max Werte
    private var Min = 10f
    private var Max = 0f
    private var divideFactor = 2.0

    private var pointingFingerSpreadMax = 0f
    private var pointingFingerSpreadMin = 10f
    private var pointingFingerSpread = false
    private var fingersSpreadCounter = 0


    private var littleFingerSpreadMax = 0f
    private var littleFingerSpreadMin = 10f
    private var littleFingerSpread = false
    private var littleFingerCounter = 0

    private var thumbSpreadMax = 0f
    private var thumbSpreadMin = 10f
    private var thumbSpread = false
    private var thumbSpreadCounter = 0

    private var middleFingerSpreadMax = 0f
    private var middleFingerSpreadMin = 10f
    private var middleFingerSpread = false

    private var allFingersSpread = false

    private var thumbUp = false
    private var counterUpDown = 0

    //all Fingers
    private var allFingersOpen = false
    private var allFingersClosed = false
    private var allFingershalfClosed = false
    private var counterAllFingersSpread = 0
    private var counterAllFingersOpenClose = 0

    //Thumb
    private var thumbOpen = false
    private var thumbClosed = false
    private var thumbHalfClosed = false
    private var thumbMax = 0f
    private var thumbMin = 10f
    private var thumbOpenClose = false

    //PointingFinger
    private var pointingFingerOpen = false
    private var pointingFingerClosed = false
    private var pointingFingerHalfClosed = false
    private var pointingFingerHalfOpen = false
    private var pointingFingerMax = 0f
    private var pointingFingerMin = 10f
    private var pointingFingerOpenClose = false

    //Middle Finger
    private var middleFingerOpen = false
    private var middleFingerClosed = false
    private var middleFingerHalfClosed = false
    private var middleFingerHalfOpen = false
    private var middleFingerMax = 0f
    private var middleFingerMin = 10f
    private var middleFingerOpenClose = false

    //Ring Finger
    private var ringFingerOpen = false
    private var ringFingerClosed = false
    private var ringFingerHalfClosed = false
    private var ringFingerHalfOpen = false
    private var ringFingerMax = 0f
    private var ringFingerMin = 10f
    private var ringFingerOpenClose = false

    //Little Finger
    private var littleFingerOpen = false
    private var littleFingerClosed = false
    private var littleFingerHalfClosed = false
    private var littleFingerHalfOpen = false
    private var littleFingerMax = 0f
    private var littleFingerMin = 10f
    private var littleFingerOpenClose = false

    //Orientation of Hand
    private var orientationHand = ""

    //Hand Joint angle
    private var handJointMaxAngleLeft = 90.0
    private var handJointMaxAngleRight = -90.0
    private var handJointLeft = false

    //Counter all
    private var counter = 0
    private var sets = 0
    private var exerciseCompleted = false
    private var counterHalfClosed = 0
    private var counterHalfOpen = 0

    //Aktuelle Phase
    private var currentPhase = 1
    private var phase = ""

    //show infos
    private var showInfo = true

    //Alert-Dialog
    private var alertDialogShown = false

    //Handseite
    private var selectedHandSide = ""


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
        savedInstanceState: Bundle?,
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //set min max and current value
        //set min max and current value
        fragmentCameraBinding.halfGauge.minValue = 0.0
        fragmentCameraBinding.halfGauge.maxValue = 100.0
        fragmentCameraBinding.halfGauge.value = 0.0

        fragmentCameraBinding.halfGauge.setNeedleColor(Color.DKGRAY)
        fragmentCameraBinding.halfGauge.valueColor = Color.TRANSPARENT
        fragmentCameraBinding.halfGauge.minValueTextColor = Color.RED
        fragmentCameraBinding.halfGauge.maxValueTextColor = Color.GREEN


        setHasOptionsMenu(true)

        divideFactor = viewModel.getDivideFactor()!!

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        //Ausgewählte Hand zu Variablen zuweisen
        selectedHandSide = viewModel.getSelectedHandSide()!!

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        val view = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        view.visibility = GONE


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
        val id = viewModel.getSelectedExercise()?.id

        if (id == 2 || id == 20 || id == 13 || id == 15 || id == 16 || id == 17 || id == 18) {
            fragmentCameraBinding.halfGauge.visibility = GONE
        }

        currentPhase = if(id == 12){
            7
        } else{
            if(viewModel.getStartModus() == getString(R.string.closed)) 6 else 1
        }

        if(id == 13){
            currentPhase = if(viewModel.getStartModus() == getString(R.string.closed)) 14 else 11
        }

        if(id == 12){

            val range = Range()
            range.color = Color.RED
            range.from = 0.0
            range.to = 33.0

            val range3 = Range()
            range3.color = Color.YELLOW
            range3.from = 33.0
            range3.to = 66.0

            val range2 = Range()
            range2.color = Color.GREEN
            range2.from = 66.0
            range2.to = 100.0

            fragmentCameraBinding.halfGauge.addRange(range)
            fragmentCameraBinding.halfGauge.addRange(range2)
            fragmentCameraBinding.halfGauge.addRange(range3)

        } else {
            var rangeMiddle = 50.0

            when(viewModel.getDivideFactor()){
                1.5 -> rangeMiddle = 75.0
                2.0 -> rangeMiddle = 50.0
                3.0 -> rangeMiddle = 25.0
            }

            val range = Range()
            range.color = Color.RED
            range.from = 0.0
            range.to = rangeMiddle

            val range2 = Range()
            range2.color = Color.GREEN
            range2.from = rangeMiddle
            range2.to = 100.0

            fragmentCameraBinding.halfGauge.addRange(range)
            fragmentCameraBinding.halfGauge.addRange(range2)
        }

    }

    private fun initBottomSheetControls() {
        // init bottom sheet settings

    }

    // === onCreateOptionsMenu === //
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu)
    }

    // === onOptionsItemSelected === //
    // Hier werden Klicks auf Elemente der Aktionsleiste behandelt
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.app_bar_switch -> {
                showInfo = !showInfo
                Log.i("InfoToggleButton", showInfo.toString())

                if (showInfo) fragmentCameraBinding.halfGauge.visibility = VISIBLE
                else fragmentCameraBinding.halfGauge.visibility = GONE
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    // Update the values displayed in the bottom sheet. Reset recognition
    // helper.
    private fun updateControlsUi() {
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


                    // Überprüfung, ob die richtige Handseite in die Kamera gehalten wird
                    val detectedHandSide = resultBundle.results.first().handednesses()[0][0].displayName()

                    if (viewModel.getSelectedHandSide() == "rechts") {
                        if (detectedHandSide == "Left") {
                            if (!alertDialogShown) {
                                alertDialogShown = true
                                context?.let {
                                    MaterialAlertDialogBuilder(it)
                                        .setTitle(resources.getString(R.string.title_change_hand))
                                        .setMessage(resources.getString(R.string.supporting_text_change_hand_right))
                                        .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                                            // Respond to positive button press
                                            alertDialogShown = false
                                        }
                                        .show()
                                }
                            }
                        }
                        if (detectedHandSide == "Right") {
                            // Die Ergebnisse werden weiter ausgewertet
                            calculateDistance(resultBundle.results.first())
                        }
                    }

                    if (viewModel.getSelectedHandSide() == "links") {
                        if (detectedHandSide == "Right") {
                            if (!alertDialogShown) {
                                alertDialogShown = true
                                context?.let {
                                    MaterialAlertDialogBuilder(it)
                                        .setTitle(resources.getString(R.string.title_change_hand))
                                        .setMessage(resources.getString(R.string.supporting_text_change_hand_left))
                                        .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                                            // Respond to positive button press
                                            alertDialogShown = false
                                        }
                                        .show()
                                }
                            }
                        }

                        if (detectedHandSide == "Left") {
                            calculateDistance(resultBundle.results.first())
                        }
                    }


                    Log.i(
                        "SelectedHandSide",
                        resultBundle.results.first().handednesses()[0][0].displayName()
                    )

                    // vielleicht für spiel verwenden? -> Ansonsten entfernen
                   // thumbUpThumbDown(resultBundle.results.first())

                } else {
                    gestureRecognizerResultAdapter.updateResults(emptyList())
                    fingersSpreadCounter = 0
                    littleFingerCounter = 0
                    // counter = 0
                    counterUpDown = 0
                }


                if (viewModel.getSelectedExercise()?.id == 2) {
                    counter = counterAllFingersSpread
                }

                if (viewModel.getSelectedExercise()?.id == 13) {
                    counter = counterAllFingersOpenClose
                }


                var id = viewModel.getSelectedExercise()?.id

                if (id == 18 || id == 15 || id == 16 || id == 17) {
                    if (viewModel.getStartModus() == getString(R.string.start_mode_open)) {
                        counter = counterHalfClosed
                    } else counter = counterHalfOpen
                }

                if (counter == viewModel.getRepetitions()) {
                    counter = 0
                    counterAllFingersSpread = 0
                    counterAllFingersOpenClose = 0
                    counterHalfClosed = 0
                    counterHalfOpen = 0
                    sets++

                    if (sets == viewModel.getSets()) {
                        //Übung abgeschlossen
                        exerciseCompleted = true

                        val listExercises = arrayListOf<Exercises>()


                        for(i in viewModel.getListDay()!!) {

                            if(i != viewModel.getSelectedExercise()){
                                listExercises.add(i)
                            }
                        }

                        viewModel.setListDay(listExercises)

                        //Speichern der Übung in der Datenbank
                        if(viewModel.getSelectedExercise()!!.id == 2){
                            saveMinMax()
                        }

                        saveExercise()

                        viewModel.setSets(0)
                        viewModel.setRepetitions(0)

                    }
                }


                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM,
                    counter,
                    sets,
                    viewModel.getRepetitions(),
                    viewModel.getSets(),
                    exerciseCompleted,
                    viewModel.getSelectedExercise()!!.textItem,
                    showInfo,
                    viewModel.getSelectedHandSide()!!,
                    viewModel.getStartModus()!!,
                )

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun saveMinMax() {


        // Einlesen des aktuellen Datums
        val kalender: Calendar = Calendar.getInstance()
        val zeitformat = SimpleDateFormat("yyyy-MM-dd-kk-mm-ss")
        val date = zeitformat.format(kalender.time)

        var datetimestamp: Date? = null
        try {
            datetimestamp = zeitformat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        //Data Objekt mit Daten befüllen (ID wird automatisch ergänzt)
        val data = DataMinMax()
        data.setMaxLittleFinger(littleFingerSpreadMax*100)
        data.setMaxPointingLFinger(pointingFingerSpreadMax*100)
        data.setMaxMiddleFinger(middleFingerSpreadMax*100)
        data.setMaxThumbFinger(thumbSpreadMax*100)
        data.setExerciseName(viewModel.getSelectedExercise()!!.textItem)
        data.setDate(datetimestamp!!)
        data.setExerciseId(viewModel.getSelectedExercise()!!.id)
        data.setRepetitions(viewModel.getRepetitions()!!)
        data.setSets(viewModel.getSets()!!)
        data.setSelectedHandSide(viewModel.getSelectedHandSide()!!)


        // Schreibe Daten als Document in die Collection Messungen in DB;
        // Eine id als Document Name wird automatisch vergeben
        // Implementiere auch onSuccess und onFailure Listender
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenMinMax")
            .add(data)
            .addOnSuccessListener { documentReference ->
                toast(getString(R.string.save))
            }
            .addOnFailureListener { e ->
                toast(getString(R.string.not_save))
            }
    }

    private fun saveExercise() {


        // Einlesen des aktuellen Datums
        val kalender: Calendar = Calendar.getInstance()
        val zeitformat = SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.GERMANY)
        val date = zeitformat.format(kalender.time)
        val day = kalender.get(Calendar.DAY_OF_WEEK) - 1

        var datetimestamp: Date? = null
        try {
            datetimestamp = zeitformat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }


        //Data Objekt mit Daten befüllen (ID wird automatisch ergänzt)
        val data = Data()
        data.setCounterExercises(counter)
        data.setRepetitions(viewModel.getRepetitions()!!)
        data.setSets(viewModel.getSets()!!)
        data.setExerciseName(viewModel.getSelectedExercise()!!.textItem)
        data.setDate(datetimestamp!!)
        data.setDayOfWeek(day)
        data.setStartMode(viewModel.getStartModus()!!)
        data.setSelectedHandSide(viewModel.getSelectedHandSide()!!)
        if(viewModel.getSelectedExercise()!!.id == 13 || viewModel.getSelectedExercise()!!.id == 2){
            data.setMin(0f)
            data.setMax(0f)
        } else{
            data.setMin(Min * 100)
            data.setMax(Max * 100)
        }
        data.setExerciseId(viewModel.getSelectedExercise()!!.id)

        when(currentPhase) {
            1 -> phase = "Der Finger ist geöffnet"
            2 -> phase = "Der Finger ist halb geöffnet"
            3 -> phase = "Der Finger ist fast geschlossen"
            4 -> phase = "Der Finger ist fast geöffnet"
            5 -> phase = "Der Finger ist halb geschlossen"
            6 -> phase = "Der Finger ist geschlossen"
            7 -> phase = "Daumen geöffnet"
            8 -> phase = "Daumen überkreut Zeigefinger"
            9 -> phase = "Daumen überkreuzt Mittelfinger"
            10 -> phase = "Daumen überkreut Ringfinger"
            11 -> phase = "Alle Finger sind geöffnet"
            12 -> phase = "Alle Finger sind halb geöffnet"
            13 -> phase = "Alle Finger sind halb geschlossen"
            14 -> phase = "Alle Finger sind geschlossen"
        }

        if(viewModel.getStartModus() == getString(R.string.start_mode_close) || viewModel.getStartModus() == getString(R.string.start_mode_open)){
            data.setCurrentPhase(phase)
        }

        // Schreibe Daten als Document in die Collection Messungen in DB;
        // Eine id als Document Name wird automatisch vergeben
        // Implementiere auch onSuccess und onFailure Listender
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("Daten")
            .add(data)
            .addOnSuccessListener { documentReference ->
                toast(getString(R.string.save))
                if(viewModel.getExerciseListMode() == 2){
                    findNavController().navigate(R.id.action_CameraFragment_to_exerciseListFragment)
                } else findNavController().navigate(R.id.action_CameraFragment_to_exerciseFragment)
            }
            .addOnFailureListener { e ->
                toast(getString(R.string.not_save))
            }
    }

    private fun setGaugeValues(distance: Float, threshold: Double, Min: Float, Max: Float) {

        val id = viewModel.getSelectedExercise()?.id
        if (id == 2 || id == 20 || id == 13) {
            //Nothing
        } else {

            if (viewModel.getStartModus() == getString(R.string.start_mode_open)) {
                fragmentCameraBinding.halfGauge.value =
                    ((Max - distance) / (Max - Min) * 100).toDouble()
            } else {
                fragmentCameraBinding.halfGauge.value =
                    ((distance - Min) / (Max - Min) * 100).toDouble()
            }


        }

    }


    ///------------------------------------------------------------------------------//
    ///------------------------------------------------------------------------------//
    /// --------------------- Spreizen der Finger ---------------------------------- //
    ///------------------------------------------------------------------------------//
    ///------------------------------------------------------------------------------//

    private fun pointingFingerSpread(d0812: Float) {



        //Maximum
        if (d0812 > pointingFingerSpreadMax) {
            Max = d0812
            pointingFingerSpreadMax = d0812
            Log.i(TAG2, "Maximale Distanz: $Max")
        }

        //Minimum
        if (d0812 < pointingFingerSpreadMin) {
            Min = d0812
            pointingFingerSpreadMin = d0812
        }

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        var distMinMax = pointingFingerSpreadMax - pointingFingerSpreadMin
        if(distMinMax < 0.01){
            distMinMax = 0.008f
        }
        val threshold = pointingFingerSpreadMin + distMinMax / divideFactor
        Log.i(TAG2, "Maximale Distanz: $distMinMax")

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if (d0812 > threshold) {
            Log.i(
                TAG2,
                "factor: $divideFactor, distMinMAx: $distMinMax, ergebnis: ${distMinMax / divideFactor}"
            )
            if (!pointingFingerSpread) {
                Log.i(TAG2, "Finger Spread counter: $fingersSpreadCounter")
                pointingFingerSpread = true
            }
        }

        if (d0812 < threshold) {
            if (pointingFingerSpread) {
                counter++
                pointingFingerSpread = false
            }
        }


        setGaugeValues(d0812, threshold, pointingFingerSpreadMin, pointingFingerSpreadMax)

    }

    private fun littleFingerSpread(d1620: Float) {


        // Maximaler Abstand zwischen dem kleinen Finger und dem Ringfinger
        if (d1620 > littleFingerSpreadMax) {
            Max = d1620
            littleFingerSpreadMax = d1620
        }

        // Minimaler Abstand zwischen dem kleinen Finger und dem Ringfinger
        if (d1620 < littleFingerSpreadMin) {
            Min = d1620
            littleFingerSpreadMin = d1620
        }

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        var distMinMax = littleFingerSpreadMax - littleFingerSpreadMin

        if(distMinMax < 0.01){
            distMinMax = 0.008f
        }
        val threshold = littleFingerSpreadMin + distMinMax / divideFactor

        if (d1620 > threshold) {
            if (!littleFingerSpread) {
                littleFingerSpread = true
            }
        }

        // Hochzählen des Counters, wenn der Grenzwert wieder unterschritten wird
        if (d1620 < threshold) {
            if (littleFingerSpread) {
                counter++
                littleFingerSpread = false
            }
        }

        setGaugeValues(d1620, threshold, littleFingerSpreadMin, littleFingerSpreadMax)
    }

    private fun thumbSpread(d0408: Float) {



        //Minimum
        if (d0408 < thumbSpreadMin) {
            Min = d0408
            thumbSpreadMin = d0408
            Log.i(TAG4, "Minimale Distanz daumen: $Min")
        }

        //Maximum
        if (d0408 > thumbSpreadMax) {
            //Max = d0408
            thumbSpreadMax = d0408
            Log.i(TAG4, "Maximale Distanz daumen: $Max")
        }


        //Berechnung der Maximalen Distanz
        var distMinMax = thumbSpreadMax - thumbSpreadMin
        if(distMinMax < 0.01){
            distMinMax = 0.008f
        }
        val threshold = thumbSpreadMin + distMinMax / divideFactor

        if(distMinMax > Max){
            Max = distMinMax
        }

        Log.i(TAG4, "Maximale Distanz: $distMinMax")

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if (d0408 > threshold) {
            if (!thumbSpread) {
                Log.i(TAG4, "Thumb Spread counter: $thumbSpreadCounter")
                thumbSpread = true
            }
        }

        if (d0408 < threshold) {
            if (thumbSpread) {
                counter++
                thumbSpread = false
            }
        }

        setGaugeValues(d0408, threshold, thumbSpreadMin, thumbSpreadMax)
    }

    private fun middleFingerSpread(d1216: Float) {


        //Maximum
        if (d1216 > middleFingerSpreadMax) {
            Max = d1216
            middleFingerSpreadMax = d1216
            Log.i(TAG5, "Maximale Distanz Mittel-und Ringfinger: $Max")
        }

        //Minimum
        if (d1216 < middleFingerSpreadMin) {
            Min = d1216
            middleFingerSpreadMin = d1216
            Log.i(TAG5, "Minimale Distanz daumen: $Min")
        }

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        var distMinMax = middleFingerSpreadMax - middleFingerSpreadMin
        if(distMinMax < 0.01){
            distMinMax = 0.008f
        }
        val threshold = middleFingerSpreadMin + distMinMax / divideFactor
        Log.i(TAG5, "Maximale Distanz: $distMinMax")

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if (d1216 > threshold) {
            if (!middleFingerSpread) {
                middleFingerSpread = true
            }
        }

        if (d1216 < threshold) {
            if (middleFingerSpread) {
                counter++
                middleFingerSpread = false
            }
        }

        setGaugeValues(d1216, threshold, middleFingerSpreadMin, middleFingerSpreadMax)
    }

    private fun allFingersSpread() {

        // Überprüfung, ob alle Finger gespreizt sind
        if (thumbSpread && middleFingerSpread && littleFingerSpread && pointingFingerSpread) {
            if (!allFingersSpread) {
                // Anzahl der Wiederholungen wird hochgezählt
                counterAllFingersSpread++
            }
            allFingersSpread = true
        } else {
            allFingersSpread = false
        }
    }


    ///------------------------------------------------------------------------------//
    ///------------------------------------------------------------------------------//
    /// --------------------- Schließen/Öffnen der Finger -------------------------- //
    ///------------------------------------------------------------------------------//
    ///------------------------------------------------------------------------------//

    private fun pointingFingerDistanceToJoint(dy08: Float) {

        if(viewModel.getStartModus() == "offen") {
            if (dy08 < Min) {
                Min = dy08
                Max = dy08
                // Start offener Finger -> min wert wird gespeichert
            }
        } else {
            if (dy08 > Max) {
                Max = dy08
                //Start geschlossener Finger -> max wert wird gespeichert
            }
        }

        //Maximum
        if (dy08 > pointingFingerMax) {
            pointingFingerMax = dy08
        }

        //Minimum
        if (dy08 < pointingFingerMin) {
            pointingFingerMin = dy08
        }

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        var distMinMax = pointingFingerMax - pointingFingerMin
        if(distMinMax < 0.01){
            distMinMax = 0.01f
        }
        val threshold = pointingFingerMin + distMinMax / divideFactor

        //Hochzählen des Counters, wenn der Grenzwert wieder unterschritten wird
        if (dy08 > threshold) {
            if (!pointingFingerOpenClose) {
                pointingFingerOpenClose = true
            }
        }

        if (dy08 < threshold) {
            if (pointingFingerOpenClose) {
                counter++
                pointingFingerOpenClose = false
            }
        }

        setGaugeValues(dy08, threshold, pointingFingerMin, pointingFingerMax)
    }

    //Öffnen und Schließen des Zeigefingers
    private fun pointingFingerOpenClose(
        dist08: Float,
        dist07: Float,
        dist06: Float,
        dist05: Float,
        gestureRecognizer: GestureRecognizerResult
    ) {

        //Phase 1
        if (dist08 > dist07) {
            // 1 = Finger geöffnet
            if(viewModel.getStartModus() == getString(R.string.closed)) currentPhase = 1
            pointingFingerOpen = true
            pointingFingerHalfClosed = false
        }

        //Phase 2
        if (dist08 < dist07) {

            if (!pointingFingerHalfClosed) {
                counterHalfClosed++
            }
            pointingFingerHalfClosed = true

            //Phase 3
            if (dist08 < dist06) {


                if (dist08 < dist05) {
                    //6 = Finger geschlossen
                    if(viewModel.getStartModus() != getString(R.string.closed)) currentPhase = 6
                    pointingFingerOpen = false
                    pointingFingerHalfOpen = false
                    pointingFingerClosed = true
                } else {
                    if (viewModel.getStartModus() == getString(R.string.closed)) {
                        // 2 = Der Finger ist halb geöffnet
                        if(currentPhase != 4 && currentPhase != 1) currentPhase = 2
                    } else {
                        // 3 = Der Finger ist fast geschlossen
                        if(currentPhase != 6) currentPhase = 3
                    }

                    if (pointingFingerClosed) {
                        counterHalfOpen++
                    }

                    pointingFingerClosed = false
                }
            } else {
                if (viewModel.getStartModus() == getString(R.string.closed)) {
                    // 4 = Der Finger ist fast geöffnet
                    if(currentPhase != 1) currentPhase = 4
                } else{
                    // 5 = Der Finger ist halb geschlossen
                    if(currentPhase != 3 && currentPhase != 6) currentPhase = 5
                }

                pointingFingerHalfOpen = true

            }
        }
    }

    private fun middleFingerDistanceToJoint(dy012: Float) {

        if(viewModel.getStartModus() == "offen") {
            if (dy012 < Min) {
                Min = dy012
                Max = dy012
                Log.i(TAG8, "Zeigefinger Min: $Min")
                // Start offener Finger -> min wert wird gespeichert
            }
        } else {
            if (dy012 > Max) {
                Max = dy012
                Log.i(TAG8, "Zeigefinger Max: $Max")
                //Sie können ihren FInger schon weiter als das letzte mal öffnen
                //Start geschlossener Finger -> max wert wird gespeichert
            }
        }

        //Maximum
        if (dy012 > middleFingerMax) {
            middleFingerMax = dy012
            Log.i(TAG5, "Maximale Distanz Mittel-und Ringfinger: $Max")
        }

        //Minimum
        if (dy012 <  middleFingerMin) {
            middleFingerMin = dy012
            Log.i(TAG5, "Minimale Distanz daumen: $Min")
        }

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        var distMinMax =  middleFingerMax -  middleFingerMin
        if(distMinMax < 0.01){
            distMinMax = 0.01f
        }
        val threshold =  middleFingerMin + distMinMax / divideFactor

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if (dy012 > threshold) {
            if (! middleFingerOpenClose) {
                middleFingerOpenClose = true
            }
        }

        if (dy012 < threshold) {
            if ( middleFingerOpenClose) {
                counter++
                middleFingerOpenClose = false
            }
        }

        setGaugeValues(dy012, threshold,  middleFingerMin, middleFingerMax)
    }


    //Öffnen und SChließen des Mittelfingers
    private fun middleFingerOpenClose(
        dist09: Float,
        dist010: Float,
        dist011: Float,
        dist012: Float,
        gestureRecognizer: GestureRecognizerResult
    ) {


        //Phase 1
        if (dist012 > dist011) {
            // 1 = Finger geöffnet
            if(viewModel.getStartModus() == getString(R.string.closed)) currentPhase = 1
            middleFingerOpen = true
            middleFingerHalfClosed = false
        }

        //Phase 2
        if (dist012 < dist011) {

            if (!middleFingerHalfClosed) {
                counterHalfClosed++
            }
            middleFingerHalfClosed = true

            //Phase 3
            if (dist012 < dist010) {

                if (dist012 < dist09) {
                    //6 = Finger geschlossen
                    if(viewModel.getStartModus() != getString(R.string.closed)) currentPhase = 6

                    middleFingerOpen = false
                    middleFingerHalfOpen = false
                    middleFingerClosed = true
                } else {

                    if (viewModel.getStartModus() == getString(R.string.closed)) {
                        // 2 = Der Finger ist halb geöffnet
                        if(currentPhase != 4 && currentPhase != 1) currentPhase = 2
                    } else {
                        // 3 = Der Finger ist fast geschlossen
                        if(currentPhase != 6) currentPhase = 3
                    }

                    if (middleFingerClosed) {
                        counterHalfOpen++
                    }

                    middleFingerClosed = false
                }
            } else {

                if (viewModel.getStartModus() == getString(R.string.closed)) {
                    // 4 = Der Finger ist fast geöffnet
                    if(currentPhase != 1) currentPhase = 4
                } else{
                    // 5 = Der Finger ist halb geschlossen
                    if(currentPhase != 3 && currentPhase != 6) currentPhase = 5
                }

                middleFingerHalfOpen = true

            }
        }
    }

    private fun ringFingerDistanceToJoint(dy016: Float) {

        if(viewModel.getStartModus() == "offen") {
            if (dy016 < Min) {
                Min = dy016
                Max = dy016
                Log.i(TAG8, "Zeigefinger Min: $Min")
                // Start offener Finger -> min wert wird gespeichert
            }
        } else {
            if (dy016 > Max) {
                Max = dy016
                Log.i(TAG8, "Zeigefinger Max: $Max")
                //Sie können ihren FInger schon weiter als das letzte mal öffnen
                //Start geschlossener Finger -> max wert wird gespeichert
            }
        }

        //Maximum
        if (dy016 > ringFingerMax) {
            ringFingerMax = dy016
            Log.i(TAG5, "Maximale Distanz Mittel-und Ringfinger: $Max")
        }

        //Minimum
        if (dy016 <  ringFingerMin) {
            ringFingerMin = dy016
            Log.i(TAG5, "Minimale Distanz daumen: $Min")
        }

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        var distMinMax =  ringFingerMax -  ringFingerMin
        if(distMinMax < 0.01){
            distMinMax = 0.01f
        }
        val threshold =  ringFingerMin + distMinMax / divideFactor

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if (dy016 > threshold) {
            if (! ringFingerOpenClose) {
                ringFingerOpenClose = true
            }
        }

        if (dy016 < threshold) {
            if ( ringFingerOpenClose) {
                counter++
                ringFingerOpenClose = false
            }
        }

        setGaugeValues(dy016, threshold,  ringFingerMin, ringFingerMax)
    }


    //Öffnen und Schließen des Ringfingers
    private fun ringFingerOpenClose(
        dist013: Float,
        dist014: Float,
        dist015: Float,
        dist016: Float,
        gestureRecognizer: GestureRecognizerResult
    ) {
        //Phase 1
        if (dist016 > dist015) {
            // 1 = Finger geöffnet
            if(viewModel.getStartModus() == getString(R.string.closed)) currentPhase = 1
            ringFingerOpen = true
            ringFingerHalfClosed = false
        }

        //Phase 2
        if (dist016 < dist015) {

            if (!ringFingerHalfClosed) {
                counterHalfClosed++
            }
            ringFingerHalfClosed = true

            //Phase 3
            if (dist016 < dist014) {

                if (dist016 < dist013) {
                    //6 = Finger geschlossen
                    if(viewModel.getStartModus() != getString(R.string.closed)) currentPhase = 6
                    ringFingerOpen = false
                    ringFingerClosed = true
                    ringFingerHalfOpen = false
                } else {

                    if (viewModel.getStartModus() == getString(R.string.closed)) {
                        // 2 = Der Finger ist halb geöffnet
                        if(currentPhase != 4 && currentPhase != 1) currentPhase = 2
                    } else {
                        // 3 = Der Finger ist fast geschlossen
                        if(currentPhase != 6) currentPhase = 3
                    }
                    if (ringFingerClosed) {
                        counterHalfOpen++
                    }
                    ringFingerClosed = false
                }

            } else{
                if (viewModel.getStartModus() == getString(R.string.closed)) {
                    // 4 = Der Finger ist fast geöffnet
                    if(currentPhase != 1) currentPhase = 4
                } else{
                    // 5 = Der Finger ist halb geschlossen
                    if(currentPhase != 3 && currentPhase != 6) currentPhase = 5
                }


                ringFingerHalfOpen = true
            }
        }
    }


    private fun littleFingerDistanceToJoint(dy020: Float) {

        if(viewModel.getStartModus() == "offen") {
            if (dy020 < Min) {
                Min = dy020
                Max = dy020
                Log.i(TAG8, "Zeigefinger Min: $Min")
                // Start offener Finger -> min wert wird gespeichert
            }
        } else {
            if (dy020 > Max) {
                Max = dy020
                Log.i(TAG8, "Zeigefinger Max: $Max")
                //Sie können ihren FInger schon weiter als das letzte mal öffnen
                //Start geschlossener Finger -> max wert wird gespeichert
            }
        }

        //Maximum
        if (dy020 > littleFingerMax) {
            littleFingerMax = dy020
            Log.i(TAG5, "Maximale Distanz Mittel-und Ringfinger: $Max")
        }

        //Minimum
        if (dy020 <  littleFingerMin) {
            littleFingerMin = dy020
            Log.i(TAG5, "Minimale Distanz daumen: $Min")
        }

        //Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        var distMinMax =  littleFingerMax -  littleFingerMin
        if(distMinMax < 0.01){
            distMinMax = 0.01f
        }
        val threshold =  littleFingerMin + distMinMax / divideFactor

        //Hochzählen des Counters, wenn Mitte zwischen Maximalem und Minimalem Wert überschritten wurde
        if (dy020 > threshold) {
            if (! littleFingerOpenClose) {
                littleFingerOpenClose = true
            }
        }

        if (dy020 < threshold) {
            if ( littleFingerOpenClose) {
                counter++
                littleFingerOpenClose = false
            }
        }

        setGaugeValues(dy020, threshold,  littleFingerMin, littleFingerMax)
    }


    //Öffnen und Schließen des kleinen Finger
    private fun littleFingerOpenClose(
        dist017: Float,
        dist018: Float,
        dist019: Float,
        dist020: Float,
        gestureRecognizer: GestureRecognizerResult
    ) {

        //Phase 1
        if (dist020 > dist019) {
            // 1 = Finger geöffnet
            if(viewModel.getStartModus() == getString(R.string.closed)) currentPhase = 1
            littleFingerOpen = true
            littleFingerHalfClosed = false
        }

        //Phase 2
        if (dist020 < dist019) {

            if (!littleFingerHalfClosed) {
                counterHalfClosed++
            }
            littleFingerHalfClosed = true

            //Phase 3
            if (dist020 < dist018) {

                if (dist020 < dist017) {
                    //6 = Finger geschlossen
                    if(viewModel.getStartModus() != getString(R.string.closed)) currentPhase = 6

                    littleFingerOpen = false
                    littleFingerClosed = true
                    littleFingerHalfOpen = false
                } else {

                    if (viewModel.getStartModus() == getString(R.string.closed)) {
                        // 2 = Der Finger ist halb geöffnet
                        if(currentPhase != 4 && currentPhase != 1) currentPhase = 2
                    } else {
                        // 3 = Der Finger ist fast geschlossen
                        if(currentPhase != 6) currentPhase = 3
                    }
                    if (littleFingerClosed) {
                        counterHalfOpen++
                    }
                    littleFingerClosed = false
                }
            } else {

                if (viewModel.getStartModus() == getString(R.string.closed)) {
                    // 4 = Der Finger ist fast geöffnet
                    if(currentPhase != 1) currentPhase = 4
                } else{
                    // 5 = Der Finger ist halb geschlossen
                    if(currentPhase != 3 && currentPhase != 6) currentPhase = 5
                }


                littleFingerHalfOpen = true

            }
        }

    }

    // schließen und öffnen aller Finger erkennen
    private fun allFingersCloseOpen() {


        Log.i("allFingershalfClosed", allFingershalfClosed.toString())


        if(pointingFingerClosed && ringFingerClosed && middleFingerClosed && littleFingerClosed) {

            if(viewModel.getStartModus() == getString(R.string.start_mode_open)) currentPhase = 14
        }

        if (thumbHalfClosed && pointingFingerHalfClosed && ringFingerHalfClosed && littleFingerHalfClosed && middleFingerHalfClosed) {
            allFingershalfClosed = true
            if(viewModel.getStartModus() == getString(R.string.start_mode_close)) {
                if(currentPhase != 11) currentPhase = 12 }
            else {
                if(currentPhase != 14) currentPhase = 13
            }
        } else {
            if(viewModel.getAllFingersOpenOrClose() == 1) {
                if (allFingershalfClosed) {
                    counterAllFingersOpenClose++
                }
               }
            allFingershalfClosed = false
        }

        if (thumbOpen && pointingFingerOpen && middleFingerOpen && ringFingerOpen && littleFingerOpen) {
            allFingersOpen = true
            if(viewModel.getStartModus() == getString(R.string.start_mode_close)) {
                currentPhase = 11 }
        } else {
            if(viewModel.getAllFingersOpenOrClose() == 2) {
                if (allFingersOpen) {
                    counterAllFingersOpenClose++
                }
            }
            allFingersOpen = false
        }

    }



    ///------------------------------------------------------------------------------//
    ///------------------------------------------------------------------------------//
    /// ---------------------- Extra Aufgaben --- ---------------------------------- //
    ///------------------------------------------------------------------------------//
    ///------------------------------------------------------------------------------//

    // Daumen zur HandinnenFläche und wieder nach außen bewegen
    private fun thumbToPalm(gestureRecognizer: GestureRecognizerResult) {

        // Normalisierte Landmarks
        val x4Normalized = gestureRecognizer.landmarks()[0][4].x()
        val x5Normalized = gestureRecognizer.landmarks()[0][5].x()
        val x9Normalized = gestureRecognizer.landmarks()[0][9].x()
        val x13Normalized = gestureRecognizer.landmarks()[0][13].x()


        if (x4Normalized < Min) {
            Min = x4Normalized
            Log.i(TAG8, "Min little finger: $Min")
        }

        if (x13Normalized > Max) {
            Max = x13Normalized
            Log.i(TAG8, "Max little finger: $Max")
            //Sie können ihren FInger schon weiter als das letzte mal öffnen
        }


       // thumbOpen = x4Normalized < x5Normalized

            if (x4Normalized > x5Normalized) {
                //Daumen überkreut Zeigefinger

                if (x4Normalized > x9Normalized) {
                    //Daumen überkreut Mittelfinger
                    if (thumbHalfClosed && viewModel.getDivideFactor() == 2.0) {
                        counter++
                    }
                    thumbHalfClosed = false
                    if (x4Normalized > x13Normalized) {
                        //Daumen überkreut Ringfinger
                        Log.i(TAG9, "Daumen überkreuzt Ringfinger")
                        if (!thumbClosed && viewModel.getDivideFactor() == 1.5) {
                            counter++
                        }
                        thumbClosed = true
                        currentPhase = 10
                    } else {
                        // Daumen überkreuzt Mittelfinger
                        if(currentPhase != 10) currentPhase = 9

                        Log.i(TAG9, "Daumen überkreuzt Mittelfinger")
                    }

                } else {
                    //  Daumen überkreuzt Zeigefinger
                    if(currentPhase != 9 && currentPhase != 10) currentPhase = 8

                    if (thumbOpen && viewModel.getDivideFactor() == 3.0) {
                        counter++
                    }
                    thumbOpen = false
                    Log.i(TAG9, "Daumen überkreuzt Zeigefinger")
                }
            } else {
                thumbClosed = false
                thumbOpen = true
                thumbHalfClosed = true
            }



        fragmentCameraBinding.halfGauge.value = (((x4Normalized - Min) / (Max - Min)) * 100).toDouble()


    }


    private fun calculateDistance(gestureRecognizer: GestureRecognizerResult) {

        val x0 = gestureRecognizer.worldLandmarks()[0][0].x()
        val y0 = gestureRecognizer.worldLandmarks()[0][0].y()

        val x1 = gestureRecognizer.worldLandmarks()[0][1].x()
        val y1 = gestureRecognizer.worldLandmarks()[0][1].y()

        val x2 = gestureRecognizer.worldLandmarks()[0][2].x()
        val y2 = gestureRecognizer.worldLandmarks()[0][2].y()

        val x3 = gestureRecognizer.worldLandmarks()[0][3].x()
        val y3 = gestureRecognizer.worldLandmarks()[0][3].y()

        val x4 = gestureRecognizer.worldLandmarks()[0][4].x()
        val y4 = gestureRecognizer.worldLandmarks()[0][4].y()

        val x5 = gestureRecognizer.worldLandmarks()[0][5].x()
        val y5 = gestureRecognizer.worldLandmarks()[0][5].y()

        val x6 = gestureRecognizer.worldLandmarks()[0][6].x()
        val y6 = gestureRecognizer.worldLandmarks()[0][6].y()

        val x7 = gestureRecognizer.worldLandmarks()[0][7].x()
        val y7 = gestureRecognizer.worldLandmarks()[0][7].y()

        val x8 = gestureRecognizer.worldLandmarks()[0][8].x()
        val y8 = gestureRecognizer.worldLandmarks()[0][8].y()

        val x9 = gestureRecognizer.worldLandmarks()[0][9].x()
        val y9 = gestureRecognizer.worldLandmarks()[0][9].y()

        val x10 = gestureRecognizer.worldLandmarks()[0][10].x()
        val y10 = gestureRecognizer.worldLandmarks()[0][10].y()

        val x11 = gestureRecognizer.worldLandmarks()[0][11].x()
        val y11 = gestureRecognizer.worldLandmarks()[0][11].y()

        val x12 = gestureRecognizer.worldLandmarks()[0][12].x()
        val y12 = gestureRecognizer.worldLandmarks()[0][12].y()

        val x13 = gestureRecognizer.worldLandmarks()[0][13].x()
        val y13 = gestureRecognizer.worldLandmarks()[0][13].y()

        val x14 = gestureRecognizer.worldLandmarks()[0][14].x()
        val y14 = gestureRecognizer.worldLandmarks()[0][14].y()

        val x15 = gestureRecognizer.worldLandmarks()[0][15].x()
        val y15 = gestureRecognizer.worldLandmarks()[0][15].y()

        val x16 = gestureRecognizer.worldLandmarks()[0][16].x()
        val y16 = gestureRecognizer.worldLandmarks()[0][16].y()

        val x20 = gestureRecognizer.worldLandmarks()[0][20].x()
        val y20 = gestureRecognizer.worldLandmarks()[0][20].y()


        val x17 = gestureRecognizer.worldLandmarks()[0][17].x()
        val y17 = gestureRecognizer.worldLandmarks()[0][17].y()

        val x18 = gestureRecognizer.worldLandmarks()[0][18].x()
        val y18 = gestureRecognizer.worldLandmarks()[0][18].y()

        val x19 = gestureRecognizer.worldLandmarks()[0][19].x()
        val y19 = gestureRecognizer.worldLandmarks()[0][19].y()


        //Berechnung der Distanz Fingerspitze - Grundgelenk und Mittelgelenk Finger - Grundgelenk
        var d08 = dist(x0, y0, x8, y8)
        //Runden der Dezimalzahl auf 3 Nachkommastellen
        val bd08 = BigDecimal(d08.toDouble())
        d08 = bd08.setScale(4, RoundingMode.DOWN).toFloat()

        val d07 = dist(x0, y0, x7, y7)
        val d06 = dist(x0, y0, x6, y6)
        val d05 = dist(x0, y0, x5, y5)

        var d012 = dist(x0, y0, x12, y12)
        //Runden der Dezimalzahl auf 3 Nachkommastellen
        val bd012 = BigDecimal(d012.toDouble())
        d012 = bd012.setScale(4, RoundingMode.DOWN).toFloat()

        val d011 = dist(x0, y0, x11, y11)
        val d010 = dist(x0, y0, x10, y10)
        val d09 = dist(x0, y0, x9, y9)

        val d013 = dist(x0, y0, x13, y13)
        val d014 = dist(x0, y0, x14, y14)
        val d015 = dist(x0, y0, x15, y15)
        var d016 = dist(x0, y0, x16, y16)
        //Runden der Dezimalzahl auf 3 Nachkommastellen
        val bd016 = BigDecimal(d016.toDouble())
        d016 = bd016.setScale(4, RoundingMode.DOWN).toFloat()


        val d017 = dist(x0, y0, x17, y17)
        val d018 = dist(x0, y0, x18, y18)
        val d19 = dist(x0, y0, x19, y19)
        var d20 = dist(x0, y0, x20, y20)
        //Runden der Dezimalzahl auf 3 Nachkommastellen
        val bd020 = BigDecimal(d20.toDouble())
        d20 = bd020.setScale(4, RoundingMode.DOWN).toFloat()

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


        when (viewModel.getSelectedExercise()?.id) {

            2 -> {
                //Alle Finger spreizen
                littleFingerSpread(d1620)
                middleFingerSpread(d1216)
                pointingFingerSpread(d0812)
                thumbSpread(d0408)
                allFingersSpread()
            }

            3 -> {
                //Spreizen des kleinen Fingers
                littleFingerSpread(d1620)
            }

            4 -> { //Mittelfinger spreizen
                middleFingerSpread(d1216)
            }

            5 -> { //Zeigefinger spreizen
                pointingFingerSpread(d0812)
            }

            6 -> { //Daumen spreizen
                thumbSpread(d0408)
            }

            8 -> {
                //ZeigeFinger schließen/öffnen
                pointingFingerOpenClose(d08, d07, d06, d05, gestureRecognizer)
                pointingFingerDistanceToJoint(d08)
            }
            9 -> {
                middleFingerOpenClose(d09, d010, d011, d012, gestureRecognizer)
                middleFingerDistanceToJoint(d012)
            } //Mittelfingerschließen/öffnen
            10 ->{
                ringFingerDistanceToJoint(d016)
                ringFingerOpenClose(d013, d014, d015, d016, gestureRecognizer)  //Ringfinger schließen/öffnen
            }
            11 -> {
                littleFingerOpenClose(d017, d018, d19, d20, gestureRecognizer)
                littleFingerDistanceToJoint(d20)
            } //Kleinen finger schließen/öffnen
            12 -> if(selectedHandSide == getString(R.string.selected_hand_right)) thumbToPalm(gestureRecognizer) else thumbToPalmLeft(gestureRecognizer) // Daumen zur Handinnenflächen bewegen
            13 -> {
                pointingFingerOpenClose(d08, d07, d06, d05, gestureRecognizer)
                middleFingerOpenClose(d09, d010, d011, d012, gestureRecognizer)
                ringFingerOpenClose(d013, d014, d015, d016, gestureRecognizer)
                littleFingerOpenClose(d017, d018, d19, d20, gestureRecognizer)
                thumbToPalm(gestureRecognizer)
                allFingersCloseOpen()
            } //Alle Finger schließen/öffnen
            15 -> {
                pointingFingerOpenClose(d08, d07, d06, d05, gestureRecognizer)
            } //Zeigefinger halb schließen/öffnen
            16 -> {
                littleFingerOpenClose(d017, d018, d19, d20, gestureRecognizer)
            } //Zeigefinger halb schließen/öffnen
            17 -> {
                middleFingerOpenClose(d09, d010, d011, d012, gestureRecognizer)
            } //Mittelfinger halb schließen/öffnen
            18 ->{
                ringFingerOpenClose(d013, d014, d015, d016, gestureRecognizer)
            } //Ringfinger halb schließen/öffnen
            else -> {
                //Nothing
            }
        }
    }

    //---------------------------------------------------------------------------//
    //---------------------------------------------------------------------------//
    //--------------------- Funktionen für die linke Hand -----------------------//
    //---------------------------------------------------------------------------//
    //---------------------------------------------------------------------------//

    // Daumen zur HandinnenFläche und wieder nach außen bewegen
    private fun thumbToPalmLeft(gestureRecognizer: GestureRecognizerResult) {

        // Normalisierte Landmarks
        val x4Normalized = gestureRecognizer.landmarks()[0][4].x()
        val x5Normalized = gestureRecognizer.landmarks()[0][5].x()
        val x9Normalized = gestureRecognizer.landmarks()[0][9].x()
        val x13Normalized = gestureRecognizer.landmarks()[0][13].x()


        if (x13Normalized < Min) {
            Min = x13Normalized
            Log.i(TAG8, "Min little finger: $Min")
        }

        if (x4Normalized > Max) {
            Max = x4Normalized
            Log.i(TAG8, "Max little finger: $Max")
            //Sie können ihren FInger schon weiter als das letzte mal öffnen
        }


       // thumbOpen = x4Normalized > x5Normalized

            if (x4Normalized < x5Normalized) {
                //Daumen überkreut Zeigefinger

                if (x4Normalized < x9Normalized) {

                    //Daumen überkreut Mittelfinger
                    if (thumbHalfClosed && viewModel.getDivideFactor() == 2.0) {
                        counter++
                    }
                    thumbHalfClosed = false
                    if (x4Normalized < x13Normalized) {
                        //Daumen überkreut Ringfinger
                        Log.i(TAG9, "Daumen überkreuzt Ringfinger")
                        if (!thumbClosed && viewModel.getDivideFactor() == 1.5) {
                            counter++
                        }
                        thumbClosed = true
                        currentPhase = 10

                    } else {
                        if(currentPhase != 10) currentPhase = 9
                        Log.i(TAG9, "Daumen überkreuzt Mittelfinger")
                    }

                } else {
                    //  Daumen überkreuzt Zeigefinger
                    if(currentPhase != 9 && currentPhase != 10) currentPhase = 8

                    if (thumbOpen && viewModel.getDivideFactor() == 3.0) {
                        counter++
                    }
                    thumbOpen = false
                    Log.i(TAG9, "Daumen überkreuzt Zeigefinger")
                }
            } else {
                thumbClosed = false
                thumbOpen = true
                thumbHalfClosed = true
            }

        fragmentCameraBinding.halfGauge.value = (((Max- x4Normalized) / (Max - Min)) * 100).toDouble()


    }


    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            gestureRecognizerResultAdapter.updateResults(emptyList())
        }
    }
}
