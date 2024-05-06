package com.example.productionproject.data

data class Workout(
    var id: String = "",
    val userId: String,
    var name: String,
    var reps: Int,
    var sets: Int,
    var difficulty: String,
    var trainingDayId: String
){
    // Firestore needs a no-argument constructor
    constructor() : this("", "", "", 0, 0, "", "")
}
