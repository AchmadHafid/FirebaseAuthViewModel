package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.FirebaseUser

data class AuthStateListener(
    internal var onSignInListener: (FirebaseUser) -> Unit = {},
    internal var onSignOutListener: () -> Unit = {},
    internal var onAnyListener: (FirebaseUser?) -> Unit = {}
)

//region Kotlin DSL builder

fun AuthStateListener.onSignedIn(callback: (FirebaseUser) -> Unit) {
    onSignInListener = { user ->
        callback(user)
        onAnyListener(user)
    }
}

fun AuthStateListener.onSignedOut(callback: () -> Unit) {
    onSignOutListener = {
        if (!isSigningIn) {
            callback()
            onAnyListener(null)
        }
    }
}

fun AuthStateListener.onAny(callback: (FirebaseUser?) -> Unit) {
    onAnyListener = callback
}

//endregion
