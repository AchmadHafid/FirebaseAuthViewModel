package io.github.achmadhafid.firebase_auth_view_model.signin

sealed class SignInTask {
    data class SignIn(val providerId: String) : SignInTask()
    data class LinkCredential(val providerId: String) : SignInTask()
    data class UnlinkCredential(val providerId: String) : SignInTask()

    companion object {
        internal const val Anonymous = "firebase"
    }
}
