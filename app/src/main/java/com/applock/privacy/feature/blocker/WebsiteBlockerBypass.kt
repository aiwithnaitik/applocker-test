package com.applock.privacy.feature.blocker

import java.util.concurrent.ConcurrentHashMap

object WebsiteBlockerBypass {
    // Maps lowercase domain -> expiry timestamp (millis)
    private val bypassedDomains = ConcurrentHashMap<String, Long>()

    // Default bypass period: 10 minutes
    const val BYPASS_DURATION_MS = 10 * 60 * 1000L

    fun bypass(domain: String) {
        val cleanDomain = normalizeDomain(domain)
        bypassedDomains[cleanDomain] = System.currentTimeMillis() + BYPASS_DURATION_MS
    }

    fun isBypassed(domain: String): Boolean {
        val cleanDomain = normalizeDomain(domain)
        val expiry = bypassedDomains[cleanDomain] ?: return false
        if (System.currentTimeMillis() < expiry) {
            return true
        }
        bypassedDomains.remove(cleanDomain)
        return false
    }

    fun clearAllBypasses() {
        bypassedDomains.clear()
    }

    fun normalizeDomain(raw: String): String {
        return raw.trim()
            .lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore("/")
            .substringBefore("?")
    }
}
