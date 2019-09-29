package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.achmadhafid.firebase_auth_view_model.isSignedIn
import io.github.achmadhafid.firebase_auth_view_model.isSignedOut
import io.github.achmadhafid.firebase_auth_view_model.observeAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signOut
import io.github.achmadhafid.firebase_auth_view_model.signin.EmailSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.EmailSignInExtensions
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeEmailSignIn
import io.github.achmadhafid.firebase_auth_view_model.signin.signInByEmail
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.zpack.ktx.bindView
import io.github.achmadhafid.zpack.ktx.onSingleClick
import io.github.achmadhafid.zpack.ktx.setMaterialToolbar
import io.github.achmadhafid.zpack.ktx.setTextRes
import io.github.achmadhafid.zpack.ktx.toastShort
import io.github.achmadhafid.zpack.ktx.value

class EmailSignInActivity : BaseActivity(R.layout.activity_email_sign_in), EmailSignInExtensions {

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
                    signInByEmail(first, second, true)
                }
            }
        }
        btnAuth.onSingleClick {
            when {
                isSignedIn -> signOut()
                isSignedOut -> with(edtEmail.value to edtPassword.value) {
                    if (first.isNotEmpty() && second.isNotEmpty()) {
                        signInByEmail(first, second)
                    }
                }
            }
        }

        //endregion
        //region observe auth state

        observeAuthState {
            onSignedIn {
                toastShort("User signed In")
                btnAuth.setTextRes(R.string.logout)
                btnCreateUser.isEnabled = false
                inputLayoutEmail.isEnabled = false
                inputLayoutPassword.isEnabled = false
            }
            onSignedOut {
                toastShort("User signed out")
                btnAuth.setTextRes(R.string.login)
                btnCreateUser.isEnabled = true
                inputLayoutEmail.isEnabled = true
                inputLayoutPassword.isEnabled = true
            }
        }

        //endregion
        //region observe sign in progress

        observeEmailSignIn {
            val message = when (val state = it.getState()) {
                SignInState.Empty -> {
                    /* transient state, no need to do anything */
                    return@observeEmailSignIn
                }
                SignInState.OnProgress -> {
                    /* The API call is being made, you may want to show some progress view here */
                    /* This is a normal event, it can be consumed multiple times */
                    "Please wait..."
                }
                is SignInState.OnSuccess -> {
                    /* Sign in is successful! */
                    /* This is a single event, it can only be consumed once */
                    "Sign in success!"
                }
                is SignInState.OnFailed -> {
                    /* Sign in is failed */
                    /* This is a single event, it can only be consumed once */
                    /* Use below construct to extract the exception */
                    when (val signInException = state.exception) {
                        EmailSignInException.Unknown -> "Unknown"
                        EmailSignInException.Offline -> "Internet connection unavailable"
                        EmailSignInException.Timeout -> "Connection time out"
                        is EmailSignInException.WrappedFirebaseAuthException -> {
                            signInException.firebaseAuthException.message!!
                        }
                    }
                }
            }
            toastShort(message)
        }

        //endregion
    }

    //endregion
}
