package com.android.kotlin.todomvvm.ui.deleteallcompleted

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.android.kotlin.todomvvm.ui.viewmodelhelper.GenericSavedStateViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DeleteAllCompletedDialogFragment: DialogFragment() {

    @Inject
    lateinit var deleteAllCompletedViewModelFactory: DeleteAllCompletedViewModelFactory

    private val viewModel: DeleteAllCompletedViewModel by viewModels {
        GenericSavedStateViewModelFactory(deleteAllCompletedViewModelFactory, this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm delete all tasks")
            .setMessage("Are you sure you want to delete all tasks ?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes") {_, _ ->
                viewModel.onConfirmClick()
            }
            .create()
}