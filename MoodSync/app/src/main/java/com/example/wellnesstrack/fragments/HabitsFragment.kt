package com.example.wellnesstrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstrack.R
import com.example.wellnesstrack.adapters.HabitAdapter
import com.example.wellnesstrack.models.Habit
import com.example.wellnesstrack.repository.HabitRepository
import com.example.wellnesstrack.widget.HabitWidgetProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment(), HabitAdapter.HabitInteractionListener {

    private lateinit var habitRepository: HabitRepository
    private lateinit var habitAdapter: HabitAdapter
    
    // UI components
    private lateinit var tvNoHabits: TextView
    private lateinit var rvHabits: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize repository
        habitRepository = HabitRepository(requireContext())
        
        // Initialize UI components
        tvNoHabits = view.findViewById(R.id.tvNoHabits)
        rvHabits = view.findViewById(R.id.rvHabits)
        progressBar = view.findViewById(R.id.progressBar)
        tvProgress = view.findViewById(R.id.tvTodayProgress)
        
        // Set up RecyclerView with fixed size for better performance
        habitAdapter = HabitAdapter(habitRepository.getAllHabits(), this, habitRepository)
        rvHabits.setHasFixedSize(true)
        rvHabits.isNestedScrollingEnabled = false
        rvHabits.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvHabits.adapter = habitAdapter
        
        // Set up FAB for adding new habits
        view.findViewById<FloatingActionButton>(R.id.fabAddHabit).setOnClickListener {
            showHabitDialog()
        }
        
        // Load habits and update UI
        loadHabits()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to the fragment
        loadHabits()
    }
    
    private fun loadHabits() {
        val habits = habitRepository.getAllHabits()
        
        // Log habits for debugging
        android.util.Log.d("HabitsFragment", "Loaded ${habits.size} habits: $habits")
        
        // Force recreation of adapter to ensure fresh data display
        habitAdapter = HabitAdapter(habits, this, habitRepository)
        rvHabits.adapter = habitAdapter
        
        // Update UI based on whether there are habits
        if (habits.isEmpty()) {
            tvNoHabits.visibility = View.VISIBLE
            rvHabits.visibility = View.GONE
        } else {
            tvNoHabits.visibility = View.GONE
            rvHabits.visibility = View.VISIBLE
            // Notify adapter explicitly
            habitAdapter.notifyDataSetChanged()
        }
        
        // Update progress display
        updateProgressDisplay()
    }
    
    private fun updateProgressDisplay() {
        val (completed, total) = habitRepository.getTodayCompletionStats()
        
        // Update progress bar
        progressBar.max = if (total > 0) total else 1
        progressBar.progress = completed
        
        // Update text
        tvProgress.text = getString(R.string.habit_progress, completed, total)
    }
    
    private fun showHabitDialog(habit: Habit? = null) {
        val isEdit = habit != null
        val dialog = SimpleHabitDialogFragment.newInstance(habit)
        
        dialog.setHabitDialogListener(object : SimpleHabitDialogFragment.HabitDialogListener {
            override fun onHabitSaved(newHabit: Habit) {
                habitRepository.saveHabit(newHabit)
                loadHabits()
            }
        })
        
        dialog.show(childFragmentManager, if (isEdit) "EditHabitDialog" else "AddHabitDialog")
    }
    
    // HabitInteractionListener implementation
    override fun onHabitComplete(habit: Habit) {
        onHabitCompleted(habit)
    }
    
    private fun onHabitCompleted(habit: Habit) {
        // Your existing code to mark habit as complete
        habitRepository.markHabitAsComplete(habit.id)
        
        // Update the habits list
        loadHabits() // Changed from refreshHabits() to loadHabits()
        
        // Update the widget
        HabitWidgetProvider.updateWidget(requireContext())
    }
    
    private fun refreshHabits() {
        // This method is a simple alias for loadHabits() to maintain compatibility
        loadHabits()
    }
    
    override fun onHabitEdit(habit: Habit) {
        showHabitDialog(habit)
    }
    
    override fun onHabitDelete(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_habit)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                if (habitRepository.deleteHabit(habit.id)) {
                    loadHabits()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}