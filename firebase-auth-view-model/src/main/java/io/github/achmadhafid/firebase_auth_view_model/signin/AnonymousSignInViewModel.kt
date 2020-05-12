package io.github.achmadhafid.firebase_auth_view_model.signin

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuthException
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
        is FirebaseAuthException -> AnonymousSignInException.FireAuthException(throwable)
        else -> AnonymousSignInException.Unknown
    }

    internal fun signInAnonymously(timeout: Long = Long.MAX_VALUE, context: Context? = null) {
        executeSignInTask(timeout, context) {
            firebaseAuth.signInAnonymously().await()
        }
    }
}

//region Consumer API via extension functions
//region Activity

fun AppCompatActivity.observeFireSignInAnonymously(observer: (AnonymousSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

fun AppCompatActivity.startFireSignInAnonymously(timeout: Long = Long.MAX_VALUE) {
    signInViewModel.signInAnonymously(timeout, this)
}

//endregion
//region Fragment

fun Fragment.observeFireSignInAnonymously(observer: (AnonymousSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startFireSignInAnonymously(timeout: Long = Long.MAX_VALUE) {
    signInViewModel.signInAnonymously(timeout, requireContext())
}

//endregion
//endregion
//region Internal extensions functions

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<AnonymousSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<AnonymousSignInViewModel>()

//endregion
