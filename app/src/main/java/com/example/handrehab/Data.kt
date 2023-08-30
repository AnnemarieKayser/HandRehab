package com.example.handrehab

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class Data {



    private var counterExercises = 0
    private var dateTimestamp: Date? = null
    private var date: String? = null

    // serverTimestamp soll automatisch vom Server gesetzt werden
    @ServerTimestamp
    private var serverTimestamp: Timestamp? = null


    fun getCounterExercises(): Int {
        return counterExercises
    }

    fun setCounterExercises(counter: Int) {
        this.counterExercises = counter
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String?) {
        this.date = date
    }

    fun getServerTimestamp(): Timestamp? {
        return serverTimestamp
    }

    fun setServerTimestamp(serverTimestamp: Timestamp?) {
        this.serverTimestamp = serverTimestamp
    }

    override fun toString(): String {
        return "Data{" +
                ", counterExercises=" + counterExercises +
                '}'
    }

}