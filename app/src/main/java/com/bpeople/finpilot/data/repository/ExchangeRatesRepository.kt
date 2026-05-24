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
private const val COINGECKO_URL = "https://api.coingecko.com/api/v3/simple/price"

private const val CACHE_TTL_MILLIS = 6 * 60 * 60 * 1000L        // 6 hours for fiat
private const val CRYPTO_CACHE_TTL_MILLIS = 60 * 60 * 1000L      // 1 hour for crypto

/** CoinGecko coin IDs mapped to the currency symbol used in the app. */
private val COINGECKO_COIN_IDS = mapOf(
    "tether"   to "USDT",
    "ethereum" to "ETH",
)

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
        // Crypto-specific staleness — separate from fiat
        val cryptoRatesAvailable: Boolean = false,
        val cryptoRatesLastUpdatedMillis: Long? = null,
        val cryptoRatesIsStale: Boolean = true,
    )

    private object Keys {
        val BASE = stringPreferencesKey("base")
        val RATES_JSON = stringPreferencesKey("rates_json")
        val LAST_UPDATED_MILLIS = longPreferencesKey("last_updated_millis")
        // Crypto rates are stored separately so their TTL is tracked independently.
        val CRYPTO_RATES_JSON = stringPreferencesKey("crypto_rates_json")
        val CRYPTO_LAST_UPDATED_MILLIS = longPreferencesKey("crypto_last_updated_millis")
    }

    val rates: Flow<ExchangeRatesSnapshot> = context.exchangeRatesDataStore.data.map { prefs ->
        val base = prefs[Keys.BASE] ?: "USD"
        val lastUpdatedMillis = prefs[Keys.LAST_UPDATED_MILLIS] ?: 0L
        val fiatRates = prefs[Keys.RATES_JSON]
            ?.takeIf { it.isNotBlank() }
            ?.let { parseRatesJson(it) }
            ?: emptyMap()

        val cryptoLastUpdated = prefs[Keys.CRYPTO_LAST_UPDATED_MILLIS] ?: 0L
        val cryptoRates = prefs[Keys.CRYPTO_RATES_JSON]
            ?.takeIf { it.isNotBlank() }
            ?.let { parseRatesJson(it) }
            ?: emptyMap()

        // Merge fiat + crypto into a single lookup map.  Crypto entries take
        // precedence for USDT/ETH in case OpenExchangeRates ever starts
        // returning stale crypto data.
        val mergedRates = fiatRates + cryptoRates

        val isStale = lastUpdatedMillis == 0L ||
            System.currentTimeMillis() - lastUpdatedMillis > CACHE_TTL_MILLIS
        val cryptoIsStale = cryptoLastUpdated == 0L ||
            System.currentTimeMillis() - cryptoLastUpdated > CRYPTO_CACHE_TTL_MILLIS

        ExchangeRatesSnapshot(
            base = base,
            rates = mergedRates,
            lastUpdatedMillis = lastUpdatedMillis,
            isStale = isStale,
            cryptoRatesAvailable = cryptoRates.isNotEmpty(),
            cryptoRatesLastUpdatedMillis = cryptoLastUpdated.takeIf { it > 0 },
            cryptoRatesIsStale = cryptoIsStale,
        )
    }

    /**
     * Refresh fiat rates (and best-effort crypto rates) if either cache is stale.
     * Pass [force] = true to bypass the TTL check.
     */
    suspend fun refreshRatesIfNeeded(force: Boolean = false): Result<Unit> = runCatching {
        val snapshot = rates.first()
        if (force || snapshot.rates.isEmpty() || snapshot.isStale) {
            fetchAndCacheRates()
        }
        // Crypto fetch is best-effort — failure must not fail the outer Result.
        if (force || !snapshot.cryptoRatesAvailable || snapshot.cryptoRatesIsStale) {
            try { fetchAndCacheCryptoPrices() } catch (_: Throwable) {}
        }
    }

    /** Refresh only the CoinGecko crypto rates. */
    suspend fun refreshCryptoPricesIfNeeded(force: Boolean = false): Result<Unit> = runCatching {
        val snapshot = rates.first()
        if (force || !snapshot.cryptoRatesAvailable || snapshot.cryptoRatesIsStale) {
            fetchAndCacheCryptoPrices()
        }
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

    // ── Fiat (OpenExchangeRates) ──────────────────────────────────────────────

    private suspend fun fetchAndCacheRates() {
        val appId = BuildConfig.EXCHANGE_RATES_APP_ID
        if (appId.isBlank()) throw IllegalStateException("Missing EXCHANGE_RATES_APP_ID")

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

    // ── Crypto (CoinGecko free API — no key required) ─────────────────────────

    /**
     * Fetches live prices for USDT and ETH from CoinGecko and stores them in
     * the same base-USD format used by OpenExchangeRates so that [rateToLkr]
     * works without any changes.
     *
     * OpenExchangeRates stores "how many X per 1 USD", e.g.:
     *   rates["LKR"] = 300.0  →  1 USD = 300 LKR
     *   rates["ETH"] = 0.0004 →  1 USD = 0.0004 ETH  (i.e. 1 ETH = 2500 USD)
     *
     * CoinGecko returns "USD price per 1 coin", e.g.:
     *   {"ethereum": {"usd": 2500.0}}
     *
     * Conversion: rates[symbol] = 1 / usd_price
     */
    private suspend fun fetchAndCacheCryptoPrices() {
        val coinIds = COINGECKO_COIN_IDS.keys.joinToString(",")
        val url = "$COINGECKO_URL?ids=$coinIds&vs_currencies=usd"

        val request = Request.Builder().url(url).get().build()

        val responseBody = withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("CoinGecko request failed: ${response.code}")
                }
                response.body?.string() ?: throw IOException("Empty CoinGecko response")
            }
        }

        // Parse {"tether":{"usd":1.0001},"ethereum":{"usd":2487.0}}
        val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
        val parsed: Map<String, Map<String, Double>> = gson.fromJson(responseBody, type)

        val cryptoRates = mutableMapOf<String, Double>()
        COINGECKO_COIN_IDS.forEach { (coinId, symbol) ->
            val usdPrice = parsed[coinId]?.get("usd") ?: return@forEach
            if (usdPrice > 0) cryptoRates[symbol] = 1.0 / usdPrice
        }

        if (cryptoRates.isNotEmpty()) {
            context.exchangeRatesDataStore.edit { prefs ->
                prefs[Keys.CRYPTO_RATES_JSON] = gson.toJson(cryptoRates)
                prefs[Keys.CRYPTO_LAST_UPDATED_MILLIS] = System.currentTimeMillis()
            }
        }
    }
}
