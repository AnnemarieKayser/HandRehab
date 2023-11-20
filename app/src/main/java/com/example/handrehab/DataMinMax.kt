package com.example.handrehab

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class DataMinMax {

    private var dateTimestamp: Date? = null
    private var exerciseName = ""
    private var maxPointingFinger = 0f
    private var maxlittleFinger = 0f
    private var maxMiddleFinger = 0f
    private var maxThumb = 0f
    private var selectedHandSide = ""
    private var repetitions = 0
    private var sets = 0
    // serverTimestamp soll automatisch vom Server gesetzt werden
    @ServerTimestamp
    private var serverTimestamp: Timestamp? = null
    private var exerciseId = 0


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

    fun getSelectedHandSide(): String {
        return selectedHandSide
    }

    fun setSelectedHandSide(side: String) {
        this.selectedHandSide = side
    }

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

    fun getMaxLittleFinger(): Float {
        return maxlittleFinger
    }

    fun setMaxLittleFinger(Max: Float) {
        this.maxlittleFinger = Max
    }

    fun getMaxPointingFinger(): Float {
        return maxPointingFinger
    }

    fun setMaxPointingLFinger(Max: Float) {
        this.maxPointingFinger = Max
    }

    fun getMaxMiddleFinger(): Float {
        return maxMiddleFinger
    }

    fun setMaxMiddleFinger(Max: Float) {
        this.maxMiddleFinger = Max
    }

    fun getMaxThumbFinger(): Float {
        return maxThumb
    }

    fun setMaxThumbFinger(Max: Float) {
        this.maxThumb = Max
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
                '}'
    }

}