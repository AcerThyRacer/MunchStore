package com.sugarmunch.app.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.AnonymousAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.sugarmunch.app.util.CoroutineUtils.safeCollect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

/**
 * AuthManager - Central authentication management for SugarMunch
 *
 * Features:
 * - Anonymous authentication by default
 * - Optional Google sign-in upgrade
 * - Session persistence across app restarts
 * - User profile management
 * - Auth state observation
 *
 * Usage:
 * ```kotlin
 * val authManager = AuthManager.getInstance(context)
 *
 * // Observe auth state
 * authManager.currentUser.collect { user ->
 *     if (user != null) {
 *         // User is signed in
 *     }
 * }
 *
 * // Sign in anonymously
 * authManager.signInAnonymously()
 *
 * // Upgrade to Google sign-in
 * authManager.signInWithGoogle(idToken)
 * ```
 */
@Singleton
class AuthManager @Inject constructor(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Auth state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Current user flow
    val currentUser: StateFlow<UserSession?> = _authState
        .map { state ->
            when (state) {
                is AuthState.Authenticated -> state.user
                else -> null
            }
        }
        .asStateFlow()

    // Auth state listener
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        setupAuthStateListener()
        loadPersistedSession()
    }

    /**
     * Set up Firebase auth state listener
     */
    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // User is signed in
                val user = UserSession(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    isAnonymous = firebaseUser.isAnonymous,
                    createdAt = firebaseUser.metadata.creationTimestamp,
                    lastSignInAt = firebaseUser.metadata.lastSignInTimestamp
                )
                _authState.value = AuthState.Authenticated(user)
                persistSession(user)
            } else {
                // User is signed out
                _authState.value = AuthState.Unauthenticated
                clearPersistedSession()
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener!!)
    }

    /**
     * Load persisted session from DataStore
     */
    private fun loadPersistedSession() {
        // Session will be loaded via auth state listener when Firebase initializes
        // This is just a fallback for offline scenarios
        _authState.value = AuthState.Initializing
    }

    /**
     * Persist session to DataStore
     */
    private suspend fun persistSession(user: UserSession) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_UID] = user.uid
            prefs[PreferencesKeys.USER_EMAIL] = user.email.orEmpty()
            prefs[PreferencesKeys.USER_DISPLAY_NAME] = user.displayName.orEmpty()
            prefs[PreferencesKeys.USER_PHOTO_URL] = user.photoUrl.orEmpty()
            prefs[PreferencesKeys.USER_IS_ANONYMOUS] = user.isAnonymous
            prefs[PreferencesKeys.USER_CREATED_AT] = user.createdAt
            prefs[PreferencesKeys.USER_LAST_SIGN_IN] = user.lastSignInAt
        }
    }

    /**
     * Clear persisted session from DataStore
     */
    private suspend fun clearPersistedSession() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // ═════════════════════════════════════════════════════════════
    // SIGN IN METHODS
    // ═════════════════════════════════════════════════════════════

    /**
     * Sign in anonymously
     * Creates a temporary anonymous account that can be upgraded later
     *
     * @return Result containing UserSession on success, or error message on failure
     */
    suspend fun signInAnonymously(): Result<UserSession> {
        return try {
            _authState.value = AuthState.SigningIn

            val authResult = firebaseAuth.signInAnonymously().await()
            val firebaseUser = authResult.user ?: throw AuthException("Firebase user is null")

            val user = UserSession(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString(),
                isAnonymous = true,
                createdAt = firebaseUser.metadata.creationTimestamp,
                lastSignInAt = firebaseUser.metadata.lastSignInTimestamp
            )

            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        }
    }

    /**
     * Sign in with Google
     * Upgrades anonymous account or creates new Google account
     *
     * @param idToken Google ID token from Google Sign-In
     * @return Result containing UserSession on success, or error message on failure
     */
    suspend fun signInWithGoogle(idToken: String): Result<UserSession> {
        return try {
            _authState.value = AuthState.SigningIn

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw AuthException("Firebase user is null")

            val wasAnonymous = firebaseAuth.currentUser?.isAnonymous == true

            val user = UserSession(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString(),
                isAnonymous = false,
                createdAt = firebaseUser.metadata.creationTimestamp,
                lastSignInAt = firebaseUser.metadata.lastSignInTimestamp
            )

            _authState.value = AuthState.Authenticated(user)

            if (wasAnonymous) {
                // Account was upgraded from anonymous
                Result.success(user.copy(wasUpgradedFromAnonymous = true))
            } else {
                Result.success(user)
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     * For future email/password authentication
     *
     * @param email User email
     * @param password User password
     * @return Result containing UserSession on success, or error message on failure
     */
    suspend fun signInWithEmail(email: String, password: String): Result<UserSession> {
        return try {
            _authState.value = AuthState.SigningIn

            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw AuthException("Firebase user is null")

            val user = UserSession(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString(),
                isAnonymous = false,
                createdAt = firebaseUser.metadata.creationTimestamp,
                lastSignInAt = firebaseUser.metadata.lastSignInTimestamp
            )

            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        }
    }

    /**
     * Create account with email and password
     *
     * @param email User email
     * @param password User password
     * @return Result containing UserSession on success, or error message on failure
     */
    suspend fun createAccountWithEmail(email: String, password: String): Result<UserSession> {
        return try {
            _authState.value = AuthState.SigningIn

            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw AuthException("Firebase user is null")

            val user = UserSession(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString(),
                isAnonymous = false,
                createdAt = firebaseUser.metadata.creationTimestamp,
                lastSignInAt = firebaseUser.metadata.lastSignInTimestamp
            )

            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // SIGN OUT & DELETE
    // ═════════════════════════════════════════════════════════════

    /**
     * Sign out current user
     * Clears local session and signs out from Firebase
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            clearPersistedSession()
            _authState.value = AuthState.Unauthenticated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete current user account
     * Permanently deletes the user account from Firebase
     *
     * @return Result indicating success or failure
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw AuthException("No user signed in")
            user.delete().await()
            signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // ACCOUNT UPGRADE & LINKING
    // ═════════════════════════════════════════════════════════════

    /**
     * Check if current account is anonymous
     */
    fun isAnonymousAccount(): Boolean {
        return firebaseAuth.currentUser?.isAnonymous == true
    }

    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Get current Firebase user directly
     */
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Get current user ID
     * Returns empty string if no user is signed in
     */
    fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid.orEmpty()
    }

    /**
     * Link anonymous account with Google credential
     * Upgrades anonymous account without losing data
     *
     * @param idToken Google ID token
     * @return Result indicating success or failure
     */
    suspend fun linkWithGoogle(idToken: String): Result<UserSession> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = firebaseAuth.currentUser ?: throw AuthException("No user signed in")

            user.linkWithCredential(credential).await()

            // Refresh user session
            val updatedUser = firebaseAuth.currentUser ?: throw AuthException("Firebase user is null")

            val session = UserSession(
                uid = updatedUser.uid,
                email = updatedUser.email,
                displayName = updatedUser.displayName,
                photoUrl = updatedUser.photoUrl?.toString(),
                isAnonymous = false,
                createdAt = updatedUser.metadata.creationTimestamp,
                lastSignInAt = updatedUser.metadata.lastSignInTimestamp,
                wasUpgradedFromAnonymous = true
            )

            _authState.value = AuthState.Authenticated(session)
            persistSession(session)

            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user profile
     *
     * @param displayName New display name
     * @param photoUrl New photo URL
     * @return Result indicating success or failure
     */
    suspend fun updateProfile(
        displayName: String? = null,
        photoUrl: String? = null
    ): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw AuthException("No user signed in")

            val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .apply {
                    displayName?.let { setDisplayName(it) }
                    photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
                }
                .build()

            user.updateProfile(profileUpdate).await()

            // Update local session
            val updatedUser = firebaseAuth.currentUser ?: throw AuthException("Firebase user is null")
            val session = currentUser.value?.copy(
                displayName = updatedUser.displayName,
                photoUrl = updatedUser.photoUrl?.toString()
            ) ?: UserSession(
                uid = updatedUser.uid,
                email = updatedUser.email,
                displayName = updatedUser.displayName,
                photoUrl = updatedUser.photoUrl?.toString(),
                isAnonymous = updatedUser.isAnonymous,
                createdAt = updatedUser.metadata.creationTimestamp,
                lastSignInAt = updatedUser.metadata.lastSignInTimestamp
            )

            _authState.value = AuthState.Authenticated(session)
            persistSession(session)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send password reset email
     *
     * @param email User email
     * @return Result indicating success or failure
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reauthenticate user
     * Required for sensitive operations like deleting account or changing email
     *
     * @param credential Firebase credential
     * @return Result indicating success or failure
     */
    suspend fun reauthenticate(credential: com.google.firebase.auth.AuthCredential): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw AuthException("No user signed in")
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // ACCOUNT MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    /**
     * Change email address
     * Requires recent reauthentication
     *
     * @param newEmail New email address
     * @return Result indicating success or failure
     */
    suspend fun changeEmail(newEmail: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw AuthException("No user signed in")
            user.verifyBeforeUpdateEmail(newEmail).await()
            
            // Update local session
            val updatedUser = firebaseAuth.currentUser ?: throw AuthException("Firebase user is null")
            val session = currentUser.value?.copy(
                email = updatedUser.email
            ) ?: UserSession(
                uid = updatedUser.uid,
                email = updatedUser.email,
                displayName = updatedUser.displayName,
                photoUrl = updatedUser.photoUrl?.toString(),
                isAnonymous = updatedUser.isAnonymous,
                createdAt = updatedUser.metadata.creationTimestamp,
                lastSignInAt = updatedUser.metadata.lastSignInTimestamp
            )
            
            _authState.value = AuthState.Authenticated(session)
            persistSession(session)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Change password
     * Requires recent reauthentication
     *
     * @param newPassword New password
     * @return Result indicating success or failure
     */
    suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw AuthException("No user signed in")
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get account creation date
     */
    fun getAccountCreationDate(): Long? {
        return firebaseAuth.currentUser?.metadata?.creationTimestamp
    }

    /**
     * Get account providers (email, google, etc.)
     */
    suspend fun getAccountProviders(): List<String> {
        return try {
            val email = firebaseAuth.currentUser?.email ?: return emptyList()
            val methods = firebaseAuth.fetchSignInMethodsForEmail(email).await()
            methods.signInMethods ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if email is verified
     */
    fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    /**
     * Send email verification
     */
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw AuthException("No user signed in")
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get personal data for export (GDPR compliance)
     */
    suspend fun getPersonalData(): PersonalData {
        val user = firebaseAuth.currentUser
        return PersonalData(
            uid = user?.uid.orEmpty(),
            email = user?.email.orEmpty(),
            displayName = user?.displayName.orEmpty(),
            photoUrl = user?.photoUrl?.toString().orEmpty(),
            isAnonymous = user?.isAnonymous == true,
            isEmailVerified = user?.isEmailVerified == true,
            createdAt = user?.metadata?.creationTimestamp ?: 0L,
            lastSignInAt = user?.metadata?.lastSignInTimestamp ?: 0L,
            providerId = user?.providerId.orEmpty(),
            providers = getAccountProviders()
        )
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        authStateListener?.let { firebaseAuth.removeAuthStateListener(it) }
    }

    companion object {
        private const val TAG = "AuthManager"
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

/**
 * Represents the current authentication state
 */
sealed class AuthState {
    /**
     * Auth system is initializing
     */
    object Initializing : AuthState()

    /**
     * User is in the process of signing in
     */
    object SigningIn : AuthState()

    /**
     * User is authenticated
     *
     * @param user The authenticated user session
     */
    data class Authenticated(val user: UserSession) : AuthState()

    /**
     * User is not authenticated
     */
    object Unauthenticated : AuthState()

    /**
     * Authentication error occurred
     *
     * @param error The error message
     */
    data class Error(val error: String) : AuthState()
}

/**
 * Authentication exception
 */
class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Preferences keys for DataStore
 */
private object PreferencesKeys {
    val USER_UID = stringPreferencesKey("user_uid")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
    val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
    val USER_IS_ANONYMOUS = booleanPreferencesKey("user_is_anonymous")
    val USER_CREATED_AT = longPreferencesKey("user_created_at")
    val USER_LAST_SIGN_IN = longPreferencesKey("user_last_sign_in")
}

// ═════════════════════════════════════════════════════════════
// PERSONAL DATA
// ═════════════════════════════════════════════════════════════

/**
 * Personal data for GDPR export
 */
data class PersonalData(
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean,
    val isEmailVerified: Boolean,
    val createdAt: Long,
    val lastSignInAt: Long,
    val providerId: String,
    val providers: List<String>
)
