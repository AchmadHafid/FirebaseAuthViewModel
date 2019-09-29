package io.github.achmadhafid.firebase_auth_view_model.signin.phone

class PhoneSignInEvent(private val value: PhoneSignInState) {

    private var hasBeenConsumed = false

    fun getState(): PhoneSignInState = when (value) {
        is PhoneSignInState.OnFailed,
        is PhoneSignInState.OnSuccess -> {
            if (hasBeenConsumed) PhoneSignInState.Empty
            else {
                hasBeenConsumed = true
                value
            }
        }
        else -> value
    }
}
