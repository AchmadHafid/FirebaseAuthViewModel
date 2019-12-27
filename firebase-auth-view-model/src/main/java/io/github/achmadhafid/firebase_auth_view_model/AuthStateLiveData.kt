package io.github.achmadhafid.firebase_auth_view_model

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

private class AuthStateLiveData(
    private val lifecycle: Lifecycle,
    private val recyclerOnInactive: Boolean
) : LiveData<FirebaseUser?>(), LifecycleObserver, FirebaseAuth.AuthStateListener {

    init {
        lifecycle.addObserver(this)
    }

    override fun onActive() {
        attachListener()
    }

    override fun onInactive() {
        if (recyclerOnInactive) {
            detachListener()
        }
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        value = auth.currentUser
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        lifecycle.removeObserver(this)
        detachListener()
    }

    //region State handler to avoid listener duplication

    private var isListenerAttached = false

    private fun attachListener() {
        if (!isListenerAttached) {
            fireAuth.addAuthStateListener(this)
            isListenerAttached = true
        }
    }

    private fun detachListener() {
        if (isListenerAttached) {
            fireAuth.removeAuthStateListener(this)
            isListenerAttached = false
        }
    }

    //endregion

}

//region Consumer API via extension functions

fun LifecycleOwner.observeFireAuthState(
    recyclerOnInactive: Boolean = true,
    builder: AuthStateListener.() -> Unit
) {
    val listener = AuthStateListener().apply(builder)
    val authStateLiveData = AuthStateLiveData(lifecycle, recyclerOnInactive)

    authStateLiveData.observe(this) { user ->
        if (user != null) listener.onSignInListener(user)
        else listener.onSignOutListener()
    }
}

fun Fragment.observeFireAuthState(
    recyclerOnInactive: Boolean = true,
    builder: AuthStateListener.() -> Unit
) = viewLifecycleOwner.observeFireAuthState(recyclerOnInactive, builder)

//endregion
