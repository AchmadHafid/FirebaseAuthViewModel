package io.github.achmadhafid.firebase_auth_view_model.signin.phone

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.getViewModel
import io.github.achmadhafid.firebase_auth_view_model.isConnected
import io.github.achmadhafid.firebase_auth_view_model.signin.PhoneSignInException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

internal class PhoneSignInViewModel : ViewModel() {

    private val auth by lazy { firebaseAuth }
    private val phoneAuthCallback =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verId, token)
                verificationId = verId
                resendToken = token
                setState(PhoneSignInState.GetOtpInput())
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                val exception = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> PhoneSignInException.InvalidRequest
                    is FirebaseTooManyRequestsException -> PhoneSignInException.QuotaExceeded
                    else -> PhoneSignInException.AuthException(e)
                }
                onFailed(exception)
            }
        }
    private var timeout: Long = Long.MAX_VALUE
    private var phone: String? = null
    private var languageCode: String = "en"
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val _event = MutableLiveData<PhoneSignInEvent>()
    internal val event: LiveData<PhoneSignInEvent> = _event

    private fun setState(state: PhoneSignInState) {
        _event.postValue(PhoneSignInEvent(state))
    }

    private fun onFailed(exception: PhoneSignInException) {
        setState(PhoneSignInState.OnFailed(exception))
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        setState(PhoneSignInState.SigningIn)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    withTimeout(timeout) {
                        auth.signInWithCredential(credential).await()
                    }
                }
            }.onSuccess {
                setState(PhoneSignInState.OnSuccess)
            }.onFailure {
                val exception = when (it) {
                    is TimeoutCancellationException -> PhoneSignInException.SignInTimeout
                    is FirebaseAuthException -> PhoneSignInException.AuthException(it)
                    else -> PhoneSignInException.Unknown
                }
                onFailed(exception)
            }
        }
    }

    internal fun start(
        phone: String? = null,
        languageCode: String,
        timeout: Long
    ) {
        this.phone = phone
        this.timeout = timeout
        this.languageCode = languageCode
        setState(PhoneSignInState.GetPhoneInput())
    }

    internal fun submitPhone(activity: AppCompatActivity, phone: String) {
        if (activity.isConnected) {
            auth.setLanguageCode(languageCode)
            PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone)
                    .setTimeout(timeout, TimeUnit.MILLISECONDS)
                    .setActivity(activity)
                    .setCallbacks(phoneAuthCallback)
                    .build()
            )
            setState(PhoneSignInState.RequestingOtp)
        } else {
            onFailed(PhoneSignInException.Offline)
        }
    }

    internal fun submitOtp(otp: String, context: Context) {
        if (context.isConnected) {
            signInWithCredential(PhoneAuthProvider.getCredential(verificationId!!, otp))
        } else {
            onFailed(PhoneSignInException.Offline)
        }
    }

    internal fun cancel() {
        onFailed(PhoneSignInException.Canceled)
    }

}

//region Extensions
//region Activity

fun AppCompatActivity.observeSignInByPhone(observer: (PhoneSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

fun AppCompatActivity.startSignInByPhone(
    initialPhone: String? = null,
    languageCode: String = "en",
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.start(initialPhone, languageCode, timeout)
}

fun AppCompatActivity.onSignInByPhoneSubmitPhone(phone: String) {
    signInViewModel.submitPhone(this, phone)
}

fun AppCompatActivity.onSignInByPhoneSubmitOtp(otp: String) {
    signInViewModel.submitOtp(otp, this)
}

fun AppCompatActivity.cancelSignInByPhone() {
    signInViewModel.cancel()
}

//endregion
//region Fragment

fun Fragment.observeSignInByPhone(observer: (PhoneSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startSignInByPhone(
    initialPhone: String? = null,
    languageCode: String = "en",
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.start(initialPhone, languageCode, timeout)
}

fun Fragment.onSignInByPhoneSubmitPhone(phone: String) {
    signInViewModel.submitPhone(requireActivity() as AppCompatActivity, phone)
}

fun Fragment.onSignInByPhoneSubmitOtp(otp: String) {
    signInViewModel.submitOtp(otp, requireContext())
}

fun Fragment.cancelSignInByPhone() {
    signInViewModel.cancel()
}

//endregion

//endregion
//region Internal extensions functions

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<PhoneSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<PhoneSignInViewModel>()

//endregion
