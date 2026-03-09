package com.sugarmunch.app.theme.profile

import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.sugarmunch.app.theme.builder.CustomTheme
import com.sugarmunch.app.ui.studio.ExportableTheme
import com.sugarmunch.app.ui.studio.toCustomThemeSpec

data class ThemeImportCandidate(
    val profile: ThemeProfile,
    val sourceLabel: String,
    val isLegacyFormat: Boolean = false
)

fun ThemeProfile.toTransportEnvelope(): ThemeTransportEnvelope = ThemeTransportEnvelope(profile = this)

fun ThemeTransportEnvelope.toJson(gson: Gson): String = gson.toJson(this)

fun ThemeTransportEnvelope.toBase64Payload(gson: Gson): String {
    return Base64.encodeToString(toJson(gson).toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
}

fun ThemeProfile.toDeepLinkUri(gson: Gson): Uri {
    return Uri.parse("sugarmunch://theme?payload=${toTransportEnvelope().toBase64Payload(gson)}")
}

fun parseThemeImportCandidate(raw: String, gson: Gson): ThemeImportCandidate? {
    parseEnvelope(raw, gson)?.let {
        return ThemeImportCandidate(
            profile = it.profile,
            sourceLabel = "Canonical envelope"
        )
    }

    decodeEnvelopeBase64(raw, gson)?.let {
        return ThemeImportCandidate(
            profile = it.profile,
            sourceLabel = "Canonical base64 envelope"
        )
    }

    runCatching { CustomTheme.fromExportCode(raw) }.getOrNull()?.let { legacyBuilderTheme ->
        return ThemeImportCandidate(
            profile = legacyBuilderTheme.toThemeProfile(),
            sourceLabel = "Legacy builder export code",
            isLegacyFormat = true
        )
    }

    runCatching { gson.fromJson(raw, ExportableTheme::class.java) }.getOrNull()?.let { legacyStudioTheme ->
        return ThemeImportCandidate(
            profile = legacyStudioTheme.toCustomThemeSpec().toThemeProfile(),
            sourceLabel = "Legacy studio JSON",
            isLegacyFormat = true
        )
    }

    return null
}

private fun parseEnvelope(raw: String, gson: Gson): ThemeTransportEnvelope? {
    return runCatching { gson.fromJson(raw, ThemeTransportEnvelope::class.java) }.getOrNull()
}

private fun decodeEnvelopeBase64(raw: String, gson: Gson): ThemeTransportEnvelope? {
    return runCatching {
        val decoded = String(Base64.decode(raw, Base64.URL_SAFE or Base64.NO_WRAP))
        gson.fromJson(decoded, ThemeTransportEnvelope::class.java)
    }.getOrNull()
}
