package io.github.achmadhafid.firebase_auth_view_model.signin

class SignInEvent<out E : SignInException> internal constructor(
    private val state: SignInState<E>
) {

    private var hasBeenConsumed = false

    fun getEvent(isConsumed: Boolean = true) = hasBeenConsumed.let {
        hasBeenConsumed = it || isConsumed
        state to it
    }

}

internal typealias AnonymousSignInEvent     = SignInEvent<AnonymousSignInException>
internal typealias GoogleSignInEvent        = SignInEvent<GoogleSignInException>
internal typealias EmailPasswordSignInEvent = SignInEvent<EmailPasswordSignInException>
internal typealias EmailLinkSignInEvent     = SignInEvent<EmailLinkSignInException>
