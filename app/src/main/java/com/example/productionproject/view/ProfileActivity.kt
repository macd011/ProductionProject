package com.example.productionproject.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.productionproject.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var imageViewProfile: ImageView
    private lateinit var tvDaysInRow: TextView
    private lateinit var tvSessionsLogged: TextView
    private lateinit var tvWeightLogged: TextView
    private lateinit var switchNotifications: Switch
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private val storageReference = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun getContentViewId(): Int = R.layout.activity_profile
    override fun getNavigationMenuItemId(): Int = R.id.navigation_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = FirebaseAuth.getInstance().currentUser?.uid

        imageViewProfile = findViewById(R.id.imageViewProfile)
        tvDaysInRow = findViewById(R.id.tvDaysInRow)
        tvSessionsLogged = findViewById(R.id.tvSessionsLogged)
        tvWeightLogged = findViewById(R.id.tvWeightLogged)
        switchNotifications = findViewById(R.id.switchNotifications)

        imageViewProfile.setOnClickListener {
            openFileChooser()
        }

        switchNotifications.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                showNotificationFrequencyDialog()
            } else {
                disableNotifications()
            }
        }

        loadProfilePicture()
        loadUserStatistics()
        loadNotificationSettings()
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            imageUri = data.data
            uploadImage()
        }
    }

    private fun uploadImage() {
        if (imageUri != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val fileReference = storageReference.child("profilePictures/$userId")

                fileReference.putFile(imageUri!!)
                    .addOnSuccessListener {
                        fileReference.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this)
                                .load(uri)
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.ic_profile)
                                .into(imageViewProfile)
                            Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfilePicture() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val fileReference = storageReference.child("profilePictures/$userId")
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_profile)
                    .into(imageViewProfile)
            }.addOnFailureListener {
                // Set a default profile picture if none exists
                imageViewProfile.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_profile
                    )
                )
            }
        }
    }

    private fun loadUserStatistics() {
        userId?.let { uid ->
            // Calculate consecutive days
            db.collection("Users").document(uid).get().addOnSuccessListener { document ->
                val lastLoginDate = document.getString("lastLoginDate")
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val currentDate = sdf.format(Calendar.getInstance().time)
                var consecutiveDays = document.getLong("consecutiveDays") ?: 0

                if (lastLoginDate != null) {
                    val lastDate = sdf.parse(lastLoginDate)
                    val current = sdf.parse(currentDate)
                    val diff = current.time - lastDate.time
                    val daysBetween = (diff / (1000 * 60 * 60 * 24)).toInt()

                    consecutiveDays = if (daysBetween == 1) {
                        consecutiveDays + 1
                    } else if (daysBetween > 1) {
                        1
                    } else {
                        consecutiveDays
                    }

                    db.collection("Users").document(uid)
                        .update("lastLoginDate", currentDate, "consecutiveDays", consecutiveDays)
                } else {
                    db.collection("Users").document(uid)
                        .set(mapOf("lastLoginDate" to currentDate, "consecutiveDays" to 1))
                    consecutiveDays = 1
                }
                tvDaysInRow.text = "Days in a Row: $consecutiveDays"
            }

            // Count sessions logged
            db.collection("Users").document(uid).collection("Sessions").get()
                .addOnSuccessListener { documents ->
                    val sessionCount = documents.size()
                    tvSessionsLogged.text = "Sessions Logged: $sessionCount"
                }

            // Count weight entries logged
            db.collection("Users").document(uid).collection("WeightEntries").get()
                .addOnSuccessListener { documents ->
                    val weightCount = documents.size()
                    tvWeightLogged.text = "Weight Entries Logged: $weightCount"
                }
        }
    }

    private fun loadNotificationSettings() {
        userId?.let { uid ->
            db.collection("Users").document(uid).get().addOnSuccessListener { document ->
                val notificationsEnabled = document.getBoolean("notificationsEnabled") ?: false
                switchNotifications.isChecked = notificationsEnabled
            }
        }
    }

    private fun showNotificationFrequencyDialog() {
        val options = arrayOf("Every 4 days", "Every 7 days")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Notification Frequency")
        builder.setItems(options) { _, which ->
            val frequency = if (which == 0) 4 else 7
            enableNotifications(frequency)
        }
        builder.show()
    }

    private fun enableNotifications(frequency: Int) {
        userId?.let { uid ->
            db.collection("Users").document(uid)
                .update("notificationsEnabled", true, "notificationFrequency", frequency)
                .addOnSuccessListener {
                    Toast.makeText(this, "Notifications enabled every $frequency days", Toast.LENGTH_SHORT).show()
                    scheduleNotification(frequency)
                }
        }
    }

    private fun disableNotifications() {
        userId?.let { uid ->
            db.collection("Users").document(uid)
                .update("notificationsEnabled", false)
                .addOnSuccessListener {
                    Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
                    cancelNotification()
                }
        }
    }

    private fun scheduleNotification(frequency: Int) {
        // Use Firebase Cloud Messaging or WorkManager to schedule notifications
    }

    private fun cancelNotification() {
        // Use Firebase Cloud Messaging or WorkManager to cancel notifications
    }
}
