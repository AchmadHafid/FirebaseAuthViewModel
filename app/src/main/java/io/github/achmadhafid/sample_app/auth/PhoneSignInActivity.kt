package io.github.achmadhafid.sample_app.auth

import android.os.Bundle
import com.orhanobut.logger.Logger
import io.github.achmadhafid.firebase_auth_view_model.firebaseAuth
import io.github.achmadhafid.firebase_auth_view_model.firebaseUser
import io.github.achmadhafid.firebase_auth_view_model.observeFirebaseAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signin.PhoneSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.PhoneSignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.cancelSignInByPhone
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.observeSignInByPhone
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.onSignInByPhoneSubmitOtp
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.onSignInByPhoneSubmitPhone
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.startSignInByPhone
import io.github.achmadhafid.lottie_dialog.core.lottieInputDialog
import io.github.achmadhafid.lottie_dialog.core.lottieLoadingDialog
import io.github.achmadhafid.lottie_dialog.core.onCancel
import io.github.achmadhafid.lottie_dialog.core.onValidInput
import io.github.achmadhafid.lottie_dialog.core.withAnimation
import io.github.achmadhafid.lottie_dialog.core.withCancelOption
import io.github.achmadhafid.lottie_dialog.core.withContent
import io.github.achmadhafid.lottie_dialog.core.withInputSpec
import io.github.achmadhafid.lottie_dialog.core.withTitle
import io.github.achmadhafid.lottie_dialog.model.LottieDialogInput
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.sample_app.databinding.ActivityPhoneSignInBinding
import io.github.achmadhafid.zpack.extension.intRes
import io.github.achmadhafid.zpack.extension.toastShort
import io.github.achmadhafid.zpack.extension.view.withTextRes

class PhoneSignInActivity : BaseActivity() {

    //region Resource Binding

    private val timeout by intRes(R.integer.phone_sign_in_timeout)

    //endregion
    //region View Binding

    private val binding by lazy {
        ActivityPhoneSignInBinding.inflate(layoutInflater)
    }

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
        //region setup auth button

        binding.btnAuth.setOnClickListener {
            firebaseUser?.let {
                firebaseAuth.signOut()
            } ?: run {
                val initialPhone = getString(R.string.dialog_phone_sign_in_input_phone_pre_fill)
                val languageCode = "id"
                startSignInByPhone(initialPhone, languageCode, timeout.toLong())
            }
        }

        //endregion
        //region observe auth state

        observeFirebaseAuthState(authCallbackMode) {
            onSignedIn {
                Logger.d("User signed in")
                binding.btnAuth withTextRes R.string.logout
            }
            onSignedOut {
                Logger.d("User signed out")
                binding.btnAuth withTextRes R.string.login
            }
        }

        //endregion
        //region observe phone sign in state in view model

        observeSignInByPhone {
            val (state, hasBeenConsumed) = it.state
            when (state) {
                is PhoneSignInState.GetPhoneInput -> showInputPhoneDialog(state.phone)
                PhoneSignInState.RequestingOtp -> showProgressDialog()
                is PhoneSignInState.GetOtpInput -> showInputOtpDialog()
                PhoneSignInState.SigningIn -> showProgressDialog()
                PhoneSignInState.OnSuccess -> {
                    dismissDialog()
                    toastShort("Phone sign in success!")
                }
                is PhoneSignInState.OnFailed -> if (!hasBeenConsumed) {
                    dismissDialog()
                    val message = when (val signInException = state.exception) {
                        PhoneSignInException.Canceled -> "Canceled"
                        PhoneSignInException.Unknown -> "Unknown"
                        PhoneSignInException.Offline -> "No internet connection"
                        PhoneSignInException.RequestOtpTimeout -> "Timeout when requesting OTP code"
                        PhoneSignInException.SignInTimeout -> "Time out when signing in"
                        PhoneSignInException.InvalidRequest -> "Invalid (phone number) request"
                        PhoneSignInException.QuotaExceeded -> "Quota exceeded"
                        is PhoneSignInException.InvalidOtp -> "Invalid OTP"
                        is PhoneSignInException.AuthException -> signInException.exception.message
                    }
                    toastShort("Phone sign in error: $message")
                }
            }
        }

        //endregion
    }

    //endregion
    //region Dialog Helper for Sign In Flow

    @Suppress("MagicNumber")
    private fun showInputPhoneDialog(phone: String?) = lottieInputDialog(0) {
        withAnimation(R.raw.lottie_animation_phone)
        withTitle(R.string.dialog_phone_sign_in_input_phone_title)
        withContent(R.string.dialog_phone_sign_in_input_phone_content)
        withInputSpec {
            inputType = LottieDialogInput.Type.PHONE
            inputFormat = getString(R.string.dialog_phone_sign_in_input_phone_format)
            initialValue = phone
        }
        withCancelOption {
            onTouchOutside = false
        }
        onValidInput {
            onSignInByPhoneSubmitPhone(it)
        }
        onCancel {
            cancelSignInByPhone()
        }
    }

    @Suppress("MagicNumber")
    private fun showInputOtpDialog() = lottieInputDialog(0) {
        withAnimation(R.raw.lottie_animation_input_password)
        withTitle(R.string.dialog_phone_sign_in_input_otp_title)
        withContent(R.string.dialog_phone_sign_in_input_otp_content)
        withInputSpec {
            inputType = LottieDialogInput.Type.PHONE
            inputFormat = getString(R.string.dialog_phone_sign_in_input_otp_format)
        }
        withCancelOption {
            onTouchOutside = false
        }
        onValidInput {
            onSignInByPhoneSubmitOtp(it)
        }
        onCancel {
            cancelSignInByPhone()
        }
    }

    @Suppress("MagicNumber")
    private fun showProgressDialog() = lottieLoadingDialog(0) {
        showTimeOutProgress = false
        withAnimation {
            fileRes = R.raw.lottie_animation_loading
            lottieAnimationProperties = {
                speed = 2.0f
            }
            showCloseButton = false
        }
        withTitle(R.string.dialog_phone_sign_in_progress_title)
        withCancelOption {
            onBackPressed = false
            onTouchOutside = false
        }
    }

    //endregion

}
