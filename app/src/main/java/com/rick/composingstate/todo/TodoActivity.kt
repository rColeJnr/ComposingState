package com.rick.composingstate.todo

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable

class TodoActivity: AppCompatActivity() {

    val todoViewModel by viewModels<TodoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                //surface adds a background to the app, and configures the color of texts
                TodoActivityScreen(todoViewModel)
            }
        }
    }
}

/*
* .observeAsState observes a LiveData<T> and converts it into a State<T> object so compose
* can react to value changes.
* listOf() is an initial valut to avoid possible null result vefore the livedata is initialized,
* if it wasn't passed items would be List<TodoItem>? which is nullable.
* by is the property delegate syntax in kotlin, it lists us automatically unwrap the State<List<TodoItem>>
from observeAsState into a regular List<TodoItem>
* */
@Composable
private fun TodoActivityScreen(todoViewModel: TodoViewModel){
    TodoScreen(
        items = todoViewModel.todoItems,
        currentlyEditing = todoViewModel.currentEditItem,
        onAddItem = todoViewModel::addItem,
        onRemoveItem = todoViewModel::removeItem,
        onStartEdit = todoViewModel::onEditItemSelected,
        onEditItemChange = todoViewModel::onEditItemChange,
        onEditDone = todoViewModel::onEditDone
    )
}












