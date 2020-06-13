package io.github.achmadhafid.firebase_auth_view_model.signin

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuthException
import io.github.achmadhafid.firebase_auth_view_model.AuthStateListener
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.zpack.extension.getViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await

internal class AnonymousSignInViewModel : SignInViewModel<AnonymousSignInException>() {

    override val offlineException: AnonymousSignInException
        get() = AnonymousSignInException.Offline

    override fun parseException(throwable: Throwable): AnonymousSignInException = when (throwable) {
        is AnonymousSignInException -> throwable
        is TimeoutCancellationException -> AnonymousSignInException.Timeout
        is FirebaseAuthException -> AnonymousSignInException.AuthException(throwable)
        else -> AnonymousSignInException.Unknown
    }

    internal fun signInAnonymously(
        context: Context? = null,
        authStateListener: AuthStateListener,
        timeout: Long = Long.MAX_VALUE
    ) {
        executeTask(context, SignInTask.SignIn(SignInTask.Anonymous), authStateListener, timeout) {
            firebaseAuth.signInAnonymously().await()
        }
    }
}

//region Consumer API via extension functions
//region Activity

fun AppCompatActivity.observeSignInAnonymously(observer: (AnonymousSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

fun AppCompatActivity.startSignInAnonymously(
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.signInAnonymously(this, authStateListener, timeout)
}

//endregion
//region Fragment

fun Fragment.observeSignInAnonymously(observer: (AnonymousSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startSignInAnonymously(
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.signInAnonymously(requireContext(), authStateListener, timeout)
}

//endregion
//endregion
//region Internal extensions functions

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<AnonymousSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<AnonymousSignInViewModel>()

//endregion
