package com.example.handrehab.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.handrehab.R
import com.example.handrehab.databinding.SigninTabFragmentBinding
import com.google.firebase.auth.FirebaseAuth

class SigninTabFragment: Fragment() {

/*
  =============================================================
  =======                   Variablen                   =======
  =============================================================
*/

    private var _binding: SigninTabFragmentBinding? = null
    private val binding get() = _binding!!

    // === Datenbank === //
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener


/*
  =============================================================
  =======                                               =======
  =======         onCreateView & onViewCreated          =======
  =======                                               =======
  =============================================================
*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SigninTabFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // --- E-mail wird an Benutzer geschickt zur Verifizierung seiner Mail --- //
        mAuthListener = FirebaseAuth.AuthStateListener {
            val user = mFirebaseAuth.currentUser

            if (user != null) {
                user.sendEmailVerification()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            FirebaseAuth.getInstance().signOut()
                            //toast(R.string.verify_mail)
                        } else {
                            //toast(it.exception!!.message.toString())
                        }
                    }
            }
        }

        // --- E-Mail und Passwort werden eingelesen und an die register-Funktion übergeben --- //
        binding.buttonRegister.setOnClickListener {

            val email : String = binding.EditViewEmail.text.toString()
            val password : String = binding.EditViewPassword.text.toString()
            register(email, password)
        }

    }

/*
  =============================================================
  =======                                               =======
  =======                   Funktionen                  =======
  =======                                               =======
  =============================================================
*/

    // === validateForm === //
    // Überprüfen, ob E-Mail und Passwort eingegeben wurden
    private fun validateForm(email: String, password: String): Boolean {
        var valid = true

        if (email.isEmpty()) {
            binding.EditViewEmail.error = getString(R.string.required)
            valid = false
        }

        if (password.isEmpty()) {
            binding.EditViewPassword.error = getString(R.string.required)
            valid = false
        }

        return valid
    }

    // === register === //
    private fun register(email: String, password: String) {

        // Überprüfen, ob E-mail und Passwort eingegeben wurden
        if (!validateForm(email, password)) {
            return
        }

        // User wird neu angelegt in der Datenbank
        activity?.let {
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        mAuthListener.onAuthStateChanged(mFirebaseAuth)
                    } else {
                        //toast(task.exception!!.message.toString())
                    }
                }
        }
    }

}