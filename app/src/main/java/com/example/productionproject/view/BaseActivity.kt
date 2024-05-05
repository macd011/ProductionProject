package com.example.productionproject.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.productionproject.R
import com.example.productionproject.viewmodel.MainActivity

abstract class BaseActivity : AppCompatActivity() {
    protected abstract fun getContentViewId(): Int
    protected abstract fun getNavigationMenuItemId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentViewId())

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            navigateTo(item)
            true
        }
        bottomNavigationView.selectedItemId = getNavigationMenuItemId()
    }

    private fun navigateTo(item: MenuItem) {
        if (item.itemId == getNavigationMenuItemId()) {
            return // This prevents recreation of the current activity.
        }

        when (item.itemId) {
            R.id.navigation_home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.navigation_cardio -> startActivity(Intent(this, CardioActivity::class.java))
            R.id.navigation_strength -> startActivity(Intent(this, StrengthActivity::class.java))
            R.id.navigation_macro -> startActivity(Intent(this, MacroActivity::class.java))
        }
        finish() // This line ensures that the current activity is closed, so the user navigates only forward.
    }
}
