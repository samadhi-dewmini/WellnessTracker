package com.example.wellnesstracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MoodFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var moods: MutableList<MoodEntry>
    private lateinit var adapter: MoodAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var moodCountText: TextView

    private val emojiList = listOf("😊", "😃", "😢", "😡", "😰", "😴", "🤗", "😎", "🤔", "😷")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        dataManager = DataManager(requireContext())
        moods = dataManager.loadMoods()

        // Initialize views
        recyclerView = view.findViewById(R.id.moods_recycler)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        moodCountText = view.findViewById(R.id.mood_count_text)

        // Set up RecyclerView
        adapter = MoodAdapter(moods,
            onDelete = { mood -> deleteMood(mood) }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Add mood button
        view.findViewById<Button>(R.id.add_mood_btn).setOnClickListener {
            showMoodDialog()
        }

        // Share button
        view.findViewById<Button>(R.id.share_mood_btn).setOnClickListener {
            shareMoodSummary()
        }

        updateUI()

        return view
    }

    private fun showMoodDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_mood, null)

        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.emoji_grid)
        val noteInput = dialogView.findViewById<EditText>(R.id.mood_note_input)
        var selectedEmoji = emojiList[0]

        // Create emoji buttons
        emojiList.forEachIndexed { index, emoji ->
            val button = Button(requireContext()).apply {
                text = emoji
                textSize = 28f
                setPadding(16, 16, 16, 16)
                alpha = if (index == 0) 1f else 0.5f // First one selected by default

                setOnClickListener {
                    selectedEmoji = emoji
                    // Highlight selected
                    for (i in 0 until emojiGrid.childCount) {
                        (emojiGrid.getChildAt(i) as? Button)?.alpha = 0.5f
                    }
                    alpha = 1f
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }

            emojiGrid.addView(button, params)
        }

        builder.setView(dialogView)
            .setTitle("How are you feeling?")
            .setPositiveButton("Save") { _, _ ->
                val note = noteInput.text.toString().trim()
                val mood = MoodEntry(
                    emoji = selectedEmoji,
                    note = note
                )
                moods.add(0, mood) // Add to beginning
                dataManager.saveMoods(moods)
                adapter.notifyDataSetChanged()
                updateUI()
                Toast.makeText(requireContext(), "Mood saved! $selectedEmoji", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMood(mood: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                moods.remove(mood)
                dataManager.saveMoods(moods)
                adapter.notifyDataSetChanged()
                updateUI()
                Toast.makeText(requireContext(), "Mood entry deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    //Sharing moods (Implicit Intent)
    private fun shareMoodSummary() {
        if (moods.isEmpty()) {
            Toast.makeText(requireContext(), "No moods to share yet", Toast.LENGTH_SHORT).show()
            return
        }

        val summary = buildString {
            append("📔 My Mood Journal\n")
            append("═══════════════════\n\n")

            moods.take(10).forEach { mood ->
                append("${mood.emoji} ${mood.getFormattedDate()}\n")
                if (mood.note.isNotEmpty()) {
                    append("   \"${mood.note}\"\n")
                }
                append("\n")
            }

            append("═══════════════════\n")
            append("Tracked with Wellness Tracker 💪")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Journal")
            putExtra(Intent.EXTRA_TEXT, summary)
        }

        startActivity(Intent.createChooser(shareIntent, "Share your mood journal"))
    }

    private fun updateUI() {
        val count = moods.size
        moodCountText.text = if (count == 1) "1 entry" else "$count entries"

        if (moods.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}

// RecyclerView Adapter
class MoodAdapter(
    private val moods: List<MoodEntry>,
    private val onDelete: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emojiText: TextView = view.findViewById(R.id.mood_emoji)
        val dateText: TextView = view.findViewById(R.id.mood_date)
        val noteText: TextView = view.findViewById(R.id.mood_note)
        val deleteBtn: ImageButton = view.findViewById(R.id.delete_mood_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]

        holder.emojiText.text = mood.emoji
        holder.dateText.text = mood.getFormattedDate()
        holder.noteText.text = mood.note
        holder.noteText.visibility = if (mood.note.isEmpty()) View.GONE else View.VISIBLE

        holder.deleteBtn.setOnClickListener { onDelete(mood) }
    }

    override fun getItemCount() = moods.size
}