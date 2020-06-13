package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.actionCodeSettings
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.observeFirebaseAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.EmailLinkSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.SignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.observeSignInByEmailLink
import io.github.achmadhafid.firebase_auth_view_model.signin.sendSignInLinkToEmail
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.sample_app.databinding.ActivityEmailLinkSignInBinding
import io.github.achmadhafid.zpack.extension.toastShort
import io.github.achmadhafid.zpack.extension.view.gone
import io.github.achmadhafid.zpack.extension.view.onSingleClick
import io.github.achmadhafid.zpack.extension.view.setTextRes
import io.github.achmadhafid.zpack.extension.view.value
import io.github.achmadhafid.zpack.extension.view.visible
import kotlinx.coroutines.launch

class EmailLinkSignInActivity : BaseActivity() {

    //region View Binding

    private val binding by lazy {
        ActivityEmailLinkSignInBinding.inflate(layoutInflater)
    }

    //endregion
    //region Auth State Listener

    private val authStateListener by lazy {
        observeFirebaseAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                with(binding) {
                    btnAuth.setTextRes(R.string.logout)
                    inputLayoutEmail.gone()
                }
            }
            onSignedOut {
                Logger.d("User signed out")
                with(binding) {
                    btnAuth.setTextRes(R.string.btn_send_sign_in_link_to_email)
                    inputLayoutEmail.visible()
                }
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

        binding.btnAuth.onSingleClick {
            firebaseUser?.let {
                firebaseAuth.signOut()
            } ?: lifecycleScope.launch {
                runCatching {
                    sendSignInLinkToEmail(binding.edtEmail.value, actionCodeSettings {
                        url = "https://achmadhafid.page.link/"
                        handleCodeInApp = true
                        setAndroidPackageName(packageName, true, "1")
                        dynamicLinkDomain = "achmadhafid.page.link"
                    })
                }.onSuccess {
                    finish()
                }.onFailure {
                    Logger.e("Email link can not be sent: ${it.message}")
                }
            }
        }

        //endregion
        //region observe sign in progress

        observeSignInByEmailLink {
            val (_, state, hasBeenConsumed) = it.getEvent()
            when (state) {
                SignInState.OnProgress -> showLoadingDialog()
                is SignInState.OnSuccess -> if (!hasBeenConsumed) {
                    dismissDialog()
                    toastShort("Sign in success!")
                }
                is SignInState.OnFailed -> if (!hasBeenConsumed) {
                    dismissDialog()
                    val message = when (val signInException = state.exception) {
                        EmailLinkSignInException.Unknown          -> "Unknown"
                        EmailLinkSignInException.Offline          -> "Internet connection unavailable"
                        EmailLinkSignInException.Timeout          -> "Connection time out"
                        EmailLinkSignInException.InvalidLink      -> "Invalid Link"
                        EmailLinkSignInException.NoEmailFound     -> "No Email Found"
                        EmailLinkSignInException.Unauthenticated  -> "User no authenticated"
                        is EmailLinkSignInException.AuthException -> signInException.exception.message!!
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
