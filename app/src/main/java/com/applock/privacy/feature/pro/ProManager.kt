package com.applock.privacy.feature.pro

import android.content.Context
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.flow.Flow

data class ProPlan(
    val id: String,
    val title: String,
    val price: String,
    val billingPeriod: String,
    val description: String,
    val isBestValue: Boolean = false,
    val discountTag: String? = null
)

object ProManager {

    val PLANS = listOf(
        ProPlan(
            id = "pro_monthly",
            title = "Monthly VIP",
            price = "$1.99",
            billingPeriod = "per month",
            description = "Flexible monthly subscription. Cancel anytime from Google Play.",
            isBestValue = false
        ),
        ProPlan(
            id = "pro_yearly",
            title = "Annual VIP",
            price = "$9.99",
            billingPeriod = "per year ($0.83/mo)",
            description = "Maximum savings. Full 365 days of elite privacy and updates.",
            isBestValue = true,
            discountTag = "SAVE 60%"
        ),
        ProPlan(
            id = "pro_lifetime",
            title = "Lifetime VIP",
            price = "$19.99",
            billingPeriod = "one-time payment",
            description = "Pay once, keep VIP forever. Includes all future major features.",
            isBestValue = false,
            discountTag = "FOREVER"
        )
    )

    val PRO_FEATURES = listOf(
        "Stealth Disguise Covers (Crash Dialog & Calculator Decoy)",
        "Unlimited Custom Themes with Personal Wallpapers",
        "Unlimited Domain Rules in Website Blocker",
        "Silent High-Definition Intruder Camera Capture",
        "Intruder Siren Alarm with Custom Thresholds",
        "Zero Advertisements & Pure Focus Privacy Shield"
    )

    fun isProUserFlow(preferencesDataSource: AppPreferencesDataSource): Flow<Boolean> {
        return preferencesDataSource.isProUserFlow
    }

    suspend fun purchasePlan(
        preferencesDataSource: AppPreferencesDataSource,
        plan: ProPlan
    ): Boolean {
        val durationDays = when (plan.id) {
            "pro_monthly" -> 30L
            "pro_yearly" -> 365L
            else -> 36500L // 100 years for lifetime
        }
        val expiryTimestamp = System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000L)
        preferencesDataSource.setProSubscription(
            isPro = true,
            planId = plan.id,
            expiryTimestamp = expiryTimestamp
        )
        return true
    }

    suspend fun restorePurchases(preferencesDataSource: AppPreferencesDataSource): Boolean {
        // In local/sandbox hardware testing, restores active annual entitlement
        val expiryTimestamp = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000L)
        preferencesDataSource.setProSubscription(
            isPro = true,
            planId = "pro_yearly",
            expiryTimestamp = expiryTimestamp
        )
        return true
    }
}
