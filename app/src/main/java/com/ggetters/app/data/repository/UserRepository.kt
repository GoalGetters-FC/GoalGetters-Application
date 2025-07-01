package com.ggetters.app.data.repository

/**
 * com.ggetters.app.data.repository
 *
 * Contains the “single source of truth” abstractions that coordinate
 * between local (Room) and remote (Firestore) data sources.
 *
 * Each Repository:
 *  - Exposes domain‐friendly methods (e.g. observeUser, saveUser, syncUser)
 *  - Knows how to map Entities ↔ DTOs
 *  - Decides when to read/write locally vs. remotely
 *
 * Examples:
 *  - UserRepository
 *  - TeamRepository
 */

class UserRepository {
}