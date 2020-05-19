package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.observeFirebaseAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.EmailPasswordSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeSignInByEmailPassword
import io.github.achmadhafid.firebase_auth_view_model.signin.startSignInByEmailPassword
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.sample_app.databinding.ActivityEmailPasswordSignInBinding
import io.github.achmadhafid.zpack.extension.toastShort
import io.github.achmadhafid.zpack.extension.view.enabled
import io.github.achmadhafid.zpack.extension.view.onSingleClick
import io.github.achmadhafid.zpack.extension.view.setTextRes
import io.github.achmadhafid.zpack.extension.view.value

class EmailPasswordSignInActivity : BaseActivity() {

    //region View Binding

    private val binding by lazy {
        ActivityEmailPasswordSignInBinding.inflate(layoutInflater)
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

        binding.btnCreateUser.onSingleClick {
            with(binding.edtEmail.value to binding.edtPassword.value) {
                if (first.isNotEmpty() && second.isNotEmpty()) {
                    startSignInByEmailPassword(first, second, true)
                }
            }
        }
        binding.btnAuth.onSingleClick {
            firebaseUser?.let {
                firebaseAuth.signOut()
            } ?: with(binding.edtEmail.value to binding.edtPassword.value) {
                if (first.isNotEmpty() && second.isNotEmpty()) {
                    startSignInByEmailPassword(first, second)
                }
            }
        }

        //endregion
        //region observe auth state

        observeFirebaseAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                with(binding) {
                    btnAuth.setTextRes(R.string.logout)
                    listOf(btnCreateUser, inputLayoutEmail, inputLayoutPassword)
                        .enabled(false)
                }
            }
            onSignedOut {
                Logger.d("User signed out")
                with(binding) {
                    btnAuth.setTextRes(R.string.login)
                    listOf(btnCreateUser, inputLayoutEmail, inputLayoutPassword)
                        .enabled(true)
                }
            }
        }

        //endregion
        //region observe sign in progress

        observeSignInByEmailPassword {
            val (state, hasBeenConsumed) = it.getState()
            when (state) {
                SignInState.OnProgress -> showLoadingDialog()
                is SignInState.OnSuccess -> if (!hasBeenConsumed) {
                    dismissDialog()
                    toastShort("Sign in success!")
                }
                is SignInState.OnFailed -> if (!hasBeenConsumed) {
                    dismissDialog()
                    val message = when (val signInException = state.exception) {
                        EmailPasswordSignInException.Unknown -> "Unknown"
                        EmailPasswordSignInException.Offline -> "Internet connection unavailable"
                        EmailPasswordSignInException.Timeout -> "Connection time out"
                        is EmailPasswordSignInException.AuthException -> {
                            signInException.exception.message!!
                        }
                    }
                    toastShort(message)
                }
            }
        }

        //endregion
    }

    //endregion
}