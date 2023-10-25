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
    private var exerciseId = 0
    private var dayOfWeek = 1
    private var selectedHandSide = "right"
    private var max = 0f
    private var min = 0f
    private var startMode = "open"
    // serverTimestamp soll automatisch vom Server gesetzt werden
    @ServerTimestamp
    private var serverTimestamp: Timestamp? = null


    fun getExerciseId(): Int {
        return exerciseId
    }

    fun setExerciseId(ExerciseId: Int) {
        this.exerciseId = ExerciseId
    }


    fun getMin(): Float {
        return min
    }

    fun setMin(Min: Float) {
        this.min = Min
    }

    fun getMax(): Float {
        return max
    }

    fun setMax(Max: Float) {
        this.max = Max
    }


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

    fun getSelectedHandSide(): String {
        return selectedHandSide
    }

    fun setSelectedHandSide(side: String) {
        this.selectedHandSide = side
    }

    fun getStartMode(): String {
        return startMode
    }

    fun setStartMode(mode: String) {
        this.startMode = mode
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
                ", Min=" + min +
                ", Max=" + max +
                ", SelectedHand=" + selectedHandSide +
                ", StartMode=" + startMode +
                ", ExerciseId=" + exerciseId +
                '}'
    }

}