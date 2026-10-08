package com.applock.privacy.feature.vault

import org.json.JSONArray
import org.json.JSONObject

data class VaultMediaItem(
    val id: String,
    val name: String,
    val filePath: String,
    val isVideo: Boolean,
    val addedTimestamp: Long,
    val sizeBytes: Long
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("filePath", filePath)
            put("isVideo", isVideo)
            put("addedTimestamp", addedTimestamp)
            put("sizeBytes", sizeBytes)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): VaultMediaItem {
            return VaultMediaItem(
                id = json.optString("id", ""),
                name = json.optString("name", "Untitled"),
                filePath = json.optString("filePath", ""),
                isVideo = json.optBoolean("isVideo", false),
                addedTimestamp = json.optLong("addedTimestamp", 0L),
                sizeBytes = json.optLong("sizeBytes", 0L)
            )
        }

        fun parseMediaJson(raw: String?): List<VaultMediaItem> {
            if (raw.isNullOrEmpty()) return emptyList()
            return try {
                val array = JSONArray(raw)
                val list = mutableListOf<VaultMediaItem>()
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }

        fun serializeMedia(items: List<VaultMediaItem>): String {
            val array = JSONArray()
            items.forEach { array.put(it.toJson()) }
            return array.toString()
        }
    }
}
