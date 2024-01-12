package com.example.handrehab.fragment

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.MediaController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.handrehab.Data
import com.example.handrehab.DataAllFingersSpread
import com.example.handrehab.MainViewModel
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentExerciseBinding
import com.example.handrehab.item.Datasource
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import splitties.toast.toast
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale


class ExerciseFragment : Fragment() {


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

      - In diesem Fragment werden die Details zu einer ausgewählten Übung angezeigt
      - Es wird ein Video sowie eine Beschreibung angezeigt
      - Es kann die zu trainierende Handseite, das Schwierigkeitslevel und eine
      geschlossene oder geöffnete Startposition abhängig von der Übung ausgewählt werden
      - Es wird ein Graph mit den Daten zu bereits durchgeführten Übungen angezeigt
      - Die Übung kann über einen FloatingActionButton gestartet werden


    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    // MediaController für Anzeige eines Videos
    private var mediaController: MediaController? = null

    private val viewModel: MainViewModel by activityViewModels()

    // Firebase Datenbank
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Listen für Daten aus Datenbank
    private var dbList = ArrayList <Data> ()
    private var dbListAllFingersSpread = ArrayList <DataAllFingersSpread> ()


    // Variablen für Graphen
    private var valueSeries : ArrayList<Entry> = ArrayList()

    // Variablen für Graphen der Übung, in der alle Finger gespreizt werden
    private var valueSeriesLittleFinger : ArrayList<Entry> = ArrayList()
    private var valueSeriesMiddleFinger : ArrayList<Entry> = ArrayList()
    private var valueSeriesPointingFinger : ArrayList<Entry> = ArrayList()
    private var valueSeriesThumb : ArrayList<Entry> = ArrayList()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Layout anpassen
        binding.textViewDescription.text = getString(viewModel.getSelectedExercise()!!.descriptionItem)
        binding.videoViewExercise.clipToOutline = true

        // Ausblenden der Navigationsleiste
        val view = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        view.visibility = GONE

        // Titel der Statusleiste auf die ausgewählte Übung anpassen
        (activity as AppCompatActivity).supportActionBar?.title = viewModel.getSelectedExercise()!!.textItem

        val id = viewModel.getSelectedExercise()!!.id

        // Ausblenden des ToggleButtons für die Auswahl der Startposition bei
        // den Übungen, wo die Finger gespreizt werden und wo der Daumen zur
        // Handinnenfläche bewegt wird
        if(id == 2 || id==3 || id==4 || id==5 || id == 6 || id == 12) {
            binding.toggleButton.visibility = GONE
            binding.textView4.visibility = GONE
            viewModel.setStartModus("")
        }

        // Ausblenden des ToggleButtons für die Auswahl des SChwierigkeitslevels bei
        // den Übungen, wo die Finger halb geöffnet/geschlossen werden und
        // wo alle Finger gespreizt werden
        if(id == 15 || id == 16 || id == 17 || id == 18 || id == 2){
            binding.toggleButtonLevel.visibility = GONE
            binding.textViewLevel.visibility = GONE
        }

        // Ausblenden der Beschreibung der Messanzeige bei den Übungen,
        //  wo alle Finger gespreizt und alle Finger geöffnet/geschlossen werden
        if (id == 2 || id == 13) {
            binding.textView6.visibility = GONE
            binding.imageView7.visibility = GONE
        }

        // Für die Übung, wo alle Finger gespreizt werden, wird die
        // Schwierigkeitsstufe auf leicht gestellt
        if(id == 2){
            viewModel.setDivideFactor(3.0)
        }

        // Starten einer Übung
        binding.extendedFab.setOnClickListener {
            // Speichern der Wiederholungen und Sätze
            binding.editTextRepetition.text.toString().toIntOrNull()
                ?.let { it1 -> viewModel.setRepetitions(it1) }
            binding.editTextSets.text.toString().toIntOrNull()
                ?.let { it1 -> viewModel.setSets(it1) }

            if(viewModel.getRepetitions() == 0 || viewModel.getSets() == 0){
                toast(getString(R.string.toast_enter_number))
                binding.editTextRepetition.setHintTextColor(Color.RED)
            } else // Zunächst Überprüfung, ob Zugriff auf Kamera vorhanden ist
                findNavController().navigate(R.id.action_exerciseFragment_to_permissionsFragment)
        }

        // Einstellung der Startposition der Hand
        binding.toggleButton.check(if(viewModel.getStartModus() == getString(R.string.closed)) R.id.button2 else R.id.button1)
        binding.toggleButton.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->

            if (isChecked) {
                // Überprüfen, welcher Button ausgewählt wurde
                when (checkedId) {
                    R.id.button1 -> {
                        viewModel.setStartModus(getString(R.string.start_mode_open))
                        dbList.clear()
                        // Daten laden zu der eingestellten Startposition
                        loadDbData()
                    }

                    R.id.button2 -> {
                        viewModel.setStartModus(getString(R.string.start_mode_close))
                        dbList.clear()
                        // Daten laden zu der eingestellten Startposition
                        loadDbData()
                    }
                }
            }
        }


        // Einstellung, welche Hand verwendet wird
        binding.toggleButtonHandSide.check(if(viewModel.getSelectedHandSide() == getString(R.string.selected_hand_right)) R.id.buttonRight else R.id.buttonLeft)
        binding.toggleButtonHandSide.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->

            if (isChecked) {
                // Überprüfen, welcher Button ausgewählt wurde
                when (checkedId) {
                    R.id.buttonRight -> {
                        viewModel.setSelectedHandSide(getString(R.string.selected_hand_right))
                        dbList.clear()
                        dbListAllFingersSpread.clear()
                        // Daten laden zu der eingestellten Handseite
                        if(viewModel.getSelectedExercise()!!.id == 2){
                            loadDbDataAllFingers()
                        } else loadDbData()
                    }

                    R.id.buttonLeft -> {
                        viewModel.setSelectedHandSide(getString(R.string.selected_hand_left))
                        dbList.clear()
                        dbListAllFingersSpread.clear()
                        // Daten laden zu der eingestellten Handseite
                        if(viewModel.getSelectedExercise()!!.id == 2){
                            loadDbDataAllFingers()
                        } else loadDbData()
                    }
                }
            }
        }

        // Anzeige eines Alert-Dialogs mit Infos zu den Werten im Graphen
        binding.imageButtonInfoGraph.setOnClickListener {

            val id = viewModel.getSelectedExercise()!!.id
            var text = getString(R.string.supporting_text_info_graph_close_fingers)

            // Übung: Spreizen der Finger
            if(id == 2 || id == 3 || id == 4 || id == 5 || id == 6) text = getString(R.string.supporting_text_info_graph_spread_finger)

            // Übung: Öffnen/Schließen der Finger
            if(id == 8 || id == 9 || id == 10|| id == 11|| id == 12 || id == 13) {
                text = if(viewModel.getStartModus()!! == getString(R.string.closed)){
                    getString(R.string.supporting_text_info_graph_close_fingers)
                } else getString(R.string.supporting_text_info_graph_open_fingers)
            }

            context?.let {
                MaterialAlertDialogBuilder(it)
                    .setTitle(resources.getString(R.string.title_alert_dialog_info_graph))
                    .setMessage(text)
                    .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                    }
                    .show()
            }
        }


        // Einstellung des ToggelButtons für das Schwierigkeitslevel
        val buttonId = when(viewModel.getDivideFactor()) {
            1.5 -> R.id.buttonHard
            2.0 -> R.id.buttonMedium
            3.0 -> R.id.buttonEasy
            else -> R.id.buttonMedium
        }
        binding.toggleButtonLevel.check(buttonId)

        // Einstellung des Schwierigkeitslevels
        binding.toggleButtonLevel.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->

            if (isChecked) {
                // Überprüfen, welcher Button ausgewählt wurde
                when (checkedId) {
                    R.id.buttonEasy -> {
                        viewModel.setDivideFactor(3.0)
                    }
                    R.id.buttonMedium -> {
                        viewModel.setDivideFactor(2.0)
                    }
                    R.id.buttonHard -> {
                        viewModel.setDivideFactor(1.5)
                    }
                }
            }
        }

        // Anzeige des ToggleButtons für die Übung, in welcher alle Finger
        // geöffnet und geschlossen werden
        // Auswahl zwischen halb und ganz schließen
        if(viewModel.getSelectedExercise()!!.id == 13){
            binding.toggleButtonLevel.visibility = GONE
            binding.textViewLevel.text = getString(R.string.text_open_close_all_fingers)
        } else binding.toggleButtonLevelAllFingers.visibility = GONE

        val buttonId2 = when(viewModel.getAllFingersOpenOrClose()) {
            1 -> R.id.buttonHalfOpenClose
            2-> R.id.buttonOpenClose
            else -> R.id.buttonHalfOpenClose
        }
        binding.toggleButtonLevelAllFingers.check(buttonId2)

        // Einstellung zwischen ganz und halb schließen
        binding.toggleButtonLevelAllFingers.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->

            if (isChecked) {
                // Überprüfen, welcher Button ausgewählt wurde
                when (checkedId) {
                    R.id.buttonHalfOpenClose -> {
                        viewModel.setAllFingersOpenOrClose(1)
                    }
                    R.id.buttonOpenClose -> {
                        viewModel.setAllFingersOpenOrClose(2)
                    }
                }
            }
        }

        // Initialisierung MediaController
        if (mediaController == null) {
            mediaController = MediaController(activity)
            mediaController!!.setAnchorView(binding.videoViewExercise)
        }
        binding.videoViewExercise.setMediaController(mediaController)

        //  Uri-Adresse des Videos einlesen
        val uri: Uri = Uri.parse("android.resource://" + activity?.packageName  + "/" + Datasource().getVideo(viewModel.getSelectedExercise()!!.id))
        binding.videoViewExercise.setVideoURI(uri)

        // Video starten
        binding.videoViewExercise.requestFocus()
        binding.videoViewExercise.seekTo(1)

        // Laden der Daten aus der Datenbank
        dbList.clear()
        dbListAllFingersSpread.clear()
        if(viewModel.getSelectedExercise()!!.id == 2){
            loadDbDataAllFingers()
        } else loadDbData()

    }

    /*
    =============================================================
    =======                   Funktionen                  =======
    =============================================================
    */

    // https://github.com/PhilJay/MPAndroidChart
    @RequiresApi(Build.VERSION_CODES.O)
    private fun configGraph(arr: ArrayList<String>) {

        // Formatierung der x-Achse
        val formatterX: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return if(value.toInt() < arr.size && value.toInt() >= 0) arr[value.toInt()]
                else "0"
            }
        }

        // Formatierung der y-Achse
        val formatterY: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return "${value.toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()} cm"
            }
        }

        // Beschreibung
        binding.lineChart.description.isEnabled = false

        // linke y-Achse
        val yAxis : YAxis = binding.lineChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.gridColor = Color.LTGRAY
        yAxis.valueFormatter = formatterY

        //rechte y-Achse
        binding.lineChart.axisRight.isEnabled = false

        // x-Achse
        val xAxis : XAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = formatterX
        xAxis.labelRotationAngle = -45f
        xAxis.gridColor = Color.LTGRAY
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        xAxis.mLabelRotatedHeight = 60

        // Scrolling und Skalieren
        binding.lineChart.isDragEnabled = true
        binding.lineChart.setScaleEnabled(true)

        val l: Legend = binding.lineChart.legend
        l.form = LegendForm.CIRCLE
        l.verticalAlignment = LegendVerticalAlignment.TOP
    }



    // Einlesen der Daten aus der Datenbank zu der Übung, wo Alle Finger gespreizt werden
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDbDataAllFingers() {

        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenAllFingersSpread")
            .whereEqualTo("selectedHandSide", viewModel.getSelectedHandSide())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    updateGraphAllFingers(task)
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }
    }

    // Aktualisierung des Graphen für die Übung, nei der alle Finger gespreizt werden
    // https://github.com/PhilJay/MPAndroidChart
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateGraphAllFingers(task: Task<QuerySnapshot>) {


        // Diese for-Schleife durchläuft alle Dokumente der Abfrage
        for (document in task.result!!) {
            (dbListAllFingersSpread).add(document.toObject(DataAllFingersSpread::class.java))
        }

        // Sortieren der Liste nach Datum
        val yourList: List<DataAllFingersSpread> = dbListAllFingersSpread
        val yourSortedList: List<DataAllFingersSpread> = yourList.sortedBy { it.getDate() }

        var counter = 0f
        val dataArray = arrayListOf<String>()

        valueSeriesLittleFinger.clear()
        valueSeriesMiddleFinger.clear()
        valueSeriesPointingFinger.clear()
        valueSeriesThumb.clear()

        // Abfrage des aktuellen Datums
        val zeitformat = SimpleDateFormat("dd.MM kk:mm", Locale.GERMANY)
        val year = SimpleDateFormat("yyyy")

        // Erstellen der Datenpunkte
        for (i in yourSortedList) {
            val dateFormat = zeitformat.format(i.getDate()!!)

            valueSeriesLittleFinger.add(Entry(counter, i.getMaxLittleFinger()))
            valueSeriesPointingFinger.add(Entry(counter, i.getMaxPointingFinger()))
            valueSeriesMiddleFinger.add(Entry(counter, i.getMaxMiddleFinger()))
            valueSeriesThumb.add(Entry(counter, i.getMaxThumbFinger()))

            dataArray.add(dateFormat)
            counter++
        }

        if(dbListAllFingersSpread.size != 0) {
            // Wenn Daten vorhanden sind, werden die Layout-Elemente eingeblendet
            binding.textViewPhase.visibility = GONE
            binding.lineChart.visibility = VISIBLE
            binding.textViewTitleGraph.visibility = VISIBLE
            binding.imageButtonInfoGraph.visibility = VISIBLE

            // Automatische Einstellung der Anzahl an Wiederholungen und Sätzen
            if(dbListAllFingersSpread.last().getRepetitions() >= 10) {
                binding.editTextRepetition.setText("5")
                binding.editTextSets.setText((yourSortedList.last().getSets()+1).toString())
            } else {
                binding.editTextRepetition.setText((yourSortedList.last().getRepetitions()+1).toString())
                binding.editTextSets.setText("2")
            }
        } else {
            binding.editTextRepetition.setText("2")
            binding.editTextSets.setText("2")
            // Wenn keine Daten vorhanden sind, werden die Layout-Elemente ausgeblendet
            binding.lineChart.visibility = GONE
            binding.textViewTitleGraph.visibility = GONE
            binding.imageButtonInfoGraph.visibility = GONE
            binding.textViewPhase.text = getString(R.string.no_data)
        }


        // Erstellen der Daten-Sets
        val lineDataSetLittleFinger = LineDataSet(valueSeriesLittleFinger, if(viewModel.getSelectedHandSide() == getString(R.string.right)) getString(R.string.values_right_little_finger_cm) else getString(R.string.values_left_little_finger_cm))
        lineDataSetLittleFinger.setColors(Color.MAGENTA)
        lineDataSetLittleFinger.setCircleColor(Color.MAGENTA)
        val lineDataSetMiddleFinger = LineDataSet(valueSeriesMiddleFinger, if(viewModel.getSelectedHandSide() == getString(R.string.right)) getString(R.string.values_right_middle_finger_cm) else getString(R.string.values_left_middle_finger_cm))
        lineDataSetMiddleFinger.setColors(Color.CYAN)
        lineDataSetMiddleFinger.setCircleColor(Color.CYAN)
        val lineDataSetPointingFinger = LineDataSet(valueSeriesPointingFinger, if(viewModel.getSelectedHandSide() == getString(R.string.right)) getString(R.string.values_right_pointing_finger_cm) else getString(R.string.values_left_pointing_finger_cm))
        lineDataSetPointingFinger.color = Color.BLUE
        lineDataSetPointingFinger.setCircleColor(Color.BLUE)
        val lineDataSetThumb = LineDataSet(valueSeriesThumb, if(viewModel.getSelectedHandSide() == getString(R.string.right)) getString(R.string.values_right_thumb_cm) else getString(R.string.values_left_thumb_cm))
        lineDataSetThumb.color = Color.DKGRAY
        lineDataSetThumb.setCircleColor(Color.DKGRAY)

        val dataSets = ArrayList <ILineDataSet>()
        dataSets.add(lineDataSetLittleFinger)
        dataSets.add(lineDataSetMiddleFinger)
        dataSets.add(lineDataSetPointingFinger)
        dataSets.add(lineDataSetThumb)

        // Einstellung der Legende
        val l: Legend = binding.lineChart.legend
        l.form = LegendForm.CIRCLE
        l.verticalAlignment = LegendVerticalAlignment.TOP
        l.isWordWrapEnabled = true


        // Anzeige des Graphen mit neuen Werten aktualisieren
        binding.lineChart.data = LineData(dataSets)
        binding.textViewTitleGraph.text = if(dbListAllFingersSpread.size != 0) year.format(dbListAllFingersSpread.last().getDate()!!) else ""
        configGraph(dataArray)
        binding.lineChart.invalidate()
    }

    // Einlesen der Daten aus der Datenbank
    // Es werden die Daten abhängig von der ausgewählten Übung, eingestellten Handseite
    // und dem Startmodus geladen
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDbData() {

        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("Daten")
            .whereEqualTo("exerciseId", viewModel.getSelectedExercise()!!.id)
            .whereEqualTo("selectedHandSide", viewModel.getSelectedHandSide())
            .whereEqualTo("startMode", viewModel.getStartModus())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    updateListView(task)
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateListView(task: Task<QuerySnapshot>) {

        // Diese for-Schleife durchläuft alle Dokumente der Abfrage
        for (document in task.result!!) {
            (dbList).add(document.toObject(Data::class.java))
        }

        dataToGraph(dbList)
    }

    // https://github.com/PhilJay/MPAndroidChart
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun dataToGraph(data: ArrayList<Data>) {

        // Sortieren der Liste nach Datum
        val yourList: List<Data> = data
        val yourSortedList: List<Data> = yourList.sortedBy { it.getDate() }

        var counter = 0f
        val dataArray = arrayListOf<String>()
        valueSeries.clear()

        // Abfrage des aktuellen Datums
        val zeitformat = SimpleDateFormat("dd.MM kk:mm", Locale.GERMANY)
        val year = SimpleDateFormat("yyyy")

        // Erstellen der Datenpunkte
        for (i in yourSortedList) {
            val dateFormat = zeitformat.format(i.getDate()!!)
            valueSeries.add(Entry(counter, i.getMax()))
            dataArray.add(dateFormat)
            counter++
        }


        if(data.size != 0) {
            val id = viewModel.getSelectedExercise()!!.id

            // Wenn Daten vorhanden sind, werden die Layout-Elemente eingeblendet
            binding.lineChart.visibility = VISIBLE
            binding.textViewTitleGraph.visibility = VISIBLE
            binding.imageButtonInfoGraph.visibility = VISIBLE

            // Anzeige der erreichten Phase
            if(id == 8 || id == 9 || id == 10 || id == 11 || id == 12 || id == 13) {
                binding.textViewPhase.text = getString(R.string.tv_phase, yourSortedList.last().getCurrentPhase())
            } else binding.textViewPhase.visibility = GONE

            // Ausblenden des Graphen (Alle Finger schließen/öffnen, Daumen zur Handinnenfläche)
            if(id == 12 || id == 13) {
                binding.lineChart.visibility = GONE
                binding.textViewTitleGraph.visibility = GONE
                binding.imageButtonInfoGraph.visibility = GONE
            }

            // Keine Daten werden zu diesen Übungen angezeigt (halbes schließen/öffnen der Finger)
            if(id == 15 || id == 16 || id == 17 || id ==18){
                binding.lineChart.visibility = GONE
                binding.textViewTitleGraph.visibility = GONE
                binding.imageButtonInfoGraph.visibility = GONE
                binding.textViewPhase.visibility = GONE
                binding.textView5.visibility = GONE
            }

            // Automatische Einstellung der Anzahl an Wiederholungen und Sätzen
            if(data.last().getRepetitions() >= 10) {
                binding.editTextRepetition.setText("5")
                binding.editTextSets.setText((yourSortedList.last().getSets()+1).toString())
            } else {
                binding.editTextRepetition.setText((yourSortedList.last().getRepetitions()+1).toString())
                binding.editTextSets.setText("2")
            }
        } else {
            binding.editTextRepetition.setText("2")
            binding.editTextSets.setText("2")
            // Wenn keine Daten vorhanden sind, werden die Layout-Elemente ausgeblendet
            binding.lineChart.visibility = GONE
            binding.textViewTitleGraph.visibility = GONE
            binding.imageButtonInfoGraph.visibility = GONE
            binding.textViewPhase.text = getString(R.string.no_data)
        }


        // Erstellung des Daten-Sets
        val lineDataSet = LineDataSet(valueSeries, if(viewModel.getSelectedHandSide() == getString(R.string.right)) getString(R.string.values_right_hand_cm) else getString(R.string.values_left_hand_cm))

        // Aktualisierung des Graphen
        binding.lineChart.data = LineData(lineDataSet)
        binding.textViewTitleGraph.text = if(data.size != 0) year.format(data.last().getDate()!!) else ""
        configGraph(dataArray)
        binding.lineChart.invalidate()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        dbList.clear()
        dbListAllFingersSpread.clear()
    }
}