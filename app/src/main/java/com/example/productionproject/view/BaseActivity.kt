package com.example.productionproject.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.productionproject.R
import com.example.productionproject.viewmodel.MainActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    protected abstract fun getContentViewId(): Int
    protected abstract fun getNavigationMenuItemId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BaseActivity", "onCreate called in ${this::class.java.simpleName}")
        setContentView(getContentViewId())

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        highlightNavigationItem(navView)
    }

    override fun onResume() {
        super.onResume()
        Log.d("BaseActivity", "onResume called in ${this::class.java.simpleName}")
        highlightNavigationItem(findViewById(R.id.nav_view))
    }

    protected open fun highlightNavigationItem(navView: NavigationView) {
        val navigationItemId = getNavigationMenuItemId()
        Log.d("BaseActivity", "Highlighting navigation item: $navigationItemId in ${this::class.java.simpleName}")
        navView.setCheckedItem(navigationItemId)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val activityClass = when (item.itemId) {
            R.id.navigation_profile -> ProfileActivity::class.java
            R.id.navigation_macro -> MacroActivity::class.java
            R.id.navigation_strength -> StrengthActivity::class.java
            R.id.navigation_cardio -> CardioActivity::class.java
            R.id.navigation_diary -> MainActivity::class.java
            R.id.navigation_logout -> {
                showLogoutConfirmationDialog()
                return true
            }
            else -> null
        }

        activityClass?.let {
            startActivity(Intent(this, it))
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    protected fun setupToolbarAndNavigation(toolbar: Toolbar) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }
}
