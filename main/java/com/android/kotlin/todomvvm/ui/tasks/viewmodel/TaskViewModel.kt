package com.android.kotlin.todomvvm.ui.tasks.viewmodel

import androidx.lifecycle.*
import com.android.kotlin.todomvvm.data.PreferencesRepository
import com.android.kotlin.todomvvm.data.SortOrder
import com.android.kotlin.todomvvm.data.Task
import com.android.kotlin.todomvvm.data.TaskDao
import com.android.kotlin.todomvvm.ui.ADD_TASK_RESULT_OK
import com.android.kotlin.todomvvm.ui.EDIT_TASK_RESULT_OK
import com.android.kotlin.todomvvm.ui.viewmodelhelper.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskViewModel @AssistedInject constructor(
    private val taskDao: TaskDao,
    private val preferencesRepository: PreferencesRepository,
    @Assisted private val handle: SavedStateHandle
) : ViewModel() {
    val searchQuery = handle.getLiveData("searchQuery", "")
    val preferencesFlow = preferencesRepository.preferencesFlow
    private val taskEventChannel = Channel<TasksEvent>()
    val tasksEvent = taskEventChannel.receiveAsFlow()
    private val taskFlow =
        combine(searchQuery.asFlow(), preferencesFlow) { query, filterPref ->
            Pair(query, filterPref)
        }.flatMapLatest { (query, filterPref) ->
            taskDao.getTasks(query, filterPref.sortOrder, filterPref.hideCompleted)
        }
    val tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesRepository.updateSortOrder(sortOrder)
    }

    fun onHideCompleted(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesRepository.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmMessage("Task added!")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmMessage("Task updated!")
        }
    }

    private fun showTaskSavedConfirmMessage(text: String) = viewModelScope.launch {
        taskEventChannel.send(TasksEvent.ShowTaskSavedConfirmMessage(text))
    }

    fun onDeleteAllCompleted() = viewModelScope.launch {
        taskEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmMessage(val msg: String) : TasksEvent()
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
    }

}

class TaskViewModelFactory @Inject constructor(
    val taskDao: TaskDao,
    val preferencesRepository: PreferencesRepository
) : ViewModelAssistedFactory<TaskViewModel> {
    override fun create(handle: SavedStateHandle): TaskViewModel {
        return TaskViewModel(taskDao, preferencesRepository, handle)
    }
}
