package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.local.dao.SubscriptionDao
import com.bpeople.finpilot.data.local.entities.RoomSubscription
import com.bpeople.finpilot.data.model.Subscription
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
class SubscriptionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val subscriptionDao: SubscriptionDao,
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    private var listenerReg: ListenerRegistration? = null

    init {
        attachListener(auth.currentUser?.uid)
        auth.addAuthStateListener { attachListener(it.currentUser?.uid) }
    }

    fun observeSubscriptions(): Flow<List<Subscription>> = _subscriptions.asStateFlow()

    private fun attachListener(uid: String?) {
        listenerReg?.remove()
        listenerReg = null
        if (uid.isNullOrBlank()) { _subscriptions.value = emptyList(); return }
        listenerReg = firestore.collection("users").document(uid)
            .collection("subscriptions")
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.mapNotNull { doc ->
                    doc.toObject(Subscription::class.java)?.copy(id = doc.id, userId = uid)
                }
                _subscriptions.value = list
                repositoryScope.launch {
                    try {
                        list.forEach { sub ->
                            subscriptionDao.insertOrUpdate(sub.toRoom())
                        }
                    } catch (_: Throwable) {}
                }
            }
    }

    fun addOrUpdate(sub: Subscription): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("Not signed in")
            val col = firestore.collection("users").document(uid).collection("subscriptions")
            val docRef = if (sub.id.isBlank()) col.document() else col.document(sub.id)
            val saved = sub.copy(id = docRef.id, userId = uid)
            docRef.set(saved).await()
            subscriptionDao.insertOrUpdate(saved.toRoom())
            emit(Result.Success(Unit))
        } catch (t: Throwable) { emit(Result.Error(t)) }
    }

    fun delete(subId: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("Not signed in")
            firestore.collection("users").document(uid)
                .collection("subscriptions").document(subId).delete().await()
            subscriptionDao.deleteById(subId)
            emit(Result.Success(Unit))
        } catch (t: Throwable) { emit(Result.Error(t)) }
    }

    private fun Subscription.toRoom() = RoomSubscription(
        id = id,
        userId = userId,
        name = name,
        amountLKR = amountLKR,
        billingCycle = billingCycle,
        nextBillingMillis = nextBillingMillis,
        isActive = isActive,
        category = category,
        note = note,
    )
}
