// app/src/main/java/com/ggetters/app/data/di/FirestoreModule.kt
package com.ggetters.app.data.di

import com.ggetters.app.data.remote.firestore.BroadcastFirestore
import com.ggetters.app.data.remote.firestore.BroadcastStatusFirestore
import com.ggetters.app.data.remote.firestore.TeamFirestore
import com.ggetters.app.data.remote.firestore.UserFirestore
import com.ggetters.app.data.remote.firestore.EventFirestore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    /** The core FirebaseFirestore client */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        Firebase.firestore

    /** Your wrapper for the "team" collection */
    @Provides
    @Singleton
    fun provideTeamFirestore(firestore: FirebaseFirestore): TeamFirestore =
        TeamFirestore(firestore)

    /** Your wrapper for the "user" collection */
    @Provides
    @Singleton
    fun provideUserFirestore(firestore: FirebaseFirestore): UserFirestore =
        UserFirestore(firestore)

    /** Your wrapper for the "Broadcast" collection */
    @Provides @Singleton
    fun provideBroadcastFirestore(firestore: FirebaseFirestore): BroadcastFirestore =
        BroadcastFirestore(firestore)

    /** Your wrapper for the "BroadcastStatus" collection */
    @Provides @Singleton
    fun provideBroadcastStatusFirestore(firestore: FirebaseFirestore): BroadcastStatusFirestore =
        BroadcastStatusFirestore(firestore)

    /** Your wrapper for the "Event" collection */
    @Provides @Singleton
    fun provideEventFirestore(firestore: FirebaseFirestore): EventFirestore =
        EventFirestore(firestore)
}
