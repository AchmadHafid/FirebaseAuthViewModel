package io.github.achmadhafid.firebase_auth_view_model.signin.phone

class PhoneSignInEvent(private val value: PhoneSignInState) {

    private var isNew = false

    val state: Pair<PhoneSignInState, Boolean>
        get() {
            val ret = if (isNew) value to true else value to false
            isNew = false
            return ret
        }
}
