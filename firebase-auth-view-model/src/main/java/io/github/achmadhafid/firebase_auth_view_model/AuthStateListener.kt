package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.FirebaseUser

data class AuthStateListener internal constructor(
    internal var onSignInListener: (FirebaseUser) -> Unit = {},
    internal var onSignOutListener: () -> Unit = {},
    internal var onCredentialLinkedListener: (providerId: String) -> Unit = {},
    internal var onCredentialUnlinkedListener: (providerId: String) -> Unit = {}
)

//region Kotlin DSL builder

fun AuthStateListener.onSignedIn(callback: (FirebaseUser) -> Unit) {
    onSignInListener = callback
}

fun AuthStateListener.onSignedOut(callback: () -> Unit) {
    onSignOutListener = callback
}

fun AuthStateListener.onCredentialLinked(callback: (String) -> Unit) {
    onCredentialLinkedListener = callback
}

fun AuthStateListener.onCredentialUnlinked(callback: (String) -> Unit) {
    onCredentialUnlinkedListener = callback
}

//endregion
