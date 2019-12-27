package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.FirebaseUser

data class AuthStateListener(
    internal var onSignInListener: (FirebaseUser) -> Unit = {},
    internal var onSignOutListener: () -> Unit = {},
    internal var onAnyListener: () -> Unit = {}
)

//region Kotlin DSL builder

fun AuthStateListener.onSignedIn(callback: (FirebaseUser) -> Unit) {
    onSignInListener = { user ->
        callback(user)
        onAnyListener()
    }
}

fun AuthStateListener.onSignedOut(callback: () -> Unit) {
    onSignOutListener = {
        if (!isSigningIn) {
            callback()
            onAnyListener()
        }
    }
}

fun AuthStateListener.onAny(callback: () -> Unit) {
    onAnyListener = callback
}

//endregion
