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
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handrehab.Data
import com.example.handrehab.DataGoal
import com.example.handrehab.LoginInActivity
import com.example.handrehab.R
import com.example.handrehab.RecyclerAdapter
import com.example.handrehab.databinding.FragmentMainBinding
import com.example.handrehab.item.Datasource
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import splitties.toast.toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    // === Datenbank === //
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var dateMonday: String
    private var data: Data? = null
    private var dbList = ArrayList <Data> ()

    // === ListView === //
    private lateinit var layoutManager : LinearLayoutManager
    private lateinit var adapter : RecyclerAdapter

    // === Circular-Progress-Bar === //
    private var timeMaxProgressBar = 20F
    private var progressTime: Float = 0F
    private var goal = 0f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.recyclerView.layoutManager = layoutManager

        val myDataset = Datasource().loadItems()


        adapter = RecyclerAdapter(myDataset, "Main")
        binding.recyclerView.adapter = adapter

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


        binding.buttonLogOut.setOnClickListener {
            mFirebaseAuth.signOut()
            val intent = Intent(activity, LoginInActivity::class.java)
            activity?.startActivity(intent)
        }

        // --- Konfiguration CircularProgressBar --- //
        // Anzeige des Fortschritts mit gerader Haltung
        // https://github.com/lopspower/CircularProgressBar
        binding.circularProgressBar.apply {
            // Progress Max
            progressMax = goal

            // ProgressBar Farbe
            progressBarColorStart = Color.parseColor("#924275")
            progressBarColorEnd = Color.parseColor("#3C002C")

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


        loadDbData()

    }

    // === onCreateOptionsMenu === //
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main_2, menu)

    }

    // === onOptionsItemSelected === //
    // Hier werden Klicks auf Elemente der Aktionsleiste behandelt
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

    // === loadDbData === //
    // Einlesen der Daten aus der Datenbank
    private fun loadDbData() {

        dbList.clear()

        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd")

        val day = calendar.get(Calendar.DAY_OF_WEEK) - 2

        calendar.add(Calendar.DAY_OF_WEEK, - day)

        dateMonday = format.format(calendar.time)

        val dateMondayNew = format.parse(dateMonday) as Date

        calendar.add(Calendar.DAY_OF_WEEK, + 6)

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


    // === loadDbData === //
    // Einlesen der Daten aus der Datenbank
    private fun loadDbGoalData() {

        var data: DataGoal?

        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DatenGoal")
            .document("Ziel")
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


        // Aktualisierung der Anzeige
        // https://github.com/lopspower/CircularProgressBar
        binding.circularProgressBar.progress = dbList.size.toFloat()

        loadDbGoalData()

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}