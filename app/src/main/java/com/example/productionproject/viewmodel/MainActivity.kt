package com.example.productionproject.viewmodel

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.productionproject.R
import com.example.productionproject.view.BaseActivity
import com.example.productionproject.view.CardioActivity
import com.example.productionproject.view.LoginActivity
import com.example.productionproject.view.MacroActivity
import com.example.productionproject.view.ProfileActivity
import com.example.productionproject.view.StrengthActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun getContentViewId(): Int = R.layout.activity_main
    override fun getNavigationMenuItemId(): Int = R.id.navigation_diary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        setupToolbarAndNavigation(toolbar)

        navView.setNavigationItemSelectedListener(this)

        try {
            val typeface = ResourcesCompat.getFont(this, R.font.proxima_nova_black)
            val textView = TextView(this)
            textView.text = "MUSCLE FLOW"
            textView.setTextColor(resources.getColor(android.R.color.black))
            textView.textSize = 24f
            textView.typeface = typeface
            textView.gravity = Gravity.CENTER_VERTICAL

            val imageView = ImageView(this)
            imageView.setImageResource(R.drawable.muscleflow)
            imageView.adjustViewBounds = true
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            val params = LinearLayout.LayoutParams(
                115, // Width
                115  // Height
            )
            params.setMargins(16, 8, 16, 8)
            imageView.layoutParams = params

            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.gravity = Gravity.CENTER_VERTICAL
            linearLayout.addView(textView)
            linearLayout.addView(imageView)

            val layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.MATCH_PARENT
            )
            layoutParams.gravity = Gravity.START
            toolbar.addView(linearLayout, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        highlightNavigationItem(findViewById(R.id.nav_view))
    }

    override fun highlightNavigationItem(navView: NavigationView) {
        val navigationItemId = R.id.navigation_diary
        navView.setCheckedItem(navigationItemId)
    }

    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
}
