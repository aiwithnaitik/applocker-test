package com.applock.privacy.core.security

import android.util.Base64
import com.applock.privacy.data.local.AppPreferencesDataSource
import java.security.MessageDigest
import java.security.SecureRandom

object SecurityManager {

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$pin$salt".toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(input)
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    suspend fun setupNewPin(preferencesDataSource: AppPreferencesDataSource, pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        preferencesDataSource.savePin(hash, salt)
    }

    suspend fun verifyPin(preferencesDataSource: AppPreferencesDataSource, inputPin: String): Boolean {
        val storedHash = preferencesDataSource.getPinHash() ?: return false
        val storedSalt = preferencesDataSource.getPinSalt() ?: return false
        val computedHash = hashPin(inputPin, storedSalt)
        return computedHash == storedHash
    }

    fun hashPattern(pattern: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "pattern_$pattern$salt".toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(input)
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    suspend fun setupNewPattern(preferencesDataSource: AppPreferencesDataSource, pattern: String) {
        val salt = generateSalt()
        val hash = hashPattern(pattern, salt)
        preferencesDataSource.savePattern(hash, salt)
    }

    suspend fun verifyPattern(preferencesDataSource: AppPreferencesDataSource, inputPattern: String): Boolean {
        val storedHash = preferencesDataSource.getPatternHash() ?: return false
        val storedSalt = preferencesDataSource.getPatternSalt() ?: return false
        val computedHash = hashPattern(inputPattern, storedSalt)
        return computedHash == storedHash
    }
}
