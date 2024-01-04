package com.example.handrehab

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class DataGoal {
    private var goalExercises = 10f
    @ServerTimestamp
    private var serverTimestamp: Timestamp? = null
    private var dateTimestamp: Date? = null
    private var dayOfWeek = 1


    fun getGoalExercises(): Float {
        return goalExercises
    }

    fun setGoalExercises(goal: Float) {
        this.goalExercises = goal
    }

    fun getDate(): Date? {
        return dateTimestamp
    }

    fun setDate(Date: Date) {
        this.dateTimestamp = Date
    }

    fun getDayOfWeek(): Int {
        return dayOfWeek
    }

    fun setDayOfWeek(day: Int) {
        this.dayOfWeek = day
    }
    override fun toString(): String {
        return "DataMinMax{" +
                ", Ziel: " + goalExercises +
                '}'
    }

}