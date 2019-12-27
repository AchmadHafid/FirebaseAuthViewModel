package io.github.achmadhafid.firebase_auth_view_model.signin

class SignInEvent<out E : SignInException>(private val value: SignInState<E>) {

    private var hasBeenConsumed = false

    val state: Pair<SignInState<E>, Boolean>
        get() {
            val ret = value to hasBeenConsumed
            hasBeenConsumed = true
            return ret
        }
}

internal typealias AnonymousSignInEvent = SignInEvent<AnonymousSignInException>
internal typealias GoogleSignInEvent    = SignInEvent<GoogleSignInException>
internal typealias EmailSignInEvent     = SignInEvent<EmailSignInException>
