package com.ggetters.app.core.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Centralized date utilities for consistent date handling across the app
 */
object DateUtils {
    
    // Standard date format for storage and API (ISO 8601)
    const val STORAGE_FORMAT = "yyyy-MM-dd"
    val STORAGE_FORMATTER = DateTimeFormatter.ofPattern(STORAGE_FORMAT)
    
    // User-friendly display format
    const val DISPLAY_FORMAT = "dd/MM/yyyy"
    val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_FORMAT)
    
    // Date picker format (for DatePickerDialog)
    const val PICKER_FORMAT = "yyyy-MM-dd"
    
    /**
     * Format a LocalDate for storage (database/API)
     */
    fun formatForStorage(date: LocalDate): String {
        return date.format(STORAGE_FORMATTER)
    }
    
    /**
     * Format a LocalDate for display (user interface)
     */
    fun formatForDisplay(date: LocalDate): String {
        return date.format(DISPLAY_FORMATTER)
    }
    
    /**
     * Parse a date string from storage format
     */
    fun parseFromStorage(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, STORAGE_FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }
    
    /**
     * Parse a date string from display format
     */
    fun parseFromDisplay(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, DISPLAY_FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }
    
    /**
     * Parse a date string, trying both formats
     */
    fun parseDate(dateString: String): LocalDate? {
        return parseFromStorage(dateString) ?: parseFromDisplay(dateString)
    }
    
    /**
     * Validate if a date string is in correct format
     */
    fun isValidDate(dateString: String): Boolean {
        return parseDate(dateString) != null
    }
    
    /**
     * Get current date for date picker
     */
    fun getCurrentDate(): LocalDate = LocalDate.now()
    
    /**
     * Get date for date picker (year, month, day)
     */
    fun getDatePickerValues(date: LocalDate = getCurrentDate()): Triple<Int, Int, Int> {
        return Triple(date.year, date.monthValue - 1, date.dayOfMonth) // Month is 0-based in DatePicker
    }
    
    /**
     * Create date from date picker values
     */
    fun createDateFromPicker(year: Int, month: Int, day: Int): LocalDate {
        return LocalDate.of(year, month + 1, day) // Month is 0-based in DatePicker
    }
}
