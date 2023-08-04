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


    // === Datenbank === //
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // === Handler-Runnable-Konstrukt === //
    private val mHandler: Handler by lazy { Handler() }
    private lateinit var mRunnable: Runnable


/*
  =============================================================
  =======                                               =======
  =======                   onCreate                    =======
  =======                                               =======
  =============================================================
*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        // --- Prüfen, ob eine Verbindung zum WLAN besteht --- //
        // Wenn keine Verbindung besteht, wird ein alertDialog angezeigt und erinnert
        // den Benutzer daran, sein Handy mit dem Wifi zu verbinden
        // danach wird die App geschlossen
        if(!isNetworkAvailable(this)){
            // --- alertDialog --- //
            alertDialog (
                title = getString(R.string.alert_dialog_title_reminder),
                message = getString(R.string.alert_dialog_message_wifi)) {
                okButton{
                    finish()
                }
            }.show()
        }
        else {
            // LoginActivity oder MainActivity wird nach 2 Sekunden geöffnet, je nachdem
            // ob der User eingeloggt ist oder nicht
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

    // === isNetworkAvailable === //
    // diese Funktion prüft, ob eine Verbindung zum WLAN besteht
    // gibt true zurück, wenn eine Verbindung vorhanden ist
    // sonst gibt sie false zurück
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