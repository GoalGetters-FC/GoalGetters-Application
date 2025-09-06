package com.ggetters.app.ui.central.dialogs

import android.R
import android.app.DatePickerDialog
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

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

        // Setup DOB date picker
        setupDatePicker()

        binding.btPositive.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            val surname = binding.etUserSurname.text.toString().trim()
            val position = binding.etUserPosition.text.toString().trim()
            val number = binding.etUserNumber.text.toString().trim()
            val dateOfBirth = binding.etDob.text.toString().trim()

            // Basic validation
            if (name.isEmpty() || surname.isEmpty() || position.isEmpty() || number.isEmpty()) {
                // Show error - you could add a Snackbar or Toast here
                return@setOnClickListener
            }

            onComplete?.invoke(
                Final.Success(
                    Result(
                        name = name,
                        surname = surname,
                        position = position,
                        number = number,
                        dateOfBirth = dateOfBirth
                    )
                )
            )

            dialog.dismiss()
        }

        binding.btNegative.setOnClickListener {
            onComplete?.invoke(Final.Failure(ModalError.DISMISSED_CLICKED))
            dialog.dismiss()
        }

        return dialog
    }


    private fun setCallback(
        callback: (Final<Result, ModalError>) -> Unit
    ) {
        onComplete = callback
    }

    private fun setupDatePicker() {
        binding.etDob.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formattedDate = selectedDate.format(DateTimeFormatter.ISO_DATE)
                binding.etDob.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set max date to today (can't be born in the future)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        
        // Set min date to 100 years ago (reasonable age limit)
        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.YEAR, -100)
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
        
        datePickerDialog.show()
    }


// --- Result


    data class Result(
        val name: String,
        val surname: String,
        val position: String,
        val number: String,
        val dateOfBirth: String
    )
}