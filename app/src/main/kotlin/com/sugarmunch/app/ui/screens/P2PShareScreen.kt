package com.sugarmunch.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.p2p.*
import com.sugarmunch.app.progression.ProgressionTracker
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * P2P Share Screen - Complete UI for peer-to-peer APK sharing
 * 
 * Features:
 * - Scan for nearby devices
 * - Show available apps to share
 * - Transfer progress display
 * - Received apps queue
 * - QR code pairing
 * - Share history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PShareScreen(
    onBack: () -> Unit,
    availableApps: List<AppEntry> = emptyList(),
    downloadedAppsDir: File? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Theme
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    // P2P Managers
    val nearbyManager = remember { NearbyShareManager.getInstance(context) }
    val localDiscovery = remember { LocalNetworkDiscovery.getInstance(context) }
    val uiManager = remember { P2PUiManager.getInstance(context) }
    val progressionTracker = remember { ProgressionTracker.getInstance(context) }

    // State
    var discoveryMode by remember { mutableStateOf(P2PUiManager.DiscoveryMode.NEARBY) }
    var isScanning by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppEntry?>(null) }
    var selectedDevice by remember { mutableStateOf<P2PUiManager.NearbyDevice?>(null) }
    
    val nearbyDevices by uiManager.nearbyDevices.collectAsState()
    val currentTransfer by uiManager.currentTransfer.collectAsState()
    val shareHistory by uiManager.shareHistory.collectAsState()
    val connectionState by nearbyManager.connectionState.collectAsState()
    val discoveredEndpoints by nearbyManager.discoveredEndpoints.collectAsState()

    // Permission handling
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startScanning()
        }
    }

    // Sync Nearby endpoints with UI manager
    LaunchedEffect(discoveredEndpoints) {
        val devices = discoveredEndpoints.map { endpoint ->
            P2PUiManager.NearbyDevice(
                id = endpoint.endpointId,
                name = endpoint.endpointName,
                connectionType = P2PUiManager.ConnectionType.WIFI_DIRECT,
                status = P2PUiManager.DeviceStatus.AVAILABLE,
                endpointId = endpoint.endpointId
            )
        }
        uiManager.updateNearbyDevices(devices)
    }

    // Sync local discovery with UI manager
    val localPeers by localDiscovery.discoveredPeers.collectAsState()
    LaunchedEffect(localPeers) {
        val devices = localPeers.map { peer ->
            P2PUiManager.NearbyDevice(
                id = peer.deviceId,
                name = peer.getDisplayName(),
                connectionType = P2PUiManager.ConnectionType.LOCAL_NETWORK,
                status = if (peer.isOnline) P2PUiManager.DeviceStatus.AVAILABLE else P2PUiManager.DeviceStatus.OFFLINE,
                ipAddress = peer.hostAddress?.hostAddress,
                port = peer.port
            )
        }
        uiManager.updateNearbyDevices(devices)
    }

    fun checkAndRequestPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermissions.isEmpty()) {
            true
        } else {
            permissionsLauncher.launch(missingPermissions.toTypedArray())
            false
        }
    }

    fun startScanning() {
        isScanning = true
        when (discoveryMode) {
            P2PUiManager.DiscoveryMode.NEARBY -> {
                nearbyManager.startDiscovery()
                nearbyManager.startAdvertising()
            }
            P2PUiManager.DiscoveryMode.LOCAL_WIFI -> {
                localDiscovery.startDiscovery()
                localDiscovery.startAdvertising()
            }
            P2PUiManager.DiscoveryMode.QR_CODE -> {
                showQrCode = true
            }
        }
    }

    fun stopScanning() {
        isScanning = false
        nearbyManager.stopDiscovery()
        nearbyManager.stopAdvertising()
        localDiscovery.stopDiscovery()
        localDiscovery.stopAdvertising()
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            stopScanning()
            nearbyManager.cleanup()
            localDiscovery.cleanup()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Nearby Share",
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Discovery Mode Selector
                DiscoveryModeSelector(
                    selectedMode = discoveryMode,
                    onModeSelected = { mode ->
                        stopScanning()
                        discoveryMode = mode
                        uiManager.setDiscoveryMode(mode)
                    },
                    colors = colors
                )

                // Scan/Stop Button
                ScanButton(
                    isScanning = isScanning,
                    onClick = {
                        if (isScanning) {
                            stopScanning()
                        } else {
                            if (checkAndRequestPermissions()) {
                                startScanning()
                            }
                        }
                    },
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // QR Code Display (when in QR mode)
                AnimatedVisibility(
                    visible = showQrCode && discoveryMode == P2PUiManager.DiscoveryMode.QR_CODE,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    QrCodeCard(
                        colors = colors,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Nearby Devices List
                if (nearbyDevices.isNotEmpty()) {
                    Text(
                        "Nearby Devices (${nearbyDevices.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(nearbyDevices) { device ->
                            DeviceCard(
                                device = device,
                                isSelected = selectedDevice?.id == device.id,
                                onClick = {
                                    selectedDevice = if (selectedDevice?.id == device.id) null else device
                                },
                                onConnect = {
                                    if (device.endpointId != null) {
                                        nearbyManager.connectToEndpoint(device.endpointId)
                                    }
                                },
                                colors = colors
                            )
                        }
                    }
                } else if (isScanning) {
                    ScanningIndicator(colors = colors)
                } else {
                    EmptyState(
                        icon = Icons.Default.NearbyError,
                        title = "No devices found",
                        subtitle = "Tap Scan to find nearby SugarMunch users",
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Apps to Share Section
                if (selectedDevice != null && availableApps.isNotEmpty()) {
                    Text(
                        "Select app to share with ${selectedDevice!!.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableApps) { app ->
                            AppShareCard(
                                app = app,
                                onClick = {
                                    selectedApp = app
                                    showTransferDialog = true
                                },
                                colors = colors
                            )
                        }
                    }
                }
            }

            // Transfer Progress Dialog
            if (currentTransfer != null) {
                TransferProgressDialog(
                    transfer = currentTransfer!!,
                    onCancel = {
                        uiManager.cancelTransfer()
                    },
                    colors = colors
                )
            }

            // Transfer Confirmation Dialog
            if (showTransferDialog && selectedApp != null && selectedDevice != null) {
                TransferConfirmationDialog(
                    app = selectedApp!!,
                    device = selectedDevice!!,
                    onConfirm = {
                        showTransferDialog = false
                        // Start transfer
                        scope.launch {
                            val file = downloadedAppsDir?.listFiles()?.find { 
                                it.name.contains(selectedApp!!.id, ignoreCase = true) 
                            }
                            if (file != null && selectedDevice!!.endpointId != null) {
                                nearbyManager.sendApp(
                                    endpointId = selectedDevice!!.endpointId!!,
                                    app = selectedApp!!,
                                    apkFile = file
                                )
                                progressionTracker.onShareCompleted()
                            }
                        }
                    },
                    onDismiss = {
                        showTransferDialog = false
                    },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun DiscoveryModeSelector(
    selectedMode: P2PUiManager.DiscoveryMode,
    onModeSelected: (P2PUiManager.DiscoveryMode) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DiscoveryModeButton(
                icon = Icons.Default.Wifi,
                label = "Nearby",
                isSelected = selectedMode == P2PUiManager.DiscoveryMode.NEARBY,
                onClick = { onModeSelected(P2PUiManager.DiscoveryMode.NEARBY) },
                colors = colors
            )
            DiscoveryModeButton(
                icon = Icons.Default.WifiTethering,
                label = "Local WiFi",
                isSelected = selectedMode == P2PUiManager.DiscoveryMode.LOCAL_WIFI,
                onClick = { onModeSelected(P2PUiManager.DiscoveryMode.LOCAL_WIFI) },
                colors = colors
            )
            DiscoveryModeButton(
                icon = Icons.Default.QrCode,
                label = "QR Code",
                isSelected = selectedMode == P2PUiManager.DiscoveryMode.QR_CODE,
                onClick = { onModeSelected(P2PUiManager.DiscoveryMode.QR_CODE) },
                colors = colors
            )
        }
    }
}

@Composable
private fun DiscoveryModeButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) colors.primary.copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ScanButton(
    isScanning: Boolean,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isScanning) colors.error else colors.primary
        )
    ) {
        Icon(
            if (isScanning) Icons.Default.Stop else Icons.Default.Radar,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            if (isScanning) "Stop Scanning" else "Scan for Devices",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DeviceCard(
    device: P2PUiManager.NearbyDevice,
    isSelected: Boolean,
    onClick: () -> Unit,
    onConnect: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val connectionTypeIcon = when (device.connectionType) {
        P2PUiManager.ConnectionType.WIFI_DIRECT -> Icons.Default.Wifi
        P2PUiManager.ConnectionType.BLUETOOTH -> Icons.Default.Bluetooth
        P2PUiManager.ConnectionType.LOCAL_NETWORK -> Icons.Default.NetworkWifi
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                colors.primary.copy(alpha = 0.15f) 
            else 
                colors.surface.copy(alpha = 0.95f)
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, colors.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.primary,
                                colors.secondary
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        connectionTypeIcon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        device.getStatusText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (device.status == P2PUiManager.DeviceStatus.AVAILABLE) {
                FilledTonalIconButton(
                    onClick = onConnect,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Connect",
                        tint = Color.White
                    )
                }
            } else if (device.status == P2PUiManager.DeviceStatus.CONNECTED) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Connected",
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AppShareCard(
    app: AppEntry,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        colors.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    tint = colors.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "v${app.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun ScanningIndicator(
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .alpha(alpha)
                .background(
                    colors.primary.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Radar,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Scanning for devices...",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface.copy(alpha = 0.8f)
        )
        
        Text(
            "Make sure both devices are close to each other",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun QrCodeCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiManager = remember { P2PUiManager.getInstance(context) }
    val qrCode by uiManager.pairingQrCode.collectAsState()

    LaunchedEffect(Unit) {
        uiManager.generatePairingQrCode()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Scan to Connect",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Have your friend scan this code to connect",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            qrCode?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            colors.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
        }
    }
}

@Composable
private fun TransferProgressDialog(
    transfer: P2PUiManager.TransferUiState,
    onCancel: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Transfer Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            colors.primary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (transfer.direction == P2PUiManager.TransferDirection.SENDING)
                            Icons.Default.Upload else Icons.Default.Download,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    if (transfer.direction == P2PUiManager.TransferDirection.SENDING)
                        "Sending ${transfer.appName ?: "App"}"
                    else
                        "Receiving ${transfer.appName ?: "App"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    if (transfer.direction == P2PUiManager.TransferDirection.SENDING)
                        "to ${transfer.toDevice}"
                    else
                        "from ${transfer.fromDevice}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { transfer.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = colors.primary,
                    trackColor = colors.onSurface.copy(alpha = 0.1f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${transfer.getProgressPercent()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Text(
                        transfer.getFormattedSpeed(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Transfer Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TransferDetail(
                        icon = Icons.Default.DataUsage,
                        label = "Size",
                        value = transfer.getFormattedSize(),
                        colors = colors
                    )
                    TransferDetail(
                        icon = Icons.Default.Schedule,
                        label = "Time left",
                        value = transfer.getFormattedEta(),
                        colors = colors
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel Button
                if (transfer.status == P2PUiManager.TransferStatus.IN_PROGRESS ||
                    transfer.status == P2PUiManager.TransferStatus.PENDING
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Transfer")
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferDetail(
    icon: ImageVector,
    label: String,
    value: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface.copy(alpha = 0.5f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface
        )
    }
}

@Composable
private fun TransferConfirmationDialog(
    app: AppEntry,
    device: P2PUiManager.NearbyDevice,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Share App?",
                color = colors.onSurface
            )
        },
        text = {
            Column {
                Text(
                    "You are about to share:",
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    app.name,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    "Version ${app.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "with ${device.name}",
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.onSurface.copy(alpha = 0.7f))
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
