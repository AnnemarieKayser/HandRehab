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
import splitties.toast.longToast
import splitties.toast.toast
import java.text.ParseException
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGetData.setOnClickListener {
            loadDbData()
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

       // db.collection("users").document(uid).collection(date).document(viewModel.getSelectedExercise()!!.textItem).collection(hour)

        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd")
        val dayNumberFormat = SimpleDateFormat("c")
        var dayNumber = dayNumberFormat.format(calendar.time)
     //   var date2 = format.format(calendar.time)
      //  toast(date2)

       // val myDate: Date = format.parse(calendar.time.toString())

        //val calendar = Calendar.getInstance()
       // calendar.time = myDate
        //calendar.add(Calendar.DAY_OF_WEEK, -2)
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        toast(day.toString())
       // val newDate: Date = calendar.time
        //val date: String = dayNumberFormat.format(newDate)
        //longToast(date)


            //calendar.add(Calendar.DAY_OF_WEEK, -2)
            calendar.add(Calendar.DAY_OF_WEEK, -day)
            // val day = calendar.get(Calendar.DAY_OF_WEEK)
            //toast(day.toString())
            val newDate: Date = calendar.time
        toast(newDate.toString())
            val date: String = format.format(newDate)


        var datetimestamp: Date? = null
        try {
            datetimestamp = format.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

            // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
            val uid = mFirebaseAuth.currentUser!!.uid
            db.collection("users").document(uid).collection("Daten")
                .whereGreaterThanOrEqualTo("dateTimestamp", datetimestamp!!) // abrufen
                // .whereEqualTo("exerciseName", "Zeigefinger Extension,Flexion") // alle Einträge abrufen
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Datenbankantwort in Objektvariable speichern
                       // data = task.result!!.toObject(Data::class.java)
                         updateListView(task)
                        //toast(data.toString())
                    } else {
                        Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                    }
                }


/*
        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid)
           // .whereEqualTo("exerciseName", "Zeigefinger Extension,Flexion") // alle Einträge abrufen
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    data = task.result!!.toObject(Data::class.java)
                   // updateListView(task)
                    //toast(data.toString())
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }*/
    }

    private fun updateListView(task: Task<QuerySnapshot>) {
        // Einträge in dbList kopieren, um sie im ListView anzuzeigen
       // dbList = ArrayList()

        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task.result!!) {
            (dbList as ArrayList<Data>).add(document.toObject(Data::class.java))
            //Log.d(TAG, document.id + " => " + document.data)
        }
        // jetzt liegt die vollständige Liste vor und
        // kann im ListView angezeigt werden

        // Adapter für den ListView
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,   // Layout zur Darstellung der ListItems
            dbList)


        binding.listViewData.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}