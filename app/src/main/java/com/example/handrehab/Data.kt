package com.example.handrehab

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class Data {

    private var counterExercises = 0
    private var dateTimestamp: Date? = null
    private var repetitions = 0
    private var sets = 0
    private var exerciseName = ""
    private var dayOfWeek = 1
    // serverTimestamp soll automatisch vom Server gesetzt werden
    @ServerTimestamp
    private var serverTimestamp: Timestamp? = null


    fun getDayOfWeek(): Int {
        return dayOfWeek
    }

    fun setDayOfWeek(day: Int) {
        this.dayOfWeek = day
    }

    fun getDate(): Date? {
        return dateTimestamp
    }

    fun setDate(Date: Date) {
        this.dateTimestamp = Date
    }

    fun getCounterExercises(): Int {
        return counterExercises
    }

    fun setCounterExercises(counter: Int) {
        this.counterExercises = counter
    }

    fun getRepetitions(): Int {
        return repetitions
    }

    fun setRepetitions(rep: Int) {
        this.repetitions = rep
    }

    fun getSets(): Int {
        return sets
    }

    fun setSets(set: Int) {
        this.sets = set
    }

    fun getExerciseName(): String {
        return exerciseName
    }

    fun setExerciseName(name: String) {
        this.exerciseName = name
    }
    fun getServerTimestamp(): Timestamp? {
        return serverTimestamp
    }

    fun setServerTimestamp(serverTimestamp: Timestamp?) {
        this.serverTimestamp = serverTimestamp
    }

    override fun toString(): String {
        return "Data{" +
                ", Name=" + exerciseName +
                ", dateTimestamp=" + dateTimestamp +
                ", counterExercises=" + counterExercises +
                ", Repetitions=" + repetitions +
                ", Sets=" + sets +
                ", dayOfWeek=" + dayOfWeek +
                '}'
    }

}