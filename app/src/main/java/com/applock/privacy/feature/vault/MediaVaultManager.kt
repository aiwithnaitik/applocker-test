package com.applock.privacy.feature.vault

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object MediaVaultManager {

    private fun getVaultDirectory(context: Context): File {
        val dir = File(context.filesDir, "private_vault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun importMedia(
        context: Context,
        preferencesDataSource: AppPreferencesDataSource,
        uri: Uri,
        isVideo: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val vaultDir = getVaultDirectory(context)
            val id = UUID.randomUUID().toString()
            val extension = if (isVideo) ".mp4" else ".jpg"
            val targetFile = File(vaultDir, "$id$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext false

            val name = "Item_${System.currentTimeMillis() % 10000}$extension"
            val item = VaultMediaItem(
                id = id,
                name = name,
                filePath = targetFile.absolutePath,
                isVideo = isVideo,
                addedTimestamp = System.currentTimeMillis(),
                sizeBytes = targetFile.length()
            )

            preferencesDataSource.addVaultMediaItem(item)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteMedia(
        preferencesDataSource: AppPreferencesDataSource,
        item: VaultMediaItem
    ) = withContext(Dispatchers.IO) {
        try {
            val file = File(item.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        preferencesDataSource.deleteVaultMediaItem(item.id)
    }

    fun loadBitmap(filePath: String): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2 // Performance: downsample for smooth grid loading
            }
            BitmapFactory.decodeFile(filePath, options)
        } catch (_: Exception) {
            null
        }
    }
}
