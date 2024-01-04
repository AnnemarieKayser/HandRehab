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
import com.example.handrehab.DataMinMax
import com.example.handrehab.MainViewModel
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentExerciseBinding
import com.example.handrehab.item.Datasource
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.components.LimitLine
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
    private var dbList = ArrayList <Data> ()
    private var dbListMinMax = ArrayList <DataMinMax> ()


    // === Line Graph === //
    private var valueSeries : ArrayList<Entry> = ArrayList()
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

        if(id == 12){
            binding.toggleButton.visibility = GONE
            binding.textView4.visibility = GONE
        }

        if(id == 15 || id == 16 || id == 17 || id == 18){
            binding.toggleButtonLevel.visibility = GONE
            binding.textViewLevel.visibility = GONE
        }

        if (id == 2 || id == 20 || id == 13) {
            binding.textView6.visibility = GONE
        }

        if(id == 2){
            binding.textViewLevel.visibility = GONE
            binding.toggleButtonLevel.visibility = GONE
            viewModel.setDivideFactor(3.0)
        }


        binding.extendedFab.setOnClickListener {
            binding.editTextRepetition.text.toString().toIntOrNull()
                ?.let { it1 -> viewModel.setRepetitions(it1) }
            binding.editTextSets.text.toString().toIntOrNull()
                ?.let { it1 -> viewModel.setSets(it1) }
            counterExercise++
           // insertDataInDb(counterExercise)
            if(viewModel.getRepetitions() == 0 || viewModel.getSets() == 0){
                toast(getString(R.string.toast_enter_number))
                binding.editTextRepetition.setHintTextColor(Color.RED)
            } else findNavController().navigate(R.id.action_exerciseFragment_to_permissionsFragment)

        }

        // --- Änderung des Status des toggle-Buttons --- //
        // Einstellung der Start Position der Hand
        binding.toggleButton.check(if(viewModel.getStartModus() == getString(R.string.closed)) R.id.button2 else R.id.button1)
        binding.toggleButton.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->

            if (isChecked) {
                // Überprüfen, welcher Button ausgewählt wurde
                when (checkedId) {
                    R.id.button1 -> {
                        viewModel.setStartModus(getString(R.string.start_mode_open))
                        dbList.clear()
                        loadDbData()
                    }

                    R.id.button2 -> {
                        viewModel.setStartModus(getString(R.string.start_mode_close))
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
                        dbList.clear()
                        dbListMinMax.clear()
                        if(viewModel.getSelectedExercise()!!.id == 2){
                            loadDbDataAllFingers()
                        } else loadDbData()
                    }

                    R.id.buttonLeft -> {
                        viewModel.setSelectedHandSide(getString(R.string.selected_hand_left))
                        dbList.clear()
                        dbListMinMax.clear()
                        if(viewModel.getSelectedExercise()!!.id == 2){
                            loadDbDataAllFingers()
                        } else loadDbData()
                    }
                }
            }
        }

        binding.imageButtonInfoGraph.setOnClickListener {

            val id = viewModel.getSelectedExercise()!!.id
            var text = getString(R.string.supporting_text_info_graph_close_fingers)

            if(id == 2 || id == 3 || id == 4 || id == 5 || id == 6) text = getString(R.string.supporting_text_info_graph_spread_finger)
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
                        // Respond to positive button press
                    }
                    .show()
            }
        }


        // --- Änderung des Status des toggle-Buttons --- //
        // Einstellung der Start Position der Hand

        val buttonId = when(viewModel.getDivideFactor()) {
            1.5 -> R.id.buttonHard
            2.0 -> R.id.buttonMedium
            3.0 -> R.id.buttonEasy
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


        val formatter: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return if(value.toInt() < arr.size && value.toInt() >= 0) arr[value.toInt()]
                else "0"
            }
        }

        val formatterY: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return "${value.toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()} cm"
            }
        }

        val id = viewModel.getSelectedExercise()!!.id
        if( id == 2 || id == 3 || id == 4 || id == 5 || id == 6 ) {
            //Finger spreizen
            val lGood = LimitLine(5F, "Gut")
            lGood.lineColor = Color.GREEN
            lGood.lineWidth = 1F

            lGood.textColor = Color.BLACK
            lGood.textSize = 10f

            val lMiddle = LimitLine(2F, "Mittel")
            lMiddle.lineColor = Color.YELLOW
            lMiddle.lineWidth = 1F

            lMiddle.textColor = Color.BLACK
            lMiddle.textSize = 10f

            val lBad = LimitLine(0F, "Verbesserungsfähig")
            lBad.lineColor = Color.RED
            lBad.lineWidth = 1F

            lBad.textColor = Color.BLACK
            lBad.textSize = 10f

            binding.lineChart.axisLeft.addLimitLine(lGood)
            binding.lineChart.axisLeft.addLimitLine(lMiddle)
            binding.lineChart.axisLeft.addLimitLine(lBad)


            binding.lineChart.axisLeft.setDrawLimitLinesBehindData(true)
        } else {
            //Finger spreizen
            val lGood = LimitLine(14F, "Gut")
            lGood.lineColor = Color.GREEN
            lGood.lineWidth = 1F

            lGood.textColor = Color.BLACK
            lGood.textSize = 10f


            val lMiddle = LimitLine(10F, "Mittel")
            lMiddle.lineColor = Color.YELLOW
            lMiddle.lineWidth = 1F

            lMiddle.textColor = Color.BLACK
            lMiddle.textSize = 10f

            val lBad = LimitLine(0F, "Verbesserungsfähig")
            lBad.lineColor = Color.RED
            lBad.lineWidth = 1F

            lBad.textColor = Color.BLACK
            lBad.textSize = 10f

            binding.lineChart.axisLeft.addLimitLine(lGood)
            binding.lineChart.axisLeft.addLimitLine(lMiddle)
            binding.lineChart.axisLeft.addLimitLine(lBad)


            binding.lineChart.axisLeft.setDrawLimitLinesBehindData(true)
        }




        //Beschreibung
        binding.lineChart.description.isEnabled = false

        //linke y-Achse
        val yAxis : YAxis = binding.lineChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.gridColor = Color.LTGRAY
        yAxis.valueFormatter = formatterY


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

        val l: Legend = binding.lineChart.legend
        //l.formSize = 10f // set the size of the legend forms/shapes

        l.form = LegendForm.CIRCLE // set what type of form/shape should be used
        l.verticalAlignment = LegendVerticalAlignment.TOP
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
        }

        val yourList: List<DataMinMax> = dbListMinMax
        val yourSortedList: List<DataMinMax> = yourList.sortedBy { it.getDate() }

        var counter = 0f
        val dataArray = arrayListOf<String>()

        valueSeriesLittleFinger.clear()
        valueSeriesMiddleFinger.clear()
        valueSeriesPointingFinger.clear()
        valueSeriesThumb.clear()

        val zeitformat = SimpleDateFormat("dd.MM kk:mm", Locale.GERMANY)
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
            binding.textViewPhase.visibility = GONE
            binding.lineChart.visibility = VISIBLE
            binding.textViewTitleGraph.visibility = VISIBLE
            binding.imageButtonInfoGraph.visibility = VISIBLE

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
            binding.lineChart.visibility = GONE
            binding.textViewTitleGraph.visibility = GONE
            binding.imageButtonInfoGraph.visibility = GONE
            binding.textViewPhase.text = getString(R.string.no_data)
        }


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

        val l: Legend = binding.lineChart.legend
        //l.formSize = 10f // set the size of the legend forms/shapes

        l.form = LegendForm.CIRCLE // set what type of form/shape should be used
        l.verticalAlignment = LegendVerticalAlignment.TOP
        l.isWordWrapEnabled = true


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
        val zeitformat = SimpleDateFormat("dd.MM kk:mm", Locale.GERMANY)
        val year = SimpleDateFormat("yyyy")



        for (i in yourSortedList) {
            val dateFormat = zeitformat.format(i.getDate()!!)
            valueSeries.add(Entry(counter, i.getMax()))
            dataArray.add(dateFormat)
            counter++
        }



        if(data.size != 0) {
            val id = viewModel.getSelectedExercise()!!.id
            binding.lineChart.visibility = VISIBLE
            binding.textViewTitleGraph.visibility = VISIBLE
            binding.imageButtonInfoGraph.visibility = VISIBLE

            if(id == 8 || id == 9 || id == 10 || id == 11 || id == 12 || id == 13) {
                binding.textViewPhase.text = getString(R.string.tv_phase, yourSortedList.last().getCurrentPhase())
            } else binding.textViewPhase.visibility = GONE

            if(id == 12 || id == 13) {
                binding.lineChart.visibility = GONE
                binding.textViewTitleGraph.visibility = GONE
                binding.imageButtonInfoGraph.visibility = GONE
            }

            if(id == 15 || id == 16 || id == 17 || id ==18){
                binding.lineChart.visibility = GONE
                binding.textViewTitleGraph.visibility = GONE
                binding.imageButtonInfoGraph.visibility = GONE
                binding.textViewPhase.visibility = GONE
                binding.textView5.visibility = GONE
            }

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
            binding.lineChart.visibility = GONE
            binding.textViewTitleGraph.visibility = GONE
            binding.imageButtonInfoGraph.visibility = GONE
            binding.textViewPhase.text = getString(R.string.no_data)

        }


        val lineDataSet = LineDataSet(valueSeries, if(viewModel.getSelectedHandSide() == getString(R.string.right)) getString(R.string.values_right_hand_cm) else getString(R.string.values_left_hand_cm))
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