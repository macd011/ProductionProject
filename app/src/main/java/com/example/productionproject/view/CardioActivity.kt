package com.example.productionproject.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.productionproject.R
import com.example.productionproject.databinding.ActivityCardioBinding
import kotlin.math.roundToInt

class CardioActivity : BaseActivity() {

    private lateinit var binding: ActivityCardioBinding

    override fun getContentViewId(): Int = R.layout.activity_cardio
    override fun getNavigationMenuItemId(): Int = R.id.navigation_cardio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("CardioActivity", "onCreate called")

        // Navigation drawer setup
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        setupCustomToolbar(toolbar, toggle)

        initializeViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        highlightNavigationItem(findViewById(R.id.nav_view))
        Log.d("CardioActivity", "onResume called")
    }

    private fun initializeViews() {
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGender.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.zone_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerZone.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.unit_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerUnits.adapter = adapter
        }

        binding.spinnerUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateHintsBasedOnUnits(binding.spinnerUnits.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupListeners() {
        binding.buttonCalculate.setOnClickListener {
            calculatePace()
        }
    }

    private fun calculatePace() {
        val weight = binding.editTextWeight.text.toString().toDoubleOrNull()
        val height = binding.editTextHeight.text.toString().toDoubleOrNull()
        val age = binding.editTextAge.text.toString().toIntOrNull()

        if (weight == null || height == null || age == null) {
            Toast.makeText(this, "Please enter valid values for weight, height, and age.", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = binding.spinnerGender.selectedItem.toString()
        val zone = binding.spinnerZone.selectedItemPosition

        val mhr = calculateHeartRate(age)
        val suggestedPace = calculateSuggestedPace(mhr, zone)
        binding.textViewResult.text = "Suggested Pace: $suggestedPace"
    }

    private fun updateHintsBasedOnUnits(units: String) {
        if (units == "Imperial (lbs, in)") {
            binding.editTextHeight.hint = "Height (in)"
            binding.editTextWeight.hint = "Weight (lbs)"
        } else {
            binding.editTextHeight.hint = "Height (cm)"
            binding.editTextWeight.hint = "Weight (kg)"
        }
    }

    private fun calculateHeartRate(age: Int): Int {
        return 220 - age
    }

    private fun calculateSuggestedPace(mhr: Int, zone: Int): String {
        val lowerBound = mhr * when (zone) {
            0 -> 0.50
            1 -> 0.60
            2 -> 0.70
            3 -> 0.80
            4 -> 0.90
            else -> 0.50
        }
        val upperBound = mhr * when (zone) {
            0 -> 0.60
            1 -> 0.70
            2 -> 0.80
            3 -> 0.90
            4 -> 1.00
            else -> 0.60
        }
        return "Zone $zone: ${lowerBound.roundToInt()} - ${upperBound.roundToInt()} bpm"
    }
}
