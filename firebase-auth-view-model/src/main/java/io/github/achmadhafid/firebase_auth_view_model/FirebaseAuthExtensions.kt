package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

inline val firebaseAuth get() = Firebase.auth
inline val firebaseUser get() = firebaseAuth.currentUser

inline val FirebaseUser.hasMultipleAuth
    get() = providerData.size > 1

inline val FirebaseUser.hasAnonymousAuth
    get() = providerData.any { it.providerId == FirebaseAuthProvider.PROVIDER_ID }

inline val FirebaseUser.hasGoogleAuth
    get() = providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }

inline val FirebaseUser.hasEmailPasswordAuth
    get() = providerData.any { it.providerId == EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD }

inline val FirebaseUser.hasEmailLinkAuth
    get() = providerData.any { it.providerId == EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD }

inline val FirebaseUser.isAnonymousAuth
    get() = hasAnonymousAuth && !hasMultipleAuth

inline val FirebaseUser.isGoogleAuth
    get() = hasGoogleAuth && !hasMultipleAuth

inline val FirebaseUser.isEmailPasswordAuth
    get() = hasEmailPasswordAuth && !hasMultipleAuth

inline val FirebaseUser.isEmailLinkAuth
    get() = hasEmailLinkAuth && !hasMultipleAuth
