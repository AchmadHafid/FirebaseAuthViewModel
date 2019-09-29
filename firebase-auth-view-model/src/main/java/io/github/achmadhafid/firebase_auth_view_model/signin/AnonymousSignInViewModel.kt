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

internal class AnonymousSignInViewModel : SignInViewModel<AnonymousSignInException>() {

    override val offlineException: AnonymousSignInException
        get() = AnonymousSignInException.Offline

    override fun parseException(throwable: Throwable): AnonymousSignInException = when (throwable) {
        is AnonymousSignInException -> throwable
        is TimeoutCancellationException -> AnonymousSignInException.Timeout
        is FirebaseAuthException -> AnonymousSignInException.WrappedFirebaseAuthException(throwable)
        else -> AnonymousSignInException.Unknown
    }

    internal fun signInAnonymously(timeout: Long = Long.MAX_VALUE, context: Context? = null) {
        executeSignInTask(timeout, context) {
            auth.signInAnonymously().await()
        }
    }
}

//region Consumer API via extension functions

interface AnonymousSignInExtensions : FirebaseAuthExtensions

//region Activity

fun <T> T.signInAnonymously(
    timeout: Long = Long.MAX_VALUE
) where T : AnonymousSignInExtensions, T : FragmentActivity {
    signInViewModel.signInAnonymously(timeout, this)
}

fun <T> T.observeAnonymousSignIn(
    observer: (AnonymousSignInEvent) -> Unit
) where T : AnonymousSignInExtensions, T : FragmentActivity {
    signInViewModel.event.observe(this, observer)
}

//endregion
//region Fragment

fun <T> T.signInAnonymously(
    timeout: Long = Long.MAX_VALUE
) where T : AnonymousSignInExtensions, T : Fragment {
    signInViewModel.signInAnonymously(timeout, requireContext())
}

fun <T> T.observeAnonymousSignIn(
    observer: (AnonymousSignInEvent) -> Unit
) where T : AnonymousSignInExtensions, T : Fragment {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

//endregion

//endregion
//region Internal extensions functions

private val FragmentActivity.signInViewModel
    get() = getViewModel<AnonymousSignInViewModel>()

private val Fragment.signInViewModel
    get() = getViewModel<AnonymousSignInViewModel>()

//endregion
