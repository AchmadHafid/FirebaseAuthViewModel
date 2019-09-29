package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.FirebaseAuth

interface FirebaseAuthExtensions

val FirebaseAuthExtensions.auth by lazy {
    FirebaseAuth.getInstance()
}

inline val FirebaseAuthExtensions.user
    get() = auth.currentUser

inline val FirebaseAuthExtensions.isSignedIn
    get() = user != null

inline val FirebaseAuthExtensions.isSignedOut
    get() = user == null

fun FirebaseAuthExtensions.signOut() {
    auth.signOut()
}
