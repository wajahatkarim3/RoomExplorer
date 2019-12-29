package com.wajahatkarim3.roomexplorer.demo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey(autoGenerate = true) var id : Int = 0,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "done") var done : Boolean
)