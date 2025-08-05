package com.ggetters.app.ui.central.models

// TODO: Backend - Implement team data models and validation
// TODO: Backend - Add team data serialization and deserialization
// TODO: Backend - Implement team data caching and offline support
// TODO: Backend - Add team data synchronization across devices
// TODO: Backend - Implement team data analytics and tracking

data class Team(
    val id: String,
    val name: String,
    val alias: String?,
    val description: String?,
    val composition: TeamComposition,
    val denomination: TeamDenomination,
    val contact: TeamContact,
    val isCurrentTeam: Boolean = false,
    val memberCount: Int = 0,
    val role: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class TeamComposition {
    UNISEX_MALE,
    UNISEX_FEMALE,
    MALE_ONLY,
    FEMALE_ONLY
}

enum class TeamDenomination {
    ALL_U15,
    U15_BOYS,
    U15_GIRLS,
    U16,
    U17,
    U18,
    SENIORS
}

data class TeamContact(
    val email: String?,
    val phone: String?,
    val website: String?
) 