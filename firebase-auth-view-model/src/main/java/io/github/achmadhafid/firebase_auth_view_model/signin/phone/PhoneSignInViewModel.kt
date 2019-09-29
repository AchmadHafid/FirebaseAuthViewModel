package io.github.achmadhafid.firebase_auth_view_model.signin.phone

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import io.github.achmadhafid.firebase_auth_view_model.FirebaseAuthExtensions
import io.github.achmadhafid.firebase_auth_view_model.auth
import io.github.achmadhafid.firebase_auth_view_model.signin.PhoneSignInException
import io.github.achmadhafid.zpack.ktx.getViewModel
import io.github.achmadhafid.zpack.ktx.getViewModelWithActivityScope
import io.github.achmadhafid.zpack.ktx.isConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class PhoneSignInViewModel : ViewModel(), FirebaseAuthExtensions {

    private val executor = Executors.newFixedThreadPool(1)
    private val phoneAuthProvider = PhoneAuthProvider.getInstance()
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
                    else -> PhoneSignInException.WrappedFirebaseException(e)
                }
                onFailed(exception)
            }
        }
    private var timeout: Long = Long.MAX_VALUE
    private var phone: String? = null
    private var languageCode: String = "en"
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val _event = MutableLiveData<PhoneSignInEvent>(PhoneSignInEvent(PhoneSignInState.Empty))
    internal val event: LiveData<PhoneSignInEvent> = _event

    private fun setState(state: PhoneSignInState) {
        _event.postValue(PhoneSignInEvent(state))
    }

    private fun onFailed(exception: PhoneSignInException) {
        setState(PhoneSignInState.OnFailed(exception))
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
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
                    is FirebaseAuthException -> PhoneSignInException.WrappedFirebaseException(it)
                    else -> PhoneSignInException.Unknown
                }
                onFailed(exception)
            }
        }
        setState(PhoneSignInState.SigningIn)
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

    internal fun submitPhone(phone: String, context: Context) {
        if (true == context.isConnected) {
            auth.setLanguageCode(languageCode)
            phoneAuthProvider.verifyPhoneNumber(
                phone,
                timeout, TimeUnit.MILLISECONDS,
                executor, phoneAuthCallback
            )
            setState(PhoneSignInState.RequestingOtp)
        } else {
            onFailed(PhoneSignInException.Offline)
        }
    }

    internal fun submitOtp(otp: String, context: Context) {
        if (true == context.isConnected) {
            signInWithCredential(PhoneAuthProvider.getCredential(verificationId!!, otp))
        } else {
            onFailed(PhoneSignInException.Offline)
        }
    }

    internal fun cancel() {
        onFailed(PhoneSignInException.Canceled)
    }

    override fun onCleared() {
        super.onCleared()
        executor.shutdownNow()
    }

}

//region Extensions

interface PhoneSignInExtensions : FirebaseAuthExtensions

//region Activity

fun <T> T.observePhoneSignIn(
    observer: (PhoneSignInEvent) -> Unit
) where T : PhoneSignInExtensions, T : FragmentActivity {
    signInViewModel.event.observe(this, observer)
}

fun <T> T.startPhoneSignIn(
    initialPhone: String? = null,
    languageCode: String = "en",
    timeout: Long = Long.MAX_VALUE
) where T : PhoneSignInExtensions, T : FragmentActivity {
    signInViewModel.start(initialPhone, languageCode, timeout)
}

fun <T> T.submitPhone(phone: String) where T : PhoneSignInExtensions, T : FragmentActivity {
    signInViewModel.submitPhone(phone, this)
}

fun <T> T.submitOtp(otp: String) where T : PhoneSignInExtensions, T : FragmentActivity {
    signInViewModel.submitOtp(otp, this)
}

fun <T> T.cancelPhoneSignIn() where T : PhoneSignInExtensions, T : FragmentActivity {
    signInViewModel.cancel()
}

//endregion
//region Fragment

fun <T> T.observePhoneSignIn(
    observer: (PhoneSignInEvent) -> Unit
) where T : PhoneSignInExtensions, T : Fragment {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun <T> T.startPhoneSignIn(
    initialPhone: String? = null,
    languageCode: String = "en",
    timeout: Long = Long.MAX_VALUE
) where T : PhoneSignInExtensions, T : Fragment {
    signInViewModel.start(initialPhone, languageCode, timeout)
}

fun <T> T.submitPhone(phone: String) where T : PhoneSignInExtensions, T : Fragment {
    signInViewModel.submitPhone(phone, requireContext())
}

fun <T> T.submitOtp(otp: String) where T : PhoneSignInExtensions, T : Fragment {
    signInViewModel.submitOtp(otp, requireContext())
}

fun <T> T.cancelPhoneSignIn() where T : PhoneSignInExtensions, T : Fragment {
    signInViewModel.cancel()
}

//endregion

//endregion
//region Internal extensions functions

private val FragmentActivity.signInViewModel
    get() = getViewModel<PhoneSignInViewModel>()

private val Fragment.signInViewModel
    get() = getViewModelWithActivityScope<PhoneSignInViewModel>()

//endregion
