package io.github.achmadhafid.firebase_auth_view_model.signin

import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException

sealed class SignInException {
    fun toState() = SignInState.OnFailed(this)
}

sealed class AnonymousSignInException : SignInException() {
    object Unknown : AnonymousSignInException()
    object Offline : AnonymousSignInException()
    object Timeout : AnonymousSignInException()
    data class AuthException(val exception: FirebaseAuthException) : AnonymousSignInException()
}

sealed class EmailPasswordSignInException : SignInException() {
    object Unknown : EmailPasswordSignInException()
    object Offline : EmailPasswordSignInException()
    object Timeout : EmailPasswordSignInException()
    object InvalidEmail : EmailPasswordSignInException()
    object Unauthenticated : EmailPasswordSignInException()
    data class AuthException(val exception: FirebaseAuthException) : EmailPasswordSignInException()
}

sealed class EmailLinkSignInException : SignInException() {
    object Unknown : EmailLinkSignInException()
    object Offline : EmailLinkSignInException()
    object Timeout : EmailLinkSignInException()
    object InvalidLink : EmailLinkSignInException()
    object NoEmailFound : EmailLinkSignInException()
    object Unauthenticated : EmailLinkSignInException()
    data class AuthException(val exception: FirebaseAuthException) : EmailLinkSignInException()
}

sealed class GoogleSignInException : SignInException() {

    object Canceled : GoogleSignInException()
    object Unknown : GoogleSignInException()
    object Offline : GoogleSignInException()
    object Timeout : GoogleSignInException()
    //region Link Google Account
    object AlreadySignedIn : GoogleSignInException()
    object AlreadyInUse : GoogleSignInException()
    //endregion
    //region Unlink Google Account
    object Unauthenticated : GoogleSignInException()
    object NotLinkedWithGoogleSignIn : GoogleSignInException()
    object NoOtherSignInProviderFound : GoogleSignInException()
    //endregion
    data class WrappedApiException(val exception: ApiException) : GoogleSignInException()
    data class AuthException(val exception: FirebaseAuthException) : GoogleSignInException()

    companion object {
        const val USER_ALREADY_SIGNED_IN    = "ERROR_USER_ALREADY_SIGNED_IN"
        const val CREDENTIAL_ALREADY_IN_USE = "ERROR_CREDENTIAL_ALREADY_IN_USE"

        internal inline val onUserAlreadySignedIn get() = IllegalStateException(USER_ALREADY_SIGNED_IN)
    }
}

sealed class PhoneSignInException : SignInException() {
    object Canceled : PhoneSignInException()
    object Unknown : PhoneSignInException()
    object Offline : PhoneSignInException()
    object RequestOtpTimeout : PhoneSignInException()
    object SignInTimeout : PhoneSignInException()
    object InvalidRequest : PhoneSignInException()
    data class InvalidOtp(val otp: String) : PhoneSignInException()
    object QuotaExceeded : PhoneSignInException()
    data class AuthException(val exception: FirebaseException) : PhoneSignInException()
}
