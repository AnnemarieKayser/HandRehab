package com.example.handrehab

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import splitties.alertdialog.alertDialog
import splitties.alertdialog.okButton


@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {


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

      - Diese Activity wird bei Start der App aufgerufen und zeigt einen Begrüßungsbildschirm
      für zwei Sekunden an.
      - Es wird überprüft, ob eine Verbindung zum W-Lan besteht und ob ein Nuter eingeloggt ist

    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */


    // Datenbank
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Handler-Runnable-Konstrukt
    private val mHandler: Handler by lazy { Handler() }
    private lateinit var mRunnable: Runnable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Prüfen, ob eine Verbindung zum WLAN besteht
        // Wenn keine Verbindung besteht, wird ein alertDialog mit einer Erinnerung
        // angezeigt, sich damit zu verbinden
        // Danach wird die App geschlossen
        if(!isNetworkAvailable(this)){

            alertDialog (
                title = getString(R.string.alert_dialog_title_reminder),
                message = getString(R.string.alert_dialog_message_wifi)) {
                okButton{
                    finish()
                }
            }.show()
        }
        else {
            // Wenn der User eingeloggt ist, öffnet sich die MainActivity nach 2 Sekunden
            // Wenn nicht, öffnet sich die LogInActivity
            mRunnable = Runnable {
                if (mFirebaseAuth.currentUser == null) {
                    val intent = Intent (this, LoginInActivity::class.java)
                    startActivity(intent)
                }
                else {
                    val intent = Intent (this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            mHandler.postDelayed(mRunnable, 2000)
        }
    }

/*
  =============================================================
  =======                                               =======
  =======                   Funktionen                  =======
  =======                                               =======
  =============================================================
*/


    // Diese Funktion überprüft, ob eine Verbindung zum WLAN besteht
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

}