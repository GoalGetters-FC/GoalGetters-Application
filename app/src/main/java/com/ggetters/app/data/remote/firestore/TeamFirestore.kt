package com.ggetters.app.data.remote.firestore

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.ggetters.app.data.remote.FirestorePathProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import java.time.format.DateTimeParseException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

@Singleton
class TeamFirestore @Inject constructor(
    private val paths: FirestorePathProvider
) {
    private val teamsCol = paths.teamCollection()
    private val db = FirebaseFirestore.getInstance()

    // ---------- Public API ----------

    fun observeAll(): Flow<List<Team>> = callbackFlow {
        Clogger.i("TeamFirestore", "Observing all teams in Firestore")
        val subscription = teamsCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error); return@addSnapshotListener
            }
            val teams = snapshot?.documents?.mapNotNull { it.toTeam() }.orEmpty()
            trySend(teams).isSuccess
        }
        awaitClose { subscription.remove() }
    }

    suspend fun getById(id: String): Team? =
        teamsCol.document(id).get().await().toTeam()

    suspend fun getByCode(code: String): Team? {
        val result = teamsCol.whereEqualTo("code", code).limit(1).get().await()
        Clogger.i("TeamFirestore", "getByCode: Found ${result.size()} teams with code $code")
        return result.documents.firstOrNull()?.toTeam()
    }

    suspend fun save(team: Team) {
        teamsCol.document(team.id).set(team.toFirestoreMap()).await()
        Clogger.i("TeamFirestore", "Saved team: ${team.name} (${team.id})")
    }

    suspend fun delete(id: String) {
        teamsCol.document(id).delete().await()
        Clogger.i("TeamFirestore", "Deleted team with ID: $id")
    }

    suspend fun joinTeam(teamId: String, role: String = "PLAYER") {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("No logged-in user")

        val userData = mapOf(
            "role" to role,
            "joinedAt" to Timestamp.now()
        )

        paths.usersCollection(teamId)
            .document(uid)
            .set(userData)
            .await()

        Clogger.i("TeamFirestore", "User $uid joined team $teamId as $role")
    }

    /** Remove the current user from a team's membership (safe for all roles). */
    suspend fun leaveOrDelete(teamId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("No logged-in user")

        // 1) Remove membership under the team
        paths.usersCollection(teamId)               // /teams/{teamId}/users
            .document(uid)
            .delete()
            .await()

        // 2) Remove mirrored membership under the user (if you keep this mirror)
        runCatching {
            db.collection("users")                  // /users/{uid}/memberships/{teamId}
                .document(uid)
                .collection("memberships")
                .document(teamId)
                .delete()
                .await()
        }

        Clogger.i("TeamFirestore", "User $uid left team $teamId")
    }

    /**
     * Returns a Flow of teams where the current user (Auth ID) is in the team's users subcollection.
     */
    fun observeTeamsForCurrentUser(): Flow<List<Team>> = callbackFlow {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            trySend(emptyList<Team>()); close(); return@callbackFlow
        }

        val subscription = teamsCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error); return@addSnapshotListener
            }

            val docs = snapshot?.documents.orEmpty()
            if (docs.isEmpty()) {
                trySend(emptyList()).isSuccess
            } else {
                // Check membership per team in a coroutine tied to this flow
                launch {
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                    val included = docs.mapNotNull { teamSnap ->
                        val userSnap = teamSnap.reference
                            .collection("users")
                            .document(currentUid)
                            .get()
                            .await()
                        if (userSnap.exists()) teamSnap.toTeam() else null
                    }
                    trySend(included).isSuccess
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    /** One-shot fetch of all teams you belong to. */
    suspend fun fetchTeamsForCurrentUser(): List<Team> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val allTeamsSnap = teamsCol.get().await()
        return allTeamsSnap.documents.mapNotNull { teamDoc ->
            val userSnap = paths.usersCollection(teamDoc.id).document(uid).get().await()
            if (userSnap.exists()) teamDoc.toTeam() else null
        }
    }

    // ---------- Mapping helpers (Firestore <-> Domain) ----------

    private fun DocumentSnapshot.readInstant(vararg keys: String): Instant? {
        for (k in keys) {
            val v = get(k)
            when (v) {
                is Timestamp -> return v.toDate().toInstant()
                is Date -> return v.toInstant()
                is Long -> return Instant.ofEpochMilli(v)
                is Double -> return Instant.ofEpochMilli(v.toLong())
                is String -> {
                    // try ISO-8601, else skip
                    try {
                        return Instant.parse(v)
                    } catch (_: DateTimeParseException) {
                        // try seconds/millis encoded as string
                        v.toLongOrNull()?.let { millis ->
                            return Instant.ofEpochMilli(
                                if (millis < 10_000_000_000L) millis * 1000 else millis
                            )
                        }
                    }
                }
            }
        }
        return null
    }

    private fun DocumentSnapshot.readString(vararg keys: String): String? {
        for (k in keys) {
            getString(k)?.let { return it }
            // If someone stored a number, we still want a string
            val any = get(k)
            if (any is Number) return any.toString()
        }
        return null
    }

    private inline fun <reified T : Enum<T>> safeEnumFromAny(value: Any?, default: T): T {
        return when (value) {
            is String -> runCatching { enumValueOf<T>(value) }.getOrDefault(default)
            is Number -> {
                val ordinal = value.toInt()
                val constants = enumValues<T>()
                if (ordinal in constants.indices) constants[ordinal] else default
            }
            else -> default
        }
    }

    private fun DocumentSnapshot.toTeam(): Team? {
        // required
        val id = this.id
        val name = readString("name") ?: return null

        // created/updated at: accept multiple keys and formats
        val createdAt = readInstant("createdAt", "created_at") ?: Instant.now()
        val updatedAt = readInstant("updatedAt", "updated_at") ?: createdAt

        val code = readString("code")
        val alias = readString("alias")
        val description = readString("description")
        val yearFormed = readString("yearFormed", "year_formed")
        val contactCell = readString("contactCell", "contact_cell")
        val contactMail = readString("contactMail", "contact_mail")
        val clubAddress = readString("clubAddress", "club_address")

        // enums may be stored as string names or ordinals; handle both
        val compositionAny = get("composition")
        val denominationAny = get("denomination")

        val composition = safeEnumFromAny(compositionAny, TeamComposition.UNISEX_MALE)
        val denomination = safeEnumFromAny(denominationAny, TeamDenomination.OPEN)

        return Team(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            code = code,
            name = name,
            alias = alias,
            description = description,
            composition = composition,
            denomination = denomination,
            yearFormed = yearFormed,
            contactCell = contactCell,
            contactMail = contactMail,
            clubAddress = clubAddress,
            isActive = false // local-only
        )
    }

    private fun Team.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "code" to code,
        "name" to name,
        "alias" to alias,
        "description" to description,
        "composition" to composition.name,
        "denomination" to denomination.name,
        "yearFormed" to yearFormed,
        "contactCell" to contactCell,
        "contactMail" to contactMail,
        "clubAddress" to clubAddress,
        // Instant -> Timestamp via Date
        "createdAt" to Timestamp(Date.from(createdAt)),
        "updatedAt" to Timestamp(Date.from(updatedAt))
        // excluded: stainedAt, isActive
    )

    private inline fun <reified T : Enum<T>> safeEnum(value: String, default: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(default)


}
