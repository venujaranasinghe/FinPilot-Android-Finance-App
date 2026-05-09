package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.ExpenseEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

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

    suspend fun addExpense(entry: ExpenseEntry) {
        val uid = auth.currentUser?.uid ?: error("User must be signed in to add expenses")
        val collection = firestore.collection("users").document(uid).collection("expenses")
        val documentRef = if (entry.id.isBlank()) collection.document() else collection.document(entry.id)
        val payload = entry.copy(id = documentRef.id, userId = uid)
        documentRef.set(payload).await()
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
}
