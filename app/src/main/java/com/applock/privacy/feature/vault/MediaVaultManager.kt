package com.applock.privacy.feature.vault

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
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
        } catch (_: Throwable) {
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
        } catch (_: Throwable) {}
        preferencesDataSource.deleteVaultMediaItem(item.id)
    }

    fun loadBitmap(filePath: String, targetSize: Int = 1080): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, boundsOptions)

            val width = boundsOptions.outWidth
            val height = boundsOptions.outHeight
            if (width <= 0 || height <= 0) return null

            var sampleSize = 1
            while (width / sampleSize > targetSize || height / sampleSize > targetSize) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = maxOf(1, sampleSize)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (_: Throwable) {
            null
        }
    }

    fun loadThumbnail(filePath: String, isVideo: Boolean): Bitmap? {
        return try {
            if (isVideo) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)
                val frame = retriever.frameAtTime
                retriever.release()
                frame
            } else {
                loadBitmap(filePath, targetSize = 360)
            }
        } catch (_: Throwable) {
            null
        }
    }
}
