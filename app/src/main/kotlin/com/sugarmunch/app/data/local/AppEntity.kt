package com.sugarmunch.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val id: String,
    val name: String,
    val packageName: String,
    val description: String,
    val iconUrl: String?,
    val downloadUrl: String,
    val version: String,
    val source: String?,
    val category: String? = null,
    val accentColor: String? = null,
    val badge: String? = null,
    val featured: Boolean? = null,
    val sortOrder: Int? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun AppEntity.toAppEntry() = com.sugarmunch.app.data.AppEntry(
    id = id,
    name = name,
    packageName = packageName,
    description = description,
    iconUrl = iconUrl,
    downloadUrl = downloadUrl,
    version = version,
    source = source,
    category = category,
    accentColor = accentColor,
    badge = badge,
    featured = featured,
    sortOrder = sortOrder
)

fun com.sugarmunch.app.data.AppEntry.toEntity() = AppEntity(
    id = id,
    name = name,
    packageName = packageName,
    description = description,
    iconUrl = iconUrl,
    downloadUrl = downloadUrl,
    version = version,
    source = source,
    category = category,
    accentColor = accentColor,
    badge = badge,
    featured = featured,
    sortOrder = sortOrder
)