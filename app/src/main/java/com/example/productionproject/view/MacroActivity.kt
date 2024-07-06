package com.example.productionproject.view

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import com.example.productionproject.R
import com.example.productionproject.data.MacroCalculator
import com.example.productionproject.databinding.ActivityMacroBinding
import com.google.android.material.navigation.NavigationView
import kotlin.math.roundToInt

class MacroActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMacroBinding

    override fun getContentViewId(): Int = R.layout.activity_macro
    override fun getNavigationMenuItemId(): Int = R.id.navigation_macro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMacroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("MacroActivity", "onCreate called")

        // Navigation drawer setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        setupCustomToolbar(toolbar, toggle)

        initializeSpinners()
        setupButtonListener()
    }

    override fun onResume() {
        super.onResume()
        highlightNavigationItem(findViewById(R.id.nav_view))
    }

    private fun initializeSpinners() {
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
            R.array.activity_level_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerActivityLevel.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.goal_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGoal.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.unit_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerUnits.adapter = adapter
        }
    }

    private fun setupButtonListener() {
        binding.buttonCalculate.setOnClickListener {
            try {
                val gender = binding.spinnerGender.selectedItem.toString()
                val units = binding.spinnerUnits.selectedItem.toString()
                val weight =
                    convertWeight(binding.editTextWeight.text.toString().toDoubleOrNull(), units)
                val height =
                    convertHeight(binding.editTextHeight.text.toString().toDoubleOrNull(), units)
                val age = binding.editTextAge.text.toString().toIntOrNull()
                    ?: throw NumberFormatException("Invalid age input")
                val activityLevel =
                    getActivityLevel(binding.spinnerActivityLevel.selectedItem.toString())

                val calculator = MacroCalculator()
                val tdee = calculator.calculateTDEE(gender, weight, height, age, activityLevel)
                val goal = binding.spinnerGoal.selectedItem.toString()
                val adjustedCalories = calculator.adjustCaloriesForGoal(tdee, goal)
                val macros = calculator.calculateMacros(adjustedCalories, 45.0, 30.0, 25.0)

                binding.textViewCalories.text = "TDEE: ${tdee.roundToInt()} kcal"
                binding.textViewTargetCalories.text = "Target Calories: ${adjustedCalories.roundToInt()} kcal"
                binding.textViewCarbs.text = "Carbs: ${macros.first.roundToInt()} g"
                binding.textViewProtein.text = "Protein: ${macros.second.roundToInt()} g"
                binding.textViewFat.text = "Fat: ${macros.third.roundToInt()} g"
                binding.textViewBMI.text = "BMI: ${(weight / ((height / 100) * (height / 100))).roundToInt()}"
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Please ensure all fields are filled correctly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun convertWeight(weight: Double?, units: String): Double {
        return if (units == "Imperial (lbs, in)") {
            weight?.times(0.453592) ?: 0.0 // Convert pounds to kilograms
        } else {
            weight ?: 0.0 // Assume metric (kg) is entered
        }
    }

    private fun convertHeight(height: Double?, units: String): Double {
        return if (units == "Imperial (lbs, in)") {
            height?.times(2.54) ?: 0.0 // Convert inches to centimeters
        } else {
            height ?: 0.0 // Assume metric (cm) is entered
        }
    }

    private fun getActivityLevel(activityLevel: String): Double {
        return when (activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            "Extra Active" -> 1.9
            else -> 1.2
        }
    }
}
