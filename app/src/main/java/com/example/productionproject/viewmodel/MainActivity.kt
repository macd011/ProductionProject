package com.example.productionproject.viewmodel

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import com.example.productionproject.R
import com.example.productionproject.view.BaseActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : BaseActivity() {
    override fun getContentViewId(): Int {
        return R.layout.activity_main
    }

    override fun getNavigationMenuItemId(): Int {
        return R.id.navigation_home
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpWidgets()
    }

    private fun setUpWidgets() {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        gridLayout.columnCount = 2  // Maximum number of columns

        // Create six widgets dynamically
        val widgetConfigs = listOf(2, 1, 2) // Represents the number of cards in each row
        var index = 0
        widgetConfigs.forEach { count ->
            for (i in 0 until count) {
                val layoutParams = createLayoutParams(index, count)
                val cardView = createCardWithButton(index, count)
                gridLayout.addView(cardView, layoutParams)
                index++
            }
        }
    }

    private fun createLayoutParams(index: Int, count: Int): GridLayout.LayoutParams {
        return GridLayout.LayoutParams(
            GridLayout.spec(index / 2, 1f),
            GridLayout.spec(index % 2, if (count == 2) 1f else 2f)
        ).apply {
            width = 0  // MATCH_PARENT behavior within each grid column
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setMargins(8, 8, 8, 8)
        }
    }

    private fun createCardWithButton(index: Int, count: Int): MaterialCardView {
        val cardView = MaterialCardView(this)
        cardView.cardElevation = 8f
        cardView.radius = 16f
        cardView.strokeWidth = 2  // Updated for thicker borders
        cardView.strokeColor = getColor(R.color.design_default_color_primary)
        cardView.setBackgroundResource(R.drawable.widget_gradient)  // Set gradient background

        val button = Button(this)
        button.layoutParams = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.MATCH_PARENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(index % 2, if (count == 2) 1f else 2f)
        }
        button.text = getTitleForButton(index)
        button.background = null  // Allow card background to show through

        cardView.addView(button)
        return cardView
    }

    private fun getTitleForButton(index: Int): String = when (index) {
        0 -> "Progress Tracker"
        1 -> "TDEE Calculator"
        2 -> "Training Planner"
        3 -> "Settings"
        4 -> "New Widget 1"
        5 -> "New Widget 2"
        else -> "Extra Widget"
    }
}
