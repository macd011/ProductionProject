package com.example.productionproject.view

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.productionproject.R

class CardioActivity : BaseActivity() {

    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var editTextAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerZone: Spinner
    private lateinit var buttonCalculate: Button
    private lateinit var textViewResult: TextView

    override fun getContentViewId(): Int = R.layout.activity_cardio
    override fun getNavigationMenuItemId(): Int = R.id.navigation_cardio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardio)
        initializeViews()
        setupListeners()
        Log.d("CardioActivity", "onCreate called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("CardioActivity", "onResume called")
    }

    private fun initializeViews() {
        editTextWeight = findViewById(R.id.editTextWeight)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextAge = findViewById(R.id.editTextAge)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerZone = findViewById(R.id.spinnerZone)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        textViewResult = findViewById(R.id.textViewResult)

        ArrayAdapter.createFromResource(
            this,
            R.array.zone_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerZone.adapter = adapter
        }
    }

    private fun setupListeners() {
        buttonCalculate.setOnClickListener {
            calculatePace()
        }
    }

    private fun calculatePace() {
        val weight = editTextWeight.text.toString().toDoubleOrNull()
        val height = editTextHeight.text.toString().toDoubleOrNull()
        val age = editTextAge.text.toString().toIntOrNull()

        if (weight == null || height == null || age == null) {
            Toast.makeText(this, "Please enter valid values for weight, height, and age.", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = spinnerGender.selectedItem.toString()
        val zone = spinnerZone.selectedItemPosition

        val mhr = calculateHeartRate(age)
        val suggestedPace = calculateSuggestedPace(mhr, zone)
        textViewResult.text = "Suggested Pace: $suggestedPace"
    }

    private fun calculateHeartRate(age: Int): Int {
        return 220 - age
    }

    private fun calculateSuggestedPace(mhr: Int, zone: Int): String {
        val lowerBound = mhr * when(zone) {
            0 -> 0.50
            1 -> 0.60
            2 -> 0.70
            3 -> 0.80
            4 -> 0.90
            else -> 0.50
        }
        val upperBound = mhr * when(zone) {
            0 -> 0.60
            1 -> 0.70
            2 -> 0.80
            3 -> 0.90
            4 -> 1.00
            else -> 0.60
        }
        return "Zone $zone: ${lowerBound.toInt()} - ${upperBound.toInt()} bpm"
    }
}
