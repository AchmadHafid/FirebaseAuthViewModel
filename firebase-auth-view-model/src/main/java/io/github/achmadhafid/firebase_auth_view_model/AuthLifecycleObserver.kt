package io.github.achmadhafid.firebase_auth_view_model

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.auth.FirebaseAuth

private class AuthLifecycleObserver constructor(
    private val lifecycle: Lifecycle,
    listener: AuthStateListener.() -> Unit
) : LifecycleObserver, FirebaseAuthExtensions {

    private val authStateListener = FirebaseAuth.AuthStateListener {
        with(AuthStateListener().apply(listener)) {
            when {
                isSignedIn -> onSignInListener(user!!)
                isSignedOut -> onSignOutListener()
            }
            onAnyListener()
        }
    }

    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        auth.addAuthStateListener(authStateListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        auth.removeAuthStateListener(authStateListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        lifecycle.removeObserver(this)
    }

}

//region Consumer API via extension functions

fun <T> T.observeAuthState(
    listener: AuthStateListener.() -> Unit
) where T : FirebaseAuthExtensions, T : FragmentActivity {
    AuthLifecycleObserver(lifecycle, listener)
}

fun <T> T.observeAuthState(
    listener: AuthStateListener.() -> Unit
) where T : FirebaseAuthExtensions, T : LifecycleService {
    AuthLifecycleObserver(lifecycle, listener)
}

fun <T> T.observeAuthState(
    listener: AuthStateListener.() -> Unit
) where T : FirebaseAuthExtensions, T : Fragment {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            AuthLifecycleObserver(viewLifecycleOwner.lifecycle, listener)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            lifecycle.removeObserver(this)
        }
    })
}

//endregion
