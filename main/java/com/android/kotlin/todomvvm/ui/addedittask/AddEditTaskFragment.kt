package com.android.kotlin.todomvvm.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.kotlin.todomvvm.R
import com.android.kotlin.todomvvm.data.Task
import com.android.kotlin.todomvvm.data.TaskDao
import com.android.kotlin.todomvvm.databinding.FragmentAddEditTaskBinding
import com.android.kotlin.todomvvm.ui.addedittask.viewmodel.AddEditTaskViewModel
import com.android.kotlin.todomvvm.ui.addedittask.viewmodel.AddEditTaskViewModelFactory
import com.android.kotlin.todomvvm.ui.viewmodelhelper.GenericSavedStateViewModelFactory
import com.android.kotlin.todomvvm.util.exhaustive
import com.google.android.material.snackbar.Snackbar

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var addEditTaskViewModelFactory: AddEditTaskViewModelFactory

    private val viewModel: AddEditTaskViewModel by viewModels {
        GenericSavedStateViewModelFactory(addEditTaskViewModelFactory, this)
    }

    private lateinit var _updateTask: Task
    private var updateTask: Task
        get() = _updateTask
        set(value) {
            _updateTask = value
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddEditTaskBinding.bind(view)
        val args: AddEditTaskFragmentArgs by navArgs()

        binding.apply {

            if (args.title == "Edit Task") {
                editTextTaskName.setText(args.task?.name)
                checkBoxImportant.isChecked = args.task?.important ?: false
                checkBoxImportant.jumpDrawablesToCurrentState()
                textViewDateCreated.isVisible = args.task != null
                textViewDateCreated.text = "Created: ${args.task?.createdDateFormatted}"
                _updateTask = args.task!!
            } else {
                editTextTaskName.setText(viewModel.taskName)
                checkBoxImportant.isChecked = viewModel.taskImportance
                checkBoxImportant.jumpDrawablesToCurrentState()
                textViewDateCreated.isVisible = viewModel.task != null
                textViewDateCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"
            }

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()

                if (args.title == "Edit Task") {
                    _updateTask.name = it.toString()
                }
            }
            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
                if (args.title == "Edit Task") {
                    _updateTask.important = isChecked
                }
            }

            fabSaveTask.setOnClickListener {
                if (args.title == "Edit Task") {
                    viewModel.onUpdateClick(updateTask)
                } else {
                    viewModel.onSaveClick()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }

}