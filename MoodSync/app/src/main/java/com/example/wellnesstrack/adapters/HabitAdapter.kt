package com.example.wellnesstrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstrack.R
import com.example.wellnesstrack.models.Habit
import com.example.wellnesstrack.repository.HabitRepository

class HabitAdapter(
    private var habits: List<Habit>,
    private val listener: HabitInteractionListener,
    private val repository: HabitRepository
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    interface HabitInteractionListener {
        fun onHabitComplete(habit: Habit)
        fun onHabitEdit(habit: Habit)
        fun onHabitDelete(habit: Habit)
    }
    
    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tvHabitIcon)
        val tvName: TextView = view.findViewById(R.id.tvHabitName)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val tvProgress: TextView = view.findViewById(R.id.tvProgress)
        val btnComplete: Button = view.findViewById(R.id.btnComplete)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        val progress = repository.getHabitProgressForToday(habit.id)
        
        // Set basic habit data
        holder.tvIcon.text = habit.icon
        holder.tvName.text = habit.name
        
        // Set progress
        holder.progressBar.max = habit.goal
        holder.progressBar.progress = progress.progress
        holder.tvProgress.text = "${progress.progress}/${habit.goal} ${habit.unit}"
        
        // Handle completion state
        if (progress.isCompleted) {
            holder.btnComplete.text = "Completed"
            holder.btnComplete.isEnabled = false
            holder.btnComplete.alpha = 0.5f
        } else {
            holder.btnComplete.text = "Complete"
            holder.btnComplete.isEnabled = true
            holder.btnComplete.alpha = 1.0f
        }
        
        // Set click listeners
        holder.btnComplete.setOnClickListener {
            listener.onHabitComplete(habit)
        }
        
        holder.btnEdit.setOnClickListener {
            listener.onHabitEdit(habit)
        }
        
        holder.btnDelete.setOnClickListener {
            listener.onHabitDelete(habit)
        }
    }
    
    override fun getItemCount() = habits.size
    
    fun updateHabits(newHabits: List<Habit>) {
        android.util.Log.d("HabitAdapter", "Updating with ${newHabits.size} habits: $newHabits")
        this.habits = newHabits
        notifyDataSetChanged()
    }
}