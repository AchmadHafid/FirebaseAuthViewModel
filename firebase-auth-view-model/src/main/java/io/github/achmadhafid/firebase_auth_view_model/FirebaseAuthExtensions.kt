package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.FirebaseAuth

val firebaseAuth by lazy { FirebaseAuth.getInstance() }

inline val firebaseUser get() = firebaseAuth.currentUser

internal var isSigningIn: Boolean = false

//TODO("rename functions")
//TODO("Add Email Link login")
//TODO("Add link credentials")
