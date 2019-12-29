package com.wajahatkarim3.roomexplorer.demo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.thetechnocafe.gurleensethi.liteutils.RecyclerAdapterUtil
import com.wajahatkarim3.roomexplorer.RoomExplorer
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val todoList = arrayListOf<Todo>()
    lateinit var todoAdapter : RecyclerAdapterUtil<Todo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        loadAllTodos()
    }

    fun setupViews()
    {
        // Title
        supportActionBar?.title = "Todo List"

        // To-do Recycler View
        todoAdapter = RecyclerAdapterUtil(this, todoList, R.layout.todo_item_layout)
        todoAdapter.addOnDataBindListener { itemView, item, position, _ ->
            itemView.findViewById<TextView>(R.id.txtTodoTitle).text = item.title
            itemView.findViewById<CheckBox>(R.id.checkDone).isChecked = item.done
            itemView.findViewById<CheckBox>(R.id.checkDone).setOnCheckedChangeListener { compoundButton, value ->
                if (value == true)
                {
                    TodoRoomDatabase.getDatabase(this@MainActivity).todoDao().deleteToDo(item.id)
                    loadAllTodos()
                }
            }
        }
        recyclerTodos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerTodos.adapter = todoAdapter

        // Add Button
        imgAdd.setOnClickListener {
            if (txtTodoTask.text.isEmpty())
                Toast.makeText(this, "Todo title cannot be empty!", Toast.LENGTH_SHORT).show()
            else
                addTodoItem(txtTodoTask.text.toString())
        }

        // EditText Editor Action
        txtTodoTask.setImeOptions(EditorInfo.IME_ACTION_DONE)
        txtTodoTask.setOnEditorActionListener { textView, i, keyEvent ->
            if (txtTodoTask.text.isEmpty())
            {
                Toast.makeText(this, "Todo title cannot be empty!", Toast.LENGTH_SHORT).show()
                false
            }
            else
                addTodoItem(txtTodoTask.text.toString())
            true
        }
    }

    fun addTodoItem(title: String)
    {
        var item = Todo(title = title, done = false)
        TodoRoomDatabase.getDatabase(this).todoDao().insert(item)
        loadAllTodos()

        txtTodoTask.text.clear()
    }

    fun loadAllTodos()
    {
        todoList.clear()
        todoList.addAll(TodoRoomDatabase.getDatabase(this).todoDao().getAllTodos())
        recyclerTodos.post {
            todoAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_options, menu)

        // return true so that the menu pop up is opened
        // return true so that the menu pop up is opened
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuShowDatabase)
        {
            RoomExplorer.show(this, TodoRoomDatabase::class.java, TodoRoomDatabase.DB_NAME)
        }
        return super.onOptionsItemSelected(item)
    }
}
