package io.github.achmadhafid.firebase_auth_view_model.signin

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import io.github.achmadhafid.firebase_auth_view_model.isSigningIn
import io.github.achmadhafid.zpack.ktx.isConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal abstract class SignInViewModel<E : SignInException> : ViewModel() {

    private val _event = MutableLiveData<SignInEvent<E>>()
    internal val event: LiveData<SignInEvent<E>> = _event

    protected fun executeSignInTask(
        timeout: Long = Long.MAX_VALUE,
        context: Context? = null,
        task: suspend () -> AuthResult
    ) {
        if (true != context?.isConnected) {
            onFailed(offlineException)
            return
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    withTimeout(timeout) {
                        task()
                    }
                }
            }.onSuccess {
                isSigningIn = false
                _event.postValue(SignInEvent(SignInState.OnSuccess(it)))
            }.onFailure {
                onFailed(parseException(it))
            }
        }

        _event.value = SignInEvent(SignInState.OnProgress)
        isSigningIn = true
    }

    protected fun onFailed(exception: E) {
        isSigningIn = false
        _event.postValue(SignInEvent(SignInState.OnFailed(exception)))
    }

    abstract val offlineException: E
    abstract fun parseException(throwable: Throwable): E

}
