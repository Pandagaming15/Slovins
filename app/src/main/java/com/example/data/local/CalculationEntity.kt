package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val population: Long,
    val marginOfErrorPercent: Double,
    val rawResult: Double,
    val roundedResult: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
