package io.github.achmadhafid.firebase_auth_view_model.signin.phone

import io.github.achmadhafid.firebase_auth_view_model.signin.PhoneSignInException

sealed class PhoneSignInState {
    data class GetPhoneInput(val phone: String? = null) : PhoneSignInState()
    object RequestingOtp : PhoneSignInState()
    data class GetOtpInput(val phone: String? = null) : PhoneSignInState()
    object SigningIn : PhoneSignInState()
    object OnSuccess : PhoneSignInState()
    data class OnFailed(val exception: PhoneSignInException) : PhoneSignInState()
}
