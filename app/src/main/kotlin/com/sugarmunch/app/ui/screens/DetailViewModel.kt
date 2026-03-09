package com.sugarmunch.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.ManifestRepository
import com.sugarmunch.app.progression.ProgressionTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class DetailState {
    data object Loading : DetailState()
    data class Success(
        val app: AppEntry,
        val downloadProgress: Float? = null,
        val installing: Boolean = false,
        val installComplete: Boolean = false,
        val installError: String? = null
    ) : DetailState()
    data class Error(val message: String) : DetailState()
}

class DetailViewModel(
    private val appId: String,
    private val context: android.content.Context,
    private val repository: ManifestRepository = ManifestRepository(context)
) : ViewModel() {

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state.asStateFlow()
    private val progressionTracker = ProgressionTracker.getInstance(context)

    fun loadApp() {
        viewModelScope.launch {
            _state.value = DetailState.Loading
            repository.fetchApps()
                .onSuccess { apps ->
                    val app = apps.find { it.id == appId }
                    if (app != null) {
                        _state.value = DetailState.Success(app)
                    } else {
                        _state.value = DetailState.Error("App not found")
                    }
                }
                .onFailure { e ->
                    _state.value = DetailState.Error(e.message ?: "Failed to load")
                }
        }
    }

    fun downloadAndInstall() {
        viewModelScope.launch {
            val current = _state.value
            if (current !is DetailState.Success) return@launch
            val app = current.app
            _state.value = current.copy(downloadProgress = 0f, installError = null)
            
            runCatching {
                withContext(Dispatchers.IO) {
                    downloadFile(app.downloadUrl) { progress ->
                        _state.value = ( _state.value as DetailState.Success).copy(downloadProgress = progress)
                    }
                }
            }.onSuccess { file ->
                _state.value = ( _state.value as DetailState.Success).copy(
                    downloadProgress = null, 
                    installing = true
                )
                
                runCatching {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    
                    // Mark as complete after a short delay (user will handle install)
                    progressionTracker.onAppInstalled(app.id)
                    _state.value = ( _state.value as DetailState.Success).copy(
                        installing = false,
                        installComplete = true
                    )
                }.onFailure { e ->
                    _state.value = ( _state.value as DetailState.Success).copy(
                        installing = false,
                        installError = e.message ?: "Installation failed"
                    )
                }
            }.onFailure { e ->
                _state.value = ( _state.value as DetailState.Success).copy(
                    downloadProgress = null,
                    installError = e.message ?: "Download failed"
                )
            }
        }
    }

    fun resetInstallState() {
        val current = _state.value
        if (current is DetailState.Success) {
            _state.value = current.copy(
                installComplete = false,
                installError = null,
                downloadProgress = null,
                installing = false
            )
        }
    }

    private suspend fun downloadFile(
        url: String,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Download failed: ${response.code}")
        val body = response.body ?: throw Exception("No body")
        val total = body.contentLength()
        val dir = File(context.cacheDir, "apk").apply { mkdirs() }
        val file = File(dir, "app_${System.currentTimeMillis()}.apk")
        body.byteStream().use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(8192)
                var downloaded = 0L
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    downloaded += read
                    if (total > 0) onProgress((downloaded.toFloat() / total).coerceIn(0f, 1f))
                }
            }
        }
        file
    }
}

class DetailViewModelFactory(
    private val appId: String,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != DetailViewModel::class.java) throw IllegalArgumentException()
        return DetailViewModel(appId, context) as T
    }
}