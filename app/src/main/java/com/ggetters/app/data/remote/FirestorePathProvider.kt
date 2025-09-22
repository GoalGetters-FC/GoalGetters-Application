package com.ggetters.app.data.remote

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePathProvider @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /** Root collection of all teams */
    fun teamCollection(): CollectionReference =
        firestore.collection("teams")

    /** Specific team document */
    fun teamDocument(teamId: String): DocumentReference =
        teamCollection().document(teamId)

    /** Subcollection: users under a specific team */
    fun usersCollection(teamId: String): CollectionReference =
        teamDocument(teamId).collection("users")

    /** Subcollection: events under a team */
    fun eventsCollection(teamId: String): CollectionReference =
        teamDocument(teamId).collection("events")

    /** Subcollection: attendance under a specific event */
    fun attendanceCollection(teamId: String, eventId: String): CollectionReference =
        eventsCollection(teamId).document(eventId).collection("attendance")

    /** Add others here as needed... */
}
