package com.example.handrehab


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.handrehab.login.MyAdapter
import com.google.android.material.tabs.TabLayout

class LoginInActivity : AppCompatActivity() {

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

      - In dieser Aktivität wird das Tab-Layout initialisiert

    */

    /*
      =============================================================
      =======                   Variablen                   =======
      =============================================================
    */


    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_in)

        // Initialisierung der Layout-Komponenten
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.viewPager)

        tabLayout!!.addTab(tabLayout!!.newTab().setText(R.string.tv_signin))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(R.string.tv_login))
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL

        // Anbindung des Adapters
        val adapter = MyAdapter(this, supportFragmentManager, tabLayout!!.tabCount)
        viewPager!!.adapter = adapter

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        // Anzeige des ausgewählten Tabs
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }
}