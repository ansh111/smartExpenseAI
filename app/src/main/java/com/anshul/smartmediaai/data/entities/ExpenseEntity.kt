package com.anshul.smartmediaai.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity (
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val description: String,
    val amount: Double,
    val date: String,
    val category: String? = null,
    val timestamp: Long = 0L
)