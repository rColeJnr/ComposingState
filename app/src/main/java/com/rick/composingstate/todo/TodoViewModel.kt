package com.rick.composingstate.todo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TodoViewModel: ViewModel() {

    private var currentEditPosition by mutableStateOf(-1)

    // state: TodoItems, only set inside this class
    var todoItems by mutableStateOf(listOf<TodoItem>())
        private set

    // get current editing item or null
    val currentEditItem: TodoItem?
        get() = todoItems.getOrNull(currentEditPosition)

    // event: AddItem
    fun addItem(item: TodoItem){
        todoItems = todoItems + listOf(item)
    }

    fun removeItem(item: TodoItem){
        // edit the list and re-asign it withouth the removed item
        todoItems = todoItems.toMutableList().also {
            it.remove(item)
        }
        onEditDone()
    }

    // event: onEditDone
    fun onEditDone(){
        // set the edit position to outOfBounds
        currentEditPosition = -1
    }

    // event: onEditItemSelected
    fun onEditItemSelected(item: TodoItem) {
        currentEditPosition = todoItems.indexOf(item)
    }

    // event: OnEditItemChange
    fun onEditItemChange(item: TodoItem){
        // only access the value if notNull
        val currentItem = requireNotNull(currentEditItem)
        require(currentItem.id == item.id){
            "You can only change an item with same id as currentEditItem"
        }

        todoItems = todoItems.toMutableList().also {
            it[currentEditPosition] = item
        }
    }

}