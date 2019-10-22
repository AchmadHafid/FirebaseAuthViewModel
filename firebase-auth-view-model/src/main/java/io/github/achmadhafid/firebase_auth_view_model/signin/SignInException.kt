package io.github.achmadhafid.firebase_auth_view_model.signin

import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException

sealed class SignInException

sealed class AnonymousSignInException : SignInException() {
    object Unknown : AnonymousSignInException()
    object Offline : AnonymousSignInException()
    object Timeout : AnonymousSignInException()
    data class WrappedFirebaseAuthException(val firebaseAuthException: FirebaseAuthException) :
        AnonymousSignInException()
}
sealed class EmailSignInException : SignInException() {
    object Unknown : EmailSignInException()
    object Offline : EmailSignInException()
    object Timeout : EmailSignInException()
    data class WrappedFirebaseAuthException(val firebaseAuthException: FirebaseAuthException) :
        EmailSignInException()
}

sealed class GoogleSignInException : SignInException() {
    object Canceled : GoogleSignInException()
    object Unknown : GoogleSignInException()
    object Offline : GoogleSignInException()
    object Timeout : GoogleSignInException()
    data class WrappedApiException(val apiException: ApiException) : GoogleSignInException()
    data class WrappedFirebaseAuthException(val firebaseAuthException: FirebaseAuthException) :
        GoogleSignInException()
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
    data class WrappedFirebaseException(val firebaseException: FirebaseException) :
        PhoneSignInException()
}
