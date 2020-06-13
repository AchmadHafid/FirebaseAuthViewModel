package io.github.achmadhafid.sample_app

import android.os.Bundle
import androidx.lifecycle.observe
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.observeFirebaseAuthState
import io.github.achmadhafid.firebase_auth_view_model.onCredentialLinked
import io.github.achmadhafid.firebase_auth_view_model.onCredentialUnlinked
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.EmailLinkSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.isFromEmailLink
import io.github.achmadhafid.firebase_auth_view_model.signin.observeSignInByEmailLink
import io.github.achmadhafid.firebase_auth_view_model.signin.startSignInByEmailLink
import io.github.achmadhafid.sample_app.auth.AnonymousSignInActivity
import io.github.achmadhafid.sample_app.auth.EmailLinkSignInActivity
import io.github.achmadhafid.sample_app.auth.EmailPasswordSignInActivity
import io.github.achmadhafid.sample_app.auth.GoogleSignInActivity
import io.github.achmadhafid.sample_app.auth.PhoneSignInActivity
import io.github.achmadhafid.sample_app.databinding.ActivityMainBinding
import io.github.achmadhafid.simplepref.livedata.simplePrefLiveData
import io.github.achmadhafid.zpack.extension.intent
import io.github.achmadhafid.zpack.extension.toastShort
import io.github.achmadhafid.zpack.extension.view.onSingleClick

class MainActivity : BaseActivity() {

    //region View Binding

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    //endregion
    //region Auth State Listener

    private val authStateListener by lazy {
        observeFirebaseAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
            }
            onSignedOut {
                Logger.d("User signed out")
            }
            onCredentialLinked {
                Logger.d("Credential linked: $it")
            }
            onCredentialUnlinked {
                Logger.d("Credential unlinked: $it")
            }
        }
    }

    //endregion
    //region Lifecycle Callback

    @Suppress("ComplexMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        //region setup view

        binding.btnAnonymousSignInDemo.onSingleClick {
            startActivity(intent<AnonymousSignInActivity>())
        }
        binding.btnGoogleSignInDemo.onSingleClick {
            startActivity(intent<GoogleSignInActivity>())
        }
        binding.btnEmailPasswordSignInDemo.onSingleClick {
            startActivity(intent<EmailPasswordSignInActivity>())
        }
        binding.btnEmailLinkSignInDemo.onSingleClick {
            startActivity(intent<EmailLinkSignInActivity>())
        }
        binding.btnPhoneSignInDemo.onSingleClick {
            startActivity(intent<PhoneSignInActivity>())
        }

        binding.cbAuthCallbackMode.setOnCheckedChangeListener { _, isChecked ->
            authCallbackMode = isChecked
        }

        //endregion
        //region setup preference observer

        simplePrefLiveData(authCallbackMode, ::authCallbackMode).observe(this) {
            binding.cbAuthCallbackMode.isChecked = it
        }

        //endregion
        //region observe sign in by email link progress

        observeSignInByEmailLink {
            val (_, state, hasBeenConsumed) = it.getEvent()
            when (state) {
                SignInState.OnProgress -> showLoadingDialog()
                is SignInState.OnSuccess -> if (!hasBeenConsumed) {
                    dismissDialog()
                    toastShort("Sign in success!")
                }
                is SignInState.OnFailed -> if (!hasBeenConsumed) {
                    dismissDialog()
                    val message = when (val signInException = state.exception) {
                        EmailLinkSignInException.Unknown          -> "Unknown"
                        EmailLinkSignInException.Offline          -> "Internet connection unavailable"
                        EmailLinkSignInException.Timeout          -> "Connection time out"
                        EmailLinkSignInException.InvalidLink      -> "Invalid Link"
                        EmailLinkSignInException.NoEmailFound     -> "No Email Found"
                        EmailLinkSignInException.Unauthenticated  -> "User no authenticated"
                        is EmailLinkSignInException.AuthException -> signInException.exception.message
                    }
                    toastShort(message ?: "no error message available")
                }
            }
        }

        if (isFromEmailLink) {
            startSignInByEmailLink(authStateListener = authStateListener)
        }

        //endregion
    }

    override fun onResume() {
        super.onResume()
        firebaseUser?.let { user ->
            user.providerData.forEach {
                Logger.d("Provider ID: ${it.providerId}")
            }
        }
    }

    //endregion

}
