package com.example.productionproject.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productionproject.R

class WorkoutAdapter(
    private val workouts: MutableList<StrengthActivity.Workout>,
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
        holder.bind(workouts[position])
        holder.itemView.setOnClickListener {
            interactionListener.onItemSelected(position)
        }
    }

    override fun getItemCount() = workouts.size

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(workout: StrengthActivity.Workout) {
            itemView.findViewById<TextView>(R.id.tvWorkoutName).text = workout.name
            itemView.findViewById<TextView>(R.id.tvReps).text = workout.reps.toString()
            itemView.findViewById<TextView>(R.id.tvSets).text = workout.sets.toString()
            itemView.findViewById<TextView>(R.id.tvDifficulty).text = workout.difficulty
        }
    }
}


