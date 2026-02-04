package com.example.wellnesstrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstrack.R
import com.example.wellnesstrack.adapters.IconAdapter

class IconSelectorDialogFragment : DialogFragment() {

    private var onIconSelectedListener: ((String) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_icon_selector, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set dialog size
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set up RecyclerView with icons
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvIcons)
        recyclerView.layoutManager = GridLayoutManager(context, 5)
        
        val iconAdapter = IconAdapter(getHabitIcons()) { icon ->
            onIconSelectedListener?.invoke(icon)
            dismiss()
        }
        recyclerView.adapter = iconAdapter
    }
    
    private fun getHabitIcons(): List<String> {
        // Return a list of emoji for habit icons
        return listOf(
            "📝", "💧", "🏋️", "🧘", "🚶", "🍎", "🥦", "😴", 
            "📚", "💊", "🧠", "🚭", "🏃", "🚴", "🏊", "💪", 
            "⏰", "🍵", "☕", "🥤", "🧹", "🛌", "📱", "🖥️",
            "🧩", "🎯", "🎨", "🎭", "🎮", "🎧", "🌱", "☀️"
        )
    }
    
    companion object {
        fun newInstance(listener: (String) -> Unit): IconSelectorDialogFragment {
            val dialog = IconSelectorDialogFragment()
            dialog.onIconSelectedListener = listener
            return dialog
        }
    }
}