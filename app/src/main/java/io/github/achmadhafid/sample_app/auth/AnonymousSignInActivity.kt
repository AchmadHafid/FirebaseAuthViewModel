package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import io.github.achmadhafid.firebase_auth_view_model.isSignedIn
import io.github.achmadhafid.firebase_auth_view_model.isSignedOut
import io.github.achmadhafid.firebase_auth_view_model.observeAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signOut
import io.github.achmadhafid.firebase_auth_view_model.signin.AnonymousSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.AnonymousSignInExtensions
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeAnonymousSignIn
import io.github.achmadhafid.firebase_auth_view_model.signin.signInAnonymously
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.zpack.ktx.bindView
import io.github.achmadhafid.zpack.ktx.onSingleClick
import io.github.achmadhafid.zpack.ktx.setMaterialToolbar
import io.github.achmadhafid.zpack.ktx.setTextRes
import io.github.achmadhafid.zpack.ktx.toastShort

class AnonymousSignInActivity : BaseActivity(R.layout.activity_anonymous_sign_in),
    AnonymousSignInExtensions {

    //region View Binding

    private val btnAuth: MaterialButton by bindView(R.id.btn_auth)

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

        btnAuth.onSingleClick {
            when {
                isSignedIn -> signOut()
                isSignedOut -> signInAnonymously()
            }
        }

        //endregion
        //region observe auth state

        observeAuthState {
            onSignedIn {
                toastShort("User signed In")
                btnAuth.setTextRes(R.string.logout)
            }
            onSignedOut {
                toastShort("User signed out")
                btnAuth.setTextRes(R.string.login)
            }
        }

        //endregion
        //region observe sign in progress

        observeAnonymousSignIn {
            val message = when (val state = it.getState()) {
                SignInState.Empty -> {
                    /* transient state, no need to do anything */
                    return@observeAnonymousSignIn
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
                        AnonymousSignInException.Unknown -> "Unknown"
                        AnonymousSignInException.Offline -> "Internet connection unavailable"
                        AnonymousSignInException.Timeout -> "Connection time out"
                        is AnonymousSignInException.WrappedFirebaseAuthException -> {
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
