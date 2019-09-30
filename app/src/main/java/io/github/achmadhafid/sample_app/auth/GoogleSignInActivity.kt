package io.github.achmadhafid.sample_app.auth

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.common.SignInButton
import com.google.android.material.button.MaterialButton
import io.github.achmadhafid.firebase_auth_view_model.observeAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signOut
import io.github.achmadhafid.firebase_auth_view_model.signin.GoogleSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.GoogleSignInExtensions
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeGoogleSignIn
import io.github.achmadhafid.firebase_auth_view_model.signin.onSignInByGoogleResult
import io.github.achmadhafid.firebase_auth_view_model.signin.startGoogleSignInActivity
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

class GoogleSignInActivity : BaseActivity(R.layout.activity_google_sign_in), GoogleSignInExtensions {

    //region View Binding

    private val btnLogin: SignInButton by bindView(R.id.btn_login)
    private val btnLogout: MaterialButton by bindView(R.id.btn_logout)

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
            startGoogleSignInActivity(webClientId, REQUEST_CODE)
        }
        btnLogout.onSingleClick {
            signOut()
        }

        //endregion
        //region observe auth state

        observeAuthState {
            onSignedIn {
                toastShort("User signed In")
                btnLogin.gone()
                btnLogout.show()
            }
            onSignedOut {
                toastShort("User signed out")
                btnLogout.gone()
                btnLogin.show()
            }
        }

        //endregion
        //region observe sign in progress

        observeGoogleSignIn {
            val message = when (val state = it.getState()) {
                SignInState.Empty -> {
                    /* transient state, no need to do anything */
                    return@observeGoogleSignIn
                }
                SignInState.OnProgress -> {
                    /* The API call is being made, you may want to show some progress view here */
                    /* This is a normal state, it can be consumed multiple times */
                    "Please wait..."
                }
                is SignInState.OnSuccess -> {
                    /* Sign in is successful! */
                    /* This is a terminal state, most of the time it should be consumed only once */
                    "Sign in success!"
                }
                is SignInState.OnFailed -> {
                    /* Sign in is failed */
                    /* This is a terminal state, most of the time it should be consumed only once */
                    /* Use below construct to extract the exception */
                    when (val signInException = state.exception) {
                        GoogleSignInException.Canceled -> "Unknown"
                        GoogleSignInException.Unknown -> "Unknown"
                        GoogleSignInException.Offline -> "Internet connection unavailable"
                        GoogleSignInException.Timeout -> "Connection time out"
                        is GoogleSignInException.WrappedFirebaseAuthException -> {
                            signInException.firebaseAuthException.message!!
                        }
                        is GoogleSignInException.WrappedApiException -> {
                            signInException.apiException.message!!
                        }
                    }
                }
            }
            toastShort(message)
        }

        //endregion
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE == requestCode) {
            onSignInByGoogleResult(resultCode, data)
        }
    }

    //endregion

}
