package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: Flow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun getCurrentUserId(): String? = auth?.currentUser?.uid
    fun isLoggedIn(): Boolean = auth?.currentUser != null

    suspend fun login(email: String, password: String): AuthResult {
        if (auth == null) return AuthResult.Error("Auth not initialized")
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) AuthResult.Success
            else AuthResult.Error("Login failed")
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun register(email: String, password: String): AuthResult {
        if (auth == null) return AuthResult.Error("Auth not initialized")
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            if (result.user != null) AuthResult.Success
            else AuthResult.Error("Registration failed")
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    fun signOut() {
        auth?.signOut()
    }

    private fun mapFirebaseError(e: Exception): String {
        return when {
            e.message?.contains("badly formatted", ignoreCase = true) == true ->
                "Please enter a valid email address"
            e.message?.contains("password is invalid", ignoreCase = true) == true ->
                "Incorrect password"
            e.message?.contains("no user record", ignoreCase = true) == true ->
                "No account found with this email"
            e.message?.contains("already in use", ignoreCase = true) == true ->
                "An account with this email already exists"
            e.message?.contains("at least 6 characters", ignoreCase = true) == true ->
                "Password must be at least 6 characters"
            e.message?.contains("network", ignoreCase = true) == true ->
                "No internet connection"
            else -> e.message ?: "Something went wrong"
        }
    }
}