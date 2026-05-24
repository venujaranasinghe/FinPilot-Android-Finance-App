package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.local.dao.FreelanceProjectDao
import com.bpeople.finpilot.data.local.entities.RoomProject
import com.bpeople.finpilot.data.model.FreelanceProject
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
class FreelanceProjectRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val projectDao: FreelanceProjectDao,
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _projects = MutableStateFlow<List<FreelanceProject>>(emptyList())
    private var listenerRegistration: ListenerRegistration? = null

    init {
        attachListener(auth.currentUser?.uid)
        auth.addAuthStateListener { attachListener(it.currentUser?.uid) }
    }

    fun observeProjects(): Flow<List<FreelanceProject>> = _projects.asStateFlow()

    private fun attachListener(uid: String?) {
        listenerRegistration?.remove()
        listenerRegistration = null
        if (uid.isNullOrBlank()) { _projects.value = emptyList(); return }
        listenerRegistration = firestore.collection("users").document(uid)
            .collection("freelanceProjects")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FreelanceProject::class.java)?.copy(id = doc.id, userId = uid)
                }
                _projects.value = list
                repositoryScope.launch {
                    try {
                        list.forEach { project -> projectDao.insertProject(project.toRoom(uid)) }
                    } catch (_: Throwable) {}
                }
            }
    }

    fun addOrUpdate(project: FreelanceProject): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("User must be signed in")
            val col = firestore.collection("users").document(uid).collection("freelanceProjects")
            val docRef = if (project.id.isBlank()) col.document() else col.document(project.id)
            val saved = project.copy(id = docRef.id, userId = uid)
            docRef.set(saved).await()
            projectDao.insertProject(saved.toRoom(uid))
            emit(Result.Success(Unit))
        } catch (t: Throwable) { emit(Result.Error(t)) }
    }

    fun delete(projectId: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val uid = auth.currentUser?.uid ?: error("User must be signed in")
            firestore.collection("users").document(uid)
                .collection("freelanceProjects").document(projectId).delete().await()
            emit(Result.Success(Unit))
        } catch (t: Throwable) { emit(Result.Error(t)) }
    }

    private fun FreelanceProject.toRoom(uid: String) = RoomProject(
        id = id,
        userId = uid,
        clientName = clientName,
        projectTitle = projectTitle,
        agreedAmount = agreedAmount,
        paidAmount = paidAmount,
        status = status,
        entries = entries,
    )
}
