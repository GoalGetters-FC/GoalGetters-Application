// app/src/main/java/com/ggetters/app/data/di/FirestoreModule.kt
package com.ggetters.app.data.di

import com.ggetters.app.data.remote.FirestorePathProvider
import com.ggetters.app.data.remote.firestore.BroadcastFirestore
import com.ggetters.app.data.remote.firestore.BroadcastStatusFirestore
import com.ggetters.app.data.remote.firestore.NotificationFirestore
import com.ggetters.app.data.remote.firestore.TeamFirestore
import com.ggetters.app.data.remote.firestore.UserFirestore
import com.ggetters.app.data.remote.firestore.EventFirestore
import com.ggetters.app.data.remote.firestore.MatchEventFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
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

    @Provides
    @Singleton
    fun provideFirestorePathProvider(
        firestore: FirebaseFirestore
    ): FirestorePathProvider =
        FirestorePathProvider(firestore)


    /** Your wrapper for the "team" collection */
    @Provides
    @Singleton
    fun provideTeamFirestore(
        firestore: FirebaseFirestore,
        pathProvider: FirestorePathProvider
    ): TeamFirestore =
        TeamFirestore( pathProvider)

    /** Wrapper for the "users" collection(s) (uses collectionGroup) */
    @Provides
    @Singleton
    fun provideUserFirestore(
        paths: FirestorePathProvider,
        firestore: FirebaseFirestore
    ): UserFirestore = UserFirestore(paths, db = firestore)

    /** Your wrapper for the "Broadcast" collection */
    @Provides @Singleton
    fun provideBroadcastFirestore(firestore: FirebaseFirestore): BroadcastFirestore =
        BroadcastFirestore(firestore)

    /** Your wrapper for the "BroadcastStatus" collection */
    @Provides @Singleton
    fun provideBroadcastStatusFirestore(firestore: FirebaseFirestore): BroadcastStatusFirestore =
        BroadcastStatusFirestore(firestore)

    @Provides @Singleton
    fun provideEventFirestore(paths: FirestorePathProvider): EventFirestore =
        EventFirestore(paths)

    @Provides @Singleton
    fun provideNotificationFirestore(firestore: FirebaseFirestore): NotificationFirestore =
        NotificationFirestore(firestore)

    @Provides @Singleton
    fun provideMatchEventFirestore(firestore: FirebaseFirestore): MatchEventFirestore =
        MatchEventFirestore(firestore)

}
