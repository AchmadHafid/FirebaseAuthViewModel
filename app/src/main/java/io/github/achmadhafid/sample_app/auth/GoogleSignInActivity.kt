package io.github.achmadhafid.sample_app.auth

import android.content.Intent
import android.os.Bundle
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.observeFirebaseAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.GoogleSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeSignInByGoogle
import io.github.achmadhafid.firebase_auth_view_model.signin.onSignInByGoogleResult
import io.github.achmadhafid.firebase_auth_view_model.signin.startSignInByGoogle
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.sample_app.databinding.ActivityGoogleSignInBinding
import io.github.achmadhafid.zpack.extension.stringRes
import io.github.achmadhafid.zpack.extension.toastShort
import io.github.achmadhafid.zpack.extension.view.gone
import io.github.achmadhafid.zpack.extension.view.onSingleClick
import io.github.achmadhafid.zpack.extension.view.visible

private const val REQUEST_CODE = 1234

class GoogleSignInActivity : BaseActivity() {

    //region View Binding

    private val binding by lazy {
        ActivityGoogleSignInBinding.inflate(layoutInflater)
    }

    //endregion
    //region Resource Binding

    private val webClientId by stringRes(R.string.web_client_id)

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

        binding.btnLogin.onSingleClick {
            startSignInByGoogle(webClientId.toString(), REQUEST_CODE, binding.cbForceAccountChooser.isChecked)
        }
        binding.btnLogout.onSingleClick {
            firebaseAuth.signOut()
        }

        //endregion
        //region observe auth state

        observeFirebaseAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                with(binding) {
                    btnLogin.gone()
                    cbForceAccountChooser.gone()
                    btnLogout.visible()
                }
            }
            onSignedOut {
                Logger.d("User signed out")
                with(binding) {
                    btnLogout.gone()
                    btnLogin.visible()
                    cbForceAccountChooser.visible()
                }
            }
        }

        //endregion
        //region observe sign in progress

        observeSignInByGoogle {
            val (state, hasBeenConsumed) = it.state
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
                    val message = when (val signInException = state.exception) {
                        GoogleSignInException.Canceled -> "Canceled"
                        GoogleSignInException.Unknown -> "Unknown"
                        GoogleSignInException.Offline -> "Internet connection unavailable"
                        GoogleSignInException.Timeout -> "Connection time out"
                        is GoogleSignInException.AuthException -> {
                            signInException.exception.message!!
                        }
                        is GoogleSignInException.WrappedApiException -> {
                            signInException.exception.message!!
                        }
                    }
                    toastShort(message)
                }
            }
        }

        //endregion
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE == requestCode) {
            onSignInByGoogleResult(resultCode, data)
        }
    }

    //endregion

}
