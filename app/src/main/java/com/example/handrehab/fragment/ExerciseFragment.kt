package com.example.handrehab.fragment

import android.graphics.Color
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
import com.example.handrehab.Data
import com.example.handrehab.MainViewModel
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentExerciseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import splitties.toast.toast



class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    // === MediaController === //
    private var mediaController: MediaController? = null

    // === Viewmodel === //
    private val viewModel: MainViewModel by activityViewModels()

    // === Firebase database === //
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance()  }
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var counterExercise = 0


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
            counterExercise++
           // insertDataInDb(counterExercise)
            if(viewModel.getRepetitions() == 0 || viewModel.getSets() == 0){
                toast("Trage bitte eine Anzahl ein")
                binding.editTextRepetition.setHintTextColor(Color.RED)
            } else findNavController().navigate(R.id.action_exerciseFragment_to_permissionsFragment)

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

    private fun insertDataInDb(counter: Int) {

        // Weather Objekt mit Daten befüllen (ID wird automatisch ergänzt)
        val data = Data()
        data.setCounterExercises(counter)

        // Wandle String date (im Format yyyy-MM-dd !!!) um in ein Date Objekt
        /*val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        var datetimestamp: Date? = null
        try {
            datetimestamp = dateFormat.parse("")
        } catch (e: ParseException) {
            e.printStackTrace()
        }*/
        //data.setDateTimestamp(datetimestamp)

        // Schreibe Daten als Document in die Collection Messungen in DB;
        // Eine id als Document Name wird automatisch vergeben
        // Implementiere auch onSuccess und onFailure Listender
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("Daten")
            .add(data)
            .addOnSuccessListener { documentReference ->
            }
            .addOnFailureListener { e ->
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}