package com.example.wellnesstrack.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstrack.R
import com.example.wellnesstrack.adapters.MoodHistoryAdapter
import com.example.wellnesstrack.models.MoodEntry
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

//import com.github.mikephil.charting.data.PieData
//import com.github.mikephil.charting.data.PieDataSet
//import com.github.mikephil.charting.data.PieEntry
//import com.github.mikephil.charting.utils.ColorTemplate

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MoodFragment : Fragment() {

    private lateinit var moodHistoryAdapter: MoodHistoryAdapter
    private lateinit var rvMoodHistory: RecyclerView
    private lateinit var tvNoMoods: TextView
    private lateinit var chartWeeklyMood: BarChart
    private val moodEntries = mutableListOf<MoodEntry>()
    
    // Mood emoji options
    private val moodEmojis = listOf(
        MoodEmoji("😄", "Happy", R.color.colorPrimary),
        MoodEmoji("😊", "Content", R.color.colorAccent),
        MoodEmoji("😐", "Neutral", R.color.textColorPrimary),
        MoodEmoji("😔", "Sad", R.color.colorSecondary),
        MoodEmoji("😡", "Angry", android.R.color.holo_red_dark)
    )
    
    // Map mood names to numerical values (1-5)
    private val moodValues = mapOf(
        "Happy" to 5,
        "Content" to 4,
        "Neutral" to 3,
        "Sad" to 2,
        "Angry" to 1
    )
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        rvMoodHistory = view.findViewById(R.id.rvMoodHistory)
        tvNoMoods = view.findViewById(R.id.tvNoMoods)
        chartWeeklyMood = view.findViewById(R.id.chartWeeklyMood)
        
        // Set up RecyclerView
        rvMoodHistory.layoutManager = LinearLayoutManager(context)
        moodHistoryAdapter = MoodHistoryAdapter(moodEntries)
        rvMoodHistory.adapter = moodHistoryAdapter
        
        // Set up FAB for adding mood
        view.findViewById<FloatingActionButton>(R.id.fabAddMood).setOnClickListener {
            showMoodSelector()
        }
        
        // Load saved mood entries
        loadMoodEntries()
        
        // Setup chart
        setupMoodChart()
        
        // Update UI
        updateUI()

        view.findViewById<ImageButton>(R.id.btnShareMood).setOnClickListener {
            shareMoodSummary()
        }
    }
    
    private fun loadMoodEntries() {
        val sharedPrefs = requireContext().getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE)
        val moodEntriesJson = sharedPrefs.getString("moodEntries", null)
        
        if (!moodEntriesJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            val loadedEntries: List<MoodEntry> = Gson().fromJson(moodEntriesJson, type)
            moodEntries.clear()
            moodEntries.addAll(loadedEntries)
        }
    }
    
    private fun saveMoodEntries() {
        val sharedPrefs = requireContext().getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE)
        val moodEntriesJson = Gson().toJson(moodEntries)
        sharedPrefs.edit().putString("moodEntries", moodEntriesJson).apply()
    }
    
    private fun updateUI() {
        if (moodEntries.isEmpty()) {
            tvNoMoods.visibility = View.VISIBLE
            rvMoodHistory.visibility = View.GONE
            chartWeeklyMood.visibility = View.GONE
        } else {
            tvNoMoods.visibility = View.GONE
            rvMoodHistory.visibility = View.VISIBLE
            chartWeeklyMood.visibility = View.VISIBLE
            moodHistoryAdapter.notifyDataSetChanged()
            updateMoodChart()
        }
    }
    
    private fun setupMoodChart() {
        chartWeeklyMood.apply {
            description.isEnabled = false
            legend.isEnabled = true
            
            // Enable touch gestures
            setTouchEnabled(true)
            setScaleEnabled(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            
            // Set animation
            animateY(1000)
            
            // Configure X axis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            
            // Configure Y axis
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 6f
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false
            
            // Set Y axis label formatter to show mood labels instead of numbers
            axisLeft.valueFormatter = MoodValueFormatter()
            
            // Set empty data initially
            data = BarData()
        }
    }
    
    private fun updateMoodChart() {
        // Get the past week's dates
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Today is the last day of our week
        val endDate = calendar.timeInMillis
        
        // Go back 6 days to get start of week
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = calendar.timeInMillis
        
        // Create a list for the past 7 days (including today)
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayLabels = ArrayList<String>(7)
        val entries = ArrayList<BarEntry>()
        
        // Create a map of date -> mood value for the week
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val weekMoods = mutableMapOf<String, MutableList<Int>>()
        
        // Initialize the map with all 7 days
        for (i in 0..6) {
            calendar.timeInMillis = startDate
            calendar.add(Calendar.DAY_OF_YEAR, i)
            val dateString = dateFormat.format(calendar.time)
            dayLabels.add(dateString)
            
            val fullDateString = dateFormatter.format(calendar.time)
            weekMoods[fullDateString] = mutableListOf()
        }
        
        // Fill in the mood values from entries
        for (entry in moodEntries) {
            val mood = entry.mood
            val moodValue = moodValues[mood] ?: 3 // Default to neutral (3) if mood not found
            
            if (weekMoods.containsKey(entry.date)) {
                weekMoods[entry.date]?.add(moodValue)
            }
        }
        
        // Calculate average mood for each day and add to entries
        for (i in 0..6) {
            calendar.timeInMillis = startDate
            calendar.add(Calendar.DAY_OF_YEAR, i)
            val dateStr = dateFormatter.format(calendar.time)
            val moodList = weekMoods[dateStr]
            
            // If we have moods for this day, calculate the average
            if (moodList != null && moodList.isNotEmpty()) {
                val avgMood = moodList.average().toFloat()
                entries.add(BarEntry(i.toFloat(), avgMood))
            } else {
                // Add zero value for days with no data
                entries.add(BarEntry(i.toFloat(), 0f))
            }
        }
        
        // Create a dataset from the entries
        val dataSet = BarDataSet(entries, "Weekly Mood").apply {
            // Set multiple colors for better visualization
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ContextCompat.getColor(requireContext(), R.color.colorSecondary),
                ContextCompat.getColor(requireContext(), R.color.colorAccent),
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ContextCompat.getColor(requireContext(), R.color.colorSecondary),
                ContextCompat.getColor(requireContext(), R.color.colorAccent),
                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            )
            
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.textColorPrimary)
        }
        
        // Set the X-axis labels to show days of week
        chartWeeklyMood.xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
        
        // Create BarData object with the dataset
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f  // Make bars thicker
        
        // Set the data and refresh
        chartWeeklyMood.data = barData
        chartWeeklyMood.invalidate()
    }
    
    private fun showMoodSelector() {
        val dialog = MoodSelectorDialog.newInstance(moodEmojis) { emoji ->
            addMoodEntry(emoji)
        }
        dialog.show(parentFragmentManager, "MoodSelectorDialog")
    }
    
    private fun addMoodEntry(mood: MoodEmoji) {
        val currentDate = Date()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val moodEntry = MoodEntry(
            id = System.currentTimeMillis(),
            emoji = mood.emoji,
            mood = mood.name,
            date = dateFormatter.format(currentDate),
            time = timeFormatter.format(currentDate),
            colorResId = mood.colorResId,
            note = "" // Empty note by default
        )
        
        // Show note dialog
        showNoteDialog(moodEntry)
    }
    
    private fun showNoteDialog(moodEntry: MoodEntry) {
        val dialog = MoodNoteDialog.newInstance(moodEntry) { entry, note ->
            val updatedEntry = entry.copy(note = note)
            moodEntries.add(0, updatedEntry) // Add to start of list
            saveMoodEntries()
            updateUI()
            Toast.makeText(context, "Mood recorded!", Toast.LENGTH_SHORT).show()
        }
        dialog.show(parentFragmentManager, "MoodNoteDialog")
    }
    
    // Add this function to your MoodFragment.kt
    private fun shareMoodSummary() {
        // Get the last 7 days of mood data
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.timeInMillis
        
        // Filter mood entries for the last 7 days
        val recentMoods = moodEntries.filter {
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            entryDate?.time ?: 0 >= startDate
        }
        
        // Create a summary text
        val summary = buildString {
            append("My Mood Summary (Last 7 Days) - WellnessTrack\n\n")
            
            if (recentMoods.isEmpty()) {
                append("No mood entries recorded in the last 7 days.")
            } else {
                // Group by date
                val moodsByDate = recentMoods.groupBy { it.date }
                
                moodsByDate.forEach { (date, entries) ->
                    val formattedDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!)
                    
                    append("$formattedDate:\n")
                    entries.forEach { entry ->
                        append("  ${entry.emoji} ${entry.mood} (${entry.time})")
                        if (entry.note.isNotEmpty()) {
                            append(" - \"${entry.note}\"")
                        }
                        append("\n")
                    }
                    append("\n")
                }
            }
            
            append("\nTracked with WellnessTrack app")
        }
        
        // Create the share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Summary")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        
        // Create chooser and start activity
        val chooser = Intent.createChooser(shareIntent, "Share Mood Summary via")
        startActivity(chooser)
    }
    
    // Inner class for mapping mood values to text labels on Y axis
    inner class MoodValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return when (value.toInt()) {
                1 -> "Angry"
                2 -> "Sad"
                3 -> "Neutral"
                4 -> "Content"
                5 -> "Happy"
                else -> ""
            }
        }
    }
    
    // Inner class for holding mood emoji data
    data class MoodEmoji(val emoji: String, val name: String, val colorResId: Int)
}