@file:Suppress("TooManyFunctions")

package io.github.achmadhafid.firebase_auth_view_model.signin

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.observe
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import io.github.achmadhafid.firebase_auth_view_model.FirebaseAuthExtensions
import io.github.achmadhafid.firebase_auth_view_model.auth
import io.github.achmadhafid.zpack.ktx.getViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await

internal class GoogleSignInViewModel : SignInViewModel<GoogleSignInException>() {

    override val offlineException: GoogleSignInException
        get() = GoogleSignInException.Offline

    override fun parseException(throwable: Throwable): GoogleSignInException = when (throwable) {
        is TimeoutCancellationException -> GoogleSignInException.Timeout
        is FirebaseAuthException -> GoogleSignInException.WrappedFirebaseAuthException(throwable)
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
                auth.signInWithCredential(credential).await()
            }
        }
    }

}

//region Consumer API via extension functions

interface GoogleSignInExtensions : FirebaseAuthExtensions

//region Activity

fun <T> T.startGoogleSignInActivity(clientId: String, requestCode: Int)
        where T : GoogleSignInExtensions, T : FragmentActivity {
    startActivityForResult(getGoogleSignInIntent(clientId), requestCode)
}

fun <T> T.onSignInByGoogleResult(
    resultCode: Int,
    data: Intent?,
    timeout: Long = Long.MAX_VALUE
) where T : GoogleSignInExtensions, T : FragmentActivity {
    signInViewModel.onSignInByGoogleResult(resultCode, data, timeout, this)
}

fun <T> T.observeGoogleSignIn(
    observer: (GoogleSignInEvent) -> Unit
) where T : GoogleSignInExtensions, T : FragmentActivity {
    signInViewModel.event.observe(this, observer)
}

//endregion
//region Fragment

fun <T> T.startGoogleSignInActivity(clientId: String, requestCode: Int)
        where T : GoogleSignInExtensions, T : Fragment {
    startActivityForResult(getGoogleSignInIntent(clientId), requestCode)
}

fun <T> T.onSignInByGoogleResult(
    resultCode: Int,
    data: Intent?,
    timeout: Long = Long.MAX_VALUE
) where T : GoogleSignInExtensions, T : Fragment {
    signInViewModel.onSignInByGoogleResult(resultCode, data, timeout, requireContext())
}

fun <T> T.observeGoogleSignIn(
    observer: (GoogleSignInEvent) -> Unit
) where T : GoogleSignInExtensions, T : Fragment {
    signInViewModel.event.observe(this, observer)
}


//endregion

//endregion
//region Internal extension functions

private val FragmentActivity.signInViewModel
    get() = getViewModel<GoogleSignInViewModel>()

private val Fragment.signInViewModel
    get() = getViewModel<GoogleSignInViewModel>()

private fun <T> T.getGoogleSignInIntent(clientId: String): Intent
        where T : FirebaseAuthExtensions, T : FragmentActivity =
    getGoogleSignInClient(clientId).signInIntent

private fun <T> T.getGoogleSignInIntent(clientId: String): Intent
        where T : FirebaseAuthExtensions, T : Fragment =
    getGoogleSignInClient(clientId).signInIntent

private fun <T> T.getGoogleSignInClient(clientId: String): GoogleSignInClient
        where T : FirebaseAuthExtensions, T : FragmentActivity =
    GoogleSignIn.getClient(this, getGoogleSignInOptions(clientId))

private fun <T> T.getGoogleSignInClient(clientId: String): GoogleSignInClient
        where T : FirebaseAuthExtensions, T : Fragment =
    GoogleSignIn.getClient(requireContext(), getGoogleSignInOptions(clientId))

private fun getGoogleSignInOptions(clientId: String): GoogleSignInOptions =
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .build()

//endregion
