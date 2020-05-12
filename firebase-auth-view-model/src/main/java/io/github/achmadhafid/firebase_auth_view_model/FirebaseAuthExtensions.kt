package io.github.achmadhafid.firebase_auth_view_model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

inline val firebaseAuth get() = Firebase.auth
inline val firebaseUser get() = firebaseAuth.currentUser

//TODO("Add Email Link login")
//TODO("Add link credentials")
