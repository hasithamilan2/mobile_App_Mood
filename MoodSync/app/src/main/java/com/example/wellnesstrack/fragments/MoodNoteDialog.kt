package com.example.wellnesstrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.wellnesstrack.R
import com.example.wellnesstrack.models.MoodEntry

class MoodNoteDialog : DialogFragment() {

    private lateinit var onNoteAddedListener: (MoodEntry, String) -> Unit
    private lateinit var moodEntry: MoodEntry
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_mood_note, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set dialog size
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Display mood info
        view.findViewById<TextView>(R.id.tvMoodEmoji).text = moodEntry.emoji
        view.findViewById<TextView>(R.id.tvMoodName).text = moodEntry.mood
        
        val etNote = view.findViewById<EditText>(R.id.etMoodNote)
        
        // Set up buttons
        view.findViewById<Button>(R.id.btnSkip).setOnClickListener {
            onNoteAddedListener(moodEntry, "")
            dismiss()
        }
        
        view.findViewById<Button>(R.id.btnSaveNote).setOnClickListener {
            val note = etNote.text.toString()
            onNoteAddedListener(moodEntry, note)
            dismiss()
        }
    }
    
    companion object {
        fun newInstance(
            moodEntry: MoodEntry,
            listener: (MoodEntry, String) -> Unit
        ): MoodNoteDialog {
            return MoodNoteDialog().apply {
                this.moodEntry = moodEntry
                this.onNoteAddedListener = listener
            }
        }
    }
}