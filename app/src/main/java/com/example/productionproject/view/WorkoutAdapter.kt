package com.example.productionproject.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.productionproject.R
import com.example.productionproject.data.Workout

class WorkoutAdapter(
    private val workouts: MutableList<Workout>,
    private val interactionListener: InteractionListener
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    interface InteractionListener {
        fun onItemSelected(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_item, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        if (position in workouts.indices) {
            holder.bind(workouts[position])
            holder.itemView.setOnClickListener {
                interactionListener.onItemSelected(position)
            }
        }
    }

    override fun getItemCount() = workouts.size

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val workoutName: TextView = itemView.findViewById(R.id.tvWorkoutName)
        private val workoutReps: TextView = itemView.findViewById(R.id.tvReps)
        private val workoutSets: TextView = itemView.findViewById(R.id.tvSets)
        private val workoutDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)

        fun bind(workout: Workout) {
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            workoutName.text = workout.name
            workoutName.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
            workoutReps.text = workout.reps.toString()
            workoutReps.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
            workoutSets.text = workout.sets.toString()
            workoutSets.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
            workoutDifficulty.text = workout.difficulty
            workoutDifficulty.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
        }
    }
}
