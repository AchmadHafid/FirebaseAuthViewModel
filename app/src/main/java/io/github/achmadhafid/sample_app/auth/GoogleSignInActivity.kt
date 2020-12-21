package io.github.achmadhafid.sample_app.auth

import android.content.Intent
import android.os.Bundle
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.hasGoogleAuth
import io.github.achmadhafid.firebase_auth_view_model.observeFirebaseAuthState
import io.github.achmadhafid.firebase_auth_view_model.onCredentialLinked
import io.github.achmadhafid.firebase_auth_view_model.onCredentialUnlinked
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.GoogleSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.linkGoogleAccountToCurrentUser
import io.github.achmadhafid.firebase_auth_view_model.signin.observeSignInByGoogle
import io.github.achmadhafid.firebase_auth_view_model.signin.onSignInByGoogleResult
import io.github.achmadhafid.firebase_auth_view_model.signin.startSignInByGoogle
import io.github.achmadhafid.firebase_auth_view_model.signin.unlinkGoogleAccountFromCurrentUser
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.sample_app.databinding.ActivityGoogleSignInBinding
import io.github.achmadhafid.zpack.extension.stringRes
import io.github.achmadhafid.zpack.extension.toastShort
import io.github.achmadhafid.zpack.extension.view.gone
import io.github.achmadhafid.zpack.extension.view.visible
import io.github.achmadhafid.zpack.extension.view.visibleOrGone

class GoogleSignInActivity : BaseActivity() {

    //region View Binding

    private val binding by lazy {
        ActivityGoogleSignInBinding.inflate(layoutInflater)
    }

    //endregion
    //region Resource Binding

    private val webClientId by stringRes(R.string.web_client_id)

    //endregion
    //region Auth State Listener

    private val authStateListener by lazy {
        observeFirebaseAuthState(authCallbackMode) {
            fun onCredentialsChanged() {
                with(binding) {
                    btnLink.visibleOrGone {
                        firebaseUser?.hasGoogleAuth == false
                    }
                    btnUnlink.visibleOrGone {
                        firebaseUser?.hasGoogleAuth == true
                    }
                    cbForceAccountChooser.visibleOrGone {
                        firebaseUser?.run { !hasGoogleAuth } ?: true
                    }
                }
            }

            onSignedIn {
                Logger.d("User signed in")
                with(binding) {
                    btnLogin.gone()
                    btnLogout.visible()
                    onCredentialsChanged()
                }
            }
            onSignedOut {
                Logger.d("User signed out")
                with(binding) {
                    btnLogin.visible()
                    cbForceAccountChooser.visible()
                    btnLink.gone()
                    btnUnlink.gone()
                    btnLogout.gone()
                }
            }
            onCredentialLinked {
                Logger.d("Credential linked: $it")
                onCredentialsChanged()
            }
            onCredentialUnlinked {
                Logger.d("Credential unlinked: $it")
                onCredentialsChanged()
            }
        }
    }

    //endregion
    //region Lifecycle Callback

    @Suppress("ComplexMethod", "LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //region setup toolbar

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //endregion
        //region setup action widget

        with(binding) {
            btnLogin.setOnClickListener {
                startSignInByGoogle(
                    webClientId,
                    cbForceAccountChooser.isChecked
                )
            }
            btnLink.setOnClickListener {
                linkGoogleAccountToCurrentUser(
                    webClientId,
                    cbForceAccountChooser.isChecked
                )
            }
            btnUnlink.setOnClickListener {
                unlinkGoogleAccountFromCurrentUser(authStateListener)
            }
            btnLogout.setOnClickListener {
                firebaseAuth.signOut()
            }
        }

        //endregion
        //region observe sign in progress

        observeSignInByGoogle {
            val (state, hasBeenConsumed) = it.getEvent()
            when (state) {
                SignInState.OnProgress -> {
                    showLoadingDialog()
                }
                is SignInState.OnSuccess -> if (!hasBeenConsumed) {
                    dismissDialog()
                    toastShort("Sign in success!")
                }
                is SignInState.OnFailed -> if (!hasBeenConsumed) {
                    dismissDialog()
                    @Suppress("MaxLineLength")
                    val message = when (val signInException = state.exception) {
                        GoogleSignInException.Canceled                   -> "Canceled"
                        GoogleSignInException.Unknown                    -> "Unknown"
                        GoogleSignInException.Offline                    -> "Internet connection is unavailable"
                        GoogleSignInException.Timeout                    -> "Connection time out"
                        //region Linking Google Account to Current User
                        GoogleSignInException.AlreadySignedIn            -> "Linking failed: Already sign in by Google"
                        GoogleSignInException.AlreadyInUse               -> "Linking failed: Google account already registered"
                        GoogleSignInException.Unauthenticated            -> "Linking failed: User not signed in"
                        //endregion
                        //region Unlink Google Account from Current User
                        GoogleSignInException.NotLinkedWithGoogleSignIn  -> "Unlink failed: No Google Sign In found"
                        GoogleSignInException.NoOtherSignInProviderFound -> "Unlink failed: No other sign in provider found"
                        //endregion
                        is GoogleSignInException.AuthException           -> signInException.exception.message!!
                        is GoogleSignInException.WrappedApiException     -> signInException.exception.message!!
                    }
                    toastShort(message)
                }
            }
        }

        authStateListener

        //endregion

    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onSignInByGoogleResult(requestCode, resultCode, data, authStateListener)
    }

    //endregion

}
