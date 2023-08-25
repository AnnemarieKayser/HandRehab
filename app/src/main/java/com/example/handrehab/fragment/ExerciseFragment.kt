package com.example.handrehab.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.handrehab.MainViewModel
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentExerciseBinding


class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    // === MediaController === //
    private var mediaController: MediaController? = null

    // === Viewmodel === //
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStartExercise.setOnClickListener {
            binding.editTextRepetition.text.toString().toIntOrNull()
                ?.let { it1 -> viewModel.setRepetitions(it1) }
            binding.editTextSets.text.toString().toIntOrNull()
                ?.let { it1 -> viewModel.setSets(it1) }
            Log.i("Repetition", viewModel.getRepetitions().toString())
            findNavController().navigate(R.id.action_exerciseFragment_to_permissionsFragment)

        }

        binding.textViewTitle.text = viewModel.getSelectedExercise()?.textItem

        // --- Initialisierung MediaController --- //
        if (mediaController == null) {
            mediaController = MediaController(activity)
            mediaController!!.setAnchorView(binding.videoViewExercise)
        }
        binding.videoViewExercise.setMediaController(mediaController)
        binding.textViewDescription.text = viewModel.getSelectedExercise()?.descriptionItem?.let { getString(it) }

        //  Uri-Adresse einlesen
        val uri: Uri = Uri.parse("android.resource://" + activity?.packageName  + "/" + viewModel.getSelectedExercise()?.videoItem)

        binding.videoViewExercise.setVideoURI(uri)

        // Video starten
        binding.videoViewExercise.requestFocus()
        binding.videoViewExercise.seekTo(1)
        //binding.videoViewExercise.start()



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}