package com.example.productionproject.viewmodel

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.CalendarView
import android.widget.EditText
import android.widget.LinearLayout
import com.example.productionproject.R
import com.example.productionproject.view.BaseActivity
import androidx.core.content.edit

class MainActivity : BaseActivity() {
    override fun getContentViewId(): Int = R.layout.activity_main

    override fun getNavigationMenuItemId(): Int = R.id.navigation_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val sharedPreferences = getSharedPreferences("TrainingData", MODE_PRIVATE)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = "$year-${month+1}-$dayOfMonth"
            showNoteDialog(date, sharedPreferences)
        }
    }

    private fun showNoteDialog(date: String, sharedPreferences: SharedPreferences) {
        val editText = EditText(this).apply {
            setText(sharedPreferences.getString(date, ""))
            setTextColor(Color.BLACK)
            setHint("Enter Workout Note")
            setHintTextColor(Color.GRAY)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            maxLines = 4
            isVerticalScrollBarEnabled = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Workout Note $date")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                sharedPreferences.edit {
                    putString(date, editText.text.toString())
                    apply()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}
