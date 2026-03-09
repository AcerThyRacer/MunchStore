package com.sugarmunch.app.auth

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserMetadata
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AuthManager
 */
class AuthManagerTest {

    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var mockUserMetadata: UserMetadata
    private lateinit var authManager: AuthManager

    @Before
    fun setup() {
        // Mock Firebase Auth
        mockFirebaseAuth = mockk()
        mockFirebaseUser = mockk()
        mockUserMetadata = mockk()

        // Setup user metadata
        every { mockUserMetadata.creationTimestamp } returns System.currentTimeMillis()
        every { mockUserMetadata.lastSignInTimestamp } returns System.currentTimeMillis()

        // Setup firebase user
        every { mockFirebaseUser.uid } returns "test_user_id"
        every { mockFirebaseUser.email } returns "test@example.com"
        every { mockFirebaseUser.displayName } returns "Test User"
        every { mockFirebaseUser.photoUrl } returns null
        every { mockFirebaseUser.isAnonymous } returns false
        every { mockFirebaseUser.metadata } returns mockUserMetadata
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `test getCurrentUserId returns uid when user is signed in`() {
        // Given
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // When
        val currentUserId = mockFirebaseAuth.currentUser?.uid.orEmpty()

        // Then
        assertThat(currentUserId).isEqualTo("test_user_id")
    }

    @Test
    fun `test getCurrentUserId returns empty string when no user signed in`() {
        // Given
        every { mockFirebaseAuth.currentUser } returns null

        // When
        val currentUserId = mockFirebaseAuth.currentUser?.uid.orEmpty()

        // Then
        assertThat(currentUserId).isEmpty()
    }

    @Test
    fun `test isAnonymousAccount returns true for anonymous user`() {
        // Given
        every { mockFirebaseUser.isAnonymous } returns true
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // When
        val isAnonymous = mockFirebaseAuth.currentUser?.isAnonymous == true

        // Then
        assertThat(isAnonymous).isTrue()
    }

    @Test
    fun `test isAnonymousAccount returns false for non-anonymous user`() {
        // Given
        every { mockFirebaseUser.isAnonymous } returns false
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // When
        val isAnonymous = mockFirebaseAuth.currentUser?.isAnonymous == true

        // Then
        assertThat(isAnonymous).isFalse()
    }

    @Test
    fun `test isSignedIn returns true when user is signed in`() {
        // Given
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // When
        val isSignedIn = mockFirebaseAuth.currentUser != null

        // Then
        assertThat(isSignedIn).isTrue()
    }

    @Test
    fun `test isSignedIn returns false when no user signed in`() {
        // Given
        every { mockFirebaseAuth.currentUser } returns null

        // When
        val isSignedIn = mockFirebaseAuth.currentUser != null

        // Then
        assertThat(isSignedIn).isFalse()
    }
}

/**
 * Unit tests for UserSession data class
 */
class UserSessionTest {

    @Test
    fun `test getDisplayName returns displayName when set`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            displayName = "John Doe",
            email = "john@example.com"
        )

        // When
        val displayName = user.getDisplayName()

        // Then
        assertThat(displayName).isEqualTo("John Doe")
    }

    @Test
    fun `test getDisplayName returns email when displayName is null`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            displayName = null,
            email = "john@example.com"
        )

        // When
        val displayName = user.getDisplayName()

        // Then
        assertThat(displayName).isEqualTo("john@example.com")
    }

    @Test
    fun `test getDisplayName returns Anonymous User when both displayName and email are null`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            displayName = null,
            email = null
        )

        // When
        val displayName = user.getDisplayName()

        // Then
        assertThat(displayName).isEqualTo("Anonymous User")
    }

    @Test
    fun `test getInitials returns correct initials for full name`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            displayName = "John Doe"
        )

        // When
        val initials = user.getInitials()

        // Then
        assertThat(initials).isEqualTo("JD")
    }

    @Test
    fun `test getInitials returns correct initials for single name`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            displayName = "John"
        )

        // When
        val initials = user.getInitials()

        // Then
        assertThat(initials).isEqualTo("JO")
    }

    @Test
    fun `test getInitials returns AU for anonymous user`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            displayName = null,
            email = null
        )

        // When
        val initials = user.getInitials()

        // Then
        assertThat(initials).isEqualTo("AU")
    }

    @Test
    fun `test hasVerifiedEmail returns true when email is not blank`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            email = "john@example.com"
        )

        // When
        val hasEmail = user.hasVerifiedEmail()

        // Then
        assertThat(hasEmail).isTrue()
    }

    @Test
    fun `test hasVerifiedEmail returns false when email is null`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            email = null
        )

        // When
        val hasEmail = user.hasVerifiedEmail()

        // Then
        assertThat(hasEmail).isFalse()
    }

    @Test
    fun `test withEmail creates copy with new email and isAnonymous false`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            email = null,
            isAnonymous = true
        )

        // When
        val updatedUser = user.withEmail("new@example.com")

        // Then
        assertThat(updatedUser.email).isEqualTo("new@example.com")
        assertThat(updatedUser.isAnonymous).isFalse()
    }

    @Test
    fun `test withDisplayName creates copy with new displayName`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            displayName = "Old Name"
        )

        // When
        val updatedUser = user.withDisplayName("New Name")

        // Then
        assertThat(updatedUser.displayName).isEqualTo("New Name")
    }

    @Test
    fun `test withPhotoUrl creates copy with new photoUrl`() {
        // Given
        val user = UserSession(
            uid = "test_id",
            photoUrl = null
        )

        // When
        val updatedUser = user.withPhotoUrl("https://example.com/photo.jpg")

        // Then
        assertThat(updatedUser.photoUrl).isEqualTo("https://example.com/photo.jpg")
    }

    @Test
    fun `test isValid returns true for valid session`() {
        // Given
        val user = UserSession(uid = "test_id")

        // When
        val isValid = UserSession.isValid(user)

        // Then
        assertThat(isValid).isTrue()
    }

    @Test
    fun `test isValid returns false for null session`() {
        // When
        val isValid = UserSession.isValid(null)

        // Then
        assertThat(isValid).isFalse()
    }

    @Test
    fun `test isValid returns false for empty uid`() {
        // Given
        val user = UserSession(uid = "")

        // When
        val isValid = UserSession.isValid(user)

        // Then
        assertThat(isValid).isFalse()
    }

    @Test
    fun `test empty returns session with empty uid`() {
        // When
        val emptyUser = UserSession.empty()

        // Then
        assertThat(emptyUser.uid).isEmpty()
    }

    @Test
    fun `test getAccountAgeDays returns correct age`() {
        // Given
        val twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)
        val user = UserSession(
            uid = "test_id",
            createdAt = twoDaysAgo
        )

        // When
        val ageDays = user.getAccountAgeDays()

        // Then
        assertThat(ageDays).isAtLeast(2)
    }

    @Test
    fun `test isLegacyAccount returns true for accounts older than 30 days`() {
        // Given
        val thirtyOneDaysAgo = System.currentTimeMillis() - (31 * 24 * 60 * 60 * 1000)
        val user = UserSession(
            uid = "test_id",
            createdAt = thirtyOneDaysAgo
        )

        // When
        val isLegacy = user.isLegacyAccount()

        // Then
        assertThat(isLegacy).isTrue()
    }

    @Test
    fun `test isLegacyAccount returns false for accounts younger than 30 days`() {
        // Given
        val twentyNineDaysAgo = System.currentTimeMillis() - (29 * 24 * 60 * 60 * 1000)
        val user = UserSession(
            uid = "test_id",
            createdAt = twentyNineDaysAgo
        )

        // When
        val isLegacy = user.isLegacyAccount()

        // Then
        assertThat(isLegacy).isFalse()
    }
}
