package com.example.productionproject.view

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.productionproject.R
import com.google.android.material.navigation.NavigationView

class ProfileActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun getContentViewId(): Int = R.layout.activity_profile
    override fun getNavigationMenuItemId(): Int = R.id.navigation_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        setupToolbarAndNavigation(toolbar)
    }

    override fun onResume() {
        super.onResume()
        highlightNavigationItem(findViewById(R.id.nav_view))
    }
}
