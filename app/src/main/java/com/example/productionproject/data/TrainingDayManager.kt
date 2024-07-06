package com.example.productionproject.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

data class TrainingDay(
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var notes: String = "",
    var workouts: MutableList<Workout> = mutableListOf()
) {
    override fun toString(): String {
        return name
    }
}

class TrainingDayManager {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "TrainingDayManager"

    fun addTrainingDay(userId: String, name: String, completion: (Boolean, String, String?) -> Unit) {
        if (userId.isBlank()) {
            completion(false, "User ID is blank", null)
            return
        }
        val newTrainingDay = TrainingDay(userId = userId, name = name)
        db.collection("Users").document(userId).collection("TrainingDays").add(newTrainingDay)
            .addOnSuccessListener { documentReference ->
                newTrainingDay.id = documentReference.id
                Log.d(TAG, "Training day added with ID: ${documentReference.id}")
                completion(true, "Training day added successfully.", documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding training day", e)
                completion(false, e.localizedMessage ?: "An error occurred.", null)
            }
    }

    fun saveTrainingDayWithNotes(userId: String, trainingDayId: String, date: String, notes: String, completion: (Boolean, String) -> Unit) {
        val trainingDayRef = db.collection("Users").document(userId).collection("TrainingDays").document(trainingDayId)
        val data = mapOf(
            "name" to date,
            "notes" to notes
        )
        trainingDayRef.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Training day and notes saved successfully.")
                completion(true, "Training day and notes saved successfully.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving training day and notes: ${e.localizedMessage}")
                completion(false, "Error saving training day and notes: ${e.localizedMessage}")
            }
    }

    fun loadTrainingDays(userId: String, completion: (List<TrainingDay>) -> Unit) {
        if (userId.isBlank()) {
            Log.e(TAG, "Error loading training days: User ID is blank")
            completion(emptyList())
            return
        }
        db.collection("Users").document(userId).collection("TrainingDays").get()
            .addOnSuccessListener { result ->
                val days = result.documents.mapNotNull { it.toObject(TrainingDay::class.java)?.apply { id = it.id } }
                val dayCount = days.size
                var loadedDays = 0

                if (days.isEmpty()) {
                    completion(emptyList())  // Immediately return if there are no days to process
                } else {
                    days.forEach { day ->
                        loadWorkoutsForDay(userId, day.id) { workouts, message ->
                            day.workouts = workouts?.toMutableList() ?: mutableListOf()
                            loadedDays++
                            if (loadedDays == dayCount) { // Check if all days have their workouts loaded
                                completion(days)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading training days: ${e.localizedMessage}")
                completion(emptyList())
            }
    }

    fun addWorkoutToDay(userId: String, trainingDayId: String, workout: Workout, completion: (Boolean, String) -> Unit) {
        if (userId.isBlank() || trainingDayId.isBlank()) {
            completion(false, "User ID or Training Day ID is blank")
            return
        }
        db.collection("Users").document(userId).collection("TrainingDays").document(trainingDayId).collection("Workouts").add(workout)
            .addOnSuccessListener { documentReference ->
                workout.id = documentReference.id
                Log.d(TAG, "Workout added with ID: ${documentReference.id}")
                completion(true, "Workout added successfully.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding workout: ${e.localizedMessage}")
                completion(false, "Failed to add workout: ${e.localizedMessage}")
            }
    }

    fun loadWorkoutsForDay(userId: String, trainingDayId: String, completion: (List<Workout>?, String) -> Unit) {
        if (userId.isBlank() || trainingDayId.isBlank()) {
            completion(null, "User ID or Training Day ID is blank")
            return
        }
        db.collection("Users").document(userId).collection("TrainingDays").document(trainingDayId).collection("Workouts").get()
            .addOnSuccessListener { querySnapshot ->
                val workouts = querySnapshot.documents.mapNotNull { it.toObject(Workout::class.java)?.apply { id = it.id } }
                completion(workouts, "Workouts loaded successfully.")
            }
            .addOnFailureListener { e ->
                completion(null, "Failed to load workouts: ${e.localizedMessage}")
            }
    }

    fun updateWorkout(userId: String, trainingDayId: String, workout: Workout, completion: (Boolean, String) -> Unit) {
        if (userId.isBlank() || trainingDayId.isBlank()) {
            completion(false, "User ID or Training Day ID is blank")
            return
        }
        val workoutRef = db.collection("Users").document(userId).collection("TrainingDays").document(trainingDayId).collection("Workouts").document(workout.id)
        workoutRef.set(workout)
            .addOnSuccessListener {
                Log.d(TAG, "Workout updated successfully.")
                completion(true, "Workout updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update workout: ${e.localizedMessage}")
                completion(false, "Failed to update workout: ${e.localizedMessage}")
            }
    }

    fun deleteWorkoutFromDay(userId: String, trainingDayId: String, workoutId: String, completion: (Boolean, String) -> Unit) {
        if (userId.isBlank() || trainingDayId.isBlank() || workoutId.isBlank()) {
            completion(false, "User ID, Training Day ID, or Workout ID is blank")
            return
        }
        val workoutRef = db.collection("Users")
            .document(userId)
            .collection("TrainingDays")
            .document(trainingDayId)
            .collection("Workouts")
            .document(workoutId)

        workoutRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Workout deleted successfully.")
                completion(true, "Workout deleted successfully.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to delete workout: ${e.localizedMessage}")
                completion(false, "Failed to delete workout: ${e.localizedMessage}")
            }
    }
}
