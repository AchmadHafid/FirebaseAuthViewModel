package io.github.achmadhafid.firebase_auth_view_model.signin

import com.google.firebase.auth.AuthResult

sealed class SignInState<out E : SignInException> {
    object OnProgress : SignInState<Nothing>()
    data class OnSuccess(val authResult: AuthResult) : SignInState<Nothing>()
    data class OnFailed<E : SignInException>(val exception: E) : SignInState<E>()
}
