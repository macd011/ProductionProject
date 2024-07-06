package com.example.productionproject.viewmodel

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productionproject.R
import com.example.productionproject.data.TrainingDay
import com.example.productionproject.data.TrainingDayManager
import com.example.productionproject.data.Workout
import com.example.productionproject.view.BaseActivity
import com.example.productionproject.view.WorkoutAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var trainingDayManager: TrainingDayManager
    private lateinit var workoutAdapter: WorkoutAdapter
    private val workouts = mutableListOf<Workout>()
    private val trainingDays = mutableListOf<TrainingDay>()
    private var userId: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun getContentViewId(): Int = R.layout.activity_main
    override fun getNavigationMenuItemId(): Int = R.id.navigation_diary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentDateTextView: TextView = findViewById(R.id.current_date)
        val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        currentDateTextView.text = currentDate

        val calendarView: CalendarView = findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            showPopup(year, month, dayOfMonth)
        }

        val logWorkoutButton: Button = findViewById(R.id.btnLogWorkout)
        logWorkoutButton.setOnClickListener {
            val today = Date()
            val calendar = java.util.Calendar.getInstance()
            calendar.time = today
            showPopup(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
        }

        trainingDayManager = TrainingDayManager()
        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrEmpty()) {
            // Handle user not logged in
            return
        }
    }

    private fun showPopup(year: Int, month: Int, dayOfMonth: Int) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_calendar_logging, null)

        val titleTextView: TextView = view.findViewById(R.id.tvPopupTitle)
        val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            .format(Date(year - 1900, month, dayOfMonth))
        titleTextView.text = "Training Session Details for $date"

        val editTextNotes: EditText = view.findViewById(R.id.editTextNotes)
        val spinnerTrainingDays: Spinner = view.findViewById(R.id.spinnerTrainingDays)
        val rvWorkouts: RecyclerView = view.findViewById(R.id.rvWorkouts)
        val buttonSave: Button = view.findViewById(R.id.buttonSave)
        val buttonClear: Button = view.findViewById(R.id.buttonClear)

        rvWorkouts.layoutManager = LinearLayoutManager(this)
        workoutAdapter = WorkoutAdapter(workouts, object : WorkoutAdapter.InteractionListener {
            override fun onItemSelected(position: Int) {
                // No action needed for now
            }
        })
        rvWorkouts.adapter = workoutAdapter

        // Add "None Selected" option at the beginning
        val noneSelected = TrainingDay(id = "", userId = "", name = "None Selected")
        trainingDays.add(0, noneSelected)

        loadTrainingDays { days ->
            trainingDays.clear()
            trainingDays.add(noneSelected)
            trainingDays.addAll(days)
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainingDays)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTrainingDays.adapter = adapter

            spinnerTrainingDays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val selectedDay = trainingDays[position]
                    if (selectedDay.id.isNotEmpty()) {
                        loadWorkouts(selectedDay.id)
                    } else {
                        workouts.clear()
                        workoutAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // No action needed
                }
            }

            // Load existing session data if it exists
            loadSessionData(date) { sessionData ->
                editTextNotes.setText(sessionData["notes"] ?: "")
                val trainingDayId = sessionData["trainingDayId"]
                if (!trainingDayId.isNullOrEmpty()) {
                    val selectedIndex = trainingDays.indexOfFirst { it.id == trainingDayId }
                    if (selectedIndex >= 0) {
                        spinnerTrainingDays.setSelection(selectedIndex)
                        loadWorkouts(trainingDayId)
                    }
                }
            }
        }

        buttonSave.setOnClickListener {
            val selectedDay = spinnerTrainingDays.selectedItem as TrainingDay
            val notes = editTextNotes.text.toString()
            saveSession(date, selectedDay.id, notes)
            editTextNotes.text.clear()
            dialog.dismiss()
        }

        buttonClear.setOnClickListener {
            editTextNotes.text.clear()
            spinnerTrainingDays.setSelection(0)
            workouts.clear()
            workoutAdapter.notifyDataSetChanged()
        }

        dialog.setContentView(view)
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.85).toInt()
        dialog.window?.setLayout(width, height)
        dialog.show()
    }

    private fun loadTrainingDays(callback: (List<TrainingDay>) -> Unit) {
        userId?.let { uid ->
            trainingDayManager.loadTrainingDays(uid) { days ->
                callback(days)
            }
        }
    }

    private fun loadWorkouts(trainingDayId: String) {
        userId?.let { uid ->
            trainingDayManager.loadWorkoutsForDay(uid, trainingDayId) { workouts, _ ->
                this.workouts.clear()
                if (workouts != null) {
                    this.workouts.addAll(workouts)
                }
                workoutAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun saveSession(date: String, trainingDayId: String, notes: String) {
        userId?.let { uid ->
            val sessionData = hashMapOf(
                "date" to date,
                "notes" to notes,
                "trainingDayId" to trainingDayId
            )
            db.collection("Users").document(uid).collection("Sessions").document(date).set(sessionData)
                .addOnSuccessListener {
                    // Add a marker to the calendar view to indicate a saved session
                    addSessionMarker(date)
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        }
    }

    private fun loadSessionData(date: String, callback: (Map<String, String>) -> Unit) {
        userId?.let { uid ->
            db.collection("Users").document(uid).collection("Sessions").document(date).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val sessionData = mapOf(
                            "notes" to (document.getString("notes") ?: ""),
                            "trainingDayId" to (document.getString("trainingDayId") ?: "")
                        )
                        callback(sessionData)
                    } else {
                        callback(emptyMap())
                    }
                }
                .addOnFailureListener {
                    callback(emptyMap())
                }
        }
    }

    private fun addSessionMarker(date: String) {
        // Add logic to visually mark the date on the calendar
    }
}
