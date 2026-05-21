package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.IncomeEntry
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
class IncomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val _incomeEntries = MutableStateFlow<List<IncomeEntry>>(emptyList())
    private var listenerRegistration: ListenerRegistration? = null

    init {
        attachListener(auth.currentUser?.uid)
        auth.addAuthStateListener { firebaseAuth ->
            attachListener(firebaseAuth.currentUser?.uid)
        }
    }

    fun observeIncome(): Flow<List<IncomeEntry>> = _incomeEntries.asStateFlow()

    fun observeIncomePaged(sourceFilter: String?): Flow<PagingData<IncomeEntry>> = Pager(
        config = PagingConfig(
            pageSize = HISTORY_PAGE_SIZE,
            initialLoadSize = HISTORY_PAGE_SIZE,
            prefetchDistance = HISTORY_PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { IncomePagingSource(sourceFilter) },
    ).flow

    suspend fun addIncome(entry: IncomeEntry) {
        val uid = auth.currentUser?.uid ?: error("User must be signed in to add income")
        val collection = firestore.collection("users").document(uid).collection("income")
        val documentRef = if (entry.id.isBlank()) collection.document() else collection.document(entry.id)
        val payload = entry.copy(id = documentRef.id, userId = uid)
        documentRef.set(payload).await()
    }

    suspend fun deleteIncome(id: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("income").document(id).delete().await()
    }

    private fun attachListener(uid: String?) {
        listenerRegistration?.remove()
        listenerRegistration = null

        if (uid.isNullOrBlank()) {
            _incomeEntries.value = emptyList()
            return
        }

        listenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("income")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                _incomeEntries.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(IncomeEntry::class.java)?.copy(
                        id = if (doc.id.isNotBlank()) doc.id else "",
                        userId = uid,
                    )
                }
            }
    }

    private inner class IncomePagingSource(
        private val sourceFilter: String?,
    ) : PagingSource<DocumentSnapshot, IncomeEntry>() {

        override fun getRefreshKey(state: PagingState<DocumentSnapshot, IncomeEntry>): DocumentSnapshot? = null

        override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, IncomeEntry> {
            val uid = auth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            return runCatching {
                var query: Query = firestore.collection("users")
                    .document(uid)
                    .collection("income")
                    .orderBy("date", Query.Direction.DESCENDING)

                val normalizedSource = sourceFilter?.takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
                if (normalizedSource != null) {
                    query = query.whereEqualTo("source", normalizedSource)
                }

                val keyedQuery = params.key?.let { query.startAfter(it) } ?: query
                val snapshot = keyedQuery.limit(params.loadSize.toLong()).get().await()
                val entries = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(IncomeEntry::class.java)?.copy(
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