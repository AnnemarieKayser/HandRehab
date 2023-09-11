package com.example.handrehab.fragment
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.handrehab.Data
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentDataBinding
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartStackingType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAScrollablePlotArea
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import com.google.android.gms.tasks.Task
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import splitties.toast.toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class DataFragment : Fragment() {

    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!

    // Datum
    private var date = ""

    // ListView
    private var dbList = ArrayList <Data> ()
    private lateinit var adapter : ArrayAdapter<String>

    // === Firebase database === //
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var data: Data? = null

    // === Graph === //
    private var aaChartModel = AAChartModel()
    private var arrayWeekDays = arrayOfNulls<Any>(7)

    // === week days === //
    private var monday = 0
    private var thuesday = 0
    private var wednesday = 0
    private var thursday = 0
    private var friday = 0
    private var saturday = 0
    private var sunday = 0
    private var counterWeeksSunday = 0
    private var counterWeeksMonday = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGetData.setOnClickListener {
            loadDbData()
        }

        binding.imageButtonBefore.setOnClickListener {
            counterWeeksMonday+= 7
            counterWeeksSunday += 1
            loadDbData()
        }

        binding.imageButtonAfter.setOnClickListener {
            if(counterWeeksSunday != 0 && counterWeeksMonday != 0) {
                counterWeeksMonday -= 7
                counterWeeksSunday -= 1
                loadDbData()
            } else {
                toast("Aktuelles Datum")
            }
        }


        binding.imageButtonCalender.setOnClickListener {
            // Nur zurückliegende Daten können ausgewählt werden
            val constraintsBuilder = CalendarConstraints.Builder().setValidator(
                DateValidatorPointBackward.now())

            // Aufbau des DatePickers
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setTitleText(getString(R.string.date_picker_title))
                    .build()

            datePicker.show(parentFragmentManager, "tag")

            datePicker.addOnPositiveButtonClickListener {
                // Klick auf positiven Button
                // ausgewähltes Datum formatieren
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = datePicker.selection!!
                val format = SimpleDateFormat("yyyy-MM-dd")
                date = format.format(calendar.time)

                binding.textViewDate.text = date

                // Daten zu dem ausgewählten Tag werden geladen und angezeigt
               // loadDbData(date)
            }
            datePicker.addOnNegativeButtonClickListener {
                // Klick auf negativen Button
                // datePicker wird geschlossen
            }
        }

        // --- Initialisierung und Konfiguration des Graphen --- //
        setUpAAChartView()

    }

    // === setUpAAChartView === //
    // https://github.com/AAChartModel/AAChartCore-Kotlin
    fun setUpAAChartView() {
        aaChartModel = configureAAChartModel()
        binding.chartView.aa_drawChartWithChartModel(aaChartModel)
    }

    // === configureAAChartModel === //
    // https://github.com/AAChartModel/AAChartCore-Kotlin
    private fun configureAAChartModel(): AAChartModel {
        val aaChartModel = configureChartBasicContent()
        aaChartModel.series(this.configureChartSeriesArray() as Array<Any>)
        return aaChartModel
    }

    // === configureChartBasicContent === //
    // https://github.com/AAChartModel/AAChartCore-Kotlin
    private fun configureChartBasicContent(): AAChartModel {

        return AAChartModel.Builder(requireContext().applicationContext)
            .setChartType(AAChartType.Column)
            .setXAxisVisible(true)
            .setTitle(getString(R.string.chart_title))
            .setColorsTheme(arrayOf("#699638", "#BEFCA4", "#EEFF05","#345428", "#a7e810", "#0a6e09" ))
            .setStacking(AAChartStackingType.Normal)
            .setTitleStyle(AAStyle.Companion.style("#FFFFFF"))
            .setBackgroundColor("#182015")
            .setAxesTextColor("#FFFFFF")
            .setCategories("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
            .setYAxisTitle("Anzahl")
            .setYAxisMax(30f)
            .setScrollablePlotArea(
                AAScrollablePlotArea()
                    .minWidth(600)
                    .scrollPositionX(1f))
            .build()
    }

    // === configureChartSeriesArray === //
    // Initialisierung Datentyp und Kategorien
    // https://github.com/AAChartModel/AAChartCore-Kotlin
    private fun configureChartSeriesArray(): Array<AASeriesElement> {

        return arrayOf(
            AASeriesElement()
                .name("Anzahl")
                .data(arrayWeekDays as Array<Any>),
        )
    }

    // === loadDbData === //
    // Einlesen der Daten aus der Datenbank
    private fun loadDbData() {

        val seriesArr = configureChartSeriesArray()
        binding.chartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(seriesArr)

        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd")

        val day = calendar.get(Calendar.DAY_OF_WEEK) - 2

        calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))

        val dateMonday = format.format(calendar.time)

        val dateMondayBefore: Date = format.parse(dateMonday) as Date

        calendar.add(Calendar.DAY_OF_WEEK, + 6)

        val dateSunday: Date = calendar.time

        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("Daten")
            .whereGreaterThanOrEqualTo("date", dateMondayBefore) // abrufen
            .whereLessThanOrEqualTo("date", dateSunday)
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

    private fun updateListView(task: Task<QuerySnapshot>) {
        // Einträge in dbList kopieren, um sie im ListView anzuzeigen

        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task.result!!) {
            (dbList as ArrayList<Data>).add(document.toObject(Data::class.java))
             Log.d("Daten", document.id + " => " + document.data)
        }
        // jetzt liegt die vollständige Liste vor und
        // kann im ListView angezeigt werden

        // Adapter für den ListView
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,   // Layout zur Darstellung der ListItems
            dbList)

        binding.listViewData.adapter = adapter

        dataToGraph(dbList)
    }

    private fun dataToGraph(data: ArrayList<Data>) {

        for(i in data){

            when(i.getDayOfWeek()) {
                0 -> {
                    monday++
                }

                1 -> {
                    thuesday++
                }

                2 -> {
                    wednesday++
                }

                3 -> {
                    thursday++
                }

                4 -> {
                    friday++
                }

                5 -> {
                    saturday++
                }

                6 -> {
                    sunday++
                }

                else -> {}
            }
        }

        arrayWeekDays[0] = monday
        arrayWeekDays[1] = thuesday
        arrayWeekDays[2] = wednesday
        arrayWeekDays[3] = thursday
        arrayWeekDays[4] = friday
        arrayWeekDays[5] = saturday
        arrayWeekDays[6] = sunday

        val seriesArr = configureChartSeriesArray()
        binding.chartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(seriesArr)

        monday = 0; thuesday = 0; wednesday = 0; thursday = 0; friday = 0; saturday = 0; sunday = 0;

        for (i in 0 until 7) {
            arrayWeekDays[i] = 0
        }

        dbList.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}