package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.IncomeEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val _incomeEntries = MutableStateFlow<List<IncomeEntry>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _hasMore = MutableStateFlow(false)
    private var lastDocument: DocumentSnapshot? = null
    private val PAGE_SIZE = 15
    private var listenerRegistration: ListenerRegistration? = null

    init {
        val initialUid = auth.currentUser?.uid
        if (!initialUid.isNullOrBlank()) {
            attachRealtimeListener(initialUid)
        }
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                listenerRegistration?.remove()
                listenerRegistration = null
                clearPagination()
            } else {
                attachRealtimeListener(uid)
            }
        }
    }

    /**
     * Attaches a real-time Firestore snapshot listener so that any change to the
     * income collection (add, edit, delete from any device or the console) is
     * reflected immediately in [observeIncome] — satisfying the assignment's
     * real-time-dashboard requirement.
     */
    private fun attachRealtimeListener(uid: String) {
        listenerRegistration?.remove()
        _isLoading.value = true
        listenerRegistration = firestore
            .collection("users")
            .document(uid)
            .collection("income")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                _incomeEntries.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(IncomeEntry::class.java)?.copy(
                        id = if (doc.id.isNotBlank()) doc.id else "",
                        userId = uid,
                    )
                }
                _hasMore.value = false   // all entries are loaded by the listener
                _isLoading.value = false
            }
    }

    fun observeIncome(): Flow<List<IncomeEntry>> = _incomeEntries.asStateFlow()
    fun observeIsLoading(): Flow<Boolean> = _isLoading.asStateFlow()
    fun observeHasMore(): Flow<Boolean> = _hasMore.asStateFlow()

    private fun clearPagination() {
        _incomeEntries.value = emptyList()
        lastDocument = null
        _hasMore.value = false
        _isLoading.value = false
    }

    suspend fun resetAndLoadFirstPage(uid: String) {
        _isLoading.value = true
        _hasMore.value = true
        lastDocument = null
        try {
            val query = firestore.collection("users")
                .document(uid)
                .collection("income")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE.toLong())

            val snapshot = query.get().await()
            val documents = snapshot.documents
            val entries = documents.mapNotNull { doc ->
                doc.toObject(IncomeEntry::class.java)?.copy(
                    id = if (doc.id.isNotBlank()) doc.id else "",
                    userId = uid,
                )
            }
            _incomeEntries.value = entries
            if (documents.size < PAGE_SIZE) {
                _hasMore.value = false
            } else {
                lastDocument = documents.lastOrNull()
            }
        } catch (e: Exception) {
            _hasMore.value = false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadNextPage() {
        val uid = auth.currentUser?.uid ?: return
        if (_isLoading.value || !_hasMore.value) return
        _isLoading.value = true
        try {
            var query = firestore.collection("users")
                .document(uid)
                .collection("income")
                .orderBy("date", Query.Direction.DESCENDING)

            val currentLast = lastDocument
            if (currentLast != null) {
                query = query.startAfter(currentLast)
            }
            query = query.limit(PAGE_SIZE.toLong())

            val snapshot = query.get().await()
            val documents = snapshot.documents
            if (documents.isEmpty()) {
                _hasMore.value = false
                return
            }
            val newEntries = documents.mapNotNull { doc ->
                doc.toObject(IncomeEntry::class.java)?.copy(
                    id = if (doc.id.isNotBlank()) doc.id else "",
                    userId = uid,
                )
            }
            _incomeEntries.value = _incomeEntries.value + newEntries
            if (documents.size < PAGE_SIZE) {
                _hasMore.value = false
            } else {
                lastDocument = documents.lastOrNull()
            }
        } catch (e: Exception) {
            _hasMore.value = false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun addIncome(entry: IncomeEntry) {
        val uid = auth.currentUser?.uid ?: error("User must be signed in to add income")
        val collection = firestore.collection("users").document(uid).collection("income")
        val documentRef = if (entry.id.isBlank()) collection.document() else collection.document(entry.id)
        val payload = entry.copy(id = documentRef.id, userId = uid)
        documentRef.set(payload).await()

        val currentList = _incomeEntries.value
        val index = currentList.indexOfFirst { it.id == payload.id }
        if (index >= 0) {
            _incomeEntries.value = currentList.map { if (it.id == payload.id) payload else it }
        } else {
            val newList = (currentList + payload).sortedByDescending { it.date?.toDate()?.time ?: 0L }
            _incomeEntries.value = newList
        }
    }

    suspend fun deleteIncome(id: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("income").document(id).delete().await()

        _incomeEntries.value = _incomeEntries.value.filter { it.id != id }
    }
}