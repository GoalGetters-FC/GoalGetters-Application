package com.ggetters.app.ui.central.dialogs

import android.R
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.ggetters.app.core.models.results.Final
import com.ggetters.app.databinding.ModalDialogInsertUserBinding
import com.ggetters.app.ui.shared.models.ModalError
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InsertUserDialog() : DialogFragment() {
    companion object {
        const val TAG = "InsertUserDialog"

        /**
         * Factory for [InsertUserDialog]
         */
        fun newInstance(
            onComplete: (Final<Result, ModalError>) -> Unit
        ): InsertUserDialog = InsertUserDialog().apply {
            setCallback(onComplete)
        }
    }


// --- Fields


    private var onComplete: ((Final<Result, ModalError>) -> Unit)? = null


    private var _binding: ModalDialogInsertUserBinding? = null
    private val binding get() = _binding!!


// --- Lifecycle


    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        _binding = ModalDialogInsertUserBinding.inflate(layoutInflater)
        val view = binding.root
        val modalDialog = MaterialAlertDialogBuilder(requireContext()).setView(view).create()
        return setupLayoutUi(modalDialog)
    }


    override fun onCancel(dialog: DialogInterface) {
        onComplete?.invoke(Final.Failure(ModalError.DISMISSED_OUTSIDE))
        super.onCancel(dialog)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


// --- Internals


    private fun setupLayoutUi(dialog: AlertDialog): AlertDialog {
        binding.etUserPosition.setAdapter(
            ArrayAdapter(
                requireContext(), R.layout.simple_dropdown_item_1line, arrayOf(
                    "Striker",
                    "Forward",
                    "Midfielder",
                    "Defender",
                    "Goalkeeper",
                    "Winger",
                    "Center Back",
                    "Full Back"
                )
            )
        )

        binding.btPositive.setOnClickListener {
            onComplete?.invoke(
                Final.Success(
                    Result(
                        name = binding.etUserName.text.toString(),
                        surname = binding.etUserSurname.text.toString(),
                        position = binding.etUserPosition.text.toString(),
                        number = binding.etUserNumber.text.toString(),
                    )
                )
            )

            dialog.dismiss()
        }

        binding.btNegative.setOnClickListener {
            onComplete?.invoke(Final.Failure(ModalError.DISMISSED_CLICKED))
            dialog.dismiss()
        }

        // setupTouchListeners()
        return dialog
    }


    private fun setCallback(
        callback: (Final<Result, ModalError>) -> Unit
    ) {
        onComplete = callback
    }


// --- Result


    data class Result(
        val name: String,
        val surname: String,
        val position: String,
        val number: String,
    )
}