package io.github.achmadhafid.firebase_auth_view_model.signin

import com.google.firebase.auth.AuthResult

sealed class SignInState<out E : SignInException> {
    /* Transient State, you should not do anything here */
    object Empty : SignInState<Nothing>()

    object OnProgress : SignInState<Nothing>()

    /* Terminal State, can only be consumed once */
    data class OnSuccess(val authResult: AuthResult) : SignInState<Nothing>()

    /* Terminal State, can only be consumed once */
    data class OnFailed<E : SignInException>(val exception: E) : SignInState<E>()
}
