package com.example.wellnesstracker

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HabitsFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var habits: MutableList<Habit>
    private lateinit var adapter: HabitAdapter
    private lateinit var progressText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var motivationalText: TextView
    private lateinit var dateText: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        dataManager = DataManager(requireContext())
        habits = dataManager.loadHabits()

        // Initialize views
        recyclerView = view.findViewById(R.id.habits_recycler)
        progressText = view.findViewById(R.id.progress_text)
        progressBar = view.findViewById(R.id.progress_bar)
        motivationalText = view.findViewById(R.id.motivational_text)
        dateText = view.findViewById(R.id.date_text)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)

        // Set today's date
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateText.text = dateFormat.format(Date())

        // Set up RecyclerView
        adapter = HabitAdapter(habits,
            onToggle = { habit ->
                habit.toggleCompletion()
                saveAndUpdate()
            },
            onEdit = { habit -> showEditDialog(habit) },
            onDelete = { habit -> deleteHabit(habit) }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Add button
        view.findViewById<Button>(R.id.add_habit_btn).setOnClickListener {
            showAddDialog()
        }

        updateUI()

        return view
    }

    private fun showAddDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.habit_name_input)
        val descInput = dialogView.findViewById<EditText>(R.id.habit_desc_input)

        builder.setView(dialogView)
            .setTitle("Add New Habit")
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val habit = Habit(
                        name = name,
                        description = descInput.text.toString().trim()
                    )
                    habits.add(habit)
                    saveAndUpdate()
                    Toast.makeText(requireContext(), "Habit added! 🎉", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(habit: Habit) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.habit_name_input)
        val descInput = dialogView.findViewById<EditText>(R.id.habit_desc_input)

        nameInput.setText(habit.name)
        descInput.setText(habit.description)

        builder.setView(dialogView)
            .setTitle("Edit Habit")
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    habit.name = name
                    habit.description = descInput.text.toString().trim()
                    saveAndUpdate()
                    Toast.makeText(requireContext(), "Habit updated! ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteHabit(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                habits.remove(habit)
                saveAndUpdate()
                Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveAndUpdate() {
        dataManager.saveHabits(habits)
        adapter.notifyDataSetChanged()
        updateUI()
    }

    private fun updateUI() {
        if (habits.isEmpty()) {
            // Show empty state
            progressText.text = "0/0 (0%)"
            progressBar.progress = 0
            motivationalText.text = "Start your journey today! 🚀"
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            // Show habits list
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // Calculate progress
            val completed = habits.count { it.isCompletedToday() }
            val total = habits.size
            val percentage = if (total > 0) (completed * 100) / total else 0

            // Update progress text
            progressText.text = "$completed/$total ($percentage%)"

            // Update progress bar with animation
            progressBar.progress = percentage

            // Update motivational text based on progress
            motivationalText.text = getMotivationalMessage(percentage)
        }
    }

    private fun getMotivationalMessage(percentage: Int): String {
        return when {
            percentage == 0 -> "Let's get started! You got this! 💪"
            percentage < 25 -> "Good start! Keep going! 🌱"
            percentage < 50 -> "You're making progress! 🎯"
            percentage < 75 -> "Great job! Almost there! 🔥"
            percentage < 100 -> "So close! Finish strong! 🌟"
            else -> "Perfect! All done today! 🎉"
        }
    }
}

// RecyclerView Adapter
class HabitAdapter(
    private val habits: List<Habit>,
    private val onToggle: (Habit) -> Unit,
    private val onEdit: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.habit_name)
        val descText: TextView = view.findViewById(R.id.habit_desc)
        val streakText: TextView = view.findViewById(R.id.habit_streak)
        val checkbox: CheckBox = view.findViewById(R.id.habit_checkbox)
        val editBtn: ImageButton = view.findViewById(R.id.edit_btn)
        val deleteBtn: ImageButton = view.findViewById(R.id.delete_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.nameText.text = habit.name
        holder.descText.text = habit.description
        holder.descText.visibility = if (habit.description.isEmpty()) View.GONE else View.VISIBLE

        val streak = habit.getStreak()
        holder.streakText.text = if (streak > 0) "🔥 $streak day streak" else "Start your streak!"
        holder.streakText.setTextColor(
            if (streak > 0) 0xFFFF6B00.toInt() else 0xFF999999.toInt()
        )

        holder.checkbox.isChecked = habit.isCompletedToday()

        // Add visual feedback when checked
        holder.itemView.alpha = if (habit.isCompletedToday()) 0.7f else 1.0f

        holder.checkbox.setOnClickListener { onToggle(habit) }
        holder.editBtn.setOnClickListener { onEdit(habit) }
        holder.deleteBtn.setOnClickListener { onDelete(habit) }
    }

    override fun getItemCount() = habits.size
}