package io.github.achmadhafid.sample_app.auth

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.common.SignInButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.fireAuth
import io.github.achmadhafid.firebase_auth_view_model.observeFireAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.GoogleSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeFireSignInByGoogle
import io.github.achmadhafid.firebase_auth_view_model.signin.onFireSignInByGoogleActivityResult
import io.github.achmadhafid.firebase_auth_view_model.signin.startFireSignInByGoogle
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.zpack.ktx.bindView
import io.github.achmadhafid.zpack.ktx.gone
import io.github.achmadhafid.zpack.ktx.onSingleClick
import io.github.achmadhafid.zpack.ktx.setMaterialToolbar
import io.github.achmadhafid.zpack.ktx.show
import io.github.achmadhafid.zpack.ktx.stringRes
import io.github.achmadhafid.zpack.ktx.toastShort

private const val REQUEST_CODE = 1234

class GoogleSignInActivity : BaseActivity(R.layout.activity_google_sign_in) {

    //region View Binding

    private val btnLogin: SignInButton by bindView(R.id.btn_login)
    private val btnLogout: MaterialButton by bindView(R.id.btn_logout)
    private val cbForceAccountChooser: MaterialCheckBox by bindView(R.id.cb_force_account_chooser)

    //endregion
    //region Resource Binding

    private val webClientId by stringRes(R.string.web_client_id)

    //endregion
    //region Lifecycle Callback

    @Suppress("ComplexMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //region setup toolbar

        setMaterialToolbar(R.id.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //endregion
        //region setup action widget

        btnLogin.onSingleClick {
            startFireSignInByGoogle(webClientId, REQUEST_CODE, cbForceAccountChooser.isChecked)
        }
        btnLogout.onSingleClick {
            fireAuth.signOut()
        }

        //endregion
        //region observe auth state

        observeFireAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                btnLogin.gone()
                cbForceAccountChooser.gone()
                btnLogout.show()
            }
            onSignedOut {
                Logger.d("User signed out")
                btnLogout.gone()
                btnLogin.show()
                cbForceAccountChooser.show()
            }
        }

        //endregion
        //region observe sign in progress

        observeFireSignInByGoogle {
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
                        is GoogleSignInException.FireAuthException -> {
                            signInException.fireException.message!!
                        }
                        is GoogleSignInException.WrappedApiException -> {
                            signInException.apiException.message!!
                        }
                    }
                    toastShort(message)
                }
            }
        }

        //endregion
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE == requestCode) {
            onFireSignInByGoogleActivityResult(resultCode, data)
        }
    }

    //endregion

}
