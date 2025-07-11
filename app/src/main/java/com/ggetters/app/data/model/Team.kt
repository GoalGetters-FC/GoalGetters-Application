// com/ggetters/app/data/model/Team.kt

package com.ggetters.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.ServerTimestamp
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.model.supers.AuditableEntity
import com.ggetters.app.data.model.supers.CodedEntity
import com.ggetters.app.data.model.supers.KeyedEntity
import com.ggetters.app.data.model.supers.StainableEntity
import com.ggetters.app.data.model.supers.StashableEntity
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "team",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["code"], unique = true),
    ]
)
data class Team(

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

    
    @ColumnInfo(name = "code")
    override var code: String? = null,

    
    // --- Attributes

    
    @ColumnInfo(name = "name")
    var name: String,

    
    ) : KeyedEntity, CodedEntity, AuditableEntity, StainableEntity {
    companion object {
        const val TAG = "Team"
    }
}



// TODO: READ THIS RECOMMENDATION AND GIMME THOUGHTS ON IT

/*
Quick Recommendations by Layer
1. Dependency Injection (DI)
Bind Firestore data sources (TeamFirestore, UserFirestore) so you can inject them directly.

Consolidate Modules: Merge any overlapping providers (e.g. if you have separate DataModule and ConDatabaseModule, fold into one).

Named Bindings: Continue using @Named for online/offline repos, but also bind your raw DAOs and Firestore clients for easier testing.

2. Room Database & Converters
Single Database: Merge AppDatabase + ConDatabase into one @Database annotated class.

TypeConverters:

Add an InstantConverter for your Instant fields.

Ensure your JSON converter (for Lineup.spotsJson) is registered.

Migrations Stubbed: Even on v1, declare empty Migration objects so future schema changes are smooth.

3. DAOs
Base DAO: Introduce a generic BaseDao<T> interface for common CRUD (insert, delete, upsertAll).

Reactive Streams: Return Flow<…> for all @Query methods so your UI layers get live updates.

Indexes: Add @Index on any column you frequently filter by (e.g. code, date_of_event, player_id).

4. Data Models (Entities)
Soft-Delete Support: Implement StashableEntity on entities where you need soft-delete (add a stashedAt column).

Nullability: Audit fields should be non-null; optional fields (location, endAt, description) should be nullable.

Consistency with Supers: Each entity should implement the correct mix of KeyedEntity, CodedEntity, AuditableEntity, StainableEntity, and (where needed) StashableEntity.

5. Firestore Data Sources
One-Shot Fetch: Provide both observeAll() (real-time) and fetchAllOnce() (single request) to avoid hanging on .first().

Error Wrapping: Convert Firestore exceptions into a unified DataError or Resource.Error so repos can handle retries uniformly.

Batch Writes: Add saveAll(List<Team>) methods to reduce round-trips during sync.

6. Repositories
Base Repository Interface: Define a BaseRepository<T> with save(), delete(), getById(), getAll(), and sync().

SyncManager: Extract two-way sync logic into a SyncManager class that batches flushing isDirty flags, merges remotes, and resolves conflicts.

Conflict Strategy: Implement a default “last-write-wins” or timestamp-based merge, and allow per-entity overrides (e.g. custom hooks for PerformanceLog).

7. Testing & Quality
DAO Tests: In-memory Room tests for each DAO.

Repository Tests: Mock TeamFirestore and TeamDao to verify CombinedTeamRepository sync logic.

Migration Tests: Write tests that run your Migration objects against a pre-v1 schema.

CI Integration: Run these tests in GitHub Actions to catch regressions early.

8. Bonus Best Practices
Use sealed class Resource<T> in your repos/VMs to model loading, success, and error states.

Define Relation POJOs (EventWithLineup, EventWithAttendance) for single-call aggregates.

Document Conventions: Keep a README.md in data/ that summarizes naming, supers usage, and sync patterns.
 */