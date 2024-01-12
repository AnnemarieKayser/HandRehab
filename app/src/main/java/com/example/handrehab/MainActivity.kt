

package com.example.handrehab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.View
import androidx.navigation.ui.setupWithNavController
import com.example.handrehab.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {


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

      - Initialisierung der unteren Navigationsleiste


    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // --- Layout --- //
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Initialisierung Toolbar --- //
        setSupportActionBar(binding.toolbar)

        // --- Initialisierung bottomNavigationView --- //
        val navView: BottomNavigationView = binding.navView

        // --- Initialisierung navController --- //
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.MainFragment, R.id.dataFragment, R.id.exerciseListFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    /*
  =============================================================
  =======                                               =======
  =======                   Funktionen                  =======
  =======                                               =======
  =============================================================
*/


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navView: BottomNavigationView = binding.navView
        navView.visibility = View.VISIBLE
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}