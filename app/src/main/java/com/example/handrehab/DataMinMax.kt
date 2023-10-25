package com.example.handrehab

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class DataMinMax {

    private var dateTimestamp: Date? = null
    private var exerciseName = ""
    private var max = 0f
    private var min = 0f
    // serverTimestamp soll automatisch vom Server gesetzt werden
    @ServerTimestamp
    private var serverTimestamp: Timestamp? = null
    private var exerciseId = 0



    fun getExerciseId(): Int {
        return exerciseId
    }

    fun setExerciseId(ExerciseId: Int) {
        this.exerciseId = ExerciseId
    }


    fun getExerciseName(): String {
        return exerciseName
    }

    fun setExerciseName(name: String) {
        this.exerciseName = name
    }

    fun getDate(): Date? {
        return dateTimestamp
    }

    fun setDate(Date: Date) {
        this.dateTimestamp = Date
    }

    fun getMax(): Float {
        return max
    }

    fun setMax(Max: Float) {
        this.max = Max
    }

    fun getMin(): Float {
        return min
    }

    fun setMin(Min: Float) {
        this.min = Min
    }

    fun getServerTimestamp(): Timestamp? {
        return serverTimestamp
    }

    fun setServerTimestamp(serverTimestamp: Timestamp?) {
        this.serverTimestamp = serverTimestamp
    }

    override fun toString(): String {
        return "DataMinMax{" +
                ", Name=" + exerciseName +
                ", dateTimestamp=" + dateTimestamp +
                ", Min=" + min +
                ", Max=" + max +
                ", ExerciseId=" + exerciseId +
                '}'
    }

}