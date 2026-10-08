package com.applock.privacy.feature.intruder

import org.json.JSONArray
import org.json.JSONObject

data class IntruderLog(
    val id: String,
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val imagePath: String,
    val failedAttempts: Int
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("timestamp", timestamp)
            put("packageName", packageName)
            put("appName", appName)
            put("imagePath", imagePath)
            put("failedAttempts", failedAttempts)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): IntruderLog {
            return IntruderLog(
                id = json.getString("id"),
                timestamp = json.getLong("timestamp"),
                packageName = json.getString("packageName"),
                appName = json.optString("appName", "Unknown App"),
                imagePath = json.getString("imagePath"),
                failedAttempts = json.optInt("failedAttempts", 1)
            )
        }

        fun parseLogsJson(jsonString: String?): List<IntruderLog> {
            if (jsonString.isNullOrEmpty()) return emptyList()
            val list = mutableListOf<IntruderLog>()
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
            } catch (_: Exception) {}
            return list.sortedByDescending { it.timestamp }
        }

        fun serializeLogs(logs: List<IntruderLog>): String {
            val array = JSONArray()
            logs.forEach { array.put(it.toJson()) }
            return array.toString()
        }
    }
}
