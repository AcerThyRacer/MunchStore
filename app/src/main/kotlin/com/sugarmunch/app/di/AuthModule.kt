package com.sugarmunch.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sugarmunch.app.auth.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection Module - Authentication
 * Provides authentication-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /**
     * Provide FirebaseAuth instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * Provide current FirebaseUser (nullable)
     * Note: This is a snapshot of the current user at injection time
     * For reactive updates, use AuthManager.currentUser Flow
     */
    @Provides
    @Singleton
    fun provideCurrentFirebaseUser(auth: FirebaseAuth): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Provide AuthManager
     * Main authentication manager for the app
     */
    @Provides
    @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context,
        authDataStore: DataStore<Preferences>
    ): AuthManager {
        return AuthManager(context, authDataStore)
    }

    /**
     * Provide Auth DataStore
     * Separate DataStore for auth preferences
     */
    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.authDataStore
    }
}

// Extension property for Context to access auth DataStore
private val Context.authDataStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
    name = "auth_preferences"
)
