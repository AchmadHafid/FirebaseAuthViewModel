package io.github.achmadhafid.firebase_auth_view_model.signin

import android.content.Context
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import io.github.achmadhafid.firebase_auth_view_model.AuthStateListener
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.getViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await

internal class EmailPasswordSignInViewModel : SignInViewModel<EmailPasswordSignInException>() {

    internal fun signInByEmailPassword(
        context: Context? = null,
        email: String,
        password: String,
        authStateListener: AuthStateListener,
        timeout: Long = Long.MAX_VALUE
    ) {
        executeTask(context, SignInTask.SignIn(EmailAuthProvider.PROVIDER_ID), authStateListener, timeout) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .await()
        }
    }

    internal fun createUserByEmailPassword(
        context: Context? = null,
        email: String,
        password: String,
        authStateListener: AuthStateListener,
        timeout: Long = Long.MAX_VALUE
    ) {
        executeTask(context, SignInTask.SignIn(EmailAuthProvider.PROVIDER_ID), authStateListener, timeout) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .await()
        }
    }

    @Suppress("NestedBlockDepth")
    internal fun linkWithCredential(
        context: Context? = null,
        email: String,
        password: String,
        authStateListener: AuthStateListener,
        timeout: Long = Long.MAX_VALUE
    ) {
        val task = SignInTask.LinkCredential(EmailAuthProvider.PROVIDER_ID)
        if (email.isValidEmail) {
            firebaseUser?.let { user ->
                EmailAuthProvider.getCredential(email, password)
                    .let { credential ->
                        executeTask(context, task, authStateListener, timeout) {
                            user.linkWithCredential(credential)
                                .await()
                        }
                    }
            } ?: onFailed(task, EmailPasswordSignInException.Unauthenticated)
        } else onFailed(task, EmailPasswordSignInException.InvalidEmail)
    }

    override val offlineException: EmailPasswordSignInException
        get() = EmailPasswordSignInException.Offline

    override fun parseException(throwable: Throwable): EmailPasswordSignInException = when (throwable) {
        is TimeoutCancellationException -> EmailPasswordSignInException.Timeout
        is FirebaseAuthException -> EmailPasswordSignInException.AuthException(throwable)
        else -> EmailPasswordSignInException.Unknown
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
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.signInByEmailPassword(this, email, password, authStateListener, timeout)
}

fun AppCompatActivity.createUserByEmailPassword(
    email: String,
    password: String,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.createUserByEmailPassword(this, email, password, authStateListener, timeout)
}

fun AppCompatActivity.linkEmailPasswordToCurrentUser(
    email: String,
    password: String,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.linkWithCredential(this, email, password, authStateListener, timeout)
}

//endregion
//region Fragment

fun Fragment.observeSignInByEmailPassword(observer: (EmailPasswordSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startSignInByEmailPassword(
    email: String,
    password: String,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.signInByEmailPassword(requireContext(), email, password, authStateListener, timeout)
}

fun Fragment.createUserByEmailPassword(
    email: String,
    password: String,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.createUserByEmailPassword(requireContext(), email, password, authStateListener, timeout)
}

fun Fragment.linkEmailPasswordToCurrentUser(
    email: String,
    password: String,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.linkWithCredential(requireContext(), email, password, authStateListener, timeout)
}

//endregion
//endregion
//region Private extension functions helper

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<EmailPasswordSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<EmailPasswordSignInViewModel>()

private val String.isValidEmail
    get() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

//endregion
