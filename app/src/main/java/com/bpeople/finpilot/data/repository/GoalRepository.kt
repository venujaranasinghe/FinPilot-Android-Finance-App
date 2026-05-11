package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.Goal
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@Singleton
class GoalRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    private var listenerRegistration: ListenerRegistration? = null

    init {
        attachListener(auth.currentUser?.uid)
        auth.addAuthStateListener { firebaseAuth ->
            attachListener(firebaseAuth.currentUser?.uid)
        }
    }

    fun observeGoals(): Flow<List<Goal>> = _goals.asStateFlow()

    fun observeActiveGoal(): Flow<Goal?> = _goals.map { goals ->
        goals.firstOrNull { it.isActive }
    }

    suspend fun upsertGoal(goal: Goal) {
        val uid = auth.currentUser?.uid ?: error("User must be signed in to save a goal")
        val id = if (goal.id.isBlank()) UUID.randomUUID().toString() else goal.id
        val collection = firestore.collection("users").document(uid).collection("goals")
        val payload = goal.copy(id = id, userId = uid)
        collection.document(id).set(payload).await()
    }

    /**
     * Appends a single savings-log entry to
     * `users/{uid}/goals/{goalId}/savingsLogs/{autoId}`.
     * Each document contains: amount (Double) and timestamp (Timestamp).
     */
    suspend fun logSavingsEntry(goalId: String, amount: Double) {
        val uid = auth.currentUser?.uid ?: return
        val entry = hashMapOf(
            "amount" to amount,
            "timestamp" to Timestamp.now()
        )
        firestore
            .collection("users")
            .document(uid)
            .collection("goals")
            .document(goalId)
            .collection("savingsLogs")
            .add(entry)
            .await()
    }

    /**
     * Returns a real-time [Flow] of savings-log entries (timestampMillis to amount)
     * created within the last 6 months for the given goal.
     */
    fun observeSavingsLogs(goalId: String): Flow<List<Pair<Long, Double>>> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        val sixMonthsAgo = Timestamp(
            Date(System.currentTimeMillis() - 6L * 30 * 24 * 60 * 60 * 1000)
        )
        return callbackFlow {
            val reg = firestore
                .collection("users")
                .document(uid)
                .collection("goals")
                .document(goalId)
                .collection("savingsLogs")
                .whereGreaterThan("timestamp", sixMonthsAgo)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val entries = snapshot.documents.mapNotNull { doc ->
                        val amount = doc.getDouble("amount") ?: return@mapNotNull null
                        val ts = doc.getTimestamp("timestamp") ?: return@mapNotNull null
                        Pair(ts.toDate().time, amount)
                    }
                    trySend(entries)
                }
            awaitClose { reg.remove() }
        }
    }

    private fun attachListener(uid: String?) {
        listenerRegistration?.remove()
        listenerRegistration = null

        if (uid.isNullOrBlank()) {
            _goals.value = emptyList()
            return
        }

        listenerRegistration = firestore
            .collection("users")
            .document(uid)
            .collection("goals")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _goals.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Goal::class.java)?.copy(
                        id = doc.id,
                        userId = uid,
                    )
                }
            }
    }
}
