package com.android.kotlin.todomvvm.ui.addedittask.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.todomvvm.data.Task
import com.android.kotlin.todomvvm.data.TaskDao
import com.android.kotlin.todomvvm.ui.ADD_TASK_RESULT_OK
import com.android.kotlin.todomvvm.ui.EDIT_TASK_RESULT_OK
import com.android.kotlin.todomvvm.ui.viewmodelhelper.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddEditTaskViewModel @AssistedInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val handle: SavedStateHandle
) : ViewModel() {
    val task = handle.get<Task>("task")
    var taskName = handle.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            handle.set("taskName", value)
        }

    var taskImportance = handle.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            handle.set("taskImportance", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMsg("Task Name cannot be empty")
            return
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    fun onUpdateClick(task: Task?) {
        if (task?.name?.isBlank() == true) {
            showInvalidInputMsg("Task Name cannot be empty")
            return
        } else {
            updatedTask(task!!)
        }
    }


    private fun createTask(newTask: Task) = viewModelScope.launch {
        taskDao.insert(newTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updatedTask(updatedTask: Task) = viewModelScope.launch {
        taskDao.update(updatedTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMsg(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}

class AddEditTaskViewModelFactory @Inject constructor(
    val taskDao: TaskDao
) : ViewModelAssistedFactory<AddEditTaskViewModel> {
    override fun create(handle: SavedStateHandle): AddEditTaskViewModel {
        return AddEditTaskViewModel(taskDao, handle)
    }
}