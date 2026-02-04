package com.example.wellnesstrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.example.wellnesstrack.activities.MainActivity
import com.example.wellnesstrack.R
import com.example.wellnesstrack.utils.PreferenceManager

class HomeFragment : Fragment() {

    private lateinit var tvWelcomeMessage: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage)
        
        // Get current user
        val preferenceManager = PreferenceManager(requireContext())
        val currentUser = preferenceManager.getCurrentUser()
        
        // Update welcome message
        if (currentUser != null) {
            tvWelcomeMessage.text = "Welcome, ${currentUser.username}!"
        }
        
        // Set up card click listeners
        view.findViewById<CardView>(R.id.cardHydration).setOnClickListener {
            navigateToHydration()
        }
        
        view.findViewById<CardView>(R.id.cardMood).setOnClickListener {
            navigateToMood()
        }

        view.findViewById<CardView>(R.id.cardHabits).setOnClickListener {
    navigateToHabits()
}
        
//        view.findViewById<CardView>(R.id.cardSettings).setOnClickListener {
//            // Navigate to settings section
//        }
    }
    
    private fun navigateToHydration() {
        (activity as? MainActivity)?.navigateToTab(R.id.navigation_hydration)
    }

    private fun navigateToMood() {
        (activity as? MainActivity)?.navigateToTab(R.id.navigation_mood)
    }

    private fun navigateToHabits() {
    (activity as? MainActivity)?.navigateToTab(R.id.navigation_habits)
}
}