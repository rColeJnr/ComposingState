package com.rick.composingstate.todo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codelabs.state.util.generateRandomTodoItem
import kotlin.random.Random

/*
* Stateless compoent that is responsible for the entire todo screen
*
* State hoisting is a pattern of mving state up to makea compontnet stateless
* when applied to composables, this often means introducing two parameters to the composable
* */
@Composable
fun TodoScreen(
    items: List<TodoItem>,
    currentlyEditing: TodoItem?,
    onAddItem: (TodoItem) -> Unit,
    onRemoveItem: (TodoItem) -> Unit,
    onStartEdit: (TodoItem) -> Unit,
    onEditItemChange: (TodoItem) -> Unit,
    onEditDone: () -> Unit
) {
    Column {
        val enableTopSection = currentlyEditing == null
        TodoItemInputBackground(elevate = enableTopSection) {
            if (enableTopSection) {
                TodoItemEntryInput(onAddItem)
            } else {
                Text(
                    "Editing item",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            items(items) { todo ->
                if (currentlyEditing?.id == todo.id) {
                    TodoItemInlineEditor(
                        item = currentlyEditing,
                        onEditItemChange = onEditItemChange,
                        onEditDone = onEditDone,
                        onRemoveItem = { onRemoveItem(todo) }
                    )
                } else {
                    TodoRow(
                        todo,
                        { onStartEdit(it) },
                        Modifier.fillParentMaxWidth()
                    )
                }
            }
        }
        // For quick testing, a random item generator button
        Button(
            onClick = { onAddItem(generateRandomTodoItem()) },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text("Add random item")
        }
    }
}

/*
* Stateless composable that displays a fulll-width [TodoItem]
*
* remember gives a composable memory during it's composed state in the screen
*
* a value computed by remember will be stored in the composition tree, and onby be recomputed
* if the keys to @param remember change
*
* remember is like a val inside a fun body
*
* A remember call consists:
*  1 key arguments - the 'key that this remeber uses, the part passed in parenthesis todo.id
*  calculation - a lambda that computes a new value to be remembered, passed in a trailling lambda, randomtint()
*
* by placing iconAlpha on the constructor, we still generatea  random tint, but
* the caller of the compsable can also specify its tint, thus this compsable is more reusable.
* */

@Composable
fun TodoItemInlineEditor(
    item: TodoItem,
    onEditItemChange: (TodoItem) -> Unit,
    onEditDone: () -> Unit,
    onRemoveItem: () -> Unit
) {
    TodoItemInput(
        text = item.task,
        onTextChange = { onEditItemChange(item.copy(task = it)) },
        icon = item.icon,
        onIconChange = { onEditItemChange(item.copy(icon = it)) },
        submit = onEditDone,
        iconsVisible = true,
        buttonSlot = {
            Row {
                val shrinkBbuttons = Modifier.widthIn(20.dp)
                TextButton(onClick = onEditDone, modifier = shrinkBbuttons) {
                    Text(text = "what's that?",
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(30.dp)
                    )
                }
                TextButton(onClick = onRemoveItem, modifier = shrinkBbuttons) {
                    Text(
                        text = "❌",
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(30.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun TodoItemInput(
    text: String,
    onTextChange: (String) -> Unit,
    submit: () -> Unit,
    iconsVisible: Boolean,
    icon: TodoIcon,
    onIconChange: (TodoIcon) -> Unit,
    buttonSlot: @Composable () -> Unit
// this is a generic slop that the caller can fill in wihth the desired buttons
// Scaffold seems to be used for topbar, appBar, bottomBar... while
// slots seems to be more intrinsic
) {
    Column {
        Row (
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .height(IntrinsicSize.Min)
        ) {
            TodoInputText(
                text = text,
                onTextChange = onTextChange,
                onImeAction = submit,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.align(Alignment.CenterVertically)) { buttonSlot() }
        }
        if (iconsVisible) {
            AnimatedIconRow(
                icon = icon,
                onIconChange = onIconChange,
                modifier = Modifier.padding(top = 8.dp)
            )
        }else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

/*
* we took a statefull composable, TodoItemInput, and split ti into two composables,
* One with state TodoItem EntryInput and on stateless
*
* The stateless composable has all of our UI-related code, and the statefull
* composable doesn't have any UI=related code, by doing this, we make the UI code
* reusable in situations where we want to bafck the stae differently.
*
* When hoisting state, there are three rusles to help you figure out where it should go
*
*   State should be hoisted to at least the lowest common parent of al composables that use the same state
*   State should be hoisted to at least the hightst level it may be changed
*   if two states change in response to the same events they should be hoisted together
* */

@Composable
fun TodoItemEntryInput(onItemComplete: (TodoItem) -> Unit, buttonText: String = "Add") {
    val (text, onTextChange) = rememberSaveable { mutableStateOf("") } // survives recreation
    val (icon, onIconChange) = remember  { mutableStateOf(TodoIcon.Default)}

    val submit = {
        if (text.isNotBlank()){
            onItemComplete(TodoItem(text, icon))
            onTextChange("")
            onIconChange(TodoIcon.Default)
        }
    }
    TodoItemInput(
        text = text,
        onTextChange = onTextChange,
        submit = submit,
        iconsVisible = text.isNotBlank(),
        icon = icon,
        onIconChange = onIconChange
    ) {
        TodoEditButton(onClick = submit, text = buttonText, enabled = text.isNotBlank() )
    }
}

@Composable
fun TodoRow(
    todo: TodoItem,
    onItemClicked: (TodoItem) -> Unit,
    modifier: Modifier = Modifier,
    iconAlpha: Float = remember (todo.id) { randomTint() }
) {
    Row(
        modifier
            .clickable { onItemClicked(todo) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement =  Arrangement.SpaceBetween
    ){
        Text(
            todo.task
        )
        Icon(
            imageVector = todo.icon.imageVector,
            tint = LocalContentColor.current.copy(alpha = iconAlpha),
            contentDescription = stringResource(id = todo.icon.contentDescription)
        )
    }
}

private fun randomTint(): Float {
    return Random.nextFloat().coerceIn(0.3f, 0.9f)
}



@Preview
@Composable
fun PreviewTodoScreen() {
    val items = listOf(
        TodoItem("Learn compose", TodoIcon.Event),
        TodoItem("Take the codelab"),
        TodoItem("Apply state", TodoIcon.Done),
        TodoItem("Build dynamic UIs", TodoIcon.Square)
    )
    TodoScreen(items, null, {}, {}, {}, {}, {})
}

@Preview
@Composable
fun PreviewTodoItemInput() = TodoItemEntryInput(onItemComplete = { })

@Preview
@Composable
fun PreviewTodoRow() {
    val todo = remember { generateRandomTodoItem() }
    TodoRow(todo = todo, onItemClicked = {}, modifier = Modifier.fillMaxWidth())
}












