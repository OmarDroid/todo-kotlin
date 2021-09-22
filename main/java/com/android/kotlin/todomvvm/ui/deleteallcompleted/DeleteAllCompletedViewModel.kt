package com.android.kotlin.todomvvm.ui.deleteallcompleted

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.todomvvm.data.TaskDao
import com.android.kotlin.todomvvm.di.ApplicationScope
import com.android.kotlin.todomvvm.ui.addedittask.viewmodel.AddEditTaskViewModel
import com.android.kotlin.todomvvm.ui.viewmodelhelper.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeleteAllCompletedViewModel @AssistedInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    fun onConfirmClick() = viewModelScope.launch{
        taskDao.deleteCompletedTasks()
    }

}

class DeleteAllCompletedViewModelFactory @Inject constructor(
    val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModelAssistedFactory<DeleteAllCompletedViewModel> {
    override fun create(handle: SavedStateHandle): DeleteAllCompletedViewModel {
        return DeleteAllCompletedViewModel(taskDao, applicationScope)
    }
}