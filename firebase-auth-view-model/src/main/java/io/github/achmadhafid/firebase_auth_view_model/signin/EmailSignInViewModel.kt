package io.github.achmadhafid.firebase_auth_view_model.signin

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuthException
import io.github.achmadhafid.firebase_auth_view_model.FirebaseAuthExtensions
import io.github.achmadhafid.firebase_auth_view_model.auth
import io.github.achmadhafid.zpack.ktx.getViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await

internal class EmailSignInViewModel : SignInViewModel<EmailSignInException>() {

    override val offlineException: EmailSignInException
        get() = EmailSignInException.Offline

    override fun parseException(throwable: Throwable): EmailSignInException = when (throwable) {
        is TimeoutCancellationException -> EmailSignInException.Timeout
        is FirebaseAuthException -> EmailSignInException.WrappedFirebaseAuthException(throwable)
        else -> EmailSignInException.Unknown
    }

    internal fun signInByEmail(
        email: String,
        password: String,
        timeout: Long = Long.MAX_VALUE,
        context: Context? = null
    ) {
        executeSignInTask(timeout, context) {
            auth.signInWithEmailAndPassword(email, password)
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
            auth.createUserWithEmailAndPassword(email, password)
                .await()
        }
    }

}

//region Consumer API via extension functions

interface EmailSignInExtensions : FirebaseAuthExtensions

//region Activity

fun <T> T.signInByEmail(
    email: String,
    password: String,
    createNew: Boolean = false,
    timeout: Long = Long.MAX_VALUE
) where T : EmailSignInExtensions, T : FragmentActivity {
    if (createNew)
        signInViewModel.createUserByEmail(email, password, timeout, this)
    else
        signInViewModel.signInByEmail(email, password, timeout, this)
}

fun <T> T.observeEmailSignIn(
    observer: (EmailSignInEvent) -> Unit
) where T : EmailSignInExtensions, T : FragmentActivity {
    signInViewModel.event.observe(this, observer)
}

//endregion
//region Fragment

fun <T> T.signInByEmail(
    email: String,
    password: String,
    createNew: Boolean = false,
    timeout: Long = Long.MAX_VALUE
) where T : EmailSignInExtensions, T : Fragment {
    if (createNew)
        signInViewModel.createUserByEmail(email, password, timeout, requireContext())
    else
        signInViewModel.signInByEmail(email, password, timeout, requireContext())
}

fun <T> T.observeEmailSignIn(
    observer: (EmailSignInEvent) -> Unit
) where T : EmailSignInExtensions, T : Fragment {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

//endregion

//endregion
//region Internal extension functions

private val FragmentActivity.signInViewModel
    get() = getViewModel<EmailSignInViewModel>()

private val Fragment.signInViewModel
    get() = getViewModel<EmailSignInViewModel>()

//endregion
