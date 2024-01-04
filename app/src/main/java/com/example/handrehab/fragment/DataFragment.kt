package com.example.handrehab.fragment
import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.handrehab.Data
import com.example.handrehab.DataGoal
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentDataBinding
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartStackingType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAScrollablePlotArea
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.mikhaellopez.circularprogressbar.CircularProgressBar
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
    private var dbList = ArrayList <Any> ()
    private var dbListGoals = ArrayList <Any> ()
    private lateinit var adapter : ArrayAdapter<String>

    // === Firebase database === //
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var data: Data? = null

    // === Graph === //
    private var aaChartModel = AAChartModel()
    private var arrayWeekDays = arrayOfNulls<Any>(7)
    private var arrayWeekGoal = arrayOfNulls<Any>(7)

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

    // === goals === //
    private var mondayGoal = 0
    private var thuesdayGoal = 0
    private var wednesdayGoal = 0
    private var thursdayGoal = 0
    private var fridayGoal = 0
    private var saturdayGoal = 0
    private var sundayGoal = 0

    private lateinit var dateMonday: String
    private lateinit var dateThuesday: String
    private var arrayDays = arrayOf("", "", "", "", "", "", "")

    // === Circular-Progress-Bar === //
    private var maxProgressBar = 0F
    private var progress: Float = 0F

    private var goal = 0f

    private var counterDate = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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
                toast(getString(R.string.currentDate))
            }
        }

        binding.ButtonSave.setOnClickListener {

            if(binding.EditTextSetGoal.text.toString() != "") {
                goal = binding.EditTextSetGoal.text.toString().toFloatOrNull()!!
                binding.textViewExercises.text =
                    getString(R.string.text_view_exercises_done, counterDate, goal.toInt())
                // Aktualisierung der Anzeige
                // https://github.com/lopspower/CircularProgressBar

                binding.circularProgressBarData.apply {
                    progressMax = goal
                }

                saveGoal()
                loadDbGoalWeek()
            }
        }

        // --- Konfiguration CircularProgressBar --- //
        // Anzeige des Fortschritts mit gerader Haltung
        // https://github.com/lopspower/CircularProgressBar
        binding.circularProgressBarData.apply {
            // Progress Max
            progressMax = maxProgressBar

            // ProgressBar Farbe
            progressBarColorStart = Color.parseColor("#924275")
            progressBarColorEnd = Color.parseColor("#5A1144")
            // Farbgradient
            progressBarColorDirection = CircularProgressBar.GradientDirection.RIGHT_TO_LEFT

            // Hintergrundfarbe
            backgroundProgressBarColor = Color.GRAY
            backgroundProgressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM

            // Weite der ProgressBar
            progressBarWidth = 10f
            backgroundProgressBarWidth = 4f

            roundBorder = true

            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
        }




        loadGoal()

    }
    private fun saveGoal() {

        //Data Objekt mit Daten befüllen (ID wird automatisch ergänzt)
        val data = DataGoal()

        data.setGoalExercises(goal)

        // --- Initialisierung und Konfiguration des Graphen --- //
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")
        var date = format.format(calendar.time)
        val day = calendar.get(Calendar.DAY_OF_WEEK) - 1

        data.setDate(format.parse(date) as Date)
        data.setDayOfWeek(day)


        // Schreibe Daten als Document in die Collection Messungen in DB;
        // Eine id als Document Name wird automatisch vergeben
        // Implementiere auch onSuccess und onFailure Listender
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenGoal").document(date)
            .set(data)
            .addOnSuccessListener { documentReference ->
                toast(getString(R.string.save))
            }
            .addOnFailureListener { e ->
                toast(getString(R.string.not_save))
            }
    }

    // === loadMinMax === //
    // Einlesen der Daten aus der Datenbank
    private fun loadGoal() {

        var data: DataGoal?

        // --- Initialisierung und Konfiguration des Graphen --- //
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")
        val date = format.format(calendar.time)


        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenGoal")
            .document(date)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    data = task.result!!.toObject(DataGoal::class.java)

                    if (data != null) {
                        goal = data!!.getGoalExercises()
                        // Aktualisierung der Anzeige
                        // https://github.com/lopspower/CircularProgressBar
                        binding.circularProgressBarData.apply {
                            progressMax = goal
                        }

                        binding.textViewExercises.text = getString(R.string.text_view_exercises_done, 0, goal.toInt())
                    } else {
                        binding.textViewExercises.text = getString(R.string.text_view_exercises_done,0 , 0)
                    }

                    // --- Initialisierung und Konfiguration des Graphen --- //
                    val calendar = Calendar.getInstance()
                    val format = SimpleDateFormat("dd.MM.yyyy")
                    dateMonday = format.format(calendar.time)
                    dateThuesday = format.format(calendar.time)

                    val day = calendar.get(Calendar.DAY_OF_WEEK) - 1


                    calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))


                    for(i in 0 until 7) {

                        //calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))


                        val date1 = format.format(calendar.time)

                        arrayDays[i] = date1

                        calendar.add(Calendar.DAY_OF_WEEK, + 1)

                    }

                    setUpAAChartView()

                    loadDbData()
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }

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
            .setStacking(AAChartStackingType.False)
            .setTitleStyle(AAStyle.Companion.style("#3C002C"))
            .setBackgroundColor(R.color.background)
            .setAxesTextColor("#291521")
            .setCategories(arrayDays[0], arrayDays[1], arrayDays[2],arrayDays[3],arrayDays[4],arrayDays[5],arrayDays[6])
            .setYAxisMax(30f)
            .setYAxisTitle(R.string.chart_title_y)
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
                .name(getString(R.string.chart_title))
                .data(arrayWeekDays as Array<Any>),
            AASeriesElement()
                .name(getString(R.string.chart_title_goals))
                .data(arrayWeekGoal as Array<Any>),
        )
    }

    // === loadDbData === //
    // Einlesen der Daten aus der Datenbank
    private fun loadDbData() {

        dbList.clear()

        monday = 0; thuesday = 0; wednesday = 0; thursday = 0; friday = 0; saturday = 0; sunday = 0;

        for (i in 0 until 7) {
            arrayWeekDays[i] = 0
        }

        //val seriesArr = configureChartSeriesArray()
     //   binding.chartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(seriesArr)

        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")

        val day = calendar.get(Calendar.DAY_OF_WEEK) - 1

        calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))

        dateMonday = format.format(calendar.time)

        val dateMondayNew = format.parse(dateMonday) as Date

        //calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))


        for(i in 0 until 7) {

            val date = format.format(calendar.time)

            arrayDays[i] = date

            calendar.add(Calendar.DAY_OF_WEEK, + 1)

        }

        setUpAAChartView()

        calendar.add(Calendar.DAY_OF_WEEK, - 1)

        val dateSunday: Date = calendar.time


        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("Daten")
            .whereGreaterThanOrEqualTo("date", dateMondayNew) // abrufen
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
        val zeitformat = SimpleDateFormat("dd.MMMM yyyy")
        val calendar = Calendar.getInstance()
        val currentDate = zeitformat.parse(zeitformat.format(calendar.time)) as Date
        var dateCompare = zeitformat.parse(zeitformat.format(calendar.time)) as Date
        counterDate = 0


        if(task.result!!.size() > 0 ) {
            date = zeitformat.format(task.result!!.first().toObject(Data::class.java).getDate()!!)
            dateCompare = zeitformat.parse(zeitformat.format(task.result!!.first().toObject(Data::class.java).getDate()!!)) as Date
            dbList.add("$date")
        }

        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task.result!!) {

            if((zeitformat.parse(zeitformat.format(document.toObject(Data::class.java).getDate()!!)) as Date) > dateCompare) {
                dbList.add(zeitformat.format(document.toObject(Data::class.java).getDate()!!))
            }

            // date neu zuweisen
            dateCompare = zeitformat.parse(zeitformat.format(document.toObject(Data::class.java).getDate()!!)) as Date


            if(document.toObject(Data::class.java).getDate()!! >= currentDate) {
                counterDate++
            }

            (dbList).add(document.toObject(Data::class.java))
             Log.d("Daten", document.id + " => " + document.data)
        }

        // jetzt liegt die vollständige Liste vor und
        // kann im ListView angezeigt werden

        // Adapter für den ListView
        val adapter = ArrayAdapter(requireContext(),
            R.layout.layout_list_view, R.id.textView,  // Layout zur Darstellung der ListItems
            dbList)

        binding.listViewData.adapter = adapter



        binding.textViewExercises.text = getString(R.string.text_view_exercises_done, counterDate, goal.toInt())

        // Aktualisierung der Anzeige
        // https://github.com/lopspower/CircularProgressBar
        binding.circularProgressBarData.progress = counterDate.toFloat()

        dataToGraph(dbList)
    }

    private fun dataToGraph(data: ArrayList<Any>) {



        for(i in data){


            if(i::class.simpleName != "String") {

                when ((i as Data).getDayOfWeek()) {
                    0 -> {
                        sunday++
                    }

                    1 -> {
                        monday++
                    }

                    2 -> {
                        thuesday++
                    }

                    3 -> {
                        wednesday++
                    }

                    4 -> {
                        thursday++
                    }

                    5 -> {
                        friday++
                    }

                    6 -> {
                        saturday++
                    }

                    else -> {}
                }
            }
        }

        arrayWeekDays[0] = sunday
        arrayWeekDays[1] = monday
        arrayWeekDays[2] = thuesday
        arrayWeekDays[3] = wednesday
        arrayWeekDays[4] = thursday
        arrayWeekDays[5] = friday
        arrayWeekDays[6] = saturday

      //  val seriesArr = configureChartSeriesArray()
       // binding.chartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(seriesArr)

        loadDbGoalWeek()
    }

    // === loadDbData === //
    // Einlesen der Daten aus der Datenbank
    private fun loadDbGoalWeek() {

        dbListGoals.clear()

        mondayGoal = 0; thuesdayGoal = 0; wednesdayGoal = 0; thursdayGoal = 0; fridayGoal = 0; saturdayGoal = 0; sundayGoal = 0;

        for (i in 0 until 7) {
            arrayWeekGoal[i] = 0
        }

        //val seriesArr = configureChartSeriesArray()
      //  binding.chartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(seriesArr)

        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")

        val day = calendar.get(Calendar.DAY_OF_WEEK) - 1

        calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))

        dateMonday = format.format(calendar.time)

        val dateMondayNew = format.parse(dateMonday) as Date

        //calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))


        for(i in 0 until 7) {

            val date = format.format(calendar.time)

            arrayDays[i] = date

            calendar.add(Calendar.DAY_OF_WEEK, + 1)

        }

        setUpAAChartView()

        calendar.add(Calendar.DAY_OF_WEEK, - 1)

        val dateSunday: Date = calendar.time



        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenGoal")
            .whereGreaterThanOrEqualTo("date", dateMondayNew) // abrufen
            .whereLessThanOrEqualTo("date", dateSunday)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    updateGraph(task)
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }

    }

    private fun updateGraph(task: Task<QuerySnapshot>) {

        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task.result!!) {

            (dbListGoals).add(document.toObject(DataGoal::class.java))
            Log.d("Daten", document.id + " => " + document.data)
        }

        for(i in dbListGoals){


            if(i::class.simpleName != "String") {

                when ((i as DataGoal).getDayOfWeek()) {
                    0 -> {
                        sundayGoal = i.getGoalExercises().toInt()
                    }

                    1 -> {
                        mondayGoal = i.getGoalExercises().toInt()
                    }

                    2 -> {
                        thuesdayGoal = i.getGoalExercises().toInt()
                    }

                    3 -> {
                        wednesdayGoal = i.getGoalExercises().toInt()
                    }

                    4 -> {
                        thursdayGoal = i.getGoalExercises().toInt()
                    }

                    5 -> {
                        fridayGoal = i.getGoalExercises().toInt()
                    }

                    6 -> {
                        saturdayGoal = i.getGoalExercises().toInt()
                    }

                    else -> {}
                }
            }
        }

        arrayWeekGoal[0] = sundayGoal
        arrayWeekGoal[1] = mondayGoal
        arrayWeekGoal[2] = thuesdayGoal
        arrayWeekGoal[3] = wednesdayGoal
        arrayWeekGoal[4] = thursdayGoal
        arrayWeekGoal[5] = fridayGoal
        arrayWeekGoal[6] = saturdayGoal

        val seriesArr = configureChartSeriesArray()
        binding.chartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(seriesArr)
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}