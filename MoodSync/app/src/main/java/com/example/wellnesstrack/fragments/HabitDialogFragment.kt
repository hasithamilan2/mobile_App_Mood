package com.example.wellnesstrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.wellnesstrack.R
import com.example.wellnesstrack.models.Habit
import com.google.android.material.card.MaterialCardView

class HabitDialogFragment : DialogFragment() {
    
    interface HabitDialogListener {
        fun onHabitSaved(habit: Habit)
        fun onCancel()
    }
    
    private var listener: HabitDialogListener? = null
    private var habit: Habit? = null
    private var isEditMode = false
    
    // UI components
    private lateinit var etHabitName: EditText
    private lateinit var etHabitGoal: EditText
    private lateinit var spinnerUnit: Spinner
    private lateinit var tvSelectedIcon: TextView
    private lateinit var cardIcon: MaterialCardView
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_simple_habit, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set dialog width
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Initialize UI components
        etHabitName = view.findViewById(R.id.etHabitName)
        etHabitGoal = view.findViewById(R.id.etHabitGoal)
        spinnerUnit = view.findViewById(R.id.spinnerUnit)

        
        // Set up unit spinner
        setupUnitSpinner()
        
        // Set dialog title
        view.findViewById<TextView>(R.id.tvDialogTitle).text = 
            if (isEditMode) getString(R.string.edit_habit) else getString(R.string.add_habit)
        
        // Set up icon selection
        cardIcon.setOnClickListener {
            showIconSelector()
        }
        
        // Populate fields if in edit mode
        if (isEditMode && habit != null) {
            populateFields()
        }
        
        // Set up buttons
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            listener?.onCancel()
            dismiss()
        }
        
        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            if (validateInputs()) {
                saveHabit()
            }
        }
    }
    
    private fun setupUnitSpinner() {
        val units = arrayOf("times", "minutes", "steps", "glasses", "pages", "custom")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = adapter
    }
    
    private fun populateFields() {
        habit?.let { h ->
            etHabitName.setText(h.name)
            etHabitGoal.setText(h.goal.toString())
            tvSelectedIcon.text = h.icon
            
            // Set unit spinner selection
            val unitPosition = when (h.unit) {
                "times" -> 0
                "minutes" -> 1
                "steps" -> 2
                "glasses" -> 3
                "pages" -> 4
                else -> 5 // custom
            }
            spinnerUnit.setSelection(unitPosition)
        }
    }
    
    private fun validateInputs(): Boolean {
        if (etHabitName.text.toString().trim().isEmpty()) {
            etHabitName.error = "Name cannot be empty"
            return false
        }
        
        if (etHabitGoal.text.toString().trim().isEmpty() || 
            etHabitGoal.text.toString().toIntOrNull() ?: 0 <= 0) {
            etHabitGoal.error = "Goal must be a positive number"
            return false
        }
        
        return true
    }
    
    private fun saveHabit() {
        val name = etHabitName.text.toString().trim()
        val goal = etHabitGoal.text.toString().toIntOrNull() ?: 1
        val unit = spinnerUnit.selectedItem.toString()
        val icon = tvSelectedIcon.text.toString()
        
        // Create or update habit
        val updatedHabit = if (isEditMode && habit != null) {
            habit!!.copy(
                name = name,
                goal = goal,
                unit = unit,
                icon = icon
            )
        } else {
            Habit(
                name = name,
                goal = goal,
                unit = unit,
                icon = icon
            )
        }
        
        // Notify listener and dismiss
        listener?.onHabitSaved(updatedHabit)
        dismiss()
    }
    
    private fun showIconSelector() {
        val dialog = IconSelectorDialogFragment.newInstance { selectedIcon ->
            tvSelectedIcon.text = selectedIcon
        }
        dialog.show(childFragmentManager, "IconSelector")
    }
    
    fun setListener(listener: HabitDialogListener) {
        this.listener = listener
    }
    
    companion object {
        fun newInstance(existingHabit: Habit? = null): HabitDialogFragment {
            val fragment = HabitDialogFragment()
            fragment.habit = existingHabit
            fragment.isEditMode = existingHabit != null
            return fragment
        }
    }
}