package com.example.handrehab

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.handrehab.item.Exercises


class RecyclerAdapter(private val listItems: List<Exercises>, val layout: String, private val exerciseListMode: Int) :


/*
   ======================================================================================
   ==========================           Einleitung             ==========================
   ======================================================================================
   Projektname: HandRehab
   Autor: Annemarie Kayser
   Anwendung: Dies ist eine App-Anwendung für die Handrehabilitation nach einem Schlaganfall.
              Es werden verschiedene Übungen für die linke als auch für die rechte Hand zur
              Verfügung gestellt. Zudem kann ein individueller Wochenplan erstellt
              sowie die Daten zu den durchgeführten Übungen eingesehen werden.
   Letztes Update: 12.01.2024

  ======================================================================================
*/


/*
  =============================================================
  =======                    Funktion                   =======
  =============================================================

  - In dieser Klasse sind das Layout und die Funktionen des Recycler Views
  implementiert
*/


    RecyclerView.Adapter<RecyclerAdapter.TextHolder>() {
    var itemClickListener: AdapterItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextHolder {

        // Hier wird das Layout eingebunden, wie jede einzelne Zeile der Liste aussieht
        // Im MainFragment und im PlannerFragment wird das "recyclerview_row_main" Layout angezeigt
        // Im ExerciseListFragment wird das "recyclerview_row" angezeigt
        return if(layout == "Main"){
            TextHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_row_main, parent, false)
            )
        } else {
            TextHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_row, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return listItems.count()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TextHolder, position: Int) {
        val listItem = listItems[position]
        this.itemClickListener = itemClickListener;
        holder.bindItemText(listItem, layout, exerciseListMode)
        if(layout != "Main") {
            holder.itemView.setOnClickListener { v ->
                itemClickListener!!.onItemClickListener(
                    listItem,
                    position
                )
            }
        }
    }

    class TextHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private val textView: TextView = v.findViewById(R.id.textViewItem)
        private var imageView: ImageView = v.findViewById(R.id.item_image)
        private var itemText: String = ""

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindItemText(itemText: Exercises, layout: String, listMode: Int) {
            this.itemText = itemText.textItem
            if(layout != "Main" && listMode != 2) {
            textView.text = itemText.textItem.split(":")[0]
            } else textView.text = itemText.textItem

            imageView.setImageResource(itemText.imageItem)
        }

        override fun onClick(v: View?) {
        }

    }

    fun setOnClickListener(itemClickListener: AdapterItemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    interface AdapterItemClickListener {
        fun onItemClickListener(exercises: Exercises, position: Int)
    }


}