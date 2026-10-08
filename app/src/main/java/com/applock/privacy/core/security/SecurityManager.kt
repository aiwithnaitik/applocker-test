package com.applock.privacy.core.security

import android.util.Base64
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.security.SecureRandom

sealed class AuthResult {
    data object Success : AuthResult()
    data class Failure(val failedCount: Int, val isNowLockedOut: Boolean) : AuthResult()
    data class LockedOut(val remainingSeconds: Int) : AuthResult()
}

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
        preferencesDataSource.resetFailedAttempts()
    }

    suspend fun verifyPinWithResult(preferencesDataSource: AppPreferencesDataSource, inputPin: String): AuthResult {
        val lockoutUntil = preferencesDataSource.lockoutUntilTimestampFlow.first()
        val now = System.currentTimeMillis()
        if (now < lockoutUntil) {
            val remainingSec = ((lockoutUntil - now) / 1000L).toInt().coerceAtLeast(1)
            return AuthResult.LockedOut(remainingSec)
        }

        val storedHash = preferencesDataSource.getPinHash() ?: return AuthResult.Failure(0, false)
        val storedSalt = preferencesDataSource.getPinSalt() ?: return AuthResult.Failure(0, false)
        val computedHash = hashPin(inputPin, storedSalt)

        return if (computedHash == storedHash) {
            preferencesDataSource.resetFailedAttempts()
            AuthResult.Success
        } else {
            val (count, isLocked) = preferencesDataSource.recordFailedAttempt()
            AuthResult.Failure(count, isLocked)
        }
    }

    suspend fun verifyPin(preferencesDataSource: AppPreferencesDataSource, inputPin: String): Boolean {
        return verifyPinWithResult(preferencesDataSource, inputPin) is AuthResult.Success
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
        preferencesDataSource.resetFailedAttempts()
    }

    suspend fun verifyPatternWithResult(preferencesDataSource: AppPreferencesDataSource, inputPattern: String): AuthResult {
        val lockoutUntil = preferencesDataSource.lockoutUntilTimestampFlow.first()
        val now = System.currentTimeMillis()
        if (now < lockoutUntil) {
            val remainingSec = ((lockoutUntil - now) / 1000L).toInt().coerceAtLeast(1)
            return AuthResult.LockedOut(remainingSec)
        }

        val storedHash = preferencesDataSource.getPatternHash() ?: return AuthResult.Failure(0, false)
        val storedSalt = preferencesDataSource.getPatternSalt() ?: return AuthResult.Failure(0, false)
        val computedHash = hashPattern(inputPattern, storedSalt)

        return if (computedHash == storedHash) {
            preferencesDataSource.resetFailedAttempts()
            AuthResult.Success
        } else {
            val (count, isLocked) = preferencesDataSource.recordFailedAttempt()
            AuthResult.Failure(count, isLocked)
        }
    }

    suspend fun verifyPattern(preferencesDataSource: AppPreferencesDataSource, inputPattern: String): Boolean {
        return verifyPatternWithResult(preferencesDataSource, inputPattern) is AuthResult.Success
    }
}
