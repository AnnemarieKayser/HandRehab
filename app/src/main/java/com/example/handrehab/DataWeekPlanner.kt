package com.example.handrehab

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Objekt für das Speichern der Daten in der Datenbank
// Speichern des Wochenplans
class DataWeekPlanner {

    private lateinit var listExercises : List<Int>
    private var day = "monday"
    private var dateTimestamp: Date? = null

    // serverTimestamp soll automatisch vom Server gesetzt werden
    @ServerTimestamp
    private var serverTimestamp: Timestamp? = null


    fun getDate(): Date? {
        return dateTimestamp
    }

    fun setDate(Date: Date) {
        this.dateTimestamp = Date
    }

    fun getListExercises(): List<Int> {
        return listExercises
    }

    fun setListExercises(list: List<Int>) {
        this.listExercises = list
    }

    fun getDay(): String{
        return day
    }

    fun setDay(Day: String) {
        this.day = Day
    }

    fun getServerTimestamp(): Timestamp? {
        return serverTimestamp
    }

    fun setServerTimestamp(serverTimestamp: Timestamp?) {
        this.serverTimestamp = serverTimestamp
    }

    override fun toString(): String {
        return "DataWeekPlanner{" +
                ", dateTimestamp=" + dateTimestamp +
                '}'
    }

}