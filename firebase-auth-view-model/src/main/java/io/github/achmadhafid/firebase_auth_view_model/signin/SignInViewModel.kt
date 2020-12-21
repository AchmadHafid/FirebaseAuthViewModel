package io.github.achmadhafid.firebase_auth_view_model.signin

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import io.github.achmadhafid.firebase_auth_view_model.AuthStateListener
import io.github.achmadhafid.firebase_auth_view_model.isConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal abstract class SignInViewModel<E : SignInException> : ViewModel() {

    private val _event = MutableLiveData<SignInEvent<E>>()
    internal val event: LiveData<SignInEvent<E>> = _event

    protected fun executeTask(
        context: Context? = null,
        task: SignInTask,
        authStateListener: AuthStateListener,
        timeout: Long = Long.MAX_VALUE,
        block: suspend () -> AuthResult
    ) {
        if (true != context?.isConnected) {
            onFailed(task, offlineException)
            return
        }

        viewModelScope.launch {
            runCatching {
                _event.value = SignInEvent(SignInState.OnProgress)
                withContext(Dispatchers.IO) {
                    withTimeout(timeout) {
                        block()
                    }
                }
            }.onSuccess {
                onSuccess(task, authStateListener, it)
            }.onFailure {
                onFailed(task, parseException(it))
            }
        }
    }

    protected open fun onSuccess(
        task: SignInTask,
        authStateListener: AuthStateListener,
        authResult: AuthResult
    ) {
        if (task is SignInTask.LinkCredential) {
            authStateListener.onCredentialLinkedListener(task.providerId)
        } else if (task is SignInTask.UnlinkCredential) {
            authStateListener.onCredentialUnlinkedListener(task.providerId)
        }

        _event.postValue(SignInEvent(SignInState.OnSuccess(authResult)))
    }

    protected fun onFailed(@Suppress("UNUSED_PARAMETER") task: SignInTask, exception: E) {
        _event.postValue(SignInEvent(SignInState.OnFailed(exception)))
    }

    abstract val offlineException: E
    abstract fun parseException(throwable: Throwable): E

}
