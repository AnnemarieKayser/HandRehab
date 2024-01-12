
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
import com.example.handrehab.DataAllFingersSpread
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
import com.example.handrehab.R


class CameraFragment : Fragment(),
    GestureRecognizerHelper.GestureRecognizerListener {

    /*
      ======================================================================================
      ==========================           Einleitung             ==========================
      ======================================================================================
      Projektname: HandRehab
      Autor: Annemarie Kayser
      Anwendung: Dies ist eine App-Anwendung für die Handrehabilitation nach einem Schlaganfall.
                 Es werden verschiedene Übungen für die linke als auch für die rechte Hand zur
                 Verfügung gestellt. Zudem kann ein individueller Wochenplan erstellt
                 sowie die Daten zu den durchgeführten Übungen eingesehen werden.
      Letztes Update: 12.01.2024

     ======================================================================================
   */


    /*
      =============================================================
      =======                    Funktion                   =======
      =============================================================

      - In diesem Fragment werden die Ergebnisse des Gestenerkennungsmodells
      ausgewertet
      - Abhängig von der ausgewählten Übung, wird hier die jeweilige Funktion
      zur Überprüfung der Durchführung der Übung angesprungen
      - Es werden die Wiederholungen gezählt
      - Die ermittelten Daten zu einer Übung werden anschließend in der
      Datenbank Firestore gespeichert
      - Übungskategorien: Spreizen der Finger, Schließen/Öffnen der Finger,
      halbes Schließen/Öffnen der Finger
      - Bei den beiden letzteren Kategorien werden ebenfalls Phasen festgestellt


    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */


    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    // === Firebase Datenbank === //
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }


    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT


    // allgemeine Min und Max Variablen
    private var Min = 10f
    private var Max = 0f

    // Faktor für die Einstellung der Schwierigkeitsstufe
    private var divideFactor = 2.0



    // Spezifische Variablen für die einzelnen Übungen
    // Variablen, für die Übungen, wo die Finger gespreizt werden
    private var pointingFingerSpreadMax = 0f
    private var pointingFingerSpreadMin = 10f
    private var pointingFingerSpread = false

    private var littleFingerSpreadMax = 0f
    private var littleFingerSpreadMin = 10f
    private var littleFingerSpread = false

    private var thumbSpreadMax = 0f
    private var thumbSpreadMin = 10f
    private var thumbSpread = false

    private var middleFingerSpreadMax = 0f
    private var middleFingerSpreadMin = 10f
    private var middleFingerSpread = false

    private var allFingersSpread = false
    private var counterAllFingersSpread = 0


    // Variablen für die Übungen, wo die Finger geschlossen/geöffnet
    // sowie halb geöffnet/geschlossen werden sollen
    private var allFingersOpen = false
    private var allFingersHalfClosed = false
    private var counterAllFingersOpenClose = 0

    private var thumbOpen = false
    private var thumbClosed = false
    private var thumbHalfClosed = false

    private var pointingFingerOpen = false
    private var pointingFingerClosed = false
    private var pointingFingerHalfClosed = false
    private var pointingFingerHalfOpen = false
    private var pointingFingerMax = 0f
    private var pointingFingerMin = 10f
    private var pointingFingerOpenClose = false

    private var middleFingerOpen = false
    private var middleFingerClosed = false
    private var middleFingerHalfClosed = false
    private var middleFingerHalfOpen = false
    private var middleFingerMax = 0f
    private var middleFingerMin = 10f
    private var middleFingerOpenClose = false

    private var ringFingerOpen = false
    private var ringFingerClosed = false
    private var ringFingerHalfClosed = false
    private var ringFingerHalfOpen = false
    private var ringFingerMax = 0f
    private var ringFingerMin = 10f
    private var ringFingerOpenClose = false

    private var littleFingerOpen = false
    private var littleFingerClosed = false
    private var littleFingerHalfClosed = false
    private var littleFingerHalfOpen = false
    private var littleFingerMax = 0f
    private var littleFingerMin = 10f
    private var littleFingerOpenClose = false

    // Variablen zum Zählen der Wiederholungen
    private var counter = 0
    private var counterHalfClosed = 0
    private var counterHalfOpen = 0

    private var sets = 0
    private var exerciseCompleted = false


    // Aktuelle Phase
    private var currentPhase = 0
    private var phase = ""

    // Variable für die Anzeige der Infos auf dem Kamera Bildschirm
    private var showInfo = true

    // Alert-Dialog
    private var alertDialogShown = false

    // Variable für die ausgewählte Handseite
    private var selectedHandSide = ""


    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    override fun onResume() {
        super.onResume()
        // Überprüfen der Berechtigungen
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main).navigate(R.id.camera_to_permissons)
        }

        // Starten des GestureRecognizerHelper, wenn die App wieder geöffnet wird
        backgroundExecutor.execute {
            if (gestureRecognizerHelper.isClosed()) {
                gestureRecognizerHelper.setupGestureRecognizer()
            }
        }
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    override fun onPause() {
        super.onPause()
        if (this::gestureRecognizerHelper.isInitialized) {
            viewModel.setMinHandDetectionConfidence(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(gestureRecognizerHelper.currentDelegate)

            // Schließen der Gestenerkennungshilfe
            backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
        }
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Beenden des backgroundExecutor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
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

        // Untere Navigationsleiste ausblenden
        val view = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        view.visibility = GONE

        // Nummer der ausgewählte Übung zu Variablen zuweisen
        val id = viewModel.getSelectedExercise()?.id

        // Einstellen der Werte der Messanzeige auf der unteren Hälfte des Bildschirms
        // https://github.com/Gruzer/simple-gauge-android/tree/master
        fragmentCameraBinding.halfGauge.minValue = 0.0
        fragmentCameraBinding.halfGauge.maxValue = 100.0
        fragmentCameraBinding.halfGauge.value = 0.0

        fragmentCameraBinding.halfGauge.setNeedleColor(Color.DKGRAY)
        fragmentCameraBinding.halfGauge.valueColor = Color.TRANSPARENT
        fragmentCameraBinding.halfGauge.minValueTextColor = Color.RED
        fragmentCameraBinding.halfGauge.maxValueTextColor = Color.GREEN

        // Bei der Übung, wo der Daumen zur Handinnenfläche bewegt wird, werden drei
        // Bereiche angezeigt. Bei allen anderen Übungen, werden zwei Bereiche
        // angezeigt
        // https://github.com/Gruzer/simple-gauge-android/tree/master
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

            // der Bereich wird abhängig vom ausgewählten Schwierigkeitslevel aufgeteilt
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

        // Messanzeige wird auf der unteren Hälfte bei den Übungen, wo die Finger nur halb
        // geöffnet/geschlossen werden, ALle Finger gespreizt und ALle Finger geöfffnet/geschlossen
        // werden nicht angezeigt
        // https://github.com/Gruzer/simple-gauge-android/tree/master
        if (id == 2 ||  id == 13 || id == 15 || id == 16 || id == 17 || id == 18) {
            fragmentCameraBinding.halfGauge.visibility = GONE
        }

        // Festlegen der Phasen am Anfang einer Übung, je nachdem, ob aus einer
        // geöffneten oder geschlossenen Position gestartet wird
        currentPhase = if(id == 12){
            7
        } else{
            if(viewModel.getStartModus() == getString(R.string.closed)) 6 else 1
        }

        if(id == 13){
            currentPhase = if(viewModel.getStartModus() == getString(R.string.closed)) 14 else 11
        }

        // Initialisierung eines Menüs in der Statusleite
        setHasOptionsMenu(true)

        // Eingestelltes Schwierigkeitslevel abfragen
        divideFactor = viewModel.getDivideFactor()!!

        // Ausgewählte Hand zu Variablen zuweisen
        selectedHandSide = viewModel.getSelectedHandSide()!!


        // übernommen von:
        // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
        // Initialisierung des background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // übernommen von:
        // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
        // Warten, dass die Views richtig ausgelegt werden
        fragmentCameraBinding.viewFinder.post {
            // Einrichten der Kamera und ihrer Anwendungsfälle
            setUpCamera()
        }

        // übernommen von:
        // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
        // Erstellen des Hand Gesture Recognition Helper, der die Inferenz handhabt
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
    }

    /*
   =============================================================
   =======                   Funktionen                  =======
   =============================================================
   */


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    // Hier werden Klicks auf Elemente der Aktionsleiste behandelt
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Nummer der ausgewählte Übung zu Variablen zuweisen
        val id = viewModel.getSelectedExercise()?.id

        return when (item.itemId) {
            R.id.app_bar_switch -> {
                showInfo = !showInfo

                if (showInfo) {
                    // Messanzeige wird auf der unteren Hälfte bei den Übungen, wo die Finger nur halb
                    // geöffnet/geschlossen werden, ALle Finger gespreizt und ALle Finger geöfffnet/geschlossen
                    // werden nicht angezeigt
                    // https://github.com/Gruzer/simple-gauge-android/tree/master
                    if (id == 2 ||  id == 13 || id == 15 || id == 16 || id == 17 || id == 18) {
                        fragmentCameraBinding.halfGauge.visibility = GONE
                    } else fragmentCameraBinding.halfGauge.visibility = VISIBLE
                }
                else fragmentCameraBinding.halfGauge.visibility = GONE
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    // Initialisiert die Kamera und Vorbereitung der Bindung der Kamera-Anwendungsfälle
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()

                //  Erstellen und Einbinden der Kameranutzungsfälle
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    // Anwendungsfälle für Vorschau, Erfassung und Analyse deklarieren und binden
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Vorschau. Es wird das Verhältnis 4:3, da dies den Modellen am nächsten kommt.
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Verwendung von RGBA 8888 zur Anpassung an die Funktionsweise der Modelle
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        recognizeHand(image)
                    }
                }

        // Muss die Bindung der Anwendungsfälle aufheben, bevor sie erneut gebunden werden
        cameraProvider.unbindAll()

        try {
            // Hier kann eine variable Anzahl von Use-Cases übergeben werden.
            // Kamera bietet Zugriff auf CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e("Error", "Use case binding failed", exc)
        }
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    private fun recognizeHand(imageProxy: ImageProxy) {
        gestureRecognizerHelper.recognizeLiveStream(
            imageProxy = imageProxy,
        )
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    // Aktualisiert die Benutzeroberfläche, nachdem eine Handgeste erkannt wurde. Extrahiert Original
    // Bildhöhe/-breite zur Skalierung und korrekten Platzierung der Orientierungspunkte durch
    // OverlayView. Es wird jeweils nur ein Ergebnis erwartet. Wenn zwei oder mehr
    // Hände im Kamerabild zu sehen sind, wird nur eine verarbeitet.
    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {

        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                // Ergebnis der erkannten Geste anzeigen
                val gestureCategories = resultBundle.results.first().gestures()


                // Eine Hand wurde in der Kamera erkannt
                if (gestureCategories.isNotEmpty()) {


                    // Überprüfung, ob die richtige Handseite in die Kamera gehalten wird
                    val detectedHandSide = resultBundle.results.first().handednesses()[0][0].displayName()

                    //Hinweis, wenn falsche Handseite in die Kamera gehalten wird
                    if (viewModel.getSelectedHandSide() == getString(R.string.selected_hand_right)) {
                        if (detectedHandSide == "Left") {
                            if (!alertDialogShown) {
                                alertDialogShown = true
                                context?.let {
                                    MaterialAlertDialogBuilder(it)
                                        .setTitle(resources.getString(R.string.title_change_hand))
                                        .setMessage(resources.getString(R.string.supporting_text_change_hand_right))
                                        .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
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
                                            alertDialogShown = false
                                        }
                                        .show()
                                }
                            }
                        }

                        if (detectedHandSide == "Left") {
                            // Die Ergebnisse werden weiter ausgewertet
                            calculateDistance(resultBundle.results.first())
                        }
                    }

                }

                // wenn die Übung "Alle Finger Spreizen" ausgewählt ist wird
                // eine extra Variable zum Zählen der Wiederholungen verwendet
                if (viewModel.getSelectedExercise()?.id == 2) {
                    counter = counterAllFingersSpread
                }

                // wenn die Übung "Alle Finger Öffnen/SChließen" ausgewählt ist wird
                // eine extra Variable zum Zählen der Wiederholungen verwendet
                if (viewModel.getSelectedExercise()?.id == 13) {
                    counter = counterAllFingersOpenClose
                }


                // wenn eine Übung, wo die Finger halb geschlossen/geöffnet werden sollen
                // ausgewählt ist, wird eine Variable abhängig von der Startposition
                // zum Zählen der Wiederholungen verwendet
                val id = viewModel.getSelectedExercise()?.id

                if (id == 18 || id == 15 || id == 16 || id == 17) {
                    counter = if (viewModel.getStartModus() == getString(R.string.start_mode_open)) {
                        counterHalfClosed
                    } else counterHalfOpen
                }

                // Überprüfungen, ob die eingestellte Anzahl an Wiederholugen
                // bereits erreicht wurde
                if (counter == viewModel.getRepetitions()) {

                    // Zähler werden auf 0 gesetzt
                    counter = 0
                    counterAllFingersSpread = 0
                    counterAllFingersOpenClose = 0
                    counterHalfClosed = 0
                    counterHalfOpen = 0
                    // Die Anzahl an Sätzen wird hochgezählt
                    sets++

                    // Wenn die Anzahl an eingestellten Sätzen erreicht wird, ist die
                    // Übung abgeschlossen
                    if (sets == viewModel.getSets()) {
                        //Übung abgeschlossen
                        exerciseCompleted = true

                        // Wenn der Wochenplan abgearbeitet wird, dann wird diese Übung
                        // aus diesem aus der Liste entfernt
                        val listExercises = arrayListOf<Exercises>()

                        for(i in viewModel.getListDay()!!) {

                            if(i != viewModel.getSelectedExercise()){
                                listExercises.add(i)
                            }
                        }

                        viewModel.setListDay(listExercises)

                        //Speichern der Übung in der Datenbank
                        if(viewModel.getSelectedExercise()!!.id == 2){
                            saveAllFingersSpread()
                        }

                        saveExercise()

                        // Eingestellte Daten zurücksetzen
                        viewModel.setSets(0)
                        viewModel.setRepetitions(0)

                    }
                }

                // Übergabe der notwendigen Informationen an OverlayView zum Zeichnen auf dem Bildschirm
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

                // Neuzeichnen erzwingen
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }


    // Speichern der Daten zu der Übung, in der alle Finger gespreizt werden
    @SuppressLint("SimpleDateFormat")
    private fun saveAllFingersSpread() {


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

        // Objekt mit Daten befüllen (ID wird automatisch ergänzt)
        val data = DataAllFingersSpread()
        // Der Wert wird mit 100 multipliziert, damit der Wert in cm gespeichert wird
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

        // Eine id als Document Name wird automatisch vergeben
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenAllFingersSpread")
            .add(data)
            .addOnSuccessListener { documentReference ->
                toast(getString(R.string.save))
            }
            .addOnFailureListener { e ->
                toast(getString(R.string.not_save))
            }
    }

    // Speichern der Daten zu der durchgeführten Übung
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
        data.setRepetitions(viewModel.getRepetitions()!!)
        data.setSets(viewModel.getSets()!!)
        data.setExerciseName(viewModel.getSelectedExercise()!!.textItem)
        data.setDate(datetimestamp!!)
        data.setDayOfWeek(day)
        data.setStartMode(viewModel.getStartModus()!!)
        data.setSelectedHandSide(viewModel.getSelectedHandSide()!!)
        data.setExerciseId(viewModel.getSelectedExercise()!!.id)
        if(viewModel.getSelectedExercise()!!.id == 13 || viewModel.getSelectedExercise()!!.id == 2){
            data.setMin(0f)
            data.setMax(0f)
        } else{
            // Der Wert wird mit 100 multipliziert, damit der Wert in cm gespeichert wird
            data.setMin(Min * 100)
            data.setMax(Max * 100)
        }

        // Die erreichte Phase beim Schließen und Öffnen der Finger wird ermittelt
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
            else -> phase = ""
        }

        data.setCurrentPhase(phase)

        // Eine id als Document Name wird automatisch vergeben
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

    // Aktualisierung der Werte der Messanzeige auf der unteren Häflte des Bildschirms
    // https://github.com/Gruzer/simple-gauge-android/tree/master
    private fun setGaugeValues(distance: Float, Min: Float, Max: Float) {

        val id = viewModel.getSelectedExercise()?.id
        if (id == 2 || id == 20 || id == 13) {
            // Keine Messanzeige vorhanden
        } else {
            // Der aktuelle Abstand wird in Relation zum Maximalwert angezeigt
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
    /// --------------------- Übungen: Spreizen der Finger ------------------------- //
    ///------------------------------------------------------------------------------//
    ///------------------------------------------------------------------------------//

    // Spreizen des Zeigefingers
    private fun pointingFingerSpread(d0812: Float) {

        // Maximaler Abstand zwischen dem Zeigefinger und dem Mittelfinger
        if (d0812 > pointingFingerSpreadMax) {
            Max = d0812
            pointingFingerSpreadMax = d0812
        }

        // Minimaler Abstand zwischen dem Zeigefinger und dem Mittelfinger
        if (d0812 < pointingFingerSpreadMin) {
            Min = d0812
            pointingFingerSpreadMin = d0812
        }

        // Berechnung der Maximalen Distanz zwischen Mittel- und Zeigefinger
        var distMinMax = pointingFingerSpreadMax - pointingFingerSpreadMin
        // Wenn die Distanz am Anfang einer Übung noch sehr klein ist, wird diese überschrieben
        if(distMinMax < 0.008){
            distMinMax = 0.008f
        }

        // Berechnung eines Grenzwertes abhängig von dem ausgewählten Schwierigkeitslevel
        val threshold = pointingFingerSpreadMin + distMinMax / divideFactor

        // Hochzählen des Counters, wenn der Grenzwert überschritten und anschließend wieder
        // unterschritten wurde
        if (d0812 > threshold) {
            if (!pointingFingerSpread) {
                pointingFingerSpread = true
            }
        }

        if (d0812 < threshold) {
            if (pointingFingerSpread) {
                counter++
                pointingFingerSpread = false
            }
        }


        // Messanzeige aktualisieren
        setGaugeValues(d0812, pointingFingerSpreadMin, pointingFingerSpreadMax)

    }

    // Kleinen Finger spreizen
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

        //Berechnung der maximal erreichten Distanz zwischen dem kleinen Finger und Ringfinger
        var distMinMax = littleFingerSpreadMax - littleFingerSpreadMin

        // Wenn die Distanz am Anfang einer Übung noch sehr klein ist, wird diese überschrieben
        if(distMinMax < 0.008){
            distMinMax = 0.008f
        }
        // Berechnung eines Grenzwertes abhängig von dem ausgewählten Schwierigkeitslevel
        val threshold = littleFingerSpreadMin + distMinMax / divideFactor

        // Hochzählen des Counters, wenn der Grenzwert überschritten und anschließend wieder
        // unterschritten wurde
        if (d1620 > threshold) {
            if (!littleFingerSpread) {
                littleFingerSpread = true
            }
        }

        if (d1620 < threshold) {
            if (littleFingerSpread) {
                counter++
                littleFingerSpread = false
            }
        }

        // Aktualisierung der Messanzeige
        setGaugeValues(d1620, littleFingerSpreadMin, littleFingerSpreadMax)
    }


    // Spreizen des Daumens
    private fun thumbSpread(d0408: Float) {


        // Maximaler Abstand zwischen dem Daumen und dem Zeigefinger
        if (d0408 < thumbSpreadMin) {
            Min = d0408
            thumbSpreadMin = d0408
        }


        // Minimaler Abstand zwischen dem Daumen und dem Zeigefinger
        if (d0408 > thumbSpreadMax) {
            Max = d0408
            thumbSpreadMax = d0408
        }


        //Berechnung der maximal erreichten Distanz zwischen dem Daumen und dem Zeigefinger
        var distMinMax = thumbSpreadMax - thumbSpreadMin
        // Wenn die Distanz am Anfang einer Übung noch sehr klein ist, wird diese überschrieben
        if(distMinMax < 0.008){
            distMinMax = 0.008f
        }

        // Berechnung eines Grenzwertes abhängig von dem ausgewählten Schwierigkeitslevel
        val threshold = thumbSpreadMin + distMinMax / divideFactor


        // Hochzählen des Counters, wenn der Grenzwert überschritten und anschließend wieder
        // unterschritten wurde
        if (d0408 > threshold) {
            if (!thumbSpread) {
                thumbSpread = true
            }
        }

        if (d0408 < threshold) {
            if (thumbSpread) {
                counter++
                thumbSpread = false
            }
        }

        // Aktualisierung der Messanzeige
        setGaugeValues(d0408, thumbSpreadMin, thumbSpreadMax)
    }

    // Mittelfinger spreizen
    private fun middleFingerSpread(d1216: Float) {


        // Maximum
        if (d1216 > middleFingerSpreadMax) {
            Max = d1216
            middleFingerSpreadMax = d1216
        }

        // Minimum
        if (d1216 < middleFingerSpreadMin) {
            Min = d1216
            middleFingerSpreadMin = d1216
        }

        //Berechnung der maximal erreichten Distanz zwischen Mittel- und Ringfinger
        var distMinMax = middleFingerSpreadMax - middleFingerSpreadMin

        // Wenn die Distanz am Anfang einer Übung noch sehr klein ist, wird diese überschrieben
        if(distMinMax < 0.008){
            distMinMax = 0.008f
        }
        // Berechnung eines Grenzwertes abhängig von dem ausgewählten Schwierigkeitslevel
        val threshold = middleFingerSpreadMin + distMinMax / divideFactor

        // Hochzählen des Counters, wenn der Grenzwert überschritten und anschließend wieder
        // unterschritten wurde
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

        // Aktualisierung der Messanzeige
        setGaugeValues(d1216, middleFingerSpreadMin, middleFingerSpreadMax)
    }

    // Alle Finger spreizen
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

    // Schließen und Öffnen des Zeigefingers
    private fun pointingFingerDistanceToJoint(d08: Float) {


        if(viewModel.getStartModus() == getString(R.string.start_mode_open)) {
            if (d08 < Min) {
                Min = d08
                Max = d08
                // Start offener Finger -> mininmaler Wert wird gespeichert
            }
        } else {
            if (d08 > Max) {
                Max = d08
                //Start geschlossener Finger -> maximaler Wert wird gespeichert
            }
        }

        // Maximum
        if (d08 > pointingFingerMax) {
            pointingFingerMax = d08
        }

        // Minimum
        if (d08 < pointingFingerMin) {
            pointingFingerMin = d08
        }

        // Berechnung der maximal erreichten Distanz zwischen der Spitze des Zeigefingers
        // und dem Handgrundgelenk
        var distMinMax = pointingFingerMax - pointingFingerMin

        // Wenn die Distanz am Anfang einer Übung noch sehr klein ist, wird diese überschrieben
        if(distMinMax < 0.008){
            distMinMax = 0.008f
        }

        // Berechnung eines Grenzwertes abhängig von dem ausgewählten Schwierigkeitslevel und
        // der Startposition
        val threshold = if(viewModel.getStartModus() == getString(R.string.start_mode_open)){
            pointingFingerMax - distMinMax / divideFactor
        } else pointingFingerMin + distMinMax / divideFactor


        //Hochzählen des Counters, wenn der Grenzwert wieder unterschritten wird
        if (d08 > threshold) {
            if (!pointingFingerOpenClose) {
                pointingFingerOpenClose = true
            }
        }

        if (d08 < threshold) {
            if (pointingFingerOpenClose) {
                counter++
                pointingFingerOpenClose = false
            }
        }


        // Aktualisierung der Messanzeige
        setGaugeValues(d08, pointingFingerMin, pointingFingerMax)
    }

    // Phasenerkennung beim Zeigefinger
    // Halbes Öffnen/Schließen des Zeigefingers
    private fun pointingFingerOpenClose(
        dist08: Float,
        dist07: Float,
        dist06: Float,
        dist05: Float,
    ) {

        // Finger geöffnet
        if (dist08 > dist07) {
            // 1 = Finger geöffnet
            if(viewModel.getStartModus() == getString(R.string.closed)) currentPhase = 1
            pointingFingerOpen = true
            pointingFingerHalfClosed = false
        }

        if (dist08 < dist07) {

            // Hochzählen einer Wiederholung, wenn Finger halb geschlossen ist
            // Startpositon geöffnet
            if (!pointingFingerHalfClosed) {
                counterHalfClosed++
            }
            pointingFingerHalfClosed = true


            if (dist08 < dist06) {


                // Finger geschlossen
                if (dist08 < dist05) {
                    // 6 = Finger geschlossen
                    if(viewModel.getStartModus() != getString(R.string.closed)) currentPhase = 6
                    pointingFingerOpen = false
                    pointingFingerHalfOpen = false
                    pointingFingerClosed = true
                } else {

                    // Hochzählen einer Wiederholung, wenn Finger halb geöffnet ist
                    // Startpositon geschlossen
                    if (pointingFingerClosed) {
                        counterHalfOpen++
                    }

                    pointingFingerClosed = false

                    // Abhängig von der Startpositon wird die Phase zugewiesen
                    if (viewModel.getStartModus() == getString(R.string.closed)) {
                        // 2 = Der Finger ist halb geöffnet, 4 = fast geöffnet, 1 = göffnet
                        if(currentPhase != 4 && currentPhase != 1) currentPhase = 2
                    } else {
                        // 3 = Der Finger ist fast geschlossen, 6 = Finger geschlossen
                        if(currentPhase != 6) currentPhase = 3
                    }

                }
            } else {
                // Abhängig von der Startpositon wird die Phase zugewiesen
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

    // Schließen und Öffnen des Mittelfingers
    private fun middleFingerDistanceToJoint(dy012: Float) {

        if(viewModel.getStartModus() == "offen") {
            if (dy012 < Min) {
                Min = dy012
                Max = dy012
                // Start offener Finger -> min wert wird gespeichert
            }
        } else {
            if (dy012 > Max) {
                Max = dy012
                //Start geschlossener Finger -> max wert wird gespeichert
            }
        }

        //Maximum
        if (dy012 > middleFingerMax) {
            middleFingerMax = dy012
        }

        //Minimum
        if (dy012 <  middleFingerMin) {
            middleFingerMin = dy012
        }

        // Berechnung der maximalen erreichten Distanz zwischen dem Mittelfinger und
        // dem Handgrundgelenk
        var distMinMax =  middleFingerMax -  middleFingerMin
        // Wenn die Distanz am Anfang einer Übung noch sehr klein ist, wird diese überschrieben
        if(distMinMax < 0.008){
            distMinMax = 0.008f
        }

        // Berechnung eines Grenzwertes abhängig von dem ausgewählten Schwierigkeitslevel und
        // der Startposition
        val threshold = if(viewModel.getStartModus() == getString(R.string.start_mode_open)){
            middleFingerMax - distMinMax / divideFactor
        } else middleFingerMin + distMinMax / divideFactor



        //Hochzählen des Counters, wenn Grenzwert überschritten und wieder unterschritten wird
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

        // Aktualisierung der Messanzeige
        setGaugeValues(dy012, middleFingerMin, middleFingerMax)
    }


    // Phasenerkennung beim Mittelfingers
    // Halbes Öffnen und Schließen des Mittelfingers
    private fun middleFingerOpenClose(
        dist09: Float,
        dist010: Float,
        dist011: Float,
        dist012: Float,
    ) {

        // Mittelfinger geöffnet
        if (dist012 > dist011) {
            // 1 = Finger geöffnet
            if(viewModel.getStartModus() == getString(R.string.closed)) currentPhase = 1
            middleFingerOpen = true
            middleFingerHalfClosed = false
        }

        if (dist012 < dist011) {

            // Hochzählen einer Wiederholung, wenn Finger halb geschlossen ist
            // Startpositon geöffnet
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

                    // Abhängig von der Startpositon wird die Phase zugewiesen
                    if (viewModel.getStartModus() == getString(R.string.closed)) {
                        // 2 = Der Finger ist halb geöffnet
                        if(currentPhase != 4 && currentPhase != 1) currentPhase = 2
                    } else {
                        // 3 = Der Finger ist fast geschlossen
                        if(currentPhase != 6) currentPhase = 3
                    }

                    // Hochzählen einer Wiederholung, wenn Finger halb geöffnet ist
                    // Startpositon geschlossen
                    if (middleFingerClosed) {
                        counterHalfOpen++
                    }

                    middleFingerClosed = false
                }
            } else {

                // Abhängig von der Startpositon wird die Phase zugewiesen
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

    // Schließend und Öffnen des Ringfingers
    private fun ringFingerDistanceToJoint(dy016: Float) {

        if(viewModel.getStartModus() == "offen") {
            if (dy016 < Min) {
                Min = dy016
                Max = dy016
                // Start offener Finger -> min wert wird gespeichert
            }
        } else {
            if (dy016 > Max) {
                Max = dy016
                //Start geschlossener Finger -> max wert wird gespeichert
            }
        }

        // Maximum
        if (dy016 > ringFingerMax) {
            ringFingerMax = dy016
        }

        // Minimum
        if (dy016 <  ringFingerMin) {
            ringFingerMin = dy016
        }

        // Berechnung der maximal erreichten Distanz zwischen dem Ringfinger und dem Handgrundgelenk
        var distMinMax =  ringFingerMax -  ringFingerMin
        // Wenn die Distanz am Anfang einer Übung noch sehr klein ist, wird diese überschrieben
        if(distMinMax < 0.008){
            distMinMax = 0.008f
        }

        // Berechnung eines Grenzwertes abhängig von dem ausgewählten Schwierigkeitslevel und
        // der Startposition
        val threshold = if(viewModel.getStartModus() == getString(R.string.start_mode_open)){
            ringFingerMax - distMinMax / divideFactor
        } else ringFingerMin + distMinMax / divideFactor


        //Hochzählen des Counters, wenn Grenzwert wieder unterschritten wird
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

        //Aktualisieren der Messanzeige
        setGaugeValues(dy016, ringFingerMin, ringFingerMax)
    }


    // Phasenerkennung beim Ringfinger
    // Halbes Öffnen und Schließen des Ringfingers
    private fun ringFingerOpenClose(
        dist013: Float,
        dist014: Float,
        dist015: Float,
        dist016: Float,
    ) {

        // Finger geöffnet
        if (dist016 > dist015) {
            // 1 = Finger geöffnet
            if(viewModel.getStartModus() == getString(R.string.closed)) currentPhase = 1
            ringFingerOpen = true
            ringFingerHalfClosed = false
        }


        if (dist016 < dist015) {

            // Hochzählen einer Wiederholung, wenn Finger halb geschlossen ist
            // Startpositon geöffnet
            if (!ringFingerHalfClosed) {
                counterHalfClosed++
            }
            ringFingerHalfClosed = true


            if (dist016 < dist014) {

                if (dist016 < dist013) {
                    //6 = Finger geschlossen
                    if(viewModel.getStartModus() != getString(R.string.closed)) currentPhase = 6
                    ringFingerOpen = false
                    ringFingerClosed = true
                    ringFingerHalfOpen = false
                } else {
                    // Abhängig von der Startpositon wird die Phase zugewiesen
                    if (viewModel.getStartModus() == getString(R.string.closed)) {
                        // 2 = Der Finger ist halb geöffnet
                        if(currentPhase != 4 && currentPhase != 1) currentPhase = 2
                    } else {
                        // 3 = Der Finger ist fast geschlossen
                        if(currentPhase != 6) currentPhase = 3
                    }

                    // Hochzählen einer Wiederholung, wenn Finger halb geöffnet ist
                    // Startpositon geschlossen
                    if (ringFingerClosed) {
                        counterHalfOpen++
                    }
                    ringFingerClosed = false
                }

            } else{

                // Abhängig von der Startpositon wird die Phase zugewiesen
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


    // Öffnen und Schließen des kleinen Fingers
    private fun littleFingerDistanceToJoint(dy020: Float) {

        if(viewModel.getStartModus() == "offen") {
            if (dy020 < Min) {
                Min = dy020
                Max = dy020
                // Start offener Finger -> min wert wird gespeichert
            }
        } else {
            if (dy020 > Max) {
                Max = dy020
                //Start geschlossener Finger -> max wert wird gespeichert
            }
        }

        // Maximum
        if (dy020 > littleFingerMax) {
            littleFingerMax = dy020
        }

        // Minimum
        if (dy020 <  littleFingerMin) {
            littleFingerMin = dy020
        }

        // Berechnung der maximal erreichten Distanz zwischen dem kleinen Finger und dem Handgrundgelenk
        var distMinMax =  littleFingerMax -  littleFingerMin
        // Wenn die Distanz am Anfang einer Übung noch sehr klein ist, wird diese überschrieben
        if(distMinMax < 0.008){
            distMinMax = 0.008f
        }

        // Berechnung eines Grenzwertes abhängig von dem ausgewählten Schwierigkeitslevel und
        // der Startposition
        val threshold = if(viewModel.getStartModus() == getString(R.string.start_mode_open)){
            littleFingerMax - distMinMax / divideFactor
        } else littleFingerMin + distMinMax / divideFactor

        //Hochzählen des Counters, wenn Grenzwert wieder unterschritten wird
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

        //Aktualisierung der Messanzeige
        setGaugeValues(dy020, littleFingerMin, littleFingerMax)
    }


    // Phasenerkennung des kleinen Fingers
    // Halbes Öffnen und Schließen des kleinen Fingers
    private fun littleFingerOpenClose(
        dist017: Float,
        dist018: Float,
        dist019: Float,
        dist020: Float,
    ) {

        // Finger geöffnet
        if (dist020 > dist019) {
            // 1 = Finger geöffnet
            if(viewModel.getStartModus() == getString(R.string.closed)) currentPhase = 1
            littleFingerOpen = true
            littleFingerHalfClosed = false
        }

        if (dist020 < dist019) {

            // Hochzählen einer Wiederholung, wenn Finger halb geschlossen ist
            // Startpositon geöffnet
            if (!littleFingerHalfClosed) {
                counterHalfClosed++
            }
            littleFingerHalfClosed = true

            if (dist020 < dist018) {

                if (dist020 < dist017) {
                    //6 = Finger geschlossen
                    if(viewModel.getStartModus() != getString(R.string.closed)) currentPhase = 6

                    littleFingerOpen = false
                    littleFingerClosed = true
                    littleFingerHalfOpen = false
                } else {
                    // Abhängig von der Startpositon wird die Phase zugewiesen
                    if (viewModel.getStartModus() == getString(R.string.closed)) {
                        // 2 = Der Finger ist halb geöffnet
                        if(currentPhase != 4 && currentPhase != 1) currentPhase = 2
                    } else {
                        // 3 = Der Finger ist fast geschlossen
                        if(currentPhase != 6) currentPhase = 3
                    }
                    // Hochzählen einer Wiederholung, wenn Finger halb geöffnet ist
                    // Startpositon geschlossen
                    if (littleFingerClosed) {
                        counterHalfOpen++
                    }
                    littleFingerClosed = false
                }
            } else {
                // Abhängig von der Startpositon wird die Phase zugewiesen
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

    // Schließen und Öffnen aller Finger
    // Die Finger können entweder ganz oder halb geschlossen werden
    private fun allFingersCloseOpen() {


        if(pointingFingerClosed && ringFingerClosed && middleFingerClosed && littleFingerClosed) {

            if(viewModel.getStartModus() == getString(R.string.start_mode_open)) currentPhase = 14
        }

        if (pointingFingerHalfClosed && ringFingerHalfClosed && littleFingerHalfClosed && middleFingerHalfClosed) {
            allFingersHalfClosed = true
            // Abhängig von der Startpositon wird die Phase zugewiesen
            // wenn die Finger halb geöffnet/geschlossen sind
            if(viewModel.getStartModus() == getString(R.string.start_mode_close)) {
                if(currentPhase != 11) currentPhase = 12 }
            else {
                if(currentPhase != 14) currentPhase = 13
            }
        } else {
            // Hochzählen einer Wiederholung wenn die Finger halb geöffnet/geschlossen wurden
            if(viewModel.getAllFingersOpenOrClose() == 1) {
                if (allFingersHalfClosed) {
                    counterAllFingersOpenClose++
                }
               }
            allFingersHalfClosed = false
        }

        if (pointingFingerOpen && middleFingerOpen && ringFingerOpen && littleFingerOpen) {
            allFingersOpen = true
            if(viewModel.getStartModus() == getString(R.string.start_mode_close)) {
                currentPhase = 11 }
        } else {
            // Hochzählen einer Wiederholung wenn die Finger ganz geöffnet/geschlossen wurden
            if(viewModel.getAllFingersOpenOrClose() == 2) {
                if (allFingersOpen) {
                    counterAllFingersOpenClose++
                }
            }
            allFingersOpen = false
        }

    }

    // Daumen zur Handinnenfläche und wieder nach außen bewegen
    // Abhängig von der eingestellten Schwierigkeitsstufe muss entweder der
    // Zeigefinger, Mittelfinger oder Ringfinger überkreut werden
    private fun thumbToPalm(gestureRecognizer: GestureRecognizerResult) {

        // Normalisierte x-Werte
        val x4Normalized = gestureRecognizer.landmarks()[0][4].x() // Daumenspitze
        val x5Normalized = gestureRecognizer.landmarks()[0][5].x() // Fingergrundgelenk des Zeigefinger
        val x9Normalized = gestureRecognizer.landmarks()[0][9].x() // Fingergrundgelenk des Mittelfingers
        val x13Normalized = gestureRecognizer.landmarks()[0][13].x() // Fingergrundgelenk des Ringfingers


        // Minimum
        if (x4Normalized < Min) {
            Min = x4Normalized
        }

        // Maximum
        if (x13Normalized > Max) {
            Max = x13Normalized
        }

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
                    if (!thumbClosed && viewModel.getDivideFactor() == 1.5) {
                        counter++
                    }
                    thumbClosed = true
                    currentPhase = 10

                } else {
                    // Daumen überkreuzt Mittelfinger
                    if(currentPhase != 10) currentPhase = 9
                }

            } else {
                //  Daumen überkreuzt Zeigefinger
                if(currentPhase != 9 && currentPhase != 10) currentPhase = 8

                if (thumbOpen && viewModel.getDivideFactor() == 3.0) {
                    counter++
                }
                thumbOpen = false
            }
        } else {
            // Daumen überkreut keinen Finger
            thumbClosed = false
            thumbOpen = true
            thumbHalfClosed = true
        }

        // Aktualisierung der Messanzeige
        // https://github.com/Gruzer/simple-gauge-android/tree/master
        fragmentCameraBinding.halfGauge.value = (((x4Normalized - Min) / (Max - Min)) * 100).toDouble()
    }


    // Berechnung der Distanzen zwischen den Fingergelenkspunkten
    // Auswahl der Funktion der ausgewählten Übung
    private fun calculateDistance(gestureRecognizer: GestureRecognizerResult) {


        // Speichern der einzelnen x- und y- Koordinaten der Fingergelenke
        val x0 = gestureRecognizer.worldLandmarks()[0][0].x()
        val y0 = gestureRecognizer.worldLandmarks()[0][0].y()

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

        val x17 = gestureRecognizer.worldLandmarks()[0][17].x()
        val y17 = gestureRecognizer.worldLandmarks()[0][17].y()

        val x18 = gestureRecognizer.worldLandmarks()[0][18].x()
        val y18 = gestureRecognizer.worldLandmarks()[0][18].y()

        val x19 = gestureRecognizer.worldLandmarks()[0][19].x()
        val y19 = gestureRecognizer.worldLandmarks()[0][19].y()

        val x20 = gestureRecognizer.worldLandmarks()[0][20].x()
        val y20 = gestureRecognizer.worldLandmarks()[0][20].y()


        // Berechnung der Distanz Zeigefingerspitze - Grundgelenk
        var d08 = dist(x0, y0, x8, y8)
        // Runden der Dezimalzahl auf 4 Nachkommastellen
        val bd08 = BigDecimal(d08.toDouble())
        d08 = bd08.setScale(4, RoundingMode.DOWN).toFloat()

        // Berechnung der weiteren Distanzen der Gelenke des Zeigefingers - Grundgelenk
        val d07 = dist(x0, y0, x7, y7)
        val d06 = dist(x0, y0, x6, y6)
        val d05 = dist(x0, y0, x5, y5)

        // Berechnung der Distanz Mittelfingerspitze - Grundgelenk
        var d012 = dist(x0, y0, x12, y12)
        // Runden der Dezimalzahl auf 4 Nachkommastellen
        val bd012 = BigDecimal(d012.toDouble())
        d012 = bd012.setScale(4, RoundingMode.DOWN).toFloat()

        // Berechnung der weiteren Distanzen der Gelenke des Mittelfingers - Grundgelenk
        val d011 = dist(x0, y0, x11, y11)
        val d010 = dist(x0, y0, x10, y10)
        val d09 = dist(x0, y0, x9, y9)



        // Berechnung der Distanz Ringfingerspitze - Grundgelenk
        var d016 = dist(x0, y0, x16, y16)
        // Runden der Dezimalzahl auf 4 Nachkommastellen
        val bd016 = BigDecimal(d016.toDouble())
        d016 = bd016.setScale(4, RoundingMode.DOWN).toFloat()

        // Berechnung der weiteren Distanzen der Gelenke des Ringfingers - Grundgelenk
        val d013 = dist(x0, y0, x13, y13)
        val d014 = dist(x0, y0, x14, y14)
        val d015 = dist(x0, y0, x15, y15)

        // Berechnung der Distanz Spitze des kleinen Fingers - Grundgelenk
        var d20 = dist(x0, y0, x20, y20)
        // Runden der Dezimalzahl auf 4 Nachkommastellen
        val bd020 = BigDecimal(d20.toDouble())
        d20 = bd020.setScale(4, RoundingMode.DOWN).toFloat()

        // Berechnung der weiteren Distanzen der Gelenke des kleinen Fingers - Grundgelenk
        val d017 = dist(x0, y0, x17, y17)
        val d018 = dist(x0, y0, x18, y18)
        val d19 = dist(x0, y0, x19, y19)

        // Distanz zwischen Zeige- und Mittelfingerspitze
        var d0812 = dist(x8, y8, x12, y12)
        // Runden der Dezimalzahl auf 4 Nachkommastellen
        val bd0812 = BigDecimal(d0812.toDouble())
        d0812 = bd0812.setScale(4, RoundingMode.DOWN).toFloat()

        // Distanz zwischen Ringfingerspitze und der Spitze des kleinen Fingers
        var d1620 = dist(x16, y16, x20, y20)
        // Runden der Dezimalzahl auf 4 Nachkommastellen
        val bd1620 = BigDecimal(d1620.toDouble())
        d1620 = bd1620.setScale(4, RoundingMode.DOWN).toFloat()

        // Distanz zwischen Mittel- und Ringfingerspitze
        var d1216 = dist(x12, y12, x16, y16)
        // Runden der Dezimalzahl auf 4 Nachkommastellen
        val bd1216 = BigDecimal(d1216.toDouble())
        d1216 = bd1216.setScale(4, RoundingMode.DOWN).toFloat()

        //Distanz zwischen Daumen und Zeigefinger
        var d0408 = dist(x4, y4, x8, y8)
        //Runden der Dezimalzahl auf 4 Nachkommastellen
        val bd0408 = BigDecimal(d0408.toDouble())
        d0408 = bd0408.setScale(4, RoundingMode.DOWN).toFloat()


        // Anspringen der Funktion zu der ausgewählten Übung
        when (viewModel.getSelectedExercise()?.id) {

            2 -> {
                // Alle Finger spreizen
                littleFingerSpread(d1620)
                middleFingerSpread(d1216)
                pointingFingerSpread(d0812)
                thumbSpread(d0408)
                allFingersSpread()
            }

            3 -> {
                // Spreizen des kleinen Fingers
                littleFingerSpread(d1620)
            }

            4 -> {
                // Mittelfinger spreizen
                middleFingerSpread(d1216)
            }

            5 -> {
                // Zeigefinger spreizen
                pointingFingerSpread(d0812)
            }

            6 -> {
                //Daumen spreizen
                thumbSpread(d0408)
            }

            8 -> {
                // Zeigefinger schließen/öffnen
                pointingFingerOpenClose(d08, d07, d06, d05)
                // Phasenerkennung
                pointingFingerDistanceToJoint(d08)
            }
            9 -> {
                //Mittelfinger schließen/öffnen
                middleFingerOpenClose(d09, d010, d011, d012)
                // Phasenerkennung
                middleFingerDistanceToJoint(d012)
            }
            10 ->{
                // Ringfinger schließen/öffnen
                ringFingerDistanceToJoint(d016)
                // Phasenerkennung
                ringFingerOpenClose(d013, d014, d015, d016)
            }
            11 -> {
                // Kleinen finger schließen/öffnen
                littleFingerOpenClose(d017, d018, d19, d20)
                // Phasenerkennung
                littleFingerDistanceToJoint(d20)
            }
            12 -> {
                // Daumen zur Handinnenflächen bewegen
                if(selectedHandSide == getString(R.string.selected_hand_right))
                    thumbToPalm(gestureRecognizer) else thumbToPalmLeft(gestureRecognizer)
            }
            13 -> {
                // Alle Finger schließen/öffnen
                pointingFingerOpenClose(d08, d07, d06, d05)
                middleFingerOpenClose(d09, d010, d011, d012)
                ringFingerOpenClose(d013, d014, d015, d016)
                littleFingerOpenClose(d017, d018, d19, d20)
                allFingersCloseOpen()
            }
            15 -> {
                // Zeigefinger halb schließen/öffnen
                pointingFingerOpenClose(d08, d07, d06, d05)
            }
            16 -> {
                // Zeigefinger halb schließen/öffnen
                littleFingerOpenClose(d017, d018, d19, d20)
            }
            17 -> {
                // Mittelfinger halb schließen/öffnen
                middleFingerOpenClose(d09, d010, d011, d012)
            }
            18 ->{
                // Ringfinger halb schließen/öffnen
                ringFingerOpenClose(d013, d014, d015, d016)
            }
            else -> {
                // Nichts
            }
        }
    }



    // Daumen zur HandinnenFläche und wieder nach außen bewegen für den linken Daumen
    // Abhängig von der eingestellten Schwierigkeitsstufe muss entweder der
    // Zeigefinger, Mittelfinger oder Ringfinger überkreut werden
    private fun thumbToPalmLeft(gestureRecognizer: GestureRecognizerResult) {

        // Normalisierte x-Werte
        val x4Normalized = gestureRecognizer.landmarks()[0][4].x() // Daumenspitze
        val x5Normalized = gestureRecognizer.landmarks()[0][5].x() // Fingergrundgelenk des Zeigefingers
        val x9Normalized = gestureRecognizer.landmarks()[0][9].x() // Fingergrundgelenk des Mittelfingers
        val x13Normalized = gestureRecognizer.landmarks()[0][13].x() // Fingergrundgelenk des Ringfingers


        // Minimum
        if (x13Normalized < Min) {
            Min = x13Normalized
        }

        // Maximum
        if (x4Normalized > Max) {
            Max = x4Normalized
        }

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
                        if (!thumbClosed && viewModel.getDivideFactor() == 1.5) {
                            counter++
                        }
                        thumbClosed = true
                        currentPhase = 10

                    } else {
                        if(currentPhase != 10) currentPhase = 9
                    }

                } else {
                    //  Daumen überkreuzt Zeigefinger
                    if(currentPhase != 9 && currentPhase != 10) currentPhase = 8

                    if (thumbOpen && viewModel.getDivideFactor() == 3.0) {
                        counter++
                    }
                    thumbOpen = false
                }
            } else {
                thumbClosed = false
                thumbOpen = true
                thumbHalfClosed = true
            }

        // Aktualisieren der Messanzeige
        // https://github.com/Gruzer/simple-gauge-android/tree/master
        fragmentCameraBinding.halfGauge.value = (((Max- x4Normalized) / (Max - Min)) * 100).toDouble()
    }

    // übernommen von:
    // https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }
}
