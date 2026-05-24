package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.local.dao.CryptoDao
import com.bpeople.finpilot.data.local.entities.RoomCryptoEntry
import com.bpeople.finpilot.data.model.CryptoEntry
import com.bpeople.finpilot.data.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cryptoDao: CryptoDao,
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _holdings = MutableStateFlow<List<CryptoEntry>>(emptyList())
    private var listenerReg: ListenerRegistration? = null

    init {
        attachListener(auth.currentUser?.uid)
        auth.addAuthStateListener { attachListener(it.currentUser?.uid) }
    }

    fun observeHoldings(): Flow<List<CryptoEntry>> = _holdings.asStateFlow()

    private fun attachListener(uid: String?) {
        listenerReg?.remove()
        listenerReg = null
        if (uid.isNullOrBlank()) { _holdings.value = emptyList(); return }
        listenerReg = firestore.collection("users").document(uid)
            .collection("cryptoHoldings")
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.mapNotNull { doc ->
                    doc.toObject(CryptoEntry::class.java)?.copy(id = doc.id, userId = uid)
                }
                _holdings.value = list
                repositoryScope.launch {
                    try {
                        list.forEach { entry ->
                            cryptoDao.insertOrUpdate(entry.toRoom())
                        }
                    } catch (_: Throwable) {}
                }
            }
    }

    fun addOrUpdate(entry: CryptoEntry): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("Not signed in")
            val col = firestore.collection("users").document(uid).collection("cryptoHoldings")
            val docRef = if (entry.id.isBlank()) col.document() else col.document(entry.id)
            val saved = entry.copy(id = docRef.id, userId = uid)
            docRef.set(saved).await()
            cryptoDao.insertOrUpdate(saved.toRoom())
            emit(Result.Success(Unit))
        } catch (t: Throwable) { emit(Result.Error(t)) }
    }

    fun delete(holdingId: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("Not signed in")
            firestore.collection("users").document(uid)
                .collection("cryptoHoldings").document(holdingId).delete().await()
            cryptoDao.deleteById(holdingId)
            emit(Result.Success(Unit))
        } catch (t: Throwable) { emit(Result.Error(t)) }
    }

    private fun CryptoEntry.toRoom() = RoomCryptoEntry(
        id = id,
        userId = userId,
        symbol = symbol,
        name = name,
        quantity = quantity,
        buyPriceLKR = buyPriceLKR,
        currentPriceLKR = currentPriceLKR,
        note = note,
        purchasedAtMillis = purchasedAt?.toDate()?.time,
    )
}
