package com.example.wellnesstrack.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wellnesstrack.R
import com.example.wellnesstrack.workers.HydrationReminderWorker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import java.util.concurrent.TimeUnit

class HydrationFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvReminderStatus: TextView
    private lateinit var btnAddWater: Button
    private lateinit var btnSetReminder: Button
    private lateinit var btnSetGoal: Button
    private val PREFS_NAME = "HydrationPrefs"
    
    private var waterGoal = 8 // Default goal: 8 glasses
    private var currentWater = 0 // Current water intake
    private var reminderInterval = 0 // Reminder interval in minutes (0 = no reminder)
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        progressBar = view.findViewById(R.id.progressBarHydration)
        tvProgress = view.findViewById(R.id.tvHydrationProgress)
        tvGoal = view.findViewById(R.id.tvHydrationGoal)
        tvReminderStatus = view.findViewById(R.id.tvReminderStatus)
        btnAddWater = view.findViewById(R.id.btnAddWater)
        btnSetReminder = view.findViewById(R.id.btnSetReminder)
        btnSetGoal = view.findViewById(R.id.btnSetGoal)
        
        // Load saved preferences
        loadPreferences()
        
        // Set up click listeners
        btnAddWater.setOnClickListener { addWaterIntake() }
        btnSetReminder.setOnClickListener { showReminderDialog() }
        btnSetGoal.setOnClickListener { showGoalDialog() }
        
        // Add floating action button for resetting progress
        view.findViewById<FloatingActionButton>(R.id.fabResetProgress).setOnClickListener {
            resetProgress()
        }
        
        // Update UI
        updateUI()
    }
    
    private fun loadPreferences() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        waterGoal = prefs.getInt("waterGoal", 8)
        currentWater = prefs.getInt("currentWater", 0)
        reminderInterval = prefs.getInt("reminderInterval", 0)
    }
    
    private fun savePreferences() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("waterGoal", waterGoal)
            putInt("currentWater", currentWater)
            putInt("reminderInterval", reminderInterval)
        }.apply()
    }
    
    private fun updateUI() {
        progressBar.max = waterGoal
        progressBar.progress = currentWater
        
        tvProgress.text = "$currentWater/$waterGoal glasses"
        tvGoal.text = "Goal: $waterGoal glasses of water"
        
        if (reminderInterval > 0) {
            tvReminderStatus.text = "Reminder: Every $reminderInterval minutes"
        } else {
            tvReminderStatus.text = "No reminders set"
        }
        
        // Update completion status
        if (currentWater >= waterGoal) {
            Toast.makeText(context, "Congratulations! You've reached your hydration goal today!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addWaterIntake() {
        if (currentWater < waterGoal) {
            currentWater++
            savePreferences()
            updateUI()
            
            if (currentWater >= waterGoal) {
                Toast.makeText(context, "Goal achieved! Great job staying hydrated!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "You've already reached your goal today!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun resetProgress() {
        currentWater = 0
        savePreferences()
        updateUI()
        Toast.makeText(context, "Progress reset for today", Toast.LENGTH_SHORT).show()
    }
    
    private fun showReminderDialog() {
        // Updated array to include 1 minute and 15 minute options
        val intervals = arrayOf("No Reminder", "1 minute", "15 minutes", "30 minutes", "1 hour", "2 hours", "3 hours", "4 hours", "Custom")
        val intervalValues = arrayOf(0, 1, 15, 30, 60, 120, 180, 240, -1)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Set Hydration Reminder")
            .setItems(intervals) { _, which ->
                val selectedInterval = intervalValues[which]
                
                if (selectedInterval == -1) {
                    // Custom interval
                    showCustomIntervalDialog()
                } else {
                    updateReminderInterval(selectedInterval)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCustomIntervalDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_custom_interval, null)
        val slider = view.findViewById<Slider>(R.id.sliderInterval)
        val tvIntervalDisplay = view.findViewById<TextView>(R.id.tvIntervalDisplay)
        
        // Update the slider to allow for 1-minute intervals
        slider.valueFrom = 1f
        slider.valueTo = 240f  // Up to 4 hours (240 minutes)
        slider.stepSize = 1f
        
        // Initialize with current value or default to 15
        val initialValue = if (reminderInterval > 0) reminderInterval.toFloat() else 15f
        slider.value = initialValue
        tvIntervalDisplay.text = "${initialValue.toInt()} minutes"
        
        slider.addOnChangeListener { _, value, _ ->
            tvIntervalDisplay.text = "${value.toInt()} minutes"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Set Custom Reminder Interval")
            .setView(view)
            .setPositiveButton("Set") { _, _ ->
                val interval = slider.value.toInt()
                updateReminderInterval(interval)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateReminderInterval(intervalMinutes: Int) {
        reminderInterval = intervalMinutes
        savePreferences()
        
        if (intervalMinutes > 0) {
            scheduleReminder(intervalMinutes)
            Toast.makeText(context, "Reminder set every $intervalMinutes minutes", Toast.LENGTH_SHORT).show()
        } else {
            cancelReminders()
            Toast.makeText(context, "Reminders turned off", Toast.LENGTH_SHORT).show()
        }
        
        updateUI()
    }
    
    private fun scheduleReminder(intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(requireContext())

        // Cancel any existing reminders
        workManager.cancelUniqueWork(HYDRATION_REMINDER_WORK_NAME)

        // Create input data for the worker
        val inputData = Data.Builder()
            .putString("title", "Hydration Reminder")
            .putString("message", "Time to drink water! Stay hydrated.")
            .putInt("intervalMinutes", intervalMinutes)
            .build()

        if (intervalMinutes < 15 && intervalMinutes > 0) {
            // Use OneTimeWorkRequest for short intervals
            val reminderRequest = androidx.work.OneTimeWorkRequestBuilder<HydrationReminderWorker>()
                .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniqueWork(
                HYDRATION_REMINDER_WORK_NAME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
        } else if (intervalMinutes >= 15) {
            // Use PeriodicWorkRequest for 15 minutes or more
            val reminderRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            ).setInputData(inputData).build()

            workManager.enqueueUniquePeriodicWork(
                HYDRATION_REMINDER_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
            )
        }
    }
    
    private fun cancelReminders() {
        WorkManager.getInstance(requireContext())
            .cancelUniqueWork(HYDRATION_REMINDER_WORK_NAME)
    }
    
    private fun showGoalDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_hydration_goal, null)
        val slider = view.findViewById<Slider>(R.id.sliderGoal)
        val tvGoalDisplay = view.findViewById<TextView>(R.id.tvGoalDisplay)
        
        slider.value = waterGoal.toFloat()
        tvGoalDisplay.text = "${slider.value.toInt()} glasses"
        
        slider.addOnChangeListener { _, value, _ ->
            tvGoalDisplay.text = "${value.toInt()} glasses"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Set Daily Hydration Goal")
            .setView(view)
            .setPositiveButton("Set") { _, _ ->
                val goal = slider.value.toInt()
                updateHydrationGoal(goal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateHydrationGoal(goal: Int) {
        waterGoal = goal
        savePreferences()
        updateUI()
        Toast.makeText(context, "Daily goal updated to $goal glasses", Toast.LENGTH_SHORT).show()
    }
    
    companion object {
        const val HYDRATION_REMINDER_WORK_NAME = "hydration_reminder_work"
    }
}