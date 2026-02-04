package com.example.wellnesstrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstrack.R

class IconAdapter(
    private val icons: List<String>,
    private val onIconClick: (String) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    inner class IconViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tvIcon)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icon, parent, false)
        return IconViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val icon = icons[position]
        holder.tvIcon.text = icon
        
        holder.itemView.setOnClickListener {
            onIconClick(icon)
        }
    }
    
    override fun getItemCount() = icons.size
}