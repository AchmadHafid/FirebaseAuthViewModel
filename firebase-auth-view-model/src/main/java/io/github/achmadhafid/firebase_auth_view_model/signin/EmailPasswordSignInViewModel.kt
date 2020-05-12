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

internal class EmailPasswordSignInViewModel : SignInViewModel<EmailPasswordSignInException>() {

    override val offlineException: EmailPasswordSignInException
        get() = EmailPasswordSignInException.Offline

    override fun parseException(throwable: Throwable): EmailPasswordSignInException = when (throwable) {
        is TimeoutCancellationException -> EmailPasswordSignInException.Timeout
        is FirebaseAuthException -> EmailPasswordSignInException.AuthException(throwable)
        else -> EmailPasswordSignInException.Unknown
    }

    internal fun signInByEmailPassword(
        email: String,
        password: String,
        timeout: Long = Long.MAX_VALUE,
        context: Context? = null
    ) {
        executeSignInTask(timeout, context) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .await()
        }
    }

    internal fun createUserByEmailPassword(
        email: String,
        password: String,
        timeout: Long = Long.MAX_VALUE,
        context: Context? = null
    ) {
        executeSignInTask(timeout, context) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .await()
        }
    }

}

//region Consumer API via extension functions
//region Activity

fun AppCompatActivity.observeSignInByEmailPassword(observer: (EmailPasswordSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

fun AppCompatActivity.startSignInByEmailPassword(
    email: String,
    password: String,
    createNew: Boolean = false,
    timeout: Long = Long.MAX_VALUE
) {
    if (createNew) {
        signInViewModel.createUserByEmailPassword(email, password, timeout, this)
    } else {
        signInViewModel.signInByEmailPassword(email, password, timeout, this)
    }
}

//endregion
//region Fragment

fun Fragment.observeSignInByEmailPassword(observer: (EmailPasswordSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startSignInByEmailPassword(
    email: String,
    password: String,
    createNew: Boolean = false,
    timeout: Long = Long.MAX_VALUE
) {
    if (createNew) {
        signInViewModel.createUserByEmailPassword(email, password, timeout, requireContext())
    } else {
        signInViewModel.signInByEmailPassword(email, password, timeout, requireContext())
    }
}

//endregion
//endregion
//region Internal extension functions

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<EmailPasswordSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<EmailPasswordSignInViewModel>()

//endregion
