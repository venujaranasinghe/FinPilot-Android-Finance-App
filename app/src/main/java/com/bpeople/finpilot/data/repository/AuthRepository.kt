package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: Flow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) AuthResult.Success
            else AuthResult.Error("Login failed")
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            if (result.user != null) AuthResult.Success
            else AuthResult.Error("Google Sign-In failed")
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun register(email: String, password: String, displayName: String): AuthResult = withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Save profile to Firestore BEFORE signing out
                val profile = mapOf(
                    "uid" to user.uid,
                    "displayName" to displayName,
                    "email" to email,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "baseCurrency" to "LKR"
                )
                try {
                    firestore.collection("users").document(user.uid).set(profile).await()
                } catch (e: Exception) {
                    android.util.Log.e("AuthRepository", "Firestore profile write failed for uid=${user.uid}", e)
                    return@withContext AuthResult.Error(
                        "Account created but profile setup failed. Please try again."
                    )
                }
                
                user.sendEmailVerification().await()
                auth.signOut()
                AuthResult.Success
            } else {
                AuthResult.Error("Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun resendVerificationEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user == null) {
                AuthResult.Error("Login failed")
            } else {
                user.reload().await()
                if (user.isEmailVerified) {
                    auth.signOut()
                    AuthResult.Error("Email is already verified")
                } else {
                    user.sendEmailVerification().await()
                    auth.signOut()
                    AuthResult.Success
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun deleteAccount(): AuthResult {
        val user = auth.currentUser ?: return AuthResult.Error("No active account found")
        return try {
            user.delete().await()
            auth.signOut()
            AuthResult.Success
        } catch (e: Exception) {
            if (e is FirebaseAuthRecentLoginRequiredException) {
                AuthResult.Error("Please re-authenticate before deleting your account")
            } else if (e is FirebaseAuthInvalidUserException) {
                AuthResult.Error("Account is no longer available")
            } else {
                AuthResult.Error(mapFirebaseError(e))
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun mapFirebaseError(e: Exception): String {
        if (e is FirebaseAuthException) {
            return when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
                is FirebaseAuthInvalidUserException -> when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                    "ERROR_USER_DISABLED" -> "This account has been disabled"
                    else -> "This account is unavailable"
                }
                is FirebaseAuthUserCollisionException -> "An account with this email already exists"
                else -> when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Please enter a valid email address"
                    "ERROR_WRONG_PASSWORD" -> "Incorrect email or password"
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later"
                    "ERROR_NETWORK_REQUEST_FAILED" -> "No internet connection"
                    else -> "Something went wrong"
                }
            }
        }
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