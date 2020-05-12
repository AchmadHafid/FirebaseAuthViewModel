package io.github.achmadhafid.firebase_auth_view_model.signin

import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException

sealed class SignInException

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
    data class AuthException(val exception: FirebaseAuthException) : EmailPasswordSignInException()
}

sealed class EmailLinkSignInException : SignInException() {
    object Unknown : EmailLinkSignInException()
    object Offline : EmailLinkSignInException()
    object Timeout : EmailLinkSignInException()
    data class AuthException(val exception: FirebaseAuthException) : EmailLinkSignInException()
}

sealed class GoogleSignInException : SignInException() {
    object Canceled : GoogleSignInException()
    object Unknown : GoogleSignInException()
    object Offline : GoogleSignInException()
    object Timeout : GoogleSignInException()
    data class WrappedApiException(val exception: ApiException) : GoogleSignInException()
    data class AuthException(val exception: FirebaseAuthException) : GoogleSignInException()
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
