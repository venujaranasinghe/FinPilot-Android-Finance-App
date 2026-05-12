package com.bpeople.finpilot.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bpeople.finpilot.BuildConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val EXCHANGE_RATES_DATASTORE_NAME = "exchange_rates"
private const val OPEN_EXCHANGE_RATES_URL = "https://openexchangerates.org/api/latest.json"
private const val CACHE_TTL_MILLIS = 6 * 60 * 60 * 1000L

private val Context.exchangeRatesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = EXCHANGE_RATES_DATASTORE_NAME
)

@Singleton
class ExchangeRatesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {
    data class ExchangeRatesSnapshot(
        val base: String = "USD",
        val rates: Map<String, Double> = emptyMap(),
        val lastUpdatedMillis: Long = 0L,
        val isStale: Boolean = true,
    )

    private object Keys {
        val BASE = stringPreferencesKey("base")
        val RATES_JSON = stringPreferencesKey("rates_json")
        val LAST_UPDATED_MILLIS = longPreferencesKey("last_updated_millis")
    }

    val rates: Flow<ExchangeRatesSnapshot> = context.exchangeRatesDataStore.data.map { prefs ->
        val base = prefs[Keys.BASE] ?: "USD"
        val lastUpdatedMillis = prefs[Keys.LAST_UPDATED_MILLIS] ?: 0L
        val ratesJson = prefs[Keys.RATES_JSON]
        val rates = if (ratesJson.isNullOrBlank()) emptyMap() else parseRatesJson(ratesJson)
        val isStale = lastUpdatedMillis == 0L ||
            System.currentTimeMillis() - lastUpdatedMillis > CACHE_TTL_MILLIS
        ExchangeRatesSnapshot(
            base = base,
            rates = rates,
            lastUpdatedMillis = lastUpdatedMillis,
            isStale = isStale,
        )
    }

    suspend fun refreshRatesIfNeeded(force: Boolean = false): Result<Unit> = runCatching {
        val snapshot = rates.first()
        if (!force && snapshot.rates.isNotEmpty() && !snapshot.isStale) return@runCatching
        fetchAndCacheRates()
    }

    fun rateToLkr(snapshot: ExchangeRatesSnapshot, currency: String): Double? {
        if (currency == "LKR") return 1.0
        val lkrRate = snapshot.rates["LKR"] ?: return null
        val currencyRate = snapshot.rates[currency] ?: return null
        if (currencyRate == 0.0) return null
        return lkrRate / currencyRate
    }

    private fun parseRatesJson(json: String): Map<String, Double> {
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(json, type)
    }

    private suspend fun fetchAndCacheRates() {
        val appId = BuildConfig.EXCHANGE_RATES_APP_ID
        if (appId.isBlank()) {
            throw IllegalStateException("Missing EXCHANGE_RATES_APP_ID")
        }

        val request = Request.Builder()
            .url("$OPEN_EXCHANGE_RATES_URL?app_id=$appId")
            .get()
            .build()

        val responseBody = withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Exchange rates request failed: ${response.code}")
                }
                response.body?.string() ?: throw IOException("Empty exchange rates response")
            }
        }

        val payload = gson.fromJson(responseBody, OpenExchangeRatesResponse::class.java)
        val ratesJson = gson.toJson(payload.rates)
        val lastUpdatedMillis = if (payload.timestamp > 0) payload.timestamp * 1000L
        else System.currentTimeMillis()

        context.exchangeRatesDataStore.edit { prefs ->
            prefs[Keys.BASE] = payload.base
            prefs[Keys.RATES_JSON] = ratesJson
            prefs[Keys.LAST_UPDATED_MILLIS] = lastUpdatedMillis
        }
    }

    private data class OpenExchangeRatesResponse(
        val base: String = "USD",
        val rates: Map<String, Double> = emptyMap(),
        val timestamp: Long = 0L,
    )
}

