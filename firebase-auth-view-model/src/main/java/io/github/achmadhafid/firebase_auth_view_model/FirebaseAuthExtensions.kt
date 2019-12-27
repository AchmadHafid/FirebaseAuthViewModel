package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

val fireAuth by lazy {
    FirebaseAuth.getInstance()
}

inline val fireUser
    get() = fireAuth.currentUser

inline val FirebaseUser?.isSignedIn
    get() = this != null

inline val FirebaseUser?.isSignedOut
    get() = this == null

internal var isSigningIn: Boolean = false
