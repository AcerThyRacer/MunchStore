package com.sugarmunch.tv.di

import android.content.Context
import com.sugarmunch.app.repository.SmartManifestRepository
import com.sugarmunch.tv.data.TvRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for TV-specific dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object TvModule {

    @Provides
    @Singleton
    fun provideTvRepository(
        @ApplicationContext context: Context,
        smartManifestRepository: SmartManifestRepository
    ): TvRepository {
        return TvRepository(context, smartManifestRepository)
    }
}
