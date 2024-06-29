package com.example.productionproject.viewmodel

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.productionproject.R
import com.example.productionproject.view.BaseActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun getContentViewId(): Int = R.layout.activity_main
    override fun getNavigationMenuItemId(): Int = R.id.navigation_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
    }
}
