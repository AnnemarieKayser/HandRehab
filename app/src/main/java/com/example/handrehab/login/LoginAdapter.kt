package com.example.handrehab.login

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class MyAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {


    // Anzeige des ausgewählten Tabs
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                SigninTabFragment()
            }

            1 -> {
                LoginTabFragment()
            }

            else -> LoginTabFragment()
        }
    }

    // Dies zählt die Gesamtzahl der Tabs
    override fun getCount(): Int {
        return totalTabs
    }
}