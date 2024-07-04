package com.example.productionproject.view

import android.os.Bundle
import com.example.productionproject.R
import com.google.android.material.navigation.NavigationView

class ProfileActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun getContentViewId(): Int = R.layout.activity_profile
    override fun getNavigationMenuItemId(): Int = R.id.navigation_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
