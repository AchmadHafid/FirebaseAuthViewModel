package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.observeFirebaseAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.AnonymousSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeSignInAnonymously
import io.github.achmadhafid.firebase_auth_view_model.signin.startSignInAnonymously
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.sample_app.databinding.ActivityAnonymousSignInBinding
import io.github.achmadhafid.zpack.extension.toastShort
import io.github.achmadhafid.zpack.extension.view.onSingleClick
import io.github.achmadhafid.zpack.extension.view.withTextRes

class AnonymousSignInActivity : BaseActivity() {

    //region View Binding

    private val binding by lazy {
        ActivityAnonymousSignInBinding.inflate(layoutInflater)
    }

    //endregion
    //region Auth State Listener

    private val authStateListener by lazy {
        observeFirebaseAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                binding.btnAuth withTextRes R.string.logout
            }
            onSignedOut {
                Logger.d("User signed out")
                binding.btnAuth withTextRes R.string.login
            }
        }
    }

    //endregion
    //region Lifecycle Callback

    @Suppress("ComplexMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //region setup toolbar

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //endregion
        //region setup action widget

        binding.btnAuth.setOnClickListener {
            firebaseUser?.let {
                firebaseAuth.signOut()
            } ?: startSignInAnonymously(authStateListener)
        }

        //endregion
        //region observe sign in progress

        observeSignInAnonymously {
            val (state, hasBeenConsumed) = it.getEvent()
            when (state) {
                SignInState.OnProgress -> showLoadingDialog()
                is SignInState.OnSuccess -> if (!hasBeenConsumed) {
                    dismissDialog()
                    toastShort("Sign in success!")
                }
                is SignInState.OnFailed -> if (!hasBeenConsumed) {
                    dismissDialog()
                    val message = when (val signInException = state.exception) {
                        AnonymousSignInException.Unknown -> "Unknown"
                        AnonymousSignInException.Offline -> "Internet connection unavailable"
                        AnonymousSignInException.Timeout -> "Connection time out"
                        is AnonymousSignInException.AuthException -> signInException.exception.message!!
                    }
                    toastShort(message)
                }
            }
        }

        authStateListener

        //endregion
    }

    //endregion
}
