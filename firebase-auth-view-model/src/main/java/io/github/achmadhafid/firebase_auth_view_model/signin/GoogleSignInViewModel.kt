@file:Suppress("TooManyFunctions")

package io.github.achmadhafid.firebase_auth_view_model.signin

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import io.github.achmadhafid.firebase_auth_view_model.fireAuth
import io.github.achmadhafid.zpack.ktx.getViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await

internal class GoogleSignInViewModel : SignInViewModel<GoogleSignInException>() {

    override val offlineException: GoogleSignInException
        get() = GoogleSignInException.Offline

    override fun parseException(throwable: Throwable): GoogleSignInException = when (throwable) {
        is TimeoutCancellationException -> GoogleSignInException.Timeout
        is FirebaseAuthException -> GoogleSignInException.FireAuthException(throwable)
        is ApiException -> GoogleSignInException.WrappedApiException(throwable)
        else -> GoogleSignInException.Unknown
    }

    internal fun onSignInByGoogleResult(
        resultCode: Int,
        data: Intent?,
        timeout: Long = Long.MAX_VALUE,
        context: Context? = null
    ) {
        when {
            resultCode == Activity.RESULT_CANCELED -> onFailed(GoogleSignInException.Canceled)
            data == null -> onFailed(GoogleSignInException.Unknown)
            else -> executeSignInTask(timeout, context) {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                requireNotNull(account)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                fireAuth.signInWithCredential(credential).await()
            }
        }
    }

}

//region Consumer API via extension functions
//region Activity

fun AppCompatActivity.observeFireSignInByGoogle(observer: (GoogleSignInEvent) -> Unit) {
    signInViewModel.event.observe(this, observer)
}

fun AppCompatActivity.startFireSignInByGoogle(
    clientId: String,
    requestCode: Int,
    forceAccountChooser: Boolean = false
) {
    startActivityForResult(getGoogleSignInIntent(clientId, forceAccountChooser), requestCode)
}

fun AppCompatActivity.onFireSignInByGoogleActivityResult(
    resultCode: Int,
    data: Intent?,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.onSignInByGoogleResult(resultCode, data, timeout, this)
}

//endregion
//region Fragment

fun Fragment.observeFireSignInByGoogle(observer: (GoogleSignInEvent) -> Unit) {
    signInViewModel.event.observe(viewLifecycleOwner, observer)
}

fun Fragment.startFireSignInByGoogle(
    clientId: String,
    requestCode: Int,
    forceAccountChooser: Boolean = false
) {
    startActivityForResult(getGoogleSignInIntent(clientId, forceAccountChooser), requestCode)
}

fun Fragment.onFireSignInByGoogleActivityResult(
    resultCode: Int,
    data: Intent?,
    timeout: Long = Long.MAX_VALUE
) {
    signInViewModel.onSignInByGoogleResult(resultCode, data, timeout, requireContext())
}

//endregion
//endregion
//region Internal extension functions

private inline val AppCompatActivity.signInViewModel
    get() = getViewModel<GoogleSignInViewModel>()

private inline val Fragment.signInViewModel
    get() = getViewModel<GoogleSignInViewModel>()

private fun AppCompatActivity.getGoogleSignInIntent(clientId: String, clearSignInCache: Boolean): Intent =
    getGoogleSignInClient(clientId).apply {
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
