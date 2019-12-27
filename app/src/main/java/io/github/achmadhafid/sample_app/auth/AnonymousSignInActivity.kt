package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.fireAuth
import io.github.achmadhafid.firebase_auth_view_model.fireUser
import io.github.achmadhafid.firebase_auth_view_model.observeFireAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.AnonymousSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeFireSignInAnonymously
import io.github.achmadhafid.firebase_auth_view_model.signin.startFireSignInAnonymously
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.zpack.ktx.bindView
import io.github.achmadhafid.zpack.ktx.onSingleClick
import io.github.achmadhafid.zpack.ktx.setMaterialToolbar
import io.github.achmadhafid.zpack.ktx.setTextRes
import io.github.achmadhafid.zpack.ktx.toastShort

class AnonymousSignInActivity : BaseActivity(R.layout.activity_anonymous_sign_in) {

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
            fireUser?.let {
                fireAuth.signOut()
            } ?: startFireSignInAnonymously()
        }

        //endregion
        //region observe auth state

        observeFireAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                btnAuth.setTextRes(R.string.logout)
            }
            onSignedOut {
                Logger.d("User signed out")
                btnAuth.setTextRes(R.string.login)
            }
        }

        //endregion
        //region observe sign in progress

        observeFireSignInAnonymously {
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
                        AnonymousSignInException.Unknown -> "Unknown"
                        AnonymousSignInException.Offline -> "Internet connection unavailable"
                        AnonymousSignInException.Timeout -> "Connection time out"
                        is AnonymousSignInException.FireAuthException -> {
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
