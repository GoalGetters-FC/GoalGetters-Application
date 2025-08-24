package com.ggetters.app.ui.central.viewmodels

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import com.google.android.material.textfield.TextInputEditText
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Calendar

object EventFormPickers {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun attachDatePicker(
        context: Context,
        input: TextInputEditText,
        initial: LocalDate? = null,
        onSelected: (LocalDate) -> Unit
    ) {
        input.setOnClickListener {
            val now = Calendar.getInstance()
            initial?.let {
                now.set(Calendar.YEAR, it.year)
                now.set(Calendar.MONTH, it.monthValue - 1)
                now.set(Calendar.DAY_OF_MONTH, it.dayOfMonth)
            }
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    val picked = LocalDate.of(y, m + 1, d)
                    input.setText(picked.format(dateFmt))
                    onSelected(picked)
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    fun attachTimePicker(
        context: Context,
        input: TextInputEditText,
        initial: LocalTime? = null,
        onSelected: (LocalTime) -> Unit
    ) {
        input.setOnClickListener {
            val now = LocalTime.now()
            val h = initial?.hour ?: now.hour
            val m = initial?.minute ?: now.minute
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val picked = LocalTime.of(hour, minute)
                    input.setText(picked.format(timeFmt))
                    onSelected(picked)
                },
                h, m, true
            ).show()
        }
    }

    fun combine(date: LocalDate?, time: LocalTime?): Instant? =
        if (date != null && time != null)
            ZonedDateTime.of(date, time, ZoneId.systemDefault()).toInstant()
        else null
}
