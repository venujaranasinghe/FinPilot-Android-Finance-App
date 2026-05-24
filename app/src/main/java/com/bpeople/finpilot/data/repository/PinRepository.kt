package com.bpeople.finpilot.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.pinDataStore: DataStore<Preferences> by preferencesDataStore(name = "pin_prefs")

@Singleton
class PinRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private object Keys {
        val PIN_HASH = stringPreferencesKey("pin_hash")
    }

    private val pinHash: Flow<String?> = context.pinDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[Keys.PIN_HASH] }

    val hasPinSet: Flow<Boolean> = pinHash.map { !it.isNullOrBlank() }

    suspend fun savePin(pin: String) {
        context.pinDataStore.edit { it[Keys.PIN_HASH] = sha256(pin) }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val stored = pinHash.first()
        return stored != null && stored == sha256(pin)
    }

    suspend fun clearPin() {
        context.pinDataStore.edit { it.remove(Keys.PIN_HASH) }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
