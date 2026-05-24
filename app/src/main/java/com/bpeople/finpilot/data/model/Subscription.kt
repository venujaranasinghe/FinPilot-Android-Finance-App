package com.bpeople.finpilot.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Subscription(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val amountLKR: Double = 0.0,
    val billingCycle: String = "MONTHLY",  // MONTHLY, YEARLY, WEEKLY
    val nextBillingMillis: Long = 0L,
    val isActive: Boolean = true,
    val category: String = "Entertainment",
    val note: String = "",
) {
    val monthlyEquivalent: Double get() = when (billingCycle) {
        "YEARLY"  -> amountLKR / 12.0
        "WEEKLY"  -> amountLKR * 4.33
        else      -> amountLKR
    }
}
