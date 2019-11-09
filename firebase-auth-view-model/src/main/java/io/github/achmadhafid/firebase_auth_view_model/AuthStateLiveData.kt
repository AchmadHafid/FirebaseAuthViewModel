package io.github.achmadhafid.firebase_auth_view_model

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthStateLiveData(
    private val lifecycle: Lifecycle,
    private val auth: FirebaseAuth,
    private val removeAuthListenerWhenLiveDataInactive: Boolean
) : LiveData<FirebaseUser?>(), LifecycleObserver, FirebaseAuth.AuthStateListener {

    private var isListenerAttached = false

    init {
        lifecycle.addObserver(this)
    }

    override fun onActive() {
        if (!isListenerAttached) {
            auth.addAuthStateListener(this)
            isListenerAttached = true
        }
    }

    override fun onInactive() {
        if (removeAuthListenerWhenLiveDataInactive) {
            auth.removeAuthStateListener(this)
            isListenerAttached = false
        }
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        value = auth.currentUser
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        lifecycle.removeObserver(this)
        auth.removeAuthStateListener(this)
        isListenerAttached = false
    }

}

//region Consumer API via extension functions

fun LifecycleOwner.observeAuthState(
    auth: FirebaseAuth,
    removeAuthListenerWhenLiveDataInactive: Boolean = false,
    builder: AuthStateListener.() -> Unit
) {
    val listener = AuthStateListener().apply(builder)
    AuthStateLiveData(
        lifecycle,
        auth,
        removeAuthListenerWhenLiveDataInactive
    ).observe(this) { user ->
        if (user != null) listener.onSignInListener(user)
        else listener.onSignOutListener()
    }
}

fun <T> T.observeAuthState(
    removeAuthListenerWhenLiveDataInactive: Boolean = false,
    builder: AuthStateListener.() -> Unit
) where T : FirebaseAuthExtensions, T : LifecycleOwner {
    observeAuthState(auth, removeAuthListenerWhenLiveDataInactive, builder)
}

fun <T> T.observeAuthState(
    removeAuthListenerWhenLiveDataInactive: Boolean = false,
    builder: AuthStateListener.() -> Unit
) where T : FirebaseAuthExtensions, T : Fragment {
    viewLifecycleOwnerLiveData.observe(this) { lifecycleOwner ->
        lifecycleOwner?.observeAuthState(auth, removeAuthListenerWhenLiveDataInactive, builder)
    }
}

//endregion
