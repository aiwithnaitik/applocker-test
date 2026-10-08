package com.applock.privacy.core.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.applock.privacy.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val releaseName: String,
    val downloadUrl: String,
    val publishedAt: String,
    val changelog: String
)

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
    data object UpToDate : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    data class ReadyToInstall(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class AppUpdateManager(private val context: Context) {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val GITHUB_REPO = "aiwithnaitik/applocker-test"
        private const val API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/tags/latest"
        private const val KEY_LAST_KNOWN_RELEASE_DATE = "last_known_release_date"
        private const val KEY_LAST_KNOWN_RELEASE_NAME = "last_known_release_name"
    }

    suspend fun checkForUpdates(force: Boolean = false): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            _updateState.value = UpdateState.Checking

            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode != 200) {
                _updateState.value = UpdateState.Error("Server returned code ${connection.responseCode}")
                return@withContext null
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseBody)

            val releaseName = json.optString("name", "Latest Update")
            val publishedAt = json.optString("published_at", "")
            val changelog = json.optString("body", "Bug fixes and performance improvements.")

            val assets = json.optJSONArray("assets")
            var downloadUrl = ""

            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            if (downloadUrl.isEmpty()) {
                _updateState.value = UpdateState.UpToDate
                return@withContext null
            }

            // Compare publishedAt and releaseName with stored values
            val lastInstalledDate = prefs.getString(KEY_LAST_KNOWN_RELEASE_DATE, "") ?: ""
            val lastInstalledName = prefs.getString(KEY_LAST_KNOWN_RELEASE_NAME, "") ?: ""
            val isNewer = force ||
                lastInstalledDate.isEmpty() ||
                publishedAt != lastInstalledDate ||
                (releaseName.isNotEmpty() && releaseName != lastInstalledName)

            val info = UpdateInfo(
                hasUpdate = isNewer,
                releaseName = releaseName,
                downloadUrl = downloadUrl,
                publishedAt = publishedAt,
                changelog = changelog
            )

            if (isNewer) {
                _updateState.value = UpdateState.UpdateAvailable(info)
            } else {
                _updateState.value = UpdateState.UpToDate
            }

            return@withContext info
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "Failed to check for updates")
            return@withContext null
        }
    }

    suspend fun downloadAndInstall(info: UpdateInfo) = withContext(Dispatchers.IO) {
        try {
            _updateState.value = UpdateState.Downloading(0f)

            var currentUrl = info.downloadUrl
            var connection: HttpURLConnection
            var redirectCount = 0

            // Follow multi-hop redirects up to 5 times (GitHub -> release-assets -> S3)
            while (true) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; AppLock-Updater)")

                val status = connection.responseCode
                if (status in 300..399) {
                    val location = connection.getHeaderField("Location")
                    if (!location.isNullOrEmpty() && redirectCount < 5) {
                        currentUrl = location
                        redirectCount++
                        continue
                    }
                }
                break
            }

            val fileLength = connection.contentLength
            val downloadDir = context.getExternalFilesDir(null) ?: context.cacheDir
            val apkFile = File(downloadDir, "update.apk")
            if (apkFile.exists()) apkFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var total: Long = 0
                    var count: Int

                    while (input.read(buffer).also { count = it } != -1) {
                        total += count
                        output.write(buffer, 0, count)
                        if (fileLength > 0) {
                            val progress = (total.toFloat() / fileLength.toFloat()).coerceIn(0f, 1f)
                            _updateState.value = UpdateState.Downloading(progress)
                        }
                    }
                    output.flush()
                }
            }

            // Verify downloaded APK size is realistic (> 1 MB) to prevent trying to install empty or HTML files
            if (apkFile.length() < 1000000) {
                _updateState.value = UpdateState.Error("Downloaded APK is invalid or incomplete (${apkFile.length()} bytes)")
                return@withContext
            }

            // Ensure world-readable for system package installer
            apkFile.setReadable(true, false)

            // Save the published date and release name
            prefs.edit()
                .putString(KEY_LAST_KNOWN_RELEASE_DATE, info.publishedAt)
                .putString(KEY_LAST_KNOWN_RELEASE_NAME, info.releaseName)
                .apply()

            _updateState.value = UpdateState.ReadyToInstall(apkFile)

            // Trigger prompt on main thread
            withContext(Dispatchers.Main) {
                launchInstaller(apkFile)
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "Download failed")
        }
    }

    fun launchInstaller(apkFile: File) {
        try {
            if (!apkFile.exists() || apkFile.length() < 1000000) {
                _updateState.value = UpdateState.Error("APK file is missing or corrupted. Please tap to re-download.")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // Explicitly grant read URI permission to all potential installer handlers
            val resInfoList = context.packageManager.queryIntentActivities(
                installIntent,
                android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resInfoList) {
                val pkgName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(pkgName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Failed to open package installer: ${e.message}")
        }
    }
}
