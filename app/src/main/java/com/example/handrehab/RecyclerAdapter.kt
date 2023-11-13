package com.example.handrehab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.handrehab.item.Exercises
import com.example.handrehab.R


class RecyclerAdapter(val listItems: List<Exercises>, val layout: String) :

    RecyclerView.Adapter<RecyclerAdapter.TextHolder>() {
    var itemClickListener: AdapterItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextHolder {

        // Hier wird das Layout eingebunden, wie jede einzelne Zeile der Liste aussehen soll
        // Diese Datei (recycler_row.xml) anlegen mittels layout->new->layout ressource file
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

    override fun onBindViewHolder(holder: TextHolder, position: Int) {
        val listItem = listItems[position]
        this.itemClickListener = itemClickListener;
        holder.bindItemText(listItem)
        holder.itemView.setOnClickListener { v ->
            itemClickListener!!.onItemClickListener(
                listItem,
                position
            )
        }
    }

    class TextHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private val textView: TextView = v.findViewById(R.id.textViewItem)
        private var imageView: ImageView = v.findViewById(R.id.item_image)


        private var view: View = v
        private var itemText: String = ""

        // Die Elemente des Views füllen
        // Im Beispiel nur ein einfacher TextView

        fun bindItemText(itemText: Exercises) {
            this.itemText = itemText.textItem
            textView.text = itemText.textItem.split(":")[0]
            imageView.setImageResource(itemText.imageItem)
        }

        override fun onClick(v: View?) {
            TODO("Not yet implemented")

        }

    }

    // A function to bind the onclickListener.
    fun setOnClickListener(itemClickListener: AdapterItemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    interface AdapterItemClickListener {
        fun onItemClickListener(exercises: Exercises, position: Int)
    }


}