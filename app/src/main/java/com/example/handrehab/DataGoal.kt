package com.example.handrehab

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

class DataGoal {
    private var goalExercises = 10f
    @ServerTimestamp
    private var serverTimestamp: Timestamp? = null


    fun getGoalExercises(): Float {
        return goalExercises
    }

    fun setGoalExercises(goal: Float) {
        this.goalExercises = goal
    }


    override fun toString(): String {
        return "DataMinMax{" +
                ", Ziel: " + goalExercises +
                '}'
    }

}