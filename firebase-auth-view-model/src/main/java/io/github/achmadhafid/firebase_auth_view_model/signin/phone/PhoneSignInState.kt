package io.github.achmadhafid.firebase_auth_view_model.signin.phone

import io.github.achmadhafid.firebase_auth_view_model.signin.PhoneSignInException

sealed class PhoneSignInState {
    /* Transient State, you should not do anything here */
    object Empty : PhoneSignInState()

    data class GetPhoneInput(val phone: String? = null) : PhoneSignInState()

    object RequestingOtp : PhoneSignInState()

    data class GetOtpInput(val phone: String? = null) : PhoneSignInState()

    object SigningIn : PhoneSignInState()

    /* Terminal State, can only be consumed once */
    object OnSuccess : PhoneSignInState()

    /* Terminal State, can only be consumed once */
    data class OnFailed(val exception: PhoneSignInException) : PhoneSignInState()
}
