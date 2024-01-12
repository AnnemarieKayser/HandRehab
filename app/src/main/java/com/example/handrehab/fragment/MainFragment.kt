package com.example.handrehab.fragment

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handrehab.Data
import com.example.handrehab.DataGoal
import com.example.handrehab.DataWeekPlanner
import com.example.handrehab.LoginInActivity
import com.example.handrehab.MainViewModel
import com.example.handrehab.R
import com.example.handrehab.RecyclerAdapter
import com.example.handrehab.databinding.FragmentMainBinding
import com.example.handrehab.item.Datasource
import com.example.handrehab.item.Exercises
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainFragment : Fragment() {


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

      - In diesem Fragment wird eine erste Übersicht über die App gegeben
      - Es kann über verschiedene Button in die anderen Fragmente gewechselt werden
      - Es wird in einer CircularProgressBar die Anzahl an durchgeführten
      Übungen an dem aktuellen Tag angezeigt
      - Es werden die Übungen des aktuellen Tags aus dem Wochenplan angezeigt
      - Der Nutzer kann sich ausloggen

    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */


    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!


    // Datenbank
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var date: String
    private var dbList = ArrayList <Data> ()

    // ListView
    private lateinit var layoutManager : LinearLayoutManager
    private lateinit var adapter : RecyclerAdapter
    private var listExercises = arrayListOf<Exercises>()

    // Circular-Progress-Bar
    private var goal = 0f

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LayoutManger an Liste binden
        layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.recyclerView.layoutManager = layoutManager

        // Anzeige aller Übungen im ExerciseListFragment
        viewModel.setExercisesListMode(1)

        setHasOptionsMenu(true)


        binding.buttonGoToData.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_dataFragment)
        }

        binding.buttonToExerciseList.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_exerciseListFragment)
        }

        binding.buttonWeekplan.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_plannerFragment)
        }

        binding.buttonStartPlan.setOnClickListener {
            // Anzeige der Übungen des aktuellen Tages aus dem Wochenplan im ExerciseListFragment
            viewModel.setExercisesListMode(2)
            findNavController().navigate(R.id.action_MainFragment_to_exerciseListFragment)
        }

        // Konfiguration CircularProgressBar
        // Anzeige des Fortschritts an durchgeführten Übungen
        // https://github.com/lopspower/CircularProgressBar
        binding.circularProgressBar.apply {
            // Progress Max
            progressMax = goal

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

        binding.circularProgressBar.progress = 0f

        loadDbData()
    }

    /*
   =============================================================
   =======                   Funktionen                  =======
   =============================================================
   */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main_2, menu)
    }


    // Hier werden Klicks auf Elemente der Aktionsleiste behandelt
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Ausloggen
        return when (item.itemId) {
            R.id.menu_item_log_out-> {
                mFirebaseAuth.signOut()
                val intent = Intent(activity, LoginInActivity::class.java)
                activity?.startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    // Einlesen der durchgeführten Übungen an dem aktuellen Tag aus der Datenbank
    private fun loadDbData() {

        dbList.clear()

        // Abrufen des aktuellen Datums
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy")

        date = format.format(calendar.time)
        val dateNew = format.parse(date) as Date

        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("Daten")
            .whereGreaterThanOrEqualTo("date", dateNew) // abrufen
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    updateView(task)
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }

    }

    private fun updateView(task: Task<QuerySnapshot>) {

        // Diese for-Schleife durchläuft alle Dokumente der Abfrage
        for (document in task.result!!) {
            dbList.add(document.toObject(Data::class.java))
        }

        // Aktualisierung der Anzeige
        // https://github.com/lopspower/CircularProgressBar
        binding.circularProgressBar.progress = dbList.size.toFloat()

        loadDbGoalData()
    }



    // Einlesen des aktuellen Ziels an Übungen pro Tag
    private fun loadDbGoalData() {

        var data: DataGoal?

        // Abfrage des aktuellen Datums
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
                        binding.circularProgressBar.apply {
                            progressMax = goal
                        }

                        binding.textViewNumber.text = getString(R.string.text_view_goal_main, dbList.size, goal.toInt())
                    } else {
                        binding.textViewNumber.text = getString(R.string.text_view_goal_main, dbList.size, 0)
                    }

                    loadDbWeekPlan()
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }
    }


    // Einlesen des aktuellen Tages aus dem Wochenplan
    private fun loadDbWeekPlan() {

        listExercises.clear()

        // Abfrage des aktuellen Datums
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("EEEE", Locale.ENGLISH)

        val day = format.format(calendar.time).lowercase()

        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DataWeekPlanner").document(day)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    if(task.result != null) updateListViewWeekPlanner(task)
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }
    }

    // Anzeige der Übungen des aktuellen Tages aus dem Wochenplan in einer Liste
    private fun updateListViewWeekPlanner(task: Task<DocumentSnapshot>) {

        val result = task.result!!.toObject(DataWeekPlanner::class.java)

        // Übungen zu Liste hinzufügen
        if(result != null) {

            for (j in Datasource().loadItems()) {

                for (k in result.getListExercises()) {
                    if (k == j.id) {
                        listExercises.add(j)
                    }
                }
            }
        }

        if(listExercises.isEmpty()){
            binding.textViewNoData.text = getString(R.string.no_exercises_selected)
        } else binding.textViewNoData.visibility = GONE

        viewModel.setListDay(listExercises)

        adapter = RecyclerAdapter(listExercises, "Main",  viewModel.getExerciseListMode()!!)
        binding.recyclerView.adapter = adapter
    }



        override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}