package com.example.wellnesstrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstrack.R
import com.example.wellnesstrack.models.MoodEntry

class MoodHistoryAdapter(private val moodEntries: List<MoodEntry>) :
    RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {
    
    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvMoodEmoji)
        val tvMood: TextView = itemView.findViewById(R.id.tvMoodName)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvNote: TextView = itemView.findViewById(R.id.tvMoodNote)
        val viewColorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val entry = moodEntries[position]
        holder.tvEmoji.text = entry.emoji
        holder.tvMood.text = entry.mood
        holder.tvDateTime.text = "${entry.date} at ${entry.time}"
        
        // Show/hide note based on content
        if (entry.note.isNotEmpty()) {
            holder.tvNote.visibility = View.VISIBLE
            holder.tvNote.text = entry.note
        } else {
            holder.tvNote.visibility = View.GONE
        }
        
        // Set color indicator
        holder.viewColorIndicator.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, entry.colorResId)
        )
    }
    
    override fun getItemCount() = moodEntries.size
}