package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "lineup",
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["created_by"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("event_id"),                        // main lookup
        Index("created_by"),                      // secondary
        Index(value = ["event_id", "created_by"]) // combined (optional, if used often)
    ]
)
data class Lineup(
    @PrimaryKey
    @DocumentId
    @ColumnInfo(name = "id")
    override val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "created_at")
    override val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    override var updatedAt: Instant = Instant.now(),

    @Exclude
    @ColumnInfo(name = "stained_at")
    override var stainedAt: Instant? = null,

    // — core fields —

    @ColumnInfo(name = "event_id")
    val eventId: String,

    @ColumnInfo(name = "created_by")
    val createdBy: String? = null,

    @ColumnInfo(name = "formation")
    val formation: String,

    /** JSON‐serialized list of spots via your TypeConverter **/
    @ColumnInfo(name = "spots")
    val spots: List<LineupSpot> = emptyList()

) : KeyedEntity, AuditableEntity, StainableEntity {
    companion object { const val TAG = "Lineup" }
}


// TODO: finish data-layer for missing models
//
// 1) Configuration
//    • Room: ConfigurationDao (CRUD + deleteAll())
//    • Firestore: ConfigurationFirestore (observeAll, fetchOnce, save, delete)
//    • Repos: Offline/Online/CombinedConfigurationRepository + ConfigurationRepositoryModule
//
// 2) Lineup
//    • Room: LineupDao (CRUD + deleteAll())
//    • Firestore: LineupFirestore (observeAll, fetchOnce, save, delete)
//    • Repos: Offline/Online/CombinedLineupRepository + LineupRepositoryModule
//
// 3) PerformanceLog
//    • Room: PerformanceLogDao (CRUD + deleteAll())
//    • Firestore: PerformanceLogFirestore (observeAll, fetchOnce, save, delete)
//    • Repos: Offline/Online/CombinedPerformanceLogRepository + PerformanceLogRepositoryModule
//
// Suggested next-wave pipelines (as your feature set grows):
//  • PlayerStats (aggregate minutes, goals, cards…)
//  • ParentConsent (guardianship & consent records)
//  • CoachProfile (extra coach settings & prefs)
//  • NotificationSubscription (topic-based FCM & in-app feeds)
//  • AppSettings (global/team-level config synced from Firestore)
//
// Once these are in place you’ll have true end-to-end support for every core entity.


// Lineup: We need Firestore, Dao, and Repository layers
//  • Firestore: LineupFirestore (observeAll, fetchOnce, save, delete)
//  • Dao: LineupDao (CRUD + deleteAll())
//  • Repos: Offline/Online/CombinedLineupRepository + LineupRepository (Interface that extends CrudRepository<Lineup>)
//  • Di: LineupRepositoryModule