@file:Suppress("TooManyFunctions")

package io.github.achmadhafid.firebase_auth_view_model.signin

import android.content.Context
import android.content.Intent
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.SignInMethodQueryResult
import io.github.achmadhafid.firebase_auth_view_model.AuthStateListener
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.getViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal class EmailLinkSignInViewModel : SignInViewModel<EmailLinkSignInException>() {

    internal fun signInWithEmailLink(
        context: Context,
        emailLink: String,
        authStateListener: AuthStateListener,
        timeout: Long = Long.MAX_VALUE
    ) {
        val task = SignInTask.SignIn(EmailAuthProvider.PROVIDER_ID)
        context.email.let { email ->
            if (email.isValidEmail) {
                if (firebaseAuth.isSignInWithEmailLink(emailLink)) {
                    executeTask(context, task, authStateListener, timeout) {
                        firebaseAuth.signInWithEmailLink(email, emailLink)
                            .await()
                    }
                } else onFailed(task, EmailLinkSignInException.InvalidLink)
            } else onFailed(task, EmailLinkSignInException.NoEmailFound)
        }
    }

    @Suppress("NestedBlockDepth")
    internal fun reAuthenticateWithCredential(
        context: Context,
        emailLink: String,
        authStateListener: AuthStateListener,
        timeout: Long = Long.MAX_VALUE
    ) {
        val task = SignInTask.SignIn(EmailAuthProvider.PROVIDER_ID)
        context.email.let { email ->
            if (email.isValidEmail) {
                firebaseUser?.let { user ->
                    if (firebaseAuth.isSignInWithEmailLink(emailLink)) {
                        EmailAuthProvider.getCredentialWithLink(email, emailLink)
                            .let { credential ->
                                executeTask(context, task, authStateListener, timeout) {
                                    user.reauthenticateAndRetrieveData(credential)
                                        .await()
                                }
                            }
                    } else onFailed(task, EmailLinkSignInException.InvalidLink)
                } ?: onFailed(task, EmailLinkSignInException.Unauthenticated)
            } else onFailed(task, EmailLinkSignInException.NoEmailFound)
        }
    }

    @Suppress("NestedBlockDepth")
    internal fun linkWithCredential(
        context: Context,
        emailLink: String,
        authStateListener: AuthStateListener,
        timeout: Long = Long.MAX_VALUE
    ) {
        val task = SignInTask.LinkCredential(EmailAuthProvider.PROVIDER_ID)
        context.email.let { email ->
            if (email.isValidEmail) {
                firebaseUser?.let { user ->
                    if (firebaseAuth.isSignInWithEmailLink(emailLink)) {
                        EmailAuthProvider.getCredentialWithLink(email, emailLink)
                            .let { credential ->
                                executeTask(context, task, authStateListener, timeout) {
                                    user.linkWithCredential(credential)
                                        .await()
                                }
                            }
                    } else onFailed(task, EmailLinkSignInException.InvalidLink)
                } ?: onFailed(task, EmailLinkSignInException.Unauthenticated)
            } else onFailed(task, EmailLinkSignInException.NoEmailFound)
        }
    }

    override val offlineException: EmailLinkSignInException
        get() = EmailLinkSignInException.Offline

    override fun parseException(throwable: Throwable): EmailLinkSignInException = when (throwable) {
        is TimeoutCancellationException -> EmailLinkSignInException.Timeout
        is FirebaseAuthException -> EmailLinkSignInException.AuthException(throwable)
        else -> EmailLinkSignInException.Unknown
    }

}

//region Consumer API via extension functions and inline values
//region Activity

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

inline val AppCompatActivity.isFromEmailLink get() = intent.isFromEmailLink

fun AppCompatActivity.observeSignInByEmailLink(observer: (EmailLinkSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

fun AppCompatActivity.startSignInByEmailLink(
    emailLink: String? = null,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.signInWithEmailLink(
        this,
        emailLink ?: intent.emailLink,
        authStateListener,
        timeout
    )
}

fun AppCompatActivity.reAuthenticateByEmailLink(
    emailLink: String? = null,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.reAuthenticateWithCredential(
        this,
        emailLink ?: intent.emailLink,
        authStateListener,
        timeout
    )
}

fun AppCompatActivity.linkToCurrentUserFromEmailLink(
    emailLink: String? = null,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.linkWithCredential(
        this,
        emailLink ?: intent.emailLink,
        authStateListener,
        timeout
    )
}

//endregion
//region Fragment

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

inline val Fragment.isFromEmailLink get() = requireActivity().intent.isFromEmailLink

fun Fragment.observeSignInByEmailLink(observer: (EmailLinkSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startSignInByEmailLink(
    emailLink: String? = null,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.signInWithEmailLink(
        requireContext(),
        emailLink ?: requireActivity().intent.emailLink,
        authStateListener,
        timeout
    )
}

fun Fragment.linkToCurrentUserFromEmailLink(
    emailLink: String? = null,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.linkWithCredential(
        requireContext(),
        emailLink ?: requireActivity().intent.emailLink,
        authStateListener,
        timeout
    )
}

fun Fragment.reAuthenticateByEmailLink(
    emailLink: String? = null,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.reAuthenticateWithCredential(
        requireContext(),
        emailLink ?: requireActivity().intent.emailLink,
        authStateListener,
        timeout
    )
}

//endregion
//region Email Sign In Method

inline val Intent.isFromEmailLink
    get() = data?.let { firebaseAuth.isSignInWithEmailLink(it.toString()) } ?: false

inline val SignInMethodQueryResult.isFromEmailPassword
    get() = signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)

inline val SignInMethodQueryResult.isFromEmailLink
    get() = signInMethods!!.contains(EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD)

//endregion
//endregion
//region Private extension functions helper

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<EmailLinkSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<EmailLinkSignInViewModel>()

private val Context.email
    get() = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        .getString(KEY_EMAIL, "")!!

private fun Context.setEmail(email: String) {
    getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        .edit {
            putString(KEY_EMAIL, email)
        }
}

private const val SHARED_PREFERENCE_NAME = "firebase_auth"
private const val KEY_EMAIL              = "email_link"

private val Intent.emailLink
    get() = data.toString()

private val String.isValidEmail
    get() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

//endregion
