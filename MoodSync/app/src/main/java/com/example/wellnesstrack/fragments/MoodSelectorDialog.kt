package com.example.wellnesstrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstrack.R
import com.example.wellnesstrack.fragments.MoodFragment.MoodEmoji

class MoodSelectorDialog : DialogFragment() {

    private lateinit var onMoodSelectedListener: (MoodEmoji) -> Unit
    private lateinit var moods: List<MoodEmoji>
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_mood_selector, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set dialog size
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Setup RecyclerView
        val rvMoodSelector = view.findViewById<RecyclerView>(R.id.rvMoodSelector)
        rvMoodSelector.layoutManager = GridLayoutManager(context, 3)
        rvMoodSelector.adapter = MoodSelectorAdapter(moods) { mood ->
            onMoodSelectedListener(mood)
            dismiss()
        }
    }
    
    // Adapter for mood selector grid
    inner class MoodSelectorAdapter(
        private val moods: List<MoodEmoji>,
        private val onMoodClick: (MoodEmoji) -> Unit
    ) : RecyclerView.Adapter<MoodSelectorAdapter.MoodViewHolder>() {
        
        inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
            val tvMoodName: TextView = itemView.findViewById(R.id.tvMoodName)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mood_selector, parent, false)
            return MoodViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
            val mood = moods[position]
            holder.tvEmoji.text = mood.emoji
            holder.tvMoodName.text = mood.name
            
            holder.itemView.setOnClickListener {
                onMoodClick(mood)
            }
        }
        
        override fun getItemCount() = moods.size
    }
    
    companion object {
        fun newInstance(moods: List<MoodEmoji>, listener: (MoodEmoji) -> Unit): MoodSelectorDialog {
            return MoodSelectorDialog().apply {
                this.moods = moods
                this.onMoodSelectedListener = listener
            }
        }
    }
}