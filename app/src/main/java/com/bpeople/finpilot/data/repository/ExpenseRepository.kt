package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.ExpenseEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val _expenseEntries = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    private var listenerRegistration: ListenerRegistration? = null

    init {
        attachListener(auth.currentUser?.uid)
        auth.addAuthStateListener { firebaseAuth ->
            attachListener(firebaseAuth.currentUser?.uid)
        }
    }

    fun observeExpenses(): Flow<List<ExpenseEntry>> = _expenseEntries.asStateFlow()

    fun observeExpensesPaged(categoryFilter: String?): Flow<PagingData<ExpenseEntry>> = Pager(
        config = PagingConfig(
            pageSize = HISTORY_PAGE_SIZE,
            initialLoadSize = HISTORY_PAGE_SIZE,
            prefetchDistance = HISTORY_PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { ExpensePagingSource(categoryFilter) },
    ).flow

    suspend fun addExpense(entry: ExpenseEntry) {
        val uid = auth.currentUser?.uid ?: error("User must be signed in to add expenses")
        val collection = firestore.collection("users").document(uid).collection("expenses")
        val documentRef = if (entry.id.isBlank()) collection.document() else collection.document(entry.id)
        val payload = entry.copy(id = documentRef.id, userId = uid)
        documentRef.set(payload).await()
    }

    suspend fun deleteExpense(id: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("expenses").document(id).delete().await()
    }

    private fun attachListener(uid: String?) {
        listenerRegistration?.remove()
        listenerRegistration = null

        if (uid.isNullOrBlank()) {
            _expenseEntries.value = emptyList()
            return
        }

        listenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                _expenseEntries.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ExpenseEntry::class.java)?.copy(
                        id = if (doc.id.isNotBlank()) doc.id else "",
                        userId = uid,
                    )
                }
            }
    }

    private inner class ExpensePagingSource(
        private val categoryFilter: String?,
    ) : PagingSource<DocumentSnapshot, ExpenseEntry>() {

        override fun getRefreshKey(state: PagingState<DocumentSnapshot, ExpenseEntry>): DocumentSnapshot? = null

        override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, ExpenseEntry> {
            val uid = auth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            return runCatching {
                var query: Query = firestore.collection("users")
                    .document(uid)
                    .collection("expenses")
                    .orderBy("date", Query.Direction.DESCENDING)

                val normalizedCategory = categoryFilter?.takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
                if (normalizedCategory != null) {
                    query = query.whereEqualTo("category", normalizedCategory)
                }

                val keyedQuery = params.key?.let { query.startAfter(it) } ?: query
                val snapshot = keyedQuery.limit(params.loadSize.toLong()).get().await()
                val entries = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ExpenseEntry::class.java)?.copy(
                        id = if (doc.id.isNotBlank()) doc.id else "",
                        userId = uid,
                    )
                }

                LoadResult.Page(
                    data = entries,
                    prevKey = null,
                    nextKey = snapshot.documents.lastOrNull(),
                )
            }.getOrElse { error ->
                LoadResult.Error(error)
            }
        }
    }

    private companion object {
        const val HISTORY_PAGE_SIZE = 10
        const val HISTORY_PREFETCH_DISTANCE = 1
    }
}