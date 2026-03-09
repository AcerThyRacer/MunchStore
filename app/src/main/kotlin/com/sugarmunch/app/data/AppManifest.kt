package com.sugarmunch.app.data

import com.google.gson.annotations.SerializedName

data class AppEntry(
    val id: String,
    val name: String,
    @SerializedName("packageName") val packageName: String,
    val description: String,
    @SerializedName("iconUrl") val iconUrl: String? = null,
    @SerializedName("downloadUrl") val downloadUrl: String,
    val version: String,
    val source: String? = null,
    val category: String? = null,
    /** Hex color e.g. "#FFB6C1" for card accent or icon tint. */
    @SerializedName("accentColor") val accentColor: String? = null,
    /** e.g. "New", "Updated" – shown as chip on card. */
    @SerializedName("badge") val badge: String? = null,
    /** When true, show in Featured section at top of catalog. */
    @SerializedName("featured") val featured: Boolean? = null,
    /** Optional manual sort order within category (lower first). */
    @SerializedName("sortOrder") val sortOrder: Int? = null,
    /** Optional URL for in-store WebView preview (try before install). */
    @SerializedName("previewUrl") val previewUrl: String? = null,
    /** Optional: "widget", "tool", etc. for modular components. */
    @SerializedName("components") val components: List<String>? = null,
    /** Optional trailer video URL for TV / Sugartube. */
    @SerializedName("trailerUrl") val trailerUrl: String? = null,
    /** Optional screenshot URLs for detail screen carousel. */
    @SerializedName("screenshots") val screenshots: List<String>? = null
)

data class CandyTrailEntry(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    @SerializedName("appIds") val appIds: List<String>
)

data class AppsManifest(
    val apps: List<AppEntry>,
    val categories: List<CategoryEntry>? = null,
    val trails: List<CandyTrailEntry>? = null
)

data class CategoryEntry(
    val id: String,
    val name: String
)
