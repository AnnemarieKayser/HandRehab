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
import android.view.ViewGroup
import android.widget.MediaController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.handrehab.Data
import com.example.handrehab.DataMinMax
import com.example.handrehab.MainViewModel
import com.example.handrehab.PermissionsFragment
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentExerciseBinding
import com.example.handrehab.item.Datasource
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import splitties.toast.toast
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Locale


class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    // === MediaController === //
    private var mediaController: MediaController? = null

    // === Viewmodel === //
    private val viewModel: MainViewModel by activityViewModels()

    // === Firebase database === //
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var counterExercise = 0

    // === Graph === //
    private lateinit var dateMonday: String
    private lateinit var dateThuesday: String
    private var arrayDays = arrayOf("", "", "", "", "", "", "")
    private var counterWeeksMonday = 0
    private var arrayWeekDays = arrayOfNulls<Any>(7)
    private var dbList = ArrayList <Data> ()
    private var dbListMinMax = ArrayList <DataMinMax> ()
    private var values = arrayListOf<Float>()
    private var valuesDate = arrayOf<String>()
    private var listValuesDate = arrayListOf<String>()
    private var categoriesString = ""

    // === Line Graph === //
    private var valueSeries : ArrayList<Entry> = ArrayList()
    private var valueSeriesLittleFinger : ArrayList<Entry> = ArrayList()
    private var valueSeriesMiddleFinger : ArrayList<Entry> = ArrayList()
    private var valueSeriesPointingFinger : ArrayList<Entry> = ArrayList()
    private var valueSeriesThumb : ArrayList<Entry> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.O)
    private val baseTimestamp = LocalDateTime.now()



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

        binding.textViewDescription.text = getString(viewModel.getSelectedExercise()!!.descriptionItem)


        val view = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        (activity as AppCompatActivity).supportActionBar?.title = viewModel.getSelectedExercise()!!.textItem

        binding.videoViewExercise.clipToOutline = true

        view.visibility = GONE

        val id = viewModel.getSelectedExercise()!!.id

        if(id == 6 || id == 2 || id==3 || id==4 || id==5) {

            binding.toggleButton.visibility = GONE
            binding.textView4.visibility = GONE
            viewModel.setStartModus("")
        }


        binding.extendedFab.setOnClickListener {
            binding.editTextRepetition.text.toString().toIntOrNull()
                ?.let { it1 -> viewModel.setRepetitions(it1) }
            binding.editTextSets.text.toString().toIntOrNull()
                ?.let { it1 -> viewModel.setSets(it1) }
            Log.i("Repetition", viewModel.getRepetitions().toString())
            counterExercise++
           // insertDataInDb(counterExercise)
            if(viewModel.getRepetitions() == 0 || viewModel.getSets() == 0){
                toast("Trage bitte eine Anzahl ein")
                binding.editTextRepetition.setHintTextColor(Color.RED)
            } else findNavController().navigate(R.id.action_exerciseFragment_to_permissionsFragment)

        }

        // --- Änderung des Status des toggle-Buttons --- //
        // Einstellung der Start Position der Hand
        binding.toggleButton.check(if(viewModel.getStartModus() == "geschlossen") R.id.button2 else R.id.button1)
        binding.toggleButton.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->

            if (isChecked) {
                // Überprüfen, welcher Button ausgewählt wurde
                when (checkedId) {
                    R.id.button1 -> {
                        viewModel.setStartModus(getString(R.string.start_mode_open))
                        Log.i("ToggleButton", "Button 1 checked")
                        dbList.clear()
                        loadDbData()
                    }

                    R.id.button2 -> {
                        viewModel.setStartModus(getString(R.string.start_mode_close))
                        Log.i("ToggleButton", "Button 2 checked")
                        dbList.clear()
                        loadDbData()
                    }
                }
            }

        }


        // --- Änderung des Status des toggle-Buttons --- //
        //Einstellung, welche Hand verwendet wird
        binding.toggleButtonHandSide.check(if(viewModel.getSelectedHandSide() == getString(R.string.selected_hand_right)) R.id.buttonRight else R.id.buttonLeft)
        binding.toggleButtonHandSide.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->

            if (isChecked) {
                // Überprüfen, welcher Button ausgewählt wurde
                when (checkedId) {
                    R.id.buttonRight -> {
                        viewModel.setSelectedHandSide(getString(R.string.selected_hand_right))
                        Log.i("ToggleButton", "Button 1 checked")
                        dbList.clear()
                        dbListMinMax.clear()
                        if(viewModel.getSelectedExercise()!!.id == 2){
                            loadDbDataAllFingers()
                        } else loadDbData()
                    }

                    R.id.buttonLeft -> {
                        viewModel.setSelectedHandSide(getString(R.string.selected_hand_left))
                        Log.i("ToggleButton", "Button 2 checked")
                        dbList.clear()
                        dbListMinMax.clear()
                        if(viewModel.getSelectedExercise()!!.id == 2){
                            loadDbDataAllFingers()
                        } else loadDbData()
                    }
                }
            }
        }


        // --- Änderung des Status des toggle-Buttons --- //
        // Einstellung der Start Position der Hand

        var buttonId = when(viewModel.getDivideFactor()) {
            1.5 -> R.id.buttonEasy
            2.0 -> R.id.buttonMedium
            3.0 -> R.id.buttonHard
            else -> R.id.buttonMedium
        }

        binding.toggleButtonLevel.check(buttonId)

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


        // --- Initialisierung MediaController --- //
        if (mediaController == null) {
            mediaController = MediaController(activity)
            mediaController!!.setAnchorView(binding.videoViewExercise)
        }
        binding.videoViewExercise.setMediaController(mediaController)


        //  Uri-Adresse einlesen
        val uri: Uri = Uri.parse("android.resource://" + activity?.packageName  + "/" + Datasource().getVideo(viewModel.getSelectedExercise()!!.id))

        binding.videoViewExercise.setVideoURI(uri)

        // Video starten
        binding.videoViewExercise.requestFocus()
        binding.videoViewExercise.seekTo(1)
        //binding.videoViewExercise.start()

        dbList.clear()
        dbListMinMax.clear()
        if(viewModel.getSelectedExercise()!!.id == 2){
            loadDbDataAllFingers()
        } else loadDbData()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun configGraph(arr: ArrayList<String>) {

        Log.i("arrLength", arr.size.toString())


        val formatter: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                Log.i("FloatValue", value.toString())
                return if(value.toInt() < arr.size && value.toInt() >= 0) arr[value.toInt()]
                else "0"
            }
        }


        //Beschreibung
        binding.lineChart.description.isEnabled = false

        //linke y-Achse
        val yAxis : YAxis = binding.lineChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.gridColor = Color.LTGRAY


        //rechte y-Achse
        binding.lineChart.axisRight.isEnabled = false

        //x-Achse
        val xAxis : XAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = formatter
        xAxis.labelRotationAngle = -45f
        xAxis.gridColor = Color.LTGRAY
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        xAxis.mLabelRotatedHeight = 60

        //enable scrolling and scaling
        binding.lineChart.isDragEnabled = true
        binding.lineChart.setScaleEnabled(true)
    }





    // === loadDbData === //
    // Einlesen der Daten aus der Datenbank
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDbData() {

        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
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


    // === loadDbData === //
    // Einlesen der Daten aus der Datenbank
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDbDataAllFingers() {


        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenMinMax")
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

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateGraphAllFingers(task: Task<QuerySnapshot>) {
        // Einträge in dbList kopieren, um sie im ListView anzuzeigen


        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task.result!!) {
            (dbListMinMax).add(document.toObject(DataMinMax::class.java))
            Log.d("Daten", document.id + " => " + document.data)
        }

        val yourList: List<DataMinMax> = dbListMinMax
        val yourSortedList: List<DataMinMax> = yourList.sortedBy { it.getDate() }

        var counter = 0f
        val dataArray = arrayListOf<String>()

        valueSeriesLittleFinger.clear()
        valueSeriesMiddleFinger.clear()
        valueSeriesPointingFinger.clear()
        valueSeriesThumb.clear()

        val zeitformat = SimpleDateFormat("MM-dd-kk-mm", Locale.GERMANY)
        val year = SimpleDateFormat("yyyy")



        for (i in yourSortedList) {
            val dateFormat = zeitformat.format(i.getDate()!!)

            valueSeriesLittleFinger.add(Entry(counter, i.getMaxLittleFinger()))
            valueSeriesPointingFinger.add(Entry(counter, i.getMaxPointingFinger()))
            valueSeriesMiddleFinger.add(Entry(counter, i.getMaxMiddleFinger()))
            valueSeriesThumb.add(Entry(counter, i.getMaxThumbFinger()))

            dataArray.add(dateFormat)
            counter++
        }

        if(dbListMinMax.size != 0) {
            if(dbListMinMax.last().getRepetitions() >= 10) {
                binding.editTextRepetition.setText("5")
                binding.editTextSets.setText((yourSortedList.last().getSets()+1).toString())
            } else {
                binding.editTextRepetition.setText((yourSortedList.last().getRepetitions()+1).toString())
                binding.editTextSets.setText("2")
            }
        } else {
            binding.editTextRepetition.setText("2")
            binding.editTextSets.setText("2")
        }


        val lineDataSetLittleFinger = LineDataSet(valueSeriesLittleFinger, if(viewModel.getSelectedHandSide() == "rechts") "Messwerte kleiner Finger rechts" else "Messwerte kleiner Finger links")
        lineDataSetLittleFinger.setColors(Color.MAGENTA)
        lineDataSetLittleFinger.setCircleColor(Color.MAGENTA)
        val lineDataSetMiddleFinger = LineDataSet(valueSeriesMiddleFinger, if(viewModel.getSelectedHandSide() == "rechts") "Messwerte Mittelfinger rechts" else "Messwerte Mittelfinger links")
        lineDataSetMiddleFinger.setColors(Color.CYAN)
        lineDataSetMiddleFinger.setCircleColor(Color.CYAN)
        val lineDataSetPointingFinger = LineDataSet(valueSeriesPointingFinger, if(viewModel.getSelectedHandSide() == "rechts") "Messwerte Zeigefinger rechts" else "Messwerte Zeigefinger links")
        lineDataSetPointingFinger.color = Color.BLUE
        lineDataSetPointingFinger.setCircleColor(Color.BLUE)
        val lineDataSetThumb = LineDataSet(valueSeriesThumb, if(viewModel.getSelectedHandSide() == "rechts") "Messwerte Daumen rechts" else "Messwerte Daumen links")
        lineDataSetThumb.color = Color.DKGRAY
        lineDataSetThumb.setCircleColor(Color.DKGRAY)

        val dataSets = ArrayList <ILineDataSet>()
        dataSets.add(lineDataSetLittleFinger)
        dataSets.add(lineDataSetMiddleFinger)
        dataSets.add(lineDataSetPointingFinger)
        dataSets.add(lineDataSetThumb)


        binding.lineChart.data = LineData(dataSets)

        binding.textViewTitleGraph.text = if(dbListMinMax.size != 0) year.format(dbListMinMax.last().getDate()!!) else ""
        configGraph(dataArray)
        binding.lineChart.invalidate()


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateListView(task: Task<QuerySnapshot>) {
        // Einträge in dbList kopieren, um sie im ListView anzuzeigen


        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task.result!!) {
            (dbList).add(document.toObject(Data::class.java))
            Log.d("Daten", document.id + " => " + document.data)
        }

        dataToGraph(dbList)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun dataToGraph(data: ArrayList<Data>) {


        val yourList: List<Data> = data
        val yourSortedList: List<Data> = yourList.sortedBy { it.getDate() }

        var counter = 0f
        val dataArray = arrayListOf<String>()
        valueSeries.clear()
        val zeitformat = SimpleDateFormat("MM-dd-kk-mm", Locale.GERMANY)
        val year = SimpleDateFormat("yyyy")



        for (i in yourSortedList) {
            Log.i("ValuesNew", i.toString())
            val dateFormat = zeitformat.format(i.getDate()!!)
            valueSeries.add(Entry(counter, i.getMax()))
            dataArray.add(dateFormat)
            counter++
        }

        if(data.size != 0) {
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
        }


        val lineDataSet = LineDataSet(valueSeries, if(viewModel.getSelectedHandSide() == "rechts") "Messwerte Rechte Hand" else "Messwerte Linke Hand")
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
        dbListMinMax.clear()
    }


}