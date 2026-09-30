package com.prajwalhs.learningdashboard.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    // Server-assigned ID, not auto-generated, so upserts match existing rows.
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "instructor") val instructor: String,
)