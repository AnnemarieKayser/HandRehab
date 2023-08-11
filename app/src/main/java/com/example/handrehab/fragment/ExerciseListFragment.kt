package com.example.handrehab.fragment


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handrehab.R
import com.example.handrehab.RecyclerAdapter
import com.example.handrehab.databinding.FragmentExerciseListBinding
import com.example.handrehab.item.Datasource
import com.example.handrehab.item.Exercises


class ExerciseListFragment : Fragment() {

    private var _binding: FragmentExerciseListBinding? = null
    private val binding get() = _binding!!
    private lateinit var layoutManager : LinearLayoutManager
    private lateinit var adapter : RecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentExerciseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager

        val myDataset = Datasource().loadItems()


        val myList = arrayListOf<String>("1", "2", "3", "4")
        adapter = RecyclerAdapter(myDataset)
        binding.recyclerView.adapter = adapter


        // Applying OnClickListener to our Adapter
        adapter.setOnClickListener(object : RecyclerAdapter.AdapterItemClickListener {
            override fun onItemClickListener(exercises: Exercises, position: Int) {
                findNavController().navigate(R.id.action_exerciseListFragment_to_exerciseFragment)
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