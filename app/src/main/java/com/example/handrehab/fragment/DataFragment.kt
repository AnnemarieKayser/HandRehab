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

      - In diesem Fragment werden die Ergebnisse der durchgeführten Übungen dargestellt
      - In einem Graphen wird die Anzahl an absolvierten Übungen für jeden Tag angezeigt
      - In dem Graphen wird ebenfalls das festgelegte Tagesziel an Übungen angezeigt
      - In einer Liste werden die Details zu den Übungen dargestellt
      - Es kann ein Ziel an Übungen eingestellt werden, dass der Nutzer täglich erreichen möchte


    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */

    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!

    // Datum
    private var date = ""

    // Firebase Datenbank
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var dbList = ArrayList <Any> ()
    private var dbListGoals = ArrayList <Any> ()

    // Graph
    private var aaChartModel = AAChartModel()
    private var arrayWeekDays = arrayOfNulls<Any>(7)
    private var arrayWeekGoal = arrayOfNulls<Any>(7)
    // Start der Wochenanzeige an einem Sonntag im Graphen
    private lateinit var dateSunday: String
    private var arrayDays = arrayOf("", "", "", "", "", "", "")

    // Variablen für Graphen
    // Variablen für die durchgeführten Übungen für jeden Wochentag
    private var monday = 0
    private var thuesday = 0
    private var wednesday = 0
    private var thursday = 0
    private var friday = 0
    private var saturday = 0
    private var sunday = 0

    // Variablen für das festgelegte Ziel an Übungen für jeden Wochentag
    private var mondayGoal = 0
    private var tuesdayGoal = 0
    private var wednesdayGoal = 0
    private var thursdayGoal = 0
    private var fridayGoal = 0
    private var saturdayGoal = 0
    private var sundayGoal = 0

    // Variablen für das Ändern der angezeigten Woche
    private var counterWeeksSunday = 0
    private var counterWeeksMonday = 0

    // Circular-Progress-Bar
    private var maxProgressBar = 0F
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


        // Wechsel zwischen den Wochen im Graphen
        binding.imageButtonBefore.setOnClickListener {
            counterWeeksMonday+= 7
            counterWeeksSunday += 1
            // Daten zu der jeweiligen Woche laden
            loadDbData()
        }

        binding.imageButtonAfter.setOnClickListener {
            if(counterWeeksSunday != 0 && counterWeeksMonday != 0) {
                counterWeeksMonday -= 7
                counterWeeksSunday -= 1
                // Daten zu der jeweiligen Woche laden
                loadDbData()
            } else {
                toast(getString(R.string.currentDate))
            }
        }

        // Speichern der Eingabe für ein tägliches Ziel
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

        // Konfiguration der CircularProgressBar
        // Anzeige des Fortschritts der durchgeführten Übungen
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

        // Aktuelle Zielanzahl aus der Datenbank laden
        loadGoal()
    }

    /*
     =============================================================
     =======                   Funktionen                  =======
     =============================================================
   */


    // Speichern des festgelegten Ziels an Übungen pro Tag
    private fun saveGoal() {

        //DataGoal Objekt mit Daten befüllen (ID wird automatisch ergänzt)
        val data = DataGoal()

        data.setGoalExercises(goal)

        // Einlesen des aktuellen Datums
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")
        val date = format.format(calendar.time)
        // Anpassen des Bereich der Tage (1-7) auf den Bereich des Graphen (0-6)
        val day = calendar.get(Calendar.DAY_OF_WEEK) - 1

        data.setDate(format.parse(date) as Date)
        data.setDayOfWeek(day)


        // Eine id als Document Name wird automatisch vergeben
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


    // Einlesen des festgelegten Ziels an dem aktuellen Tag aus der Datenbank
    private fun loadGoal() {

        var data: DataGoal?

        // Einlesen des aktuellen Datums
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")
        val date = format.format(calendar.time)

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



                    // Nachdem das Ziel geladen wurde, sollen anschließend die Daten
                    // zu den durchgeführten Übungen geladen werden für die aktuelle Woche
                    loadDbData()

                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }

    }


    // https://github.com/AAChartModel/AAChartCore-Kotlin
    private fun setUpAAChartView() {
        aaChartModel = configureAAChartModel()
        binding.chartView.aa_drawChartWithChartModel(aaChartModel)
    }


    // https://github.com/AAChartModel/AAChartCore-Kotlin
    private fun configureAAChartModel(): AAChartModel {
        val aaChartModel = configureChartBasicContent()
        aaChartModel.series(this.configureChartSeriesArray() as Array<Any>)
        return aaChartModel
    }

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

    // Einlesen der Daten aus der Datenbank
    private fun loadDbData() {

        // Daten zurücksetzen, für den Fall, wenn die Woche geändert wird
        dbList.clear()

        monday = 0; thuesday = 0; wednesday = 0; thursday = 0; friday = 0; saturday = 0; sunday = 0;

        for (i in 0 until 7) {
            arrayWeekDays[i] = 0
        }

        // Abrufen des aktuellen Datums
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")

        // Anpassen des Bereich der Tage (1-7) auf den Bereich des Graphen (0-6)
        val day = calendar.get(Calendar.DAY_OF_WEEK) - 1

        // Anpassen des Kalenders auf die ausgewählte Woche
        calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))

        // Datum am Anfang der ausgewählten Woche
        dateSunday = format.format(calendar.time)
        val dateSundayNew = format.parse(dateSunday) as Date

        // Aktuelle Daten der Woche zu Array für Anzeige im Graphen hinzufügen
        for(i in 0 until 7) {

            val date = format.format(calendar.time)

            arrayDays[i] = date

            calendar.add(Calendar.DAY_OF_WEEK, + 1)
        }

        // Aktualisierung der Konfiguration des Graphen
        setUpAAChartView()

        calendar.add(Calendar.DAY_OF_WEEK, - 1)

        // Datum am Ende der ausgewählten Woche
        val dateSaturday: Date = calendar.time


        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("Daten")
            .whereGreaterThanOrEqualTo("date", dateSundayNew) // abrufen
            .whereLessThanOrEqualTo("date", dateSaturday)
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

    // Auslesen der Daten aus der Datenbank
    // Aktualisierung der Liste
    // Aktualisierung der CircularProgressBar
    private fun updateListView(task: Task<QuerySnapshot>) {

        // Aktuelles Datum
        val zeitformat = SimpleDateFormat("dd.MMMM yyyy")
        val calendar = Calendar.getInstance()
        val currentDate = zeitformat.parse(zeitformat.format(calendar.time)) as Date
        var dateCompare = zeitformat.parse(zeitformat.format(calendar.time)) as Date
        counterDate = 0

        // Datum zu Liste hinzufügen
        if(task.result!!.size() > 0 ) {
            date = zeitformat.format(task.result!!.first().toObject(Data::class.java).getDate()!!)
            dateCompare = zeitformat.parse(zeitformat.format(task.result!!.first().toObject(Data::class.java).getDate()!!)) as Date
            dbList.add("$date")
        }

        // Diese for Schleife durchläuft alle Dokumente der Abfrage
        // Einträge werden in dbList geschrieben, um sie im ListView anzuzeigen
        // Datum wird zu der Liste hinzugefügt und anschließend die Daten zu den
        // durchgeführten Übungen an diesem Tag
        for (document in task.result!!) {

            // Datum zu Liste hinzufügen, wenn noch nicht vorhanden
            if((zeitformat.parse(zeitformat.format(document.toObject(Data::class.java).getDate()!!)) as Date) > dateCompare) {
                dbList.add(zeitformat.format(document.toObject(Data::class.java).getDate()!!))
            }

            // Datum neu zuweisen
            dateCompare = zeitformat.parse(zeitformat.format(document.toObject(Data::class.java).getDate()!!)) as Date


            // Anzahl der Übungen an dem aktuellen Tag zählen für die Anzeige in der CircularProgressBar
            if(document.toObject(Data::class.java).getDate()!! >= currentDate) {
                counterDate++
            }

            (dbList).add(document.toObject(Data::class.java))
        }


        // Adapter für den ListView
        val adapter = ArrayAdapter(requireContext(), R.layout.layout_list_view, R.id.textView, dbList)
        // Aktualisierung der Anzeige
        binding.listViewData.adapter = adapter

        binding.textViewExercises.text = getString(R.string.text_view_exercises_done, counterDate, goal.toInt())

        // Aktualisierung der Anzeige
        // https://github.com/lopspower/CircularProgressBar
        binding.circularProgressBarData.progress = counterDate.toFloat()

        dataToGraph(dbList)
    }

    // Anzeige der durchgeführten Übungen im Graphen
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


        // Laden der festgelegten Ziele an Übungen für jeden Tag der ausgewählten Woche
        loadDbGoalWeek()
    }


    // Einlesen der festgelegten Ziele an Übungen an jedem Tag
    // der ausgewählten Woche aus der Datenbank
    private fun loadDbGoalWeek() {

        // Daten zurücksetzen, für den Fall, wenn die Woche geändert wird
        dbListGoals.clear()

        mondayGoal = 0; tuesdayGoal = 0; wednesdayGoal = 0; thursdayGoal = 0; fridayGoal = 0; saturdayGoal = 0; sundayGoal = 0;

        for (i in 0 until 7) {
            arrayWeekGoal[i] = 0
        }

        // Einlesen des aktuellen Datums
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")
        // Anpassen des Bereich der Tage (1-7) auf den Bereich des Graphen (0-6)
        val day = calendar.get(Calendar.DAY_OF_WEEK) - 1

        // Anpassen des Kalenders auf die ausgewählte Woche
        calendar.add(Calendar.DAY_OF_WEEK, - (day + counterWeeksMonday))

        // Datum am Anfang der ausgewählten Woche
        dateSunday = format.format(calendar.time)
        val dateSundayNew = format.parse(dateSunday) as Date


        // Aktuelle Daten der Woche zu Array für Anzeige im Graphen hinzufügen
        for(i in 0 until 7) {
            calendar.add(Calendar.DAY_OF_WEEK, + 1)
        }

        calendar.add(Calendar.DAY_OF_WEEK, - 1)

        // Datum am Ende der ausgewählten Woche
        val dateSaturday: Date = calendar.time


        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenGoal")
            .whereGreaterThanOrEqualTo("date", dateSundayNew) // abrufen
            .whereLessThanOrEqualTo("date", dateSaturday)
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

    // Anzeige der festgelegten Ziele an Übungen im Graphen
    private fun updateGraph(task: Task<QuerySnapshot>) {

        // Diese for-Schleife durchläuft alle Dokumente der Abfrage
        for (document in task.result!!) {
            (dbListGoals).add(document.toObject(DataGoal::class.java))
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
                        tuesdayGoal = i.getGoalExercises().toInt()
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
        arrayWeekGoal[2] = tuesdayGoal
        arrayWeekGoal[3] = wednesdayGoal
        arrayWeekGoal[4] = thursdayGoal
        arrayWeekGoal[5] = fridayGoal
        arrayWeekGoal[6] = saturdayGoal

        // Aktualisierung des Graphen mit den geladenen Werten
        // https://github.com/AAChartModel/AAChartCore-Kotlin
        val seriesArr = configureChartSeriesArray()
        binding.chartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(seriesArr)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}