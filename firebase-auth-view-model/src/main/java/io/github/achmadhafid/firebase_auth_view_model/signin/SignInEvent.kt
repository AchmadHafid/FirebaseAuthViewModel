package io.github.achmadhafid.firebase_auth_view_model.signin

class SignInEvent<out E : SignInException>(private val value: SignInState<E>) {

    private var hasBeenConsumed = false

    fun getState(ignoreTerminalStateIfHasBeenConsumed: Boolean = true): SignInState<E> = when (value) {
        is SignInState.OnFailed,
        is SignInState.OnSuccess -> {
            if (hasBeenConsumed && ignoreTerminalStateIfHasBeenConsumed) SignInState.Empty
            else {
                hasBeenConsumed = true
                value
            }
        }
        else -> value
    }
}

internal typealias AnonymousSignInEvent  = SignInEvent<AnonymousSignInException>
internal typealias GoogleSignInEvent     = SignInEvent<GoogleSignInException>
internal typealias EmailSignInEvent      = SignInEvent<EmailSignInException>
