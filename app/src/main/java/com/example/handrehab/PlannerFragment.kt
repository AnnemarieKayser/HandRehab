package com.example.handrehab

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handrehab.databinding.FragmentPlannerBinding
import com.example.handrehab.item.Datasource
import com.example.handrehab.item.Exercises
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import splitties.toast.toast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    // === ListView === //
    private lateinit var layoutManagerMonday : LinearLayoutManager
    private lateinit var layoutManagerTuesday : LinearLayoutManager
    private lateinit var layoutManagerWednesday : LinearLayoutManager
    private lateinit var layoutManagerThursday : LinearLayoutManager
    private lateinit var layoutManagerFriday : LinearLayoutManager
    private lateinit var layoutManagerSaturday : LinearLayoutManager
    private lateinit var layoutManagerSunday : LinearLayoutManager

    private lateinit var adapter : RecyclerAdapter

    private var listExercises = arrayListOf<Exercises>()
    private var listExercisesMonday = arrayListOf<Exercises>()
    private var listExercisesTuesday = arrayListOf<Exercises>()
    private var listExercisesWednesay = arrayListOf<Exercises>()
    private var listExercisesThursday = arrayListOf<Exercises>()
    private var listExercisesFriday = arrayListOf<Exercises>()
    private var listExercisesSaturday = arrayListOf<Exercises>()
    private var listExercisesSunday= arrayListOf<Exercises>()

    private lateinit var checkedItems : BooleanArray
    private var items = arrayListOf<String>()

    private val listSelected = arrayListOf<Int>()
    private val listStringSelected = arrayListOf<String>()


    private val viewModel: MainViewModel by activityViewModels()

    // === Datenbank === //
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var dbList = ArrayList <DataWeekPlanner> ()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutManagerMonday = LinearLayoutManager(activity)
        layoutManagerTuesday = LinearLayoutManager(activity)
        layoutManagerWednesday = LinearLayoutManager(activity)
        layoutManagerThursday = LinearLayoutManager(activity)
        layoutManagerFriday = LinearLayoutManager(activity)
        layoutManagerSaturday = LinearLayoutManager(activity)
        layoutManagerSunday = LinearLayoutManager(activity)
        layoutManagerMonday.orientation = LinearLayoutManager.HORIZONTAL
        layoutManagerTuesday.orientation = LinearLayoutManager.HORIZONTAL
        layoutManagerWednesday.orientation = LinearLayoutManager.HORIZONTAL
        layoutManagerThursday.orientation = LinearLayoutManager.HORIZONTAL
        layoutManagerFriday.orientation = LinearLayoutManager.HORIZONTAL
        layoutManagerSaturday.orientation = LinearLayoutManager.HORIZONTAL
        layoutManagerSunday.orientation = LinearLayoutManager.HORIZONTAL

        binding.recyclerViewMonday.layoutManager = layoutManagerMonday
        binding.recyclerViewTuesday.layoutManager = layoutManagerTuesday
        binding.recyclerViewWednesday.layoutManager = layoutManagerWednesday
        binding.recyclerViewThursday.layoutManager = layoutManagerThursday
        binding.recyclerViewFriday.layoutManager = layoutManagerFriday
        binding.recyclerViewSaturday.layoutManager = layoutManagerSaturday
        binding.recyclerViewSunday.layoutManager = layoutManagerSunday


        checkedItems = BooleanArray(Datasource().loadItems().size)

        loadDbData()

        binding.imageButtonMonday.setOnClickListener {


            items.clear()
            viewModel.setSelectedDay("monday")
            checkedItems = BooleanArray(Datasource().loadItems().size)
            listStringSelected.clear()

            for((counter,i) in Datasource().loadItems().withIndex()){
                if(!(i.checked)) items.add(i.textItem)
                for(k in listExercisesMonday){
                    if(k == i) {
                        checkedItems[counter] = true
                        listStringSelected.add(k.textItem)
                    }
                }
            }

            this.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setMultiChoiceItems(items.toTypedArray(), checkedItems) {dialog, which, isChecked ->
                        checkedItems[which] = isChecked
                        if(checkedItems[which]) listStringSelected.add(items.toTypedArray()[which]) else listStringSelected.remove(items.toTypedArray()[which])
                    }
                    .setTitle(R.string.dialog_select_exercise)
                    .setNeutralButton(R.string.dialog_cancel) { dialog, which ->
                    }
                    .setPositiveButton(R.string.dialog_OK) { dialog, which ->

                        listSelected.clear()
                        listExercisesMonday.clear()

                        for(i in Datasource().loadItems()){

                            for(j in listStringSelected){
                                if(i.textItem == j) {
                                    listSelected.add(i.id)
                                    listExercisesMonday.add(i)
                                }
                            }
                        }
                        adapter = RecyclerAdapter(listExercisesMonday, "Main", viewModel.getExerciseListMode()!!)
                        binding.recyclerViewMonday.adapter = adapter
                        saveList()

                    }
                    .show()
            }
        }

        binding.imageButtonTuesday.setOnClickListener {

            viewModel.setSelectedDay("tuesday")
            checkedItems = BooleanArray(Datasource().loadItems().size)
            listStringSelected.clear()
            items.clear()

            for((counter,i) in Datasource().loadItems().withIndex()){
                items.add(i.textItem)
                for(k in listExercisesTuesday){
                    if(k == i) {
                        checkedItems[counter] = true
                        listStringSelected.add(k.textItem)
                    }
                }
            }

            this.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setMultiChoiceItems(items.toTypedArray(), checkedItems) {dialog, which, isChecked ->
                        checkedItems[which] = isChecked
                        if(checkedItems[which]) listStringSelected.add(items.toTypedArray()[which]) else listStringSelected.remove(items.toTypedArray()[which])
                    }
                    .setTitle(R.string.dialog_select_exercise)
                    .setNeutralButton(R.string.dialog_cancel) { dialog, which ->
                    }
                    .setPositiveButton(R.string.dialog_OK) { dialog, which ->

                        listSelected.clear()
                        listExercisesTuesday.clear()

                        for(i in Datasource().loadItems()){

                            for(j in listStringSelected){
                                if(i.textItem == j) {
                                    listSelected.add(i.id)
                                    listExercisesTuesday.add(i)
                                }
                            }
                        }

                        adapter = RecyclerAdapter(listExercisesTuesday, "Main", viewModel.getExerciseListMode()!!)
                        binding.recyclerViewTuesday.adapter = adapter
                        saveList()

                    }
                    .show()
            }
        }

        binding.imageButtonWednesday.setOnClickListener {

            viewModel.setSelectedDay("wednesday")
            checkedItems = BooleanArray(Datasource().loadItems().size)
            listStringSelected.clear()
            items.clear()

            for((counter,i) in Datasource().loadItems().withIndex()){
                items.add(i.textItem)
                for(k in listExercisesWednesay){
                    if(k == i) {
                        checkedItems[counter] = true
                        listStringSelected.add(k.textItem)
                    }
                }
            }

            this.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setMultiChoiceItems(items.toTypedArray(), checkedItems) {dialog, which, isChecked ->
                        checkedItems[which] = isChecked
                        if(checkedItems[which]) listStringSelected.add(items.toTypedArray()[which]) else listStringSelected.remove(items.toTypedArray()[which])
                    }
                    .setTitle(R.string.dialog_select_exercise)
                    .setNeutralButton(R.string.dialog_cancel) { dialog, which ->
                    }
                    .setPositiveButton(R.string.dialog_OK) { dialog, which ->

                        listSelected.clear()
                        listExercisesWednesay.clear()

                        for(i in Datasource().loadItems()){

                            for(j in listStringSelected){
                                if(i.textItem == j) {
                                    listSelected.add(i.id)
                                    listExercisesWednesay.add(i)
                                }
                            }
                        }
                        adapter = RecyclerAdapter(listExercisesWednesay, "Main", viewModel.getExerciseListMode()!!)
                        binding.recyclerViewWednesday.adapter = adapter
                        saveList()

                    }
                    .show()
            }
        }

        binding.imageButtonThursday.setOnClickListener {

            viewModel.setSelectedDay("thursday")
            checkedItems = BooleanArray(Datasource().loadItems().size)
            listStringSelected.clear()
            items.clear()

            for((counter,i) in Datasource().loadItems().withIndex()){
                items.add(i.textItem)
                for(k in listExercisesThursday){
                    if(k == i) {
                        checkedItems[counter] = true
                        listStringSelected.add(k.textItem)
                    }
                }
            }

            this.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setMultiChoiceItems(items.toTypedArray(), checkedItems) {dialog, which, isChecked ->
                        checkedItems[which] = isChecked
                        if(checkedItems[which]) listStringSelected.add(items.toTypedArray()[which]) else listStringSelected.remove(items.toTypedArray()[which])
                    }
                    .setTitle(R.string.dialog_select_exercise)
                    .setNeutralButton(R.string.dialog_cancel) { dialog, which ->
                    }
                    .setPositiveButton(R.string.dialog_OK) { dialog, which ->

                        listSelected.clear()
                        listExercisesThursday.clear()

                        for(i in Datasource().loadItems()){

                            for(j in listStringSelected){
                                if(i.textItem == j) {
                                    listSelected.add(i.id)
                                    listExercisesThursday.add(i)
                                }
                            }
                        }
                        adapter = RecyclerAdapter(listExercisesThursday, "Main",  viewModel.getExerciseListMode()!!)
                        binding.recyclerViewThursday.adapter = adapter
                        saveList()

                    }
                    .show()
            }
        }

        binding.imageButtonFriday.setOnClickListener {

            viewModel.setSelectedDay("friday")
            checkedItems = BooleanArray(Datasource().loadItems().size)
            listStringSelected.clear()
            items.clear()

            for((counter,i) in Datasource().loadItems().withIndex()){
                items.add(i.textItem)
                for(k in listExercisesFriday){
                    if(k == i) {
                        checkedItems[counter] = true
                        listStringSelected.add(k.textItem)
                    }
                }
            }

            this.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setMultiChoiceItems(items.toTypedArray(), checkedItems) {dialog, which, isChecked ->
                        checkedItems[which] = isChecked
                        if(checkedItems[which]) listStringSelected.add(items.toTypedArray()[which]) else listStringSelected.remove(items.toTypedArray()[which])
                    }
                    .setTitle(R.string.dialog_select_exercise)
                    .setNeutralButton(R.string.dialog_cancel) { dialog, which ->
                    }
                    .setPositiveButton(R.string.dialog_OK) { dialog, which ->

                        listSelected.clear()
                        listExercisesFriday.clear()

                        for(i in Datasource().loadItems()){

                            for(j in listStringSelected){
                                if(i.textItem == j) {
                                    listSelected.add(i.id)
                                    listExercisesFriday.add(i)
                                }
                            }
                        }
                        adapter = RecyclerAdapter(listExercisesFriday, "Main",  viewModel.getExerciseListMode()!!)
                        binding.recyclerViewFriday.adapter = adapter
                        saveList()

                    }
                    .show()
            }
        }

        binding.imageButtonSaturday.setOnClickListener {

            viewModel.setSelectedDay("saturday")
            checkedItems = BooleanArray(Datasource().loadItems().size)
            listStringSelected.clear()
            items.clear()

            for((counter,i) in Datasource().loadItems().withIndex()){
                items.add(i.textItem)
                for(k in listExercisesSaturday){
                    if(k == i) {
                        checkedItems[counter] = true
                        listStringSelected.add(k.textItem)
                    }
                }
            }

            this.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setMultiChoiceItems(items.toTypedArray(), checkedItems) {dialog, which, isChecked ->
                        checkedItems[which] = isChecked
                        if(checkedItems[which]) listStringSelected.add(items.toTypedArray()[which]) else listStringSelected.remove(items.toTypedArray()[which])
                    }
                    .setTitle(R.string.dialog_select_exercise)
                    .setNeutralButton(R.string.dialog_cancel) { dialog, which ->
                    }
                    .setPositiveButton(R.string.dialog_OK) { dialog, which ->

                        listSelected.clear()
                        listExercisesSaturday.clear()

                        for(i in Datasource().loadItems()){

                            for(j in listStringSelected){
                                if(i.textItem == j) {
                                    listSelected.add(i.id)
                                    listExercisesSaturday.add(i)
                                }
                            }
                        }
                        adapter = RecyclerAdapter(listExercisesSaturday, "Main",  viewModel.getExerciseListMode()!!)
                        binding.recyclerViewSaturday.adapter = adapter
                        saveList()

                    }
                    .show()
            }
        }

        binding.imageButtonSunday.setOnClickListener {

            viewModel.setSelectedDay("sunday")
            checkedItems = BooleanArray(Datasource().loadItems().size)
            listStringSelected.clear()
            items.clear()

            for((counter,i) in Datasource().loadItems().withIndex()){
                items.add(i.textItem)
                for(k in listExercisesSunday){
                    if(k == i) {
                        checkedItems[counter] = true
                        listStringSelected.add(k.textItem)
                    }
                }
            }

            this.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setMultiChoiceItems(items.toTypedArray(), checkedItems) {dialog, which, isChecked ->
                        checkedItems[which] = isChecked
                        if(checkedItems[which]) listStringSelected.add(items.toTypedArray()[which]) else listStringSelected.remove(items.toTypedArray()[which])
                    }
                    .setTitle(R.string.dialog_select_exercise)
                    .setNeutralButton(R.string.dialog_cancel) { dialog, which ->
                    }
                    .setPositiveButton(R.string.dialog_OK) { dialog, which ->

                        listSelected.clear()
                        listExercisesSunday.clear()

                        for(i in Datasource().loadItems()){

                            for(j in listStringSelected){
                                if(i.textItem == j) {
                                    listSelected.add(i.id)
                                    listExercisesSunday.add(i)
                                }
                            }
                        }
                        adapter = RecyclerAdapter(listExercisesSunday, "Main",  viewModel.getExerciseListMode()!!)
                        binding.recyclerViewSunday.adapter = adapter
                        saveList()

                    }
                    .show()
            }
        }





    }

    // === loadDbData === //
    // Einlesen der Daten aus der Datenbank
    private fun loadDbData() {

        dbList.clear()
        listSelected.clear()
        listExercisesSunday.clear()
        listExercisesFriday.clear()
        listExercisesSaturday.clear()
        listExercisesWednesay.clear()
        listExercisesMonday.clear()
        listExercisesTuesday.clear()
        listExercisesThursday.clear()
        listExercises.clear()


        // Einstiegspunkt für die Abfrage ist users/uid/date/Daten
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DataWeekPlanner")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datenbankantwort in Objektvariable speichern
                    updateListView(task)
                } else {
                    Log.d(ContentValues.TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }

    }

    private fun updateListView(task: Task<QuerySnapshot>) {
        // Einträge in dbList kopieren, um sie im ListView anzuzeigen

        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task.result!!) {
            (dbList).add(document.toObject(DataWeekPlanner::class.java))
            Log.d("Daten", document.id + " => " + document.data)
        }

        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("EEEE", Locale.ENGLISH)

        val day = format.format(calendar.time).lowercase()

        for(i in dbList) {
            when (i.getDay()) {
                day -> {

                    for (j in Datasource().loadItems()) {

                        for (k in i.getListExercises()) {
                            if (k == j.id) {
                                listExercises.add(j)
                            }
                        }
                    }

                    viewModel.setListDay(listExercises)
                }
            }
        }



        for(i in dbList){

            when(i.getDay()){
                "monday" -> {
                    for( j in Datasource().loadItems()){

                        for(k in i.getListExercises()){
                            if(k == j.id) {
                                listExercisesMonday.add(j)
                            }
                        }
                    }

                    adapter = RecyclerAdapter(listExercisesMonday, "Main",  viewModel.getExerciseListMode()!!)
                    binding.recyclerViewMonday.adapter = adapter
                }
                "tuesday" -> {
                    for(j in Datasource().loadItems()){

                        for(k in i.getListExercises()){
                            if(k == j.id) {
                                listExercisesTuesday.add(j)
                            }
                        }
                    }
                    adapter = RecyclerAdapter(listExercisesTuesday, "Main",  viewModel.getExerciseListMode()!!)
                    binding.recyclerViewTuesday.adapter = adapter
                }
                "wednesday" -> {
                    for(j in Datasource().loadItems()){

                        for(k in i.getListExercises()){
                            if(k == j.id) {
                                listExercisesWednesay.add(j)
                            }
                        }
                    }
                    adapter = RecyclerAdapter(listExercisesWednesay, "Main",  viewModel.getExerciseListMode()!!)
                    binding.recyclerViewWednesday.adapter = adapter
                }
                "thursday" -> {
                    for(j in Datasource().loadItems()){

                        for(k in i.getListExercises()){
                            if(k == j.id) {
                                listExercisesThursday.add(j)
                            }
                        }
                    }
                    adapter = RecyclerAdapter(listExercisesThursday, "Main",  viewModel.getExerciseListMode()!!)
                    binding.recyclerViewThursday.adapter = adapter
                }
                "friday" -> {
                    for(j in Datasource().loadItems()){

                        for(k in i.getListExercises()){
                            if(k == j.id) {
                                listExercisesFriday.add(j)
                            }
                        }
                    }
                    adapter = RecyclerAdapter(listExercisesFriday, "Main",  viewModel.getExerciseListMode()!!)
                    binding.recyclerViewFriday.adapter = adapter
                }
                "saturday" -> {
                    for(j in Datasource().loadItems()){

                        for(k in i.getListExercises()){
                            if(k == j.id) {
                                listExercisesSaturday.add(j)
                            }
                        }
                    }
                    adapter = RecyclerAdapter(listExercisesSaturday, "Main",  viewModel.getExerciseListMode()!!)
                    binding.recyclerViewSaturday.adapter = adapter
                }
                "sunday" -> {
                    for(j in Datasource().loadItems()){

                        for(k in i.getListExercises()){
                            if(k == j.id) {
                                listExercisesSunday.add(j)
                            }
                        }
                    }
                    adapter = RecyclerAdapter(listExercisesSunday, "Main",  viewModel.getExerciseListMode()!!)
                    binding.recyclerViewSunday.adapter = adapter
                }

                else -> {}
            }

        }


    }


    @SuppressLint("SimpleDateFormat")
    private fun saveList() {


        // Einlesen des aktuellen Datums
        val kalender: Calendar = Calendar.getInstance()
        val zeitformat = SimpleDateFormat("yyyy-MM-dd-kk-mm-ss")
        val date = zeitformat.format(kalender.time)

        var datetimestamp: Date? = null
        try {
            datetimestamp = zeitformat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        //Data Objekt mit Daten befüllen (ID wird automatisch ergänzt)
        val data = DataWeekPlanner()
        data.setDate(datetimestamp!!)
        data.setListExercises(listSelected)
        data.setDay(viewModel.getSelectedDay()!!)


        // Schreibe Daten als Document in die Collection Messungen in DB;
        // Eine id als Document Name wird automatisch vergeben
        // Implementiere auch onSuccess und onFailure Listender
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("users").document(uid).collection("DataWeekPlanner").document(viewModel.getSelectedDay()!!)
            .set(data)
            .addOnSuccessListener { documentReference ->
            }
            .addOnFailureListener { e ->
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}