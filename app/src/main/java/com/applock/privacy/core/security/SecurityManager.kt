package com.applock.privacy.core.security

import com.applock.privacy.data.local.AppPreferencesDataSource
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecurityManager {

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$pin$salt".toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(input)
        return Base64.getEncoder().encodeToString(hashBytes)
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
}
