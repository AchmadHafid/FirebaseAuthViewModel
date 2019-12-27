package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.fireAuth
import io.github.achmadhafid.firebase_auth_view_model.fireUser
import io.github.achmadhafid.firebase_auth_view_model.observeFireAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.EmailSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeFireSignInByEmail
import io.github.achmadhafid.firebase_auth_view_model.signin.startFireSignInByEmail
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.zpack.ktx.bindView
import io.github.achmadhafid.zpack.ktx.onSingleClick
import io.github.achmadhafid.zpack.ktx.setMaterialToolbar
import io.github.achmadhafid.zpack.ktx.setTextRes
import io.github.achmadhafid.zpack.ktx.toastShort
import io.github.achmadhafid.zpack.ktx.value

class EmailSignInActivity : BaseActivity(R.layout.activity_email_sign_in) {

    //region View Binding

    private val inputLayoutEmail: TextInputLayout by bindView(R.id.input_layout_email)
    private val inputLayoutPassword: TextInputLayout by bindView(R.id.input_layout_password)
    private val edtEmail: TextInputEditText by bindView(R.id.edt_email)
    private val edtPassword: TextInputEditText by bindView(R.id.edt_password)
    private val btnCreateUser: MaterialButton by bindView(R.id.btn_create_user)
    private val btnAuth: MaterialButton by bindView(R.id.btn_auth)

    //endregion
    //region Lifecycle Callback

    @Suppress("ComplexMethod", "LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //region setup toolbar

        setMaterialToolbar(R.id.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //endregion
        //region setup action widget

        btnCreateUser.onSingleClick {
            with(edtEmail.value to edtPassword.value) {
                if (first.isNotEmpty() && second.isNotEmpty()) {
                    startFireSignInByEmail(first, second, true)
                }
            }
        }
        btnAuth.onSingleClick {
            fireUser?.let {
                fireAuth.signOut()
            } ?: with(edtEmail.value to edtPassword.value) {
                if (first.isNotEmpty() && second.isNotEmpty()) {
                    startFireSignInByEmail(first, second)
                }
            }
        }

        //endregion
        //region observe auth state

        observeFireAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                btnAuth.setTextRes(R.string.logout)
                btnCreateUser.isEnabled       = false
                inputLayoutEmail.isEnabled    = false
                inputLayoutPassword.isEnabled = false
            }
            onSignedOut {
                Logger.d("User signed out")
                btnAuth.setTextRes(R.string.login)
                btnCreateUser.isEnabled       = true
                inputLayoutEmail.isEnabled    = true
                inputLayoutPassword.isEnabled = true
            }
        }

        //endregion
        //region observe sign in progress

        observeFireSignInByEmail {
            val (state, hasBeenConsumed) = it.state
            when (state) {
                SignInState.OnProgress -> showLoadingDialog()
                is SignInState.OnSuccess -> if (!hasBeenConsumed) {
                    dismissDialog()
                    toastShort("Sign in success!")
                }
                is SignInState.OnFailed -> if (!hasBeenConsumed) {
                    dismissDialog()
                    val message = when (val signInException = state.exception) {
                        EmailSignInException.Unknown -> "Unknown"
                        EmailSignInException.Offline -> "Internet connection unavailable"
                        EmailSignInException.Timeout -> "Connection time out"
                        is EmailSignInException.FireAuthException -> {
                            signInException.fireException.message!!
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
