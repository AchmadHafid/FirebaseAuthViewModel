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

internal class EmailSignInViewModel : SignInViewModel<EmailSignInException>() {

    override val offlineException: EmailSignInException
        get() = EmailSignInException.Offline

    override fun parseException(throwable: Throwable): EmailSignInException = when (throwable) {
        is TimeoutCancellationException -> EmailSignInException.Timeout
        is FirebaseAuthException -> EmailSignInException.FireAuthException(throwable)
        else -> EmailSignInException.Unknown
    }

    internal fun signInByEmail(
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

    internal fun createUserByEmail(
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

fun AppCompatActivity.observeFireSignInByEmail(observer: (EmailSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

fun AppCompatActivity.startFireSignInByEmail(
    email: String,
    password: String,
    createNew: Boolean = false,
    timeout: Long = Long.MAX_VALUE
) {
    if (createNew) {
        signInViewModel.createUserByEmail(email, password, timeout, this)
    } else {
        signInViewModel.signInByEmail(email, password, timeout, this)
    }
}

//endregion
//region Fragment

fun Fragment.observeFireSignInByEmail(observer: (EmailSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startFireSignInByEmail(
    email: String,
    password: String,
    createNew: Boolean = false,
    timeout: Long = Long.MAX_VALUE
) {
    if (createNew) {
        signInViewModel.createUserByEmail(email, password, timeout, requireContext())
    } else {
        signInViewModel.signInByEmail(email, password, timeout, requireContext())
    }
}

//endregion
//endregion
//region Internal extension functions

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<EmailSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<EmailSignInViewModel>()

//endregion
