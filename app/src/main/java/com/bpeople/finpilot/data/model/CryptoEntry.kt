package com.bpeople.finpilot.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CryptoEntry(
    val id: String = "",
    val userId: String = "",
    val symbol: String = "",        // e.g. BTC, ETH, USDT
    val name: String = "",          // e.g. Bitcoin, Ethereum
    val quantity: Double = 0.0,
    val buyPriceLKR: Double = 0.0,  // LKR per unit at purchase time
    val currentPriceLKR: Double = 0.0, // LKR per unit (user-updated)
    val note: String = "",
    val purchasedAt: Timestamp? = null,
) {
    val investedLKR: Double get() = quantity * buyPriceLKR
    val currentValueLKR: Double get() = quantity * currentPriceLKR
    val pnlLKR: Double get() = currentValueLKR - investedLKR
    val pnlPercent: Double get() = if (investedLKR > 0) (pnlLKR / investedLKR) * 100.0 else 0.0
}
