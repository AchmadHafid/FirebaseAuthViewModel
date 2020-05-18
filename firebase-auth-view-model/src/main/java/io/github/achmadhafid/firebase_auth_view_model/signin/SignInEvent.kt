package io.github.achmadhafid.firebase_auth_view_model.signin

class SignInEvent<out E : SignInException>(private val state: SignInState<E>) {

    private var hasBeenConsumed = false

    fun getState(isConsumed: Boolean = true) = (state to hasBeenConsumed).also {
        if (isConsumed) hasBeenConsumed = true
    }

}

internal typealias AnonymousSignInEvent     = SignInEvent<AnonymousSignInException>
internal typealias GoogleSignInEvent        = SignInEvent<GoogleSignInException>
internal typealias EmailPasswordSignInEvent = SignInEvent<EmailPasswordSignInException>
internal typealias EmailLinkSignInEvent     = SignInEvent<EmailLinkSignInException>
