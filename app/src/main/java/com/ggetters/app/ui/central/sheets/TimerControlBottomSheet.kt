package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import com.ggetters.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimerControlBottomSheet : BottomSheetDialogFragment() {

    private var currentMinute: Int = 0
    private var selectedMinute: Int = 0
    private var selectedSecond: Int = 0

    companion object {
        fun newInstance(currentMinute: Int): TimerControlBottomSheet {
            return TimerControlBottomSheet().apply {
                this.currentMinute = currentMinute
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_timer_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNumberPickers()
        setupActionButtons()
    }

    private fun setupNumberPickers() {
        val minutePicker = view?.findViewById<NumberPicker>(R.id.minutePicker)
        val secondPicker = view?.findViewById<NumberPicker>(R.id.secondPicker)

        // Set up minute picker (0-90 minutes)
        minutePicker?.apply {
            minValue = 0
            maxValue = 90
            value = currentMinute
            selectedMinute = currentMinute
            setOnValueChangedListener { _, _, newVal ->
                selectedMinute = newVal
            }
        }

        // Set up second picker (0-59 seconds)
        secondPicker?.apply {
            minValue = 0
            maxValue = 59
            value = 0
            selectedSecond = 0
            setOnValueChangedListener { _, _, newVal ->
                selectedSecond = newVal
            }
        }

        // Update current time display
        view?.findViewById<TextView>(R.id.currentTimeText)?.text = 
            "Current Time: ${currentMinute}:00"
    }

    private fun setupActionButtons() {
        view?.findViewById<MaterialButton>(R.id.btnSetTime)?.setOnClickListener {
            setMatchTime()
        }

        view?.findViewById<MaterialButton>(R.id.btnCancel)?.setOnClickListener {
            dismiss()
        }
    }

    private fun setMatchTime() {
        // TODO: Backend - Set match time in backend
        val totalSeconds = selectedMinute * 60 + selectedSecond
        val formattedTime = String.format("%d:%02d", selectedMinute, selectedSecond)
        
        Snackbar.make(
            requireView(),
            "Match time set to $formattedTime",
            Snackbar.LENGTH_SHORT
        ).show()
        
        dismiss()
    }
} 