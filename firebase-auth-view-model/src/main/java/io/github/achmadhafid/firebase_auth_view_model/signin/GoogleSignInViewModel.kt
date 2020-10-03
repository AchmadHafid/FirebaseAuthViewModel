@file:Suppress("TooManyFunctions")

package io.github.achmadhafid.firebase_auth_view_model.signin

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import io.github.achmadhafid.firebase_auth_view_model.AuthStateListener
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.hasGoogleAuth
import io.github.achmadhafid.firebase_auth_view_model.hasMultipleAuth
import io.github.achmadhafid.zpack.extension.getViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await

internal class GoogleSignInViewModel : SignInViewModel<GoogleSignInException>() {

    @Suppress("LongParameterList")
    internal fun onSignInByGoogleResult(
        context: Context?,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        authStateListener: AuthStateListener,
        timeout: Long
    ) {
        val task = when (requestCode) {
            RC_SIGN_IN         -> SignInTask.SignIn(GoogleAuthProvider.PROVIDER_ID)
            RC_LINK_CREDENTIAL -> SignInTask.LinkCredential(GoogleAuthProvider.PROVIDER_ID)
            else               -> return
        }

        when {
            resultCode == Activity.RESULT_CANCELED -> onFailed(task, GoogleSignInException.Canceled)
            data == null -> onFailed(task, GoogleSignInException.Unknown)
            else -> try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                executeTask(context, task, authStateListener, timeout) {
                    firebaseUser?.let {
                        if (task is SignInTask.LinkCredential) {
                            if (!it.hasGoogleAuth) it.linkWithCredential(credential).await()
                            else throw GoogleSignInException.onUserAlreadySignedIn
                        } else firebaseAuth.signInWithCredential(credential).await()
                    } ?: firebaseAuth.signInWithCredential(credential).await()
                }
            } catch (@Suppress("TooGenericExceptionCaught") throwable: Throwable) {
                onFailed(task, parseException(throwable))
            }
        }
    }

    internal fun unlinkGoogleAccount(
        context: Context?,
        authStateListener: AuthStateListener,
        timeout: Long
    ) {
        val task = SignInTask.UnlinkCredential(GoogleAuthProvider.PROVIDER_ID)
        firebaseUser?.let { user ->
            if (user.hasGoogleAuth) {
                if (user.hasMultipleAuth) {
                    executeTask(context, task, authStateListener, timeout) {
                        user.unlink(GoogleAuthProvider.PROVIDER_ID).await()
                    }
                } else onFailed(task, GoogleSignInException.NoOtherSignInProviderFound)
            } else onFailed(task, GoogleSignInException.NotLinkedWithGoogleSignIn)
        } ?: onFailed(task, GoogleSignInException.Unauthenticated)
    }

    override val offlineException: GoogleSignInException
        get() = GoogleSignInException.Offline

    override fun parseException(throwable: Throwable): GoogleSignInException = when (throwable) {
        is TimeoutCancellationException -> GoogleSignInException.Timeout
        is IllegalStateException -> when (throwable.message) {
            GoogleSignInException.USER_ALREADY_SIGNED_IN -> GoogleSignInException.AlreadySignedIn
            else -> GoogleSignInException.Unknown
        }
        is FirebaseAuthException -> when (throwable.errorCode) {
            GoogleSignInException.CREDENTIAL_ALREADY_IN_USE -> GoogleSignInException.AlreadyInUse
            else -> GoogleSignInException.AuthException(throwable)
        }
        is ApiException -> GoogleSignInException.WrappedApiException(throwable)
        else -> GoogleSignInException.Unknown
    }

    companion object {
        internal const val RC_SIGN_IN         = 5544
        internal const val RC_LINK_CREDENTIAL = 6655
    }

}

//region Consumer API via extension functions
//region Activity

fun AppCompatActivity.observeSignInByGoogle(observer: (GoogleSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

fun AppCompatActivity.startSignInByGoogle(
    clientId: String,
    forceAccountChooser: Boolean = false
) {
    @Suppress("DEPRECATION")
    startActivityForResult(
        getGoogleSignInIntent(clientId, forceAccountChooser),
        GoogleSignInViewModel.RC_SIGN_IN
    )
}

fun AppCompatActivity.linkGoogleAccountToCurrentUser(
    clientId: String,
    forceAccountChooser: Boolean = false
) {
    @Suppress("DEPRECATION")
    startActivityForResult(
        getGoogleSignInIntent(clientId, forceAccountChooser),
        GoogleSignInViewModel.RC_LINK_CREDENTIAL
    )
}

fun AppCompatActivity.onSignInByGoogleResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.onSignInByGoogleResult(
        this,
        requestCode,
        resultCode,
        data,
        authStateListener,
        timeout
    )
}

fun AppCompatActivity.unlinkGoogleAccountFromCurrentUser(
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.unlinkGoogleAccount(this, authStateListener, timeout)
}

//endregion
//region Fragment

fun Fragment.observeSignInByGoogle(observer: (GoogleSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startSignInByGoogle(
    clientId: String,
    forceAccountChooser: Boolean = false
) {
    @Suppress("DEPRECATION")
    startActivityForResult(
        getGoogleSignInIntent(clientId, forceAccountChooser),
        GoogleSignInViewModel.RC_SIGN_IN
    )
}

fun Fragment.linkGoogleAccountToCurrentUser(
    clientId: String,
    forceAccountChooser: Boolean = false
) {
    @Suppress("DEPRECATION")
    startActivityForResult(
        getGoogleSignInIntent(clientId, forceAccountChooser),
        GoogleSignInViewModel.RC_LINK_CREDENTIAL
    )
}

fun Fragment.onSignInByGoogleResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.onSignInByGoogleResult(
        requireContext(),
        requestCode,
        resultCode,
        data,
        authStateListener,
        timeout
    )
}

fun Fragment.unlinkGoogleAccountFromCurrentUser(
    authStateListener: AuthStateListener,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.unlinkGoogleAccount(requireContext(), authStateListener, timeout)
}

//endregion
//endregion
//region Internal extension functions

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<GoogleSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<GoogleSignInViewModel>()

private fun AppCompatActivity.getGoogleSignInIntent(
    clientId: String,
    clearSignInCache: Boolean
): Intent = getGoogleSignInClient(clientId).apply {
    if (clearSignInCache) clearSignInCache()
}.signInIntent

private fun Fragment.getGoogleSignInIntent(clientId: String, clearSignInCache: Boolean): Intent =
    getGoogleSignInClient(clientId).apply {
        if (clearSignInCache) clearSignInCache()
    }.signInIntent

private fun AppCompatActivity.getGoogleSignInClient(clientId: String): GoogleSignInClient =
    GoogleSignIn.getClient(this, getGoogleSignInOptions(clientId))

private fun Fragment.getGoogleSignInClient(clientId: String): GoogleSignInClient =
    GoogleSignIn.getClient(requireContext(), getGoogleSignInOptions(clientId))

private fun getGoogleSignInOptions(clientId: String): GoogleSignInOptions =
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .build()

private fun GoogleSignInClient.clearSignInCache() = this.also {
    Auth.GoogleSignInApi.signOut(asGoogleApiClient())
}

//endregion
