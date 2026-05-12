package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.local.ExpenseDao
import com.bpeople.finpilot.data.local.IncomeDao
import com.bpeople.finpilot.data.local.ProjectDao
import com.bpeople.finpilot.data.local.GoalDao
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.model.FreelanceProject
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.data.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val goalDao: GoalDao,
    private val projectDao: ProjectDao,
) {
    private var incomeListener: ListenerRegistration? = null
    private var expenseListener: ListenerRegistration? = null
    private var goalListeners = mutableMapOf<String, ListenerRegistration?>()

    fun addIncome(entry: IncomeEntry): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("User must be signed in")
            val collection = firestore.collection("users").document(uid).collection("income")
            val docRef = if (entry.id.isBlank()) collection.document() else collection.document(entry.id)
            val payload = entry.copy(id = docRef.id, userId = uid)
            docRef.set(payload).await()
            incomeDao.upsert(payload)
            emit(Result.Success(Unit))
        } catch (t: Throwable) {
            emit(Result.Error(t))
        }
    }

    fun getIncomeByMonth(month: Int, year: Int): Flow<Result<List<IncomeEntry>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(Result.Success(emptyList()))
            close()
            return@callbackFlow
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = Timestamp(cal.time)
        cal.add(Calendar.MONTH, 1)
        val end = Timestamp(cal.time)

        val reg = firestore.collection("users").document(uid)
            .collection("income")
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThan("date", end)
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(IncomeEntry::class.java)?.copy(id = doc.id, userId = uid)
                }
                // persist locally for offline
                try {
                    val startMillis = start.toDate().time
                    val endMillis = end.toDate().time
                    // replace local month's data
                    // best-effort: delete and insert
                    GlobalScope.launch {
                        try {
                            incomeDao.deleteBetween(uid, startMillis, endMillis)
                            incomeDao.upsertAll(list)
                        } catch (_: Throwable) {}
                    }
                } catch (_: Throwable) {}
                trySend(Result.Success(list))
            }

        awaitClose { reg.remove() }
    }

    fun addExpense(entry: ExpenseEntry): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("User must be signed in")
            val collection = firestore.collection("users").document(uid).collection("expenses")
            val docRef = if (entry.id.isBlank()) collection.document() else collection.document(entry.id)
            val payload = entry.copy(id = docRef.id, userId = uid)
            docRef.set(payload).await()
            expenseDao.upsert(payload)
            emit(Result.Success(Unit))
        } catch (t: Throwable) {
            emit(Result.Error(t))
        }
    }

    fun getExpensesByMonth(month: Int, year: Int, category: String? = null): Flow<Result<List<ExpenseEntry>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(Result.Success(emptyList()))
            close()
            return@callbackFlow
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = Timestamp(cal.time)
        cal.add(Calendar.MONTH, 1)
        val end = Timestamp(cal.time)

        var query = firestore.collection("users").document(uid).collection("expenses")
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThan("date", end)

        if (!category.isNullOrBlank()) {
            query = query.whereEqualTo("category", category)
        }
        val reg = query.orderBy("date").addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ExpenseEntry::class.java)?.copy(id = doc.id, userId = uid)
            }
            try {
                val startMillis = start.toDate().time
                val endMillis = end.toDate().time
                GlobalScope.launch {
                    try {
                        expenseDao.deleteBetween(uid, startMillis, endMillis)
                        expenseDao.upsertAll(list)
                    } catch (_: Throwable) {}
                }
            } catch (_: Throwable) {}
            trySend(Result.Success(list))
        }

        awaitClose { reg.remove() }
    }

    fun getGoal(goalId: String): Flow<Result<Goal?>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(Result.Success(null))
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection("users").document(uid).collection("goals").document(goalId)
        val reg = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.Error(error))
                return@addSnapshotListener
            }
            if (snapshot == null || !snapshot.exists()) {
                trySend(Result.Success(null))
                return@addSnapshotListener
            }
            val goal = snapshot.toObject(Goal::class.java)?.copy(id = snapshot.id, userId = uid)
            // sync locally
            goal?.let { g -> GlobalScope.launch { try { goalDao.upsert(g) } catch (_: Throwable) {} } }
            trySend(Result.Success(goal))
        }

        awaitClose { reg.remove() }
    }

    fun updateGoalProgress(goalId: String, amount: Double): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("User must be signed in")
            val docRef = firestore.collection("users").document(uid).collection("goals").document(goalId)
            docRef.update("currentAmount", FieldValue.increment(amount)).await()
            // local sync will happen when listener triggers; optionally fetch and update local now
            emit(Result.Success(Unit))
        } catch (t: Throwable) {
            emit(Result.Error(t))
        }
    }

    fun addFreelanceProject(project: FreelanceProject): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("User must be signed in")
            val collection = firestore.collection("users").document(uid).collection("freelanceProjects")
            val docRef = if (project.id.isBlank()) collection.document() else collection.document(project.id)
            val payload = project.copy(id = docRef.id, userId = uid)
            docRef.set(payload).await()
            projectDao.upsert(payload)
            emit(Result.Success(Unit))
        } catch (t: Throwable) {
            emit(Result.Error(t))
        }
    }

    fun updateProject(project: FreelanceProject): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("User must be signed in")
            val collection = firestore.collection("users").document(uid).collection("freelanceProjects")
            val docRef = collection.document(project.id)
            val payload = project.copy(userId = uid)
            docRef.set(payload).await()
            projectDao.upsert(payload)
            emit(Result.Success(Unit))
        } catch (t: Throwable) {
            emit(Result.Error(t))
        }
    }
}


