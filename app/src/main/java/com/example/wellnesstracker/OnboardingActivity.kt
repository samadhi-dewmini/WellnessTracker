package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView?>

    private val layouts = listOf(
        R.layout.onboarding_slide1,
        R.layout.onboarding_slide2,
        R.layout.onboarding_slide3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.view_pager)
        btnNext = findViewById(R.id.btn_next)
        btnSkip = findViewById(R.id.btn_skip)
        dotsLayout = findViewById(R.id.dots_layout)

        // Set up ViewPager
        val adapter = OnboardingAdapter(layouts)
        viewPager.adapter = adapter

        // Add dots
        addDots(0)

        // ViewPager page change listener
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                addDots(position)

                // Change button text on last page
                if (position == layouts.size - 1) {
                    btnNext.text = "Get Started"
                    btnSkip.visibility = View.GONE
                } else {
                    btnNext.text = "Next"
                    btnSkip.visibility = View.VISIBLE
                }
            }
        })

        // Next button click
        btnNext.setOnClickListener {
            val current = viewPager.currentItem + 1
            if (current < layouts.size) {
                viewPager.currentItem = current
            } else {
                launchMainActivity()
            }
        }

        // Skip button click
        btnSkip.setOnClickListener {
            launchMainActivity()
        }
    }

    private fun addDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size)
        dotsLayout.removeAllViews()

        for (i in dots.indices) {
            dots[i] = ImageView(this)
            dots[i]?.setImageResource(
                if (i == currentPage) R.drawable.dot_active else R.drawable.dot_inactive
            )

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            dotsLayout.addView(dots[i], params)
        }
    }

    private fun launchMainActivity() {
        // Save that onboarding is complete
        getSharedPreferences("WellnessData", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_complete", true)
            .apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

// ViewPager2 Adapter
class OnboardingAdapter(private val layouts: List<Int>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    class OnboardingViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(layouts[viewType], parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        // Content is already in the layout files
    }

    override fun getItemCount() = layouts.size

    override fun getItemViewType(position: Int) = position
}