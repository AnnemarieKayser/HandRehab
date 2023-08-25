package com.example.handrehab
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.handrehab.fragment.CameraFragment
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

    //Kleiner Finger
    private var _littleFingerCounter = MutableLiveData<Int>()


    val selectedExercise: LiveData<Exercises?>
        get() = _selectedExercise

    val repetitions: LiveData<Int>
        get() = _repetitions

    val sets: LiveData<Int>
        get() = _sets

    val counter: LiveData<Int>
        get() = _littleFingerCounter

    fun littleFingerCounter() {
        _littleFingerCounter.value = (_littleFingerCounter.value ?: 0) + 1
    }

    init {
        _selectedExercise.value = null
        _repetitions.value = 0
        _sets.value = 0
        _littleFingerCounter.value = 0
    }

    fun getCounterLittleFinger(): Int? {
        return _littleFingerCounter.value
    }

    fun setCounterLittleFinger(counter: Int){
        _littleFingerCounter.value = counter
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

    fun getSelectedExercise(): Exercises? {
        return _selectedExercise.value
    }

    fun setSelectedExercise(exercise: Exercises) {
        _selectedExercise.value = exercise
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