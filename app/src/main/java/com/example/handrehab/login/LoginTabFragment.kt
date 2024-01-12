package com.example.handrehab.login
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.handrehab.MainActivity
import com.example.handrehab.R
import com.example.handrehab.databinding.LoginTabFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import splitties.toast.toast

class LoginTabFragment: Fragment() {


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

      - Der Nutzer kann sich mit einer E-Mail und einem Passwort einloggen
      - Der Nutzer kann sein Passwort zurücksetzen

    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */


    private var _binding: LoginTabFragmentBinding? = null
    private val binding get() = _binding!!

    // Datenbank
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LoginTabFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // E-Mail und Passwort werden eingelesen und an die signIn-Funktion übergeben
        binding.buttonLogin.setOnClickListener {

            val email : String = binding.email.text.toString()
            val password : String = binding.password.text.toString()
            signIn(email, password)
        }

        binding.textViewPasswortVergessen.setOnClickListener {
            sendResetPw()
        }
    }

/*
  =============================================================
  =======                                               =======
  =======                   Funktionen                  =======
  =======                                               =======
  =============================================================
*/


    // Überprüfen, ob E-Mail und Passwort eingegeben wurden
    private fun validateForm(email: String, password: String): Boolean {
        var valid = true

        if (email.isEmpty()) {
            binding.email.error = getString(R.string.required)
            valid = false
        }

        if (password.isEmpty()) {
            binding.password.error = getString(R.string.required)
            valid = false
        }

        return valid
    }


    private fun signIn(email: String, password: String) {

        // Überprüfen, ob E-mail und Passwort eingegeben wurden
        if (!validateForm(email, password)) {
            return
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // User wird eingeloggt, wenn E-Mail bestätigt wurde
                    if (mFirebaseAuth.currentUser!!.isEmailVerified) {
                         toast(R.string.login_success)
                        // Starten der MainActivity
                        val intent = Intent (activity, MainActivity::class.java)
                        activity?.startActivity(intent)
                    } else {
                        toast(R.string.reminder_verify)
                    }
                } else {
                    toast(it.exception!!.message.toString())
                }
            }
    }


    private fun sendResetPw() {

        // AlertDialog mit Eingabefeld
        val editTextView = EditText(activity)
        editTextView.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.forgotPw_title))
                .setView(editTextView)
                .setMessage(getString(R.string.forgotPw_msg))

                .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                    val mail = editTextView.text.toString().trim()
                    if (mail.isEmpty()) {
                        toast(R.string.fill_out)
                    } else {
                        sendMail(mail)
                    }
                }
                .setNegativeButton(getString(R.string.button_cancel)) { dialog, which ->
                }
                .show()
        }
    }


    // Versenden einer Mail, um das Passwort zurückzusetzen
    private fun sendMail(mail: String) {
        mFirebaseAuth.sendPasswordResetEmail(mail)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    toast(R.string.reset_pw)
                } else {
                    toast(it.exception!!.message.toString())
                }
            }
    }

}