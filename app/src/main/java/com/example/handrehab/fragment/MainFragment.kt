package com.example.handrehab.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.handrehab.LoginInActivity
import com.example.handrehab.R
import com.example.handrehab.databinding.FragmentMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGoToData.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_dataFragment)
        }

        binding.buttonGoToSettings.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_settingFragment)
        }

        binding.buttonToExerciseList.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_exerciseListFragment)
        }



        binding.buttonLogOut.setOnClickListener {
            mFirebaseAuth.signOut()
            val intent = Intent(activity, LoginInActivity::class.java)
            activity?.startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}