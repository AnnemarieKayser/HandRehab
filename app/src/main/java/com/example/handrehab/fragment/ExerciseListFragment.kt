package com.example.handrehab.fragment

import android.os.Bundle
import android.util.Log
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

    private var _binding: FragmentExerciseListBinding? = null
    private val binding get() = _binding!!
    private lateinit var layoutManager : LinearLayoutManager
    private lateinit var adapter : RecyclerAdapter


    // === Viewmodel === //
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

        // This callback will only be called when MyFragment is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            val view = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            view.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_exerciseListFragment_to_MainFragment)
        }

        val view = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)



        layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager

        val myDataset = Datasource().loadItems()
        if(viewModel.getListDay()!!.isEmpty()) viewModel.setExercisesListMode(1)

        if(viewModel.getExerciseListMode() == 2){
            (activity as AppCompatActivity).supportActionBar?.title = if(viewModel.getListDay()!!.isEmpty()) getString(R.string.title_done) else getString(R.string.title_counter_exercises, viewModel.getListDay()!!.size)
            adapter = RecyclerAdapter(viewModel.getListDay()!!, "Exercise", viewModel.getExerciseListMode()!!)
            binding.recyclerView.adapter = adapter
            view.visibility = View.INVISIBLE

        } else {
            view.visibility = View.VISIBLE
            adapter = RecyclerAdapter(myDataset, "Exercise", viewModel.getExerciseListMode()!!)
            binding.recyclerView.adapter = adapter
        }

        // Applying OnClickListener to our Adapter
        adapter.setOnClickListener(object : RecyclerAdapter.AdapterItemClickListener {
            override fun onItemClickListener(exercises: Exercises, position: Int) {
                if(exercises.id == 1 || exercises.id == 7 || exercises.id == 14 || exercises.id == 19) {
                    //do nothing
                } else {
                        viewModel.setSelectedExercise(exercises)
                        val id = viewModel.getSelectedExercise()!!.id
                        Log.i("Selected Exercise", exercises.toString())
                        if (id != 2 && id != 3 && id != 4 && id != 5 && id != 6) {
                            viewModel.setStartModus("geschlossen")
                        }
                        findNavController().navigate(R.id.action_exerciseListFragment_to_exerciseFragment)
                    }
            }
        })
    }



    fun onItemClickListener(exercises: Exercises, position: Int) {
        //update or another job
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}