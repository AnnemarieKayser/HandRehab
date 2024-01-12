package com.example.handrehab.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handrehab.MainViewModel
import com.example.handrehab.R
import com.example.handrehab.RecyclerAdapter
import com.example.handrehab.databinding.FragmentExerciseListBinding
import com.example.handrehab.item.Datasource
import com.example.handrehab.item.Exercises
import com.google.android.material.bottomnavigation.BottomNavigationView



class ExerciseListFragment : Fragment() {


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

      - In diesem Fragment wird die Liste mit den verschiedenen Übungen angezeigt

    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */


    private var _binding: FragmentExerciseListBinding? = null
    private val binding get() = _binding!!

    // Variablen für die Anzeige der Liste
    private lateinit var layoutManager : LinearLayoutManager
    private lateinit var adapter : RecyclerAdapter

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentExerciseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BackButton überschreiben
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Anzeige der Navigationsleiste
            val view = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            view.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_exerciseListFragment_to_MainFragment)
        }


        // LayoutManger an Liste binden
        layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager

        // Übungen laden
        val myDataset = Datasource().loadItems()

        // Anzeige aller Übungen
        if(viewModel.getListDay()!!.isEmpty()) viewModel.setExercisesListMode(1)

        val view = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        // Anzeige der Übungen des Tagesplans
        if(viewModel.getExerciseListMode() == 2){
            (activity as AppCompatActivity).supportActionBar?.title = if(viewModel.getListDay()!!.isEmpty()) getString(R.string.title_done) else getString(R.string.title_counter_exercises, viewModel.getListDay()!!.size)
            adapter = RecyclerAdapter(viewModel.getListDay()!!, "Exercise", viewModel.getExerciseListMode()!!)
            binding.recyclerView.adapter = adapter
            view.visibility = View.INVISIBLE

        } else {
            // Anzeige aller Übungen
            view.visibility = View.VISIBLE
            adapter = RecyclerAdapter(myDataset, "Exercise", viewModel.getExerciseListMode()!!)
            binding.recyclerView.adapter = adapter
        }

        // Bei Klick auf eine Übung wird in das ExerciseFragment gewechselt
        adapter.setOnClickListener(object : RecyclerAdapter.AdapterItemClickListener {
            override fun onItemClickListener(exercises: Exercises, position: Int) {
                if(exercises.id == 1 || exercises.id == 7 || exercises.id == 14 || exercises.id == 19) {
                    // Elemente in der Liste zur Anzeige der Übungskategorie
                } else {
                        viewModel.setSelectedExercise(exercises)
                        val id = viewModel.getSelectedExercise()!!.id

                        if (id != 2 && id != 3 && id != 4 && id != 5 && id != 6) {
                            viewModel.setStartModus(getString(R.string.closed))
                        }
                        findNavController().navigate(R.id.action_exerciseListFragment_to_exerciseFragment)
                    }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}