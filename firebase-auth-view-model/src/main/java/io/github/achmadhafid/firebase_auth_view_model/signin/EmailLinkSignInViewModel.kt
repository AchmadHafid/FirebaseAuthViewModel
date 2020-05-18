@file:Suppress("TooManyFunctions")

package io.github.achmadhafid.firebase_auth_view_model.signin

import android.content.Context
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.auth.ktx.actionCodeSettings
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.zpack.extension.getViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal class EmailLinkSignInViewModel : SignInViewModel<EmailLinkSignInException>() {

    override val offlineException: EmailLinkSignInException
        get() = EmailLinkSignInException.Offline

    override fun parseException(throwable: Throwable): EmailLinkSignInException = when (throwable) {
        is TimeoutCancellationException -> EmailLinkSignInException.Timeout
        is FirebaseAuthException -> EmailLinkSignInException.AuthException(throwable)
        else -> EmailLinkSignInException.Unknown
    }

    internal fun signInWithEmailLink(
        context: Context,
        emailLink: String,
        timeout: Long = Long.MAX_VALUE
    ) {
        context.email.let { email ->
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (firebaseAuth.isSignInWithEmailLink(emailLink)) {
                    executeSignInTask(timeout, context) {
                        firebaseAuth.signInWithEmailLink(email, emailLink)
                            .await()
                    }
                } else onFailed(EmailLinkSignInException.InvalidLink)
            } else onFailed(EmailLinkSignInException.NoEmailFound)
        }
    }

    @Suppress("NestedBlockDepth")
    internal fun linkWithCredential(
        context: Context,
        emailLink: String,
        timeout: Long = Long.MAX_VALUE
    ) {
        context.email.let { email ->
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                firebaseUser?.let { user ->
                    if (firebaseAuth.isSignInWithEmailLink(emailLink)) {
                        EmailAuthProvider.getCredentialWithLink(email, emailLink)
                            .let { credential ->
                                executeSignInTask(timeout, context) {
                                    user.linkWithCredential(credential)
                                        .await()
                                }
                            }
                    } else onFailed(EmailLinkSignInException.InvalidLink)
                } ?: onFailed(EmailLinkSignInException.Unauthenticated)
            } else onFailed(EmailLinkSignInException.NoEmailFound)
        }
    }

    @Suppress("NestedBlockDepth")
    internal fun reAuthenticateWithCredential(
        context: Context,
        emailLink: String,
        timeout: Long = Long.MAX_VALUE
    ) {
        context.email.let { email ->
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                firebaseUser?.let { user ->
                    if (firebaseAuth.isSignInWithEmailLink(emailLink)) {
                        EmailAuthProvider.getCredentialWithLink(email, emailLink)
                            .let { credential ->
                                executeSignInTask(timeout, context) {
                                    user.reauthenticateAndRetrieveData(credential)
                                        .await()
                                }
                            }
                    } else onFailed(EmailLinkSignInException.InvalidLink)
                } ?: onFailed(EmailLinkSignInException.Unauthenticated)
            } else onFailed(EmailLinkSignInException.NoEmailFound)
        }
    }

}

//region Internal shared preference to store user entered email address

private val Context.email
    get() =
        getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EMAIL, "")!!

private fun Context.setEmail(email: String) {
    getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        .edit {
            putString(KEY_EMAIL, email)
        }
}

private const val SHARED_PREFERENCE_NAME = "firebase_auth"
private const val KEY_EMAIL              = "email_link"

//endregion
//region Consumer API via extension functions and inline values
//region Activity

fun AppCompatActivity.observeSignInByEmailLink(observer: (EmailLinkSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

suspend fun AppCompatActivity.sendSignInLinkToEmail(
    email: String,
    settings: ActionCodeSettings,
    timeout: Long = Long.MAX_VALUE
) = withContext(Dispatchers.IO) {
    withTimeout(timeout) {
        firebaseAuth.sendSignInLinkToEmail(email, settings).await()
        setEmail(email)
    }
}

suspend fun AppCompatActivity.fetchSignInMethodsForEmail(
    email: String,
    timeout: Long = Long.MAX_VALUE
) = withContext(Dispatchers.IO) {
    withTimeout(timeout) {
        firebaseAuth.fetchSignInMethodsForEmail(email).await()
    }
}

fun AppCompatActivity.signInWithEmailLink(
    emailLink: String? = null,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.signInWithEmailLink(this, emailLink ?: intent.data.toString(), timeout)
}

fun AppCompatActivity.linkWithCredential(
    emailLink: String? = null,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.linkWithCredential(this, emailLink ?: intent.data.toString(), timeout)
}

fun AppCompatActivity.reAuthenticateWithCredential(
    emailLink: String? = null,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.reAuthenticateWithCredential(this, emailLink ?: intent.data.toString(), timeout)
}

//endregion
//region Fragment

fun Fragment.observeSignInByEmailLink(observer: (EmailLinkSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

suspend fun Fragment.sendSignInLinkToEmail(
    email: String,
    timeout: Long = Long.MAX_VALUE,
    actionCodeSettings: () -> ActionCodeSettings
) = withContext(Dispatchers.IO) {
    withTimeout(timeout) {
        firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings()).await()
        requireContext().setEmail(email)
    }
}

suspend fun Fragment.fetchSignInMethodsForEmail(
    email: String,
    timeout: Long = Long.MAX_VALUE
) = withContext(Dispatchers.IO) {
    withTimeout(timeout) {
        firebaseAuth.fetchSignInMethodsForEmail(email).await()
    }
}

fun Fragment.signInWithEmailLink(emailLink: String? = null, timeout: Long = Long.MAX_VALUE) {
    signInViewModel.signInWithEmailLink(
        requireContext(),
        emailLink ?: requireActivity().intent.data.toString(),
        timeout
    )
}

fun Fragment.linkWithCredential(emailLink: String? = null, timeout: Long = Long.MAX_VALUE) {
    signInViewModel.linkWithCredential(
        requireContext(),
        emailLink ?: requireActivity().intent.data.toString(),
        timeout
    )
}

fun Fragment.reAuthenticateWithCredential(
    emailLink: String? = null,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.reAuthenticateWithCredential(
        requireContext(),
        emailLink ?: requireActivity().intent.data.toString(),
        timeout
    )
}

//endregion
//region Email Sign In Method

inline val SignInMethodQueryResult.isFromEmailPassword
    get() = signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)

inline val SignInMethodQueryResult.isFromEmailLink
    get() = signInMethods!!.contains(EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD)

//endregion
//endregion
//region Internal extension functions

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<EmailLinkSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<EmailLinkSignInViewModel>()

//endregion
