package com.example.productionproject.view

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productionproject.R
import com.example.productionproject.data.TrainingDay
import com.example.productionproject.data.TrainingDayManager
import com.example.productionproject.data.Workout
import com.google.firebase.auth.FirebaseAuth

class StrengthActivity : BaseActivity(), WorkoutAdapter.InteractionListener {
    private lateinit var workoutAdapter: WorkoutAdapter
    private val workouts = mutableListOf<Workout>()
    private val trainingDays = mutableListOf<TrainingDay>()
    private lateinit var spinnerAdapter: ArrayAdapter<TrainingDay>
    private val trainingDayManager = TrainingDayManager()
    private var userId: String? = null

    override fun getContentViewId(): Int = R.layout.activity_strength
    override fun getNavigationMenuItemId(): Int = R.id.navigation_strength

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_strength)

        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        loadTrainingDays()
    }

    private fun initializeViews() {
        val rvWorkouts = findViewById<RecyclerView>(R.id.rvWorkouts)
        val btnAddWorkout = findViewById<ImageButton>(R.id.btnAddWorkout)
        val spinnerTrainingDays = findViewById<Spinner>(R.id.spinnerTrainingDays)
        val btnAddDay = findViewById<Button>(R.id.btnAddDay)

        spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, trainingDays)
        spinnerTrainingDays.adapter = spinnerAdapter

        workoutAdapter = WorkoutAdapter(workouts, this)
        rvWorkouts.adapter = workoutAdapter
        rvWorkouts.layoutManager = LinearLayoutManager(this)

        btnAddDay.setOnClickListener {
            val trainingDayName = findViewById<EditText>(R.id.etTrainingDayName).text.toString()
            if (trainingDayName.isNotBlank()) {
                trainingDayManager.addTrainingDay(userId!!, trainingDayName) { success, message, id ->
                    if (success && id != null) {
                        val newTrainingDay = TrainingDay(id, userId!!, trainingDayName)
                        trainingDays.add(newTrainingDay)
                        spinnerAdapter.notifyDataSetChanged()
                        findViewById<EditText>(R.id.etTrainingDayName).text.clear()
                        Toast.makeText(this, "Training day added successfully.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error saving day: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a training day name.", Toast.LENGTH_SHORT).show()
            }
        }

        spinnerTrainingDays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position >= 0 && position < trainingDays.size) {
                    val selectedTrainingDay = trainingDays[position]
                    loadWorkouts(selectedTrainingDay.id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                workouts.clear()
                workoutAdapter.notifyDataSetChanged()
            }
        }


        btnAddWorkout.setOnClickListener {
            val selectedDay = spinnerTrainingDays.selectedItem as? TrainingDay
            selectedDay?.let {
                addWorkout(it.id)
            }
        }
    }


    private fun loadTrainingDays() {
        userId?.let { uid ->
            trainingDayManager.loadTrainingDays(uid) { days ->
                trainingDays.clear()
                trainingDays.addAll(days)
                spinnerAdapter.notifyDataSetChanged()
            }
        } ?: run {
            Toast.makeText(this, "User ID is null, cannot load data.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadWorkouts(trainingDayId: String) {
        if (trainingDayId.isNotEmpty()) {
            trainingDayManager.loadWorkoutsForDay()
        } else {
            Toast.makeText(this, "Invalid Training Day ID", Toast.LENGTH_LONG).show()
        }
    }

    private fun addWorkout(dayId: String) {
        val workoutName = findViewById<EditText>(R.id.etWorkoutName).text.toString()
        val reps = findViewById<EditText>(R.id.etReps).text.toString().toIntOrNull() ?: 0
        val sets = findViewById<EditText>(R.id.etSets).text.toString().toIntOrNull() ?: 0
        val difficulty = findViewById<EditText>(R.id.etRPE).text.toString()

        if (workoutName.isNotBlank() && reps > 0 && sets > 0 && difficulty.isNotBlank()) {
            val workout = Workout("", userId!!, workoutName, reps, sets, difficulty, dayId)
            trainingDayManager.addWorkoutToDay(userId!!, dayId, workout) { success, message ->
                if (success) {
                    workouts.add(workout)
                    workoutAdapter.notifyItemInserted(workouts.size - 1)
                    Toast.makeText(this, "Workout added successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add workout: $message", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Please fill all fields correctly, including numeric values for reps and sets.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onItemSelected(position: Int) {
        showEditDeleteDialog(position)
    }

    private fun showEditDeleteDialog(position: Int) {
        val workout = workouts[position]
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Edit or Delete Workout")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> editWorkout(workout, position)
                    1 -> deleteWorkout(workout, position)
                }
            }
            .show()
    }

    private fun editWorkout(workout: Workout, position: Int) {
        val view = layoutInflater.inflate(R.layout.edit_workout_dialog, null)
        val etWorkoutName = view.findViewById<EditText>(R.id.etWorkoutName)
        etWorkoutName.setText(workout.name)
        val etReps = view.findViewById<EditText>(R.id.etReps)
        etReps.setText(workout.reps.toString())
        val etSets = view.findViewById<EditText>(R.id.etSets)
        etSets.setText(workout.sets.toString())
        val etRPE = view.findViewById<EditText>(R.id.etRPE)
        etRPE.setText(workout.difficulty)

        AlertDialog.Builder(this)
            .setTitle("Edit Workout")
            .setView(view)
            .setPositiveButton("Save") { dialog, _ ->
                workout.name = etWorkoutName.text.toString()
                workout.reps = etReps.text.toString().toInt()
                workout.sets = etSets.text.toString().toInt()
                workout.difficulty = etRPE.text.toString()
                trainingDayManager.updateWorkout(userId!!, workout.trainingDayId, workout) { success, message ->
                    if (success) {
                        workoutAdapter.notifyItemChanged(position)
                        Toast.makeText(this, "Workout updated successfully.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update workout: $message", Toast.LENGTH_LONG).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteWorkout(workout: Workout, position: Int) {
        trainingDayManager.deleteWorkoutFromDay(userId!!, workout.trainingDayId, workout.id) { success, message ->
            if (success) {
                workouts.removeAt(position)
                workoutAdapter.notifyItemRemoved(position)
                Toast.makeText(this, "Workout deleted successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error deleting workout: $message", Toast.LENGTH_LONG).show()
            }
        }
    }
}
