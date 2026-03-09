package com.sugarmunch.app.theme.engine

import com.sugarmunch.app.data.PreferencesRepository
import com.sugarmunch.app.theme.profile.ThemeProfileRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeManagerEntryPoint {
    fun themeProfileRepository(): ThemeProfileRepository
    fun preferencesRepository(): PreferencesRepository
}
