package com.ggetters.app.core.validation

import android.util.Patterns
import com.ggetters.app.core.utils.DateUtils
import java.time.LocalDate
import java.time.Period

/**
 * Comprehensive validation utilities for user data
 */
object UserValidationUtils {
    
    /**
     * Validation result data class
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )
    
    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate name (first name or last name)
     */
    fun isValidName(name: String): Boolean {
        return name.trim().isNotBlank() && name.trim().length >= 2
    }
    
    /**
     * Validate player number
     */
    fun isValidPlayerNumber(number: String): Boolean {
        return number.trim().isNotBlank() && 
               number.trim().toIntOrNull()?.let { it in 1..99 } == true
    }
    
    /**
     * Validate date of birth
     */
    fun isValidDateOfBirth(dateString: String): Boolean {
        if (dateString.isBlank()) return true // Optional field
        
        val date = DateUtils.parseDate(dateString)
        if (date == null) return false
        
        val age = Period.between(date, LocalDate.now()).years
        return age in 5..100 // Reasonable age range for football players
    }
    
    /**
     * Validate phone number (basic validation)
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.isBlank()) return true // Optional field
        
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        return cleanPhone.length >= 10 && cleanPhone.length <= 15
    }
    
    /**
     * Validate position
     */
    fun isValidPosition(position: String): Boolean {
        if (position.isBlank()) return true // Optional field
        
        val validPositions = listOf(
            "GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD", "UNKNOWN",
            "CENTER_BACK", "FULL_BACK", "WINGER", "STRIKER"
        )
        return validPositions.contains(position.uppercase())
    }
    
    /**
     * Validate role
     */
    fun isValidRole(role: String): Boolean {
        val validRoles = listOf(
            "FULL_TIME_PLAYER", "PART_TIME_PLAYER", "COACH", "COACH_PLAYER", "OTHER"
        )
        return validRoles.contains(role.uppercase())
    }
    
    /**
     * Validate status
     */
    fun isValidStatus(status: String): Boolean {
        val validStatuses = listOf("ACTIVE", "INJURY")
        return validStatuses.contains(status.uppercase())
    }
    
    /**
     * Comprehensive user validation
     */
    fun validateUserData(
        firstName: String,
        lastName: String,
        email: String?,
        playerNumber: String?,
        dateOfBirth: String?,
        phoneNumber: String?,
        position: String?,
        role: String,
        status: String
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Required fields
        if (!isValidName(firstName)) {
            errors.add("First name is required and must be at least 2 characters")
        }
        
        if (!isValidName(lastName)) {
            errors.add("Last name is required and must be at least 2 characters")
        }
        
        // Email validation
        if (email.isNullOrBlank()) {
            errors.add("Email is required")
        } else if (!isValidEmail(email)) {
            errors.add("Please enter a valid email address")
        }
        
        // Player number validation
        if (!playerNumber.isNullOrBlank() && !isValidPlayerNumber(playerNumber)) {
            errors.add("Player number must be between 1 and 99")
        }
        
        // Date of birth validation
        if (!dateOfBirth.isNullOrBlank() && !isValidDateOfBirth(dateOfBirth)) {
            errors.add("Please enter a valid date of birth (age must be between 5 and 100)")
        }
        
        // Phone number validation
        if (!phoneNumber.isNullOrBlank() && !isValidPhoneNumber(phoneNumber)) {
            errors.add("Please enter a valid phone number")
        }
        
        // Position validation
        if (!position.isNullOrBlank() && !isValidPosition(position)) {
            errors.add("Please select a valid position")
        }
        
        // Role validation
        if (!isValidRole(role)) {
            errors.add("Please select a valid role")
        }
        
        // Status validation
        if (!isValidStatus(status)) {
            errors.add("Please select a valid status")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Get user-friendly error message
     */
    fun getErrorMessage(errors: List<String>): String {
        return when {
            errors.isEmpty() -> ""
            errors.size == 1 -> errors.first()
            else -> "Multiple validation errors:\n• ${errors.joinToString("\n• ")}"
        }
    }
}
