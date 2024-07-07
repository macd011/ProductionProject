package com.example.productionproject.viewmodel

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var trainingDayManager: TrainingDayManager
    private lateinit var workoutAdapter: WorkoutAdapter
    private val workouts = mutableListOf<Workout>()
    private val trainingDays = mutableListOf<TrainingDay>()
    private var userId: String? = null
    private var currentWeightEntryId: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun getContentViewId(): Int = R.layout.activity_main
    override fun getNavigationMenuItemId(): Int = R.id.navigation_diary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentDateTextView: TextView = findViewById(R.id.current_date)
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        currentDateTextView.text = currentDate

        val calendarView: CalendarView = findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            showPopup(year, month, dayOfMonth)
        }

        val logWeightButton: Button = findViewById(R.id.btnLogWeight)
        logWeightButton.setOnClickListener {
            showWeightPopup()
        }

        val logWorkoutButton: Button = findViewById(R.id.btnLogWorkout)
        logWorkoutButton.setOnClickListener {
            // Pass the current date to showPopup method
            val calendar = Calendar.getInstance()
            showPopup(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        }

        trainingDayManager = TrainingDayManager()
        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrEmpty()) {
            // Handle user not logged in
            return
        }

        loadWeightEntries { weightEntries ->
            displayWeightGraph(weightEntries)
        }
    }

    private fun showPopup(year: Int, month: Int, dayOfMonth: Int) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_calendar_logging, null)

        val titleTextView: TextView = view.findViewById(R.id.tvPopupTitle)
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val date = dateFormat.format(Date(year - 1900, month, dayOfMonth))
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

    private fun showWeightPopup(date: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_log_weight, null)

        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val etWeight: EditText = view.findViewById(R.id.etWeight)
        val rvWeightEntries: RecyclerView = view.findViewById(R.id.rvWeightEntries)

        tvDate.text = date

        rvWeightEntries.layoutManager = LinearLayoutManager(this)
        val weightAdapter = WeightAdapter(mutableListOf(), { weightEntry ->
            currentWeightEntryId = weightEntry.id
            tvDate.text = weightEntry.date
            etWeight.setText(weightEntry.weight.toString())
        }, { weightEntry ->
            showDeleteConfirmationDialog(weightEntry, rvWeightEntries)
        })
        rvWeightEntries.adapter = weightAdapter

        etWeight.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val weight = etWeight.text.toString().toDoubleOrNull()
                if (weight != null && userId != null) {
                    val weightEntry = WeightEntry(id = currentWeightEntryId ?: UUID.randomUUID().toString(), date = date, weight = weight, unit = "kg")
                    saveWeightEntry(weightEntry)
                    loadWeightEntries { weightEntries ->
                        weightAdapter.updateWeightEntries(weightEntries)
                        displayWeightGraph(weightEntries) // Update the graph after saving weight entry
                    }
                } else {
                    Toast.makeText(this, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.setContentView(view)
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, height)
        dialog.show()

        loadWeightEntries { weightEntries ->
            weightAdapter.updateWeightEntries(weightEntries)
        }
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
                    Log.d("MainActivity", "Session saved successfully for date: $date")
                    // Add a marker to the calendar view to indicate a saved session
                    addSessionMarker(date)
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error saving session: ${e.localizedMessage}")
                }
        } ?: run {
            Log.e("MainActivity", "User ID is null, cannot save session")
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

    private fun saveWeightEntry(weightEntry: WeightEntry) {
        userId?.let { uid ->
            val weightEntryRef = db.collection("Users").document(uid).collection("WeightEntries")
            if (weightEntry.id.isEmpty()) {
                weightEntryRef.add(weightEntry)
                    .addOnSuccessListener { documentReference ->
                        weightEntryRef.document(documentReference.id).update("id", documentReference.id)
                        Toast.makeText(this, "Weight entry saved successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save weight entry", Toast.LENGTH_SHORT).show()
                    }
            } else {
                weightEntryRef.document(weightEntry.id).set(weightEntry)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Weight entry updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update weight entry", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadWeightEntries(callback: (List<WeightEntry>) -> Unit) {
        userId?.let { uid ->
            db.collection("Users").document(uid).collection("WeightEntries").get()
                .addOnSuccessListener { documents ->
                    val weightEntries = documents.mapNotNull { it.toObject(WeightEntry::class.java) }
                    callback(weightEntries)
                }
                .addOnFailureListener {
                    callback(emptyList())
                }
        }
    }

    private fun deleteWeightEntry(weightEntryId: String) {
        userId?.let { uid ->
            db.collection("Users").document(uid).collection("WeightEntries").document(weightEntryId).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Weight entry deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to delete weight entry", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addSessionMarker(date: String) {
        // Add logic to visually mark the date on the calendar
    }

    private fun displayWeightGraph(weightEntries: List<WeightEntry>) {
        val graph = findViewById<GraphView>(R.id.weightProgressGraph)
        graph.removeAllSeries()

        val series = LineGraphSeries<DataPoint>()
        weightEntries.forEach { entry ->
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(entry.date)
            date?.let {
                series.appendData(DataPoint(it, entry.weight), true, weightEntries.size)
            }
        }

        graph.addSeries(series)

        // Set manual X bounds to have nice steps
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(weightEntries.firstOrNull()?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)?.time?.toDouble()
        } ?: 0.0)
        graph.viewport.setMaxX(weightEntries.lastOrNull()?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)?.time?.toDouble()
        } ?: 0.0)

        // Set manual Y bounds
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMinY(70.0)
        graph.viewport.setMaxY(120.0)

        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        graph.gridLabelRenderer.numHorizontalLabels = 3 // only 3 because of the space

        // Set the text size for the graph labels
        graph.gridLabelRenderer.textSize = 30f
        graph.gridLabelRenderer.horizontalAxisTitle = "Date"
        graph.gridLabelRenderer.verticalAxisTitle = "Weight (kg)"

        // Enable scaling and scrolling
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    private fun showDeleteConfirmationDialog(weightEntry: WeightEntry, rvWeightEntries: RecyclerView) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Weight Entry")
            .setMessage("Are you sure you want to delete this weight entry?")
            .setPositiveButton("Yes") { _, _ ->
                deleteWeightEntry(weightEntry.id)
                loadWeightEntries { weightEntries ->
                    (rvWeightEntries.adapter as? WeightAdapter)?.updateWeightEntries(weightEntries)
                    displayWeightGraph(weightEntries) // Update the graph after deleting weight entry
                }
            }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }
}
