package com.example.wellnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var reminderSwitch: Switch
    private lateinit var intervalSeekBar: SeekBar
    private lateinit var intervalText: TextView
    private lateinit var waterGoalSeekBar: SeekBar
    private lateinit var waterGoalText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        dataManager = DataManager(requireContext())

        // Initialize views
        reminderSwitch = view.findViewById(R.id.reminder_switch)
        intervalSeekBar = view.findViewById(R.id.interval_seekbar)
        intervalText = view.findViewById(R.id.interval_text)
        waterGoalSeekBar = view.findViewById(R.id.water_goal_seekbar)
        waterGoalText = view.findViewById(R.id.water_goal_text)

        // Load saved settings
        loadSettings()

        // Set up reminder switch
        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            dataManager.saveReminderEnabled(isChecked)
            if (isChecked) {
                val interval = dataManager.getReminderInterval()
                WaterReminderWorker.scheduleReminder(requireContext(), interval.toLong())
                Toast.makeText(requireContext(), "💧 Reminders enabled!", Toast.LENGTH_SHORT).show()
            } else {
                WaterReminderWorker.cancelReminder(requireContext())
                Toast.makeText(requireContext(), "Reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up interval seekbar (30 min to 4 hours = 240 min)
        intervalSeekBar.max = 210 // 240 - 30
        intervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = progress + 30
                updateIntervalText(minutes)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val minutes = (seekBar?.progress ?: 0) + 30
                dataManager.saveReminderInterval(minutes)

                // Reschedule if enabled
                if (reminderSwitch.isChecked) {
                    WaterReminderWorker.scheduleReminder(requireContext(), minutes.toLong())
                    Toast.makeText(requireContext(), "Interval updated to ${formatInterval(minutes)}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Set up water goal seekbar (4 to 16 glasses)
        waterGoalSeekBar.max = 12 // 16 - 4
        waterGoalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val glasses = progress + 4
                waterGoalText.text = "Daily Water Goal: $glasses glasses"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val glasses = (seekBar?.progress ?: 0) + 4
                dataManager.saveDailyWaterGoal(glasses)
                Toast.makeText(requireContext(), "Goal set to $glasses glasses! 🎯", Toast.LENGTH_SHORT).show()
            }
        })

        return view
    }

    private fun loadSettings() {
        // Load reminder settings
        reminderSwitch.isChecked = dataManager.isReminderEnabled()

        val interval = dataManager.getReminderInterval()
        intervalSeekBar.progress = interval - 30
        updateIntervalText(interval)

        val waterGoal = dataManager.getDailyWaterGoal()
        waterGoalSeekBar.progress = waterGoal - 4
        waterGoalText.text = "Daily Water Goal: $waterGoal glasses"
    }

    private fun updateIntervalText(minutes: Int) {
        intervalText.text = "Reminder Interval: ${formatInterval(minutes)}"
    }

    private fun formatInterval(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60

        return when {
            hours > 0 && mins > 0 -> "Every $hours hour $mins minutes"
            hours > 0 -> "Every $hours hour${if (hours > 1) "s" else ""}"
            else -> "Every $mins minutes"
        }
    }
}