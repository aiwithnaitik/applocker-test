package com.applock.privacy.feature.blocker

import org.json.JSONArray
import org.json.JSONObject

data class BlockedWebsite(
    val id: String,
    val domain: String,
    val category: String,
    val isEnabled: Boolean = true
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("domain", domain)
            put("category", category)
            put("isEnabled", isEnabled)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): BlockedWebsite {
            return BlockedWebsite(
                id = json.getString("id"),
                domain = json.getString("domain"),
                category = json.optString("category", "Custom"),
                isEnabled = json.optBoolean("isEnabled", true)
            )
        }

        fun parseWebsitesJson(jsonString: String?): List<BlockedWebsite> {
            if (jsonString.isNullOrEmpty()) {
                return defaultBlockedWebsites()
            }
            val list = mutableListOf<BlockedWebsite>()
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
            } catch (_: Exception) {
                return defaultBlockedWebsites()
            }
            return list
        }

        fun serializeWebsites(list: List<BlockedWebsite>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun defaultBlockedWebsites(): List<BlockedWebsite> {
            return listOf(
                BlockedWebsite("site_1", "facebook.com", "Social Media", true),
                BlockedWebsite("site_2", "instagram.com", "Social Media", true),
                BlockedWebsite("site_3", "tiktok.com", "Social Media", true),
                BlockedWebsite("site_4", "twitter.com", "Social Media", true),
                BlockedWebsite("site_5", "reddit.com", "Distractions", false),
                BlockedWebsite("site_6", "netflix.com", "Entertainment", false)
            )
        }
    }
}
