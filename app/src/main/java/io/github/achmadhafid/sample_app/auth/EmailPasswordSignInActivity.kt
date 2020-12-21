package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.hasEmailPasswordAuth
import io.github.achmadhafid.firebase_auth_view_model.observeFirebaseAuthState
import io.github.achmadhafid.firebase_auth_view_model.onCredentialLinked
import io.github.achmadhafid.firebase_auth_view_model.onCredentialUnlinked
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.EmailPasswordSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.createUserByEmailPassword
import io.github.achmadhafid.firebase_auth_view_model.signin.linkEmailPasswordToCurrentUser
import io.github.achmadhafid.firebase_auth_view_model.signin.observeSignInByEmailPassword
import io.github.achmadhafid.firebase_auth_view_model.signin.startSignInByEmailPassword
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.databinding.ActivityEmailPasswordSignInBinding
import io.github.achmadhafid.zpack.extension.toastShort
import io.github.achmadhafid.zpack.extension.view.enabled
import io.github.achmadhafid.zpack.extension.view.gone
import io.github.achmadhafid.zpack.extension.view.value
import io.github.achmadhafid.zpack.extension.view.visibleOrGone

class EmailPasswordSignInActivity : BaseActivity() {

    //region View Binding

    private val binding by lazy {
        ActivityEmailPasswordSignInBinding.inflate(layoutInflater)
    }

    //endregion
    //region Auth State Listener

    private val authStateListener by lazy {
        observeFirebaseAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                with(binding) {
                    btnCreateUser.gone()
                    btnSignIn.gone()
                    btnLinkCredential.visibleOrGone {
                        firebaseUser?.hasEmailPasswordAuth == false
                    }
                    listOf(inputLayoutEmail, inputLayoutPassword)
                        .enabled(false)
                }
            }
            onSignedOut {
                Logger.d("User signed out")
                with(binding) {
                    listOf(btnCreateUser, inputLayoutEmail, inputLayoutPassword)
                        .enabled(true)
                }
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
            btnCreateUser.setOnClickListener {
                with(binding.edtEmail.value to binding.edtPassword.value) {
                    if (first.isNotEmpty() && second.isNotEmpty()) {
                        createUserByEmailPassword(first, second, authStateListener)
                    }
                }
            }
            btnSignIn.setOnClickListener {
                with(binding.edtEmail.value to binding.edtPassword.value) {
                    if (first.isNotEmpty() && second.isNotEmpty()) {
                        startSignInByEmailPassword(first, second, authStateListener)
                    }
                }
            }
            btnLinkCredential.setOnClickListener {
                with(binding.edtEmail.value to binding.edtPassword.value) {
                    if (first.isNotEmpty() && second.isNotEmpty()) {
                        linkEmailPasswordToCurrentUser(first, second, authStateListener)
                    }
                }
            }
            btnUnlinkCredential.setOnClickListener {
                TODO("TBI")
            }
            btnSignOut.setOnClickListener {
                firebaseAuth.signOut()
            }
        }

        //endregion
        //region observe sign in progress

        observeSignInByEmailPassword {
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
                        EmailPasswordSignInException.Unknown          -> "Unknown"
                        EmailPasswordSignInException.Offline          -> "Internet connection unavailable"
                        EmailPasswordSignInException.Timeout          -> "Connection time out"
                        EmailPasswordSignInException.InvalidEmail     -> "Invalid email address"
                        EmailPasswordSignInException.Unauthenticated  -> "Linking account failed: user not login"
                        is EmailPasswordSignInException.AuthException -> signInException.exception.message!!
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
