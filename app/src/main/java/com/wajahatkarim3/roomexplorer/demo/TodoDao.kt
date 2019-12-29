package com.wajahatkarim3.roomexplorer.demo

import androidx.room.*

@Dao
interface TodoDao {

    @Query("SELECT * from todo_table order by done desc")
    fun getAllTodos() : List<Todo>

    @Insert
    fun insert(todo : Todo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(todo: Todo)

    @Query("DELETE FROM todo_table")
    fun deleteAll()

    @Query("DELETE FROM todo_table WHERE id = :todoId")
    fun deleteToDo(todoId: Int)
}