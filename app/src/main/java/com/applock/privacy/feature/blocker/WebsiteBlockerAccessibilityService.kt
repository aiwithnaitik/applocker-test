package com.applock.privacy.feature.blocker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.applock.privacy.core.monitoring.AppLockSession
import com.applock.privacy.feature.lock.LockActivity
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WebsiteBlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var preferencesDataSource: AppPreferencesDataSource
    private var lastBlockedTimestamp = 0L

    @Volatile
    private var cachedIsMonitorActive = true

    @Volatile
    private var cachedLockedPackages: Set<String> = emptySet()

    override fun onCreate() {
        super.onCreate()
        preferencesDataSource = AppPreferencesDataSource(applicationContext)
        serviceScope.launch {
            preferencesDataSource.isAppMonitorActiveFlow.collect { active ->
                cachedIsMonitorActive = active
            }
        }
        serviceScope.launch {
            preferencesDataSource.lockedPackagesFlow.collect { locked ->
                cachedLockedPackages = locked
            }
        }
        Log.i(TAG, "WebsiteBlockerAccessibilityService created")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // 1. Instant Zero-Latency App Lock Interception (Hardware-speed OS window event)
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            AppLockSession.onForegroundPackageChanged(packageName)

            if (packageName != applicationContext.packageName && cachedIsMonitorActive) {
                if (cachedLockedPackages.contains(packageName)) {
                    val isUnlocked = AppLockSession.isPackageUnlocked(packageName)
                    val currentShowing = AppLockSession.currentLockShowingPackage
                    if (!isUnlocked && currentShowing != packageName) {
                        AppLockSession.setLockActivityShowing(true, packageName)
                        LockActivity.start(applicationContext, packageName)
                    }
                }
            }
        }

        // 2. Browser Inspection for Website Blocker
        if (!SUPPORTED_BROWSERS.contains(packageName)) return

        val now = System.currentTimeMillis()
        if (now - lastBlockedTimestamp < 1500L) {
            // Debounce rapid window changes
            return
        }

        serviceScope.launch {
            try {
                val isBlockerEnabled = preferencesDataSource.isWebsiteBlockerEnabledFlow.first()
                if (!isBlockerEnabled) return@launch

                val blockedSites = preferencesDataSource.blockedWebsitesFlow.first().filter { it.isEnabled }
                if (blockedSites.isEmpty()) return@launch

                val rootNode = rootInActiveWindow ?: return@launch
                val detectedUrlText = extractUrlFromNodeTree(rootNode) ?: return@launch

                for (site in blockedSites) {
                    val targetDomain = WebsiteBlockerBypass.normalizeDomain(site.domain)
                    if (targetDomain.isNotEmpty() && detectedUrlText.contains(targetDomain, ignoreCase = true)) {
                        if (WebsiteBlockerBypass.isBypassed(targetDomain)) {
                            // User temporarily bypassed with PIN
                            continue
                        }

                        lastBlockedTimestamp = System.currentTimeMillis()
                        Log.i(TAG, "Detected blocked website: $targetDomain in $packageName")
                        launchBlockScreen(site.domain, site.category)
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inspecting web accessibility node", e)
            }
        }
    }

    private fun extractUrlFromNodeTree(node: AccessibilityNodeInfo): String? {
        val viewId = node.viewIdResourceName ?: ""
        // Check common browser address bar IDs
        if (viewId.contains("url_bar", ignoreCase = true) ||
            viewId.contains("location_bar", ignoreCase = true) ||
            viewId.contains("toolbar_url", ignoreCase = true) ||
            viewId.contains("search_box", ignoreCase = true)
        ) {
            val text = node.text?.toString()
            if (!text.isNullOrBlank()) return text.lowercase()
        }

        // Check node text directly if it looks like a URL
        val text = node.text?.toString()
        if (!text.isNullOrBlank() && (text.contains(".com") || text.contains(".org") || text.contains(".net") || text.contains(".io") || text.contains("http"))) {
            return text.lowercase()
        }

        // Recursively inspect child nodes
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = extractUrlFromNodeTree(child)
            if (result != null) return result
        }

        return null
    }

    private fun launchBlockScreen(domain: String, category: String) {
        val intent = Intent(this, BlockedSiteActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockedSiteActivity.EXTRA_DOMAIN, domain)
            putExtra(BlockedSiteActivity.EXTRA_CATEGORY, category)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    companion object {
        const val TAG = "WebBlockerService"

        val SUPPORTED_BROWSERS = setOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "com.google.android.apps.chrome",
            "org.mozilla.firefox",
            "org.mozilla.firefox_beta",
            "com.microsoft.emmx",
            "com.sec.android.app.sbrowser",
            "com.sec.android.app.sbrowser.beta",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.brave.browser",
            "com.duckduckgo.mobile.android",
            "com.vivaldi.browser",
            "com.kiwibrowser.browser"
        )
    }
}
