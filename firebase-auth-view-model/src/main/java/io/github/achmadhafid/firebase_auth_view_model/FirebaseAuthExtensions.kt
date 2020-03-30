package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.FirebaseAuth

val fireAuth by lazy {
    FirebaseAuth.getInstance()
}

inline val fireUser
    get() = fireAuth.currentUser

internal var isSigningIn: Boolean = false
