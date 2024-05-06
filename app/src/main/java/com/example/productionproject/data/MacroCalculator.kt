package com.example.productionproject.data

import kotlin.math.roundToInt

class MacroCalculator {
    fun calculateTDEE(gender: String, weight: Double, height: Double, age: Int, activityLevel: Double): Double {
        val ree = if (gender == "Male") {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }
        return ree * activityLevel
    }

    fun adjustCaloriesForGoal(tdee: Double, goal: String): Double {
        return when (goal) {
            "cutting" -> tdee - 500
            "bulking" -> tdee + 500
            else -> tdee // maintaining
        }
    }

    fun calculateMacros(calories: Double, carbRatio: Double, proteinRatio: Double, fatRatio: Double): Triple<Double, Double, Double> {
        val carbCalories = calories * carbRatio / 100
        val proteinCalories = calories * proteinRatio / 100
        val fatCalories = calories * fatRatio / 100

        val carbGrams = carbCalories / 4
        val proteinGrams = proteinCalories / 4
        val fatGrams = fatCalories / 9

        return Triple(carbGrams.roundToInt().toDouble(), proteinGrams.roundToInt().toDouble(), fatGrams.roundToInt().toDouble())
    }
}
