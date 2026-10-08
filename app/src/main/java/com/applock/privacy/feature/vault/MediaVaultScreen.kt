package com.applock.privacy.feature.vault

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.applock.privacy.core.ui.components.AppButton
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.components.EmptyStateView
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BackgroundSurface
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.BrightAzure
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.RoseDestructive
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class VaultTab {
    PHOTOS, VIDEOS
}

@Composable
fun MediaVaultScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesDataSource = remember { AppPreferencesDataSource(context) }

    val allVaultItems by preferencesDataSource.vaultMediaFlow.collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(VaultTab.PHOTOS) }
    var viewingItem by remember { mutableStateOf<VaultMediaItem?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    val photoItems = remember(allVaultItems) { allVaultItems.filter { !it.isVideo } }
    val videoItems = remember(allVaultItems) { allVaultItems.filter { it.isVideo } }
    val displayedItems = if (selectedTab == VaultTab.PHOTOS) photoItems else videoItems

    // Image Picker Launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                isImporting = true
                val isVideo = selectedTab == VaultTab.VIDEOS
                for (uri in uris) {
                    MediaVaultManager.importMedia(
                        context = context,
                        preferencesDataSource = preferencesDataSource,
                        uri = uri,
                        isVideo = isVideo
                    )
                }
                isImporting = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            AppTopBar(
                title = "Private Media Vault",
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(com.applock.privacy.core.ui.theme.SurfaceCard)
                    .padding(4.dp)
            ) {
                TabButton(
                    title = "Photos (${photoItems.size})",
                    isSelected = selectedTab == VaultTab.PHOTOS,
                    icon = Icons.Default.Photo,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = VaultTab.PHOTOS }
                )

                TabButton(
                    title = "Videos (${videoItems.size})",
                    isSelected = selectedTab == VaultTab.VIDEOS,
                    icon = Icons.Default.Videocam,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = VaultTab.VIDEOS }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isImporting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = ElectricCyan,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else if (displayedItems.isEmpty()) {
                EmptyStateView(
                    title = if (selectedTab == VaultTab.PHOTOS) "No Hidden Photos" else "No Hidden Videos",
                    description = "Photos and videos placed here are encrypted and completely hidden from your phone's Gallery.",
                    icon = Icons.Default.Lock,
                    actionButtonText = "+ Hide First ${if (selectedTab == VaultTab.PHOTOS) "Photo" else "Video"}",
                    onActionClick = {
                        val mimeType = if (selectedTab == VaultTab.PHOTOS) "image/*" else "video/*"
                        mediaPickerLauncher.launch(mimeType)
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        VaultMediaThumbnail(
                            item = item,
                            onClick = { viewingItem = item }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Import
        FloatingActionButton(
            onClick = {
                val mimeType = if (selectedTab == VaultTab.PHOTOS) "image/*" else "video/*"
                mediaPickerLauncher.launch(mimeType)
            },
            containerColor = BrightAzure,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Import Media"
            )
        }

        // Fullscreen Preview Dialog
        viewingItem?.let { item ->
            Dialog(onDismissRequest = { viewingItem = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

                        LaunchedEffect(item.filePath) {
                            withContext(Dispatchers.IO) {
                                previewBitmap = MediaVaultManager.loadBitmap(item.filePath)
                            }
                        }

                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = item.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = item.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        MediaVaultManager.deleteMedia(preferencesDataSource, item)
                                        viewingItem = null
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = RoseDestructive,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", color = RoseDestructive)
                            }

                            AppButton(
                                text = "Close",
                                onClick = { viewingItem = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultMediaThumbnail(
    item: VaultMediaItem,
    onClick: () -> Unit
) {
    var thumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(item.filePath) {
        withContext(Dispatchers.IO) {
            thumbnailBitmap = MediaVaultManager.loadBitmap(item.filePath)
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailBitmap != null) {
            Image(
                bitmap = thumbnailBitmap!!.asImageBitmap(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = if (item.isVideo) Icons.Default.PlayArrow else Icons.Default.Photo,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(30.dp)
            )
        }

        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) BrightAzure else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else TextMuted,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
