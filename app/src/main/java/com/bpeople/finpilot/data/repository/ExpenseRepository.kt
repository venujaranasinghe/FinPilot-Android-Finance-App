package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.ExpenseEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _expenseEntries = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _hasMore = MutableStateFlow(true)
    private var lastDocument: DocumentSnapshot? = null
    private val PAGE_SIZE = 15

    init {
        val initialUid = auth.currentUser?.uid
        if (!initialUid.isNullOrBlank()) {
            repositoryScope.launch {
                resetAndLoadFirstPage(initialUid)
            }
        }
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            repositoryScope.launch {
                if (uid.isNullOrBlank()) {
                    clearPagination()
                } else {
                    resetAndLoadFirstPage(uid)
                }
            }
        }
    }

    fun observeExpenses(): Flow<List<ExpenseEntry>> = _expenseEntries.asStateFlow()
    fun observeIsLoading(): Flow<Boolean> = _isLoading.asStateFlow()
    fun observeHasMore(): Flow<Boolean> = _hasMore.asStateFlow()

    private fun clearPagination() {
        _expenseEntries.value = emptyList()
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
                .collection("expenses")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE.toLong())

            val snapshot = query.get().await()
            val documents = snapshot.documents
            val entries = documents.mapNotNull { doc ->
                doc.toObject(ExpenseEntry::class.java)?.copy(
                    id = if (doc.id.isNotBlank()) doc.id else "",
                    userId = uid,
                )
            }
            _expenseEntries.value = entries
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
                .collection("expenses")
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
                doc.toObject(ExpenseEntry::class.java)?.copy(
                    id = if (doc.id.isNotBlank()) doc.id else "",
                    userId = uid,
                )
            }
            _expenseEntries.value = _expenseEntries.value + newEntries
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

    suspend fun addExpense(entry: ExpenseEntry) {
        val uid = auth.currentUser?.uid ?: error("User must be signed in to add expenses")
        val collection = firestore.collection("users").document(uid).collection("expenses")
        val documentRef = if (entry.id.isBlank()) collection.document() else collection.document(entry.id)
        val payload = entry.copy(id = documentRef.id, userId = uid)
        documentRef.set(payload).await()

        val currentList = _expenseEntries.value
        val index = currentList.indexOfFirst { it.id == payload.id }
        if (index >= 0) {
            _expenseEntries.value = currentList.map { if (it.id == payload.id) payload else it }
        } else {
            val newList = (currentList + payload).sortedByDescending { it.date?.toDate()?.time ?: 0L }
            _expenseEntries.value = newList
        }
    }

    suspend fun deleteExpense(id: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("expenses").document(id).delete().await()

        _expenseEntries.value = _expenseEntries.value.filter { it.id != id }
    }
}