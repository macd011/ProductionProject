package com.example.productionproject.viewmodel

import android.os.Bundle
import android.widget.TextView
import com.example.productionproject.R
import com.example.productionproject.view.BaseActivity
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun getContentViewId(): Int = R.layout.activity_main
    override fun getNavigationMenuItemId(): Int = R.id.navigation_diary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentDateTextView: TextView = findViewById(R.id.current_date)
        val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        currentDateTextView.text = currentDate
    }
}
