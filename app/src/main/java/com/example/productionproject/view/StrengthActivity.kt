package com.example.productionproject.view

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productionproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StrengthActivity : BaseActivity(), WorkoutAdapter.InteractionListener {
    private lateinit var workoutAdapter: WorkoutAdapter
    private val workouts = mutableListOf<Workout>()
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun getContentViewId(): Int = R.layout.activity_strength
    override fun getNavigationMenuItemId(): Int = R.id.navigation_strength

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rvWorkouts = findViewById<RecyclerView>(R.id.rvWorkouts)
        val btnAddWorkout = findViewById<ImageButton>(R.id.btnAddWorkout)

        workoutAdapter = WorkoutAdapter(workouts, this)
        rvWorkouts.adapter = workoutAdapter
        rvWorkouts.layoutManager = LinearLayoutManager(this)

        btnAddWorkout.setOnClickListener {
            addWorkout()
        }

        loadWorkouts()
    }

    private fun addWorkout() {
        val etWorkoutName = findViewById<EditText>(R.id.etWorkoutName)
        val etReps = findViewById<EditText>(R.id.etReps)
        val etSets = findViewById<EditText>(R.id.etSets)
        val etDifficulty = findViewById<EditText>(R.id.etRPE)

        val workoutName = etWorkoutName.text.toString()
        val reps = etReps.text.toString().toIntOrNull() ?: 0
        val sets = etSets.text.toString().toIntOrNull() ?: 0
        val difficulty = etDifficulty.text.toString()

        // Ensure that we only add the workout if reps and sets are properly provided
        if (workoutName.isNotEmpty() && reps > 0 && sets > 0 && difficulty.isNotEmpty()) {
            val workout = Workout(userId = userId ?: "", name = workoutName, reps = reps, sets = sets, difficulty = difficulty)
            userId?.let {
                db.collection("workouts").add(workout)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "Workout added successfully.", Toast.LENGTH_SHORT).show()
                        workouts.add(workout)
                        workoutAdapter.notifyItemInserted(workouts.size - 1)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error adding workout: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please fill all fields correctly, including numeric values for reps and sets.", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadWorkouts() {
        userId?.let {
            db.collection("workouts")
                .whereEqualTo("userId", it)
                .get()
                .addOnSuccessListener { documents ->
                    workouts.clear()
                    documents.forEach { document ->
                        val workout = document.toObject(Workout::class.java).apply { id = document.id }
                        workouts.add(workout)
                    }
                    workoutAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting documents: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onItemSelected(position: Int) {
        showEditDeleteDialog(position)
    }

    private fun showEditDeleteDialog(position: Int) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Edit or Delete Workout")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> editWorkout(position)
                    1 -> deleteWorkout(position)
                }
            }
            .show()
    }

    private fun editWorkout(position: Int) {
        val workout = workouts[position]
        findViewById<EditText>(R.id.etWorkoutName).setText(workout.name)
        findViewById<EditText>(R.id.etReps).setText(workout.reps)
        findViewById<EditText>(R.id.etSets).setText(workout.sets)
        findViewById<EditText>(R.id.etRPE).setText(workout.difficulty)
    }

    private fun deleteWorkout(position: Int) {
        val workout = workouts[position]
        db.collection("workouts").document(workout.id).delete()
            .addOnSuccessListener {
                workouts.removeAt(position)
                workoutAdapter.notifyItemRemoved(position)
                Toast.makeText(this, "Workout deleted successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting workout: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    data class Workout(
        val userId: String = "",
        var id: String = "",
        val name: String = "",
        val reps: Int = 0,
        val sets: Int = 0,
        val difficulty: String = ""
    )
}
