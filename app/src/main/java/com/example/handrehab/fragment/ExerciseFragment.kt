package com.example.handrehab.fragment

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.navigation.fragment.findNavController
import autovalue.shaded.com.`google$`.common.reflect.`$Reflection`.getPackageName
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentExerciseBinding


class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    // === MediaController === //
    private var mediaController: MediaController? = null

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
            findNavController().navigate(R.id.action_exerciseFragment_to_permissionsFragment)
        }

        // --- Initialisierung MediaController --- //
        if (mediaController == null) {
            mediaController = MediaController(activity)
            mediaController!!.setAnchorView(binding.videoViewExercise)
        }
        binding.videoViewExercise.setMediaController(mediaController)


        //  Uri-Adresse einlesen
        val uri: Uri = Uri.parse("android.resource://" + activity?.packageName  + "/" +  R.raw.hand_video_test)

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