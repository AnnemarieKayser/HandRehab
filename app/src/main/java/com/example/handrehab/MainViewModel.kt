/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.handrehab
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.handrehab.fragment.GestureRecognizerHelper
import com.example.handrehab.item.Exercises

class MainViewModel: ViewModel() {
    private var _delegate: Int = GestureRecognizerHelper.DELEGATE_CPU
    private var _minHandDetectionConfidence: Float = GestureRecognizerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
    private var _minHandTrackingConfidence: Float = GestureRecognizerHelper.DEFAULT_HAND_TRACKING_CONFIDENCE
    private var _minHandPresenceConfidence: Float = GestureRecognizerHelper.DEFAULT_HAND_PRESENCE_CONFIDENCE

    val currentDelegate: Int get() = _delegate

    //Exercise-Variablen
    private var _selectedExercise = MutableLiveData<Exercises?>()
    private var _repetitions = MutableLiveData<Int>()
    private var _sets = MutableLiveData<Int>()
    private var _handOpenOrClosed =  MutableLiveData<String>()
    private var _selectedHandSide = MutableLiveData<String>()
    private var _divideFactor = MutableLiveData<Double>()
    private var _exerciseListMode = MutableLiveData<Int>()
    private var _selectedDay = MutableLiveData<String>()
    private var _listDay = MutableLiveData<List<Exercises>>()
    private var _allFingersOpenOrClose = MutableLiveData<Int>()


    val listDay: LiveData<List<Exercises>>
        get() = _listDay

    val selectedDay: LiveData<String>
        get() = _selectedDay
    val exerciseListMode: LiveData<Int>
        get() = _exerciseListMode

    val allFingersOpenOrClose: LiveData<Int>
        get() = _allFingersOpenOrClose

    val handOpenOrClosed: LiveData<String>
        get() = _handOpenOrClosed

    val selectedHandSide: LiveData<String>
        get() = _selectedHandSide

    val selectedExercise: LiveData<Exercises?>
        get() = _selectedExercise

    val repetitions: LiveData<Int>
        get() = _repetitions

    val divideFactor: LiveData<Double>
        get() = _divideFactor

    val sets: LiveData<Int>
        get() = _sets


    init {
        _selectedExercise.value = null
        _repetitions.value = 0
        _sets.value = 0
        _handOpenOrClosed.value = "geschlossen"
        _selectedHandSide.value = "rechts"
        _divideFactor.value = 2.0
        _exerciseListMode.value = 1
        _selectedDay.value = "monday"
        _listDay.value = arrayListOf()
        _allFingersOpenOrClose.value = 1
    }

    fun getAllFingersOpenOrClose(): Int? {
        return _allFingersOpenOrClose.value
    }

    fun setAllFingersOpenOrClose(allFingers: Int){
        _allFingersOpenOrClose.value = allFingers
    }
    fun getListDay(): List<Exercises>? {
        return _listDay.value
    }

    fun setListDay(list: List<Exercises>){
        _listDay.value = list
    }

    fun getSelectedDay(): String? {
        return _selectedDay.value
    }

    fun setSelectedDay(day: String) {
        _selectedDay.value = day
    }

    fun getExerciseListMode(): Int? {
        return _exerciseListMode.value
    }

    fun setExercisesListMode(value: Int){
        _exerciseListMode.value = value
    }


    fun getRepetitions(): Int? {
        return _repetitions.value
    }

    fun setRepetitions(rep: Int){
        _repetitions.value = rep
    }

    fun getSets(): Int? {
        return _sets.value
    }

    fun setSets(set: Int) {
        _sets.value = set
    }

    fun getDivideFactor(): Double? {
        return _divideFactor.value
    }

    fun setDivideFactor(factor: Double){
        _divideFactor.value = factor
    }

    fun getSelectedExercise(): Exercises? {
        return _selectedExercise.value
    }

    fun setSelectedExercise(exercise: Exercises) {
        _selectedExercise.value = exercise
    }

    fun getStartModus(): String? {
        return _handOpenOrClosed.value
    }

    fun setStartModus(hand: String){
        _handOpenOrClosed.value = hand
    }

    fun getSelectedHandSide(): String? {
        return _selectedHandSide.value
    }

    fun setSelectedHandSide(side: String){
        _selectedHandSide.value = side
    }


    val currentMinHandDetectionConfidence: Float
    get() = _minHandDetectionConfidence

    val currentMinHandTrackingConfidence: Float
    get() = _minHandTrackingConfidence

    val currentMinHandPresenceConfidence: Float
    get() = _minHandPresenceConfidence

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

    fun setMinHandDetectionConfidence(confidence: Float) {
        _minHandDetectionConfidence = confidence
    }

    fun setMinHandTrackingConfidence(confidence: Float) {
        _minHandTrackingConfidence = confidence
    }

    fun setMinHandPresenceConfidence(confidence: Float) {
        _minHandPresenceConfidence = confidence
    }

    // Extension Function, um Änderung in den Einträgen von Listen
    // dem Observer anzeigen zu können
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    //Funktionen Gestenerkennung

}