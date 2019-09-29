package io.github.achmadhafid.sample_app.auth

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import io.github.achmadhafid.firebase_auth_view_model.isSignedIn
import io.github.achmadhafid.firebase_auth_view_model.isSignedOut
import io.github.achmadhafid.firebase_auth_view_model.observeAuthState
import io.github.achmadhafid.firebase_auth_view_model.onSignedIn
import io.github.achmadhafid.firebase_auth_view_model.onSignedOut
import io.github.achmadhafid.firebase_auth_view_model.signOut
import io.github.achmadhafid.firebase_auth_view_model.signin.PhoneSignInException
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.PhoneSignInExtensions
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.PhoneSignInState
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.cancelPhoneSignIn
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.observePhoneSignIn
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.startPhoneSignIn
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.submitOtp
import io.github.achmadhafid.firebase_auth_view_model.signin.phone.submitPhone
import io.github.achmadhafid.lottie_dialog.lottieInputDialog
import io.github.achmadhafid.lottie_dialog.lottieLoadingDialog
import io.github.achmadhafid.lottie_dialog.model.LottieDialogInput
import io.github.achmadhafid.lottie_dialog.model.LottieDialogType
import io.github.achmadhafid.lottie_dialog.onCancel
import io.github.achmadhafid.lottie_dialog.onValidInput
import io.github.achmadhafid.lottie_dialog.withAnimation
import io.github.achmadhafid.lottie_dialog.withCancelOption
import io.github.achmadhafid.lottie_dialog.withContent
import io.github.achmadhafid.lottie_dialog.withInputSpec
import io.github.achmadhafid.lottie_dialog.withTitle
import io.github.achmadhafid.sample_app.BaseActivity
import io.github.achmadhafid.sample_app.R
import io.github.achmadhafid.zpack.ktx.bindView
import io.github.achmadhafid.zpack.ktx.getLongRes
import io.github.achmadhafid.zpack.ktx.onSingleClick
import io.github.achmadhafid.zpack.ktx.setMaterialToolbar
import io.github.achmadhafid.zpack.ktx.setTextRes
import io.github.achmadhafid.zpack.ktx.toastShort

class PhoneSignInActivity : BaseActivity(R.layout.activity_phone_sign_in), PhoneSignInExtensions {

    //region View

    private val btnAuth by bindView<MaterialButton>(R.id.btn_auth)
    private var dialog: Dialog? = null
        set(value) {
            field?.dismiss()
            field = value
        }

    //endregion
    //region Lifecycle Callback

    @Suppress("ComplexMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //region setup toolbar

        setMaterialToolbar(R.id.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //endregion
        //region setup auth button

        btnAuth.onSingleClick {
            when {
                isSignedIn -> signOut()
                isSignedOut -> {
                    val initialPhone = getString(R.string.dialog_phone_sign_in_input_phone_pre_fill)
                    val languageCode = "en"
                    val timeout      = getLongRes(R.integer.phone_sign_in_timeout)
                    startPhoneSignIn(initialPhone, languageCode, timeout)
                }
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
        //region observe phone sign in state in view model

        observePhoneSignIn {
            dialog = when (val state = it.getState()) {
                PhoneSignInState.Empty -> return@observePhoneSignIn
                is PhoneSignInState.GetPhoneInput -> showInputPhoneDialog(state.phone)
                is PhoneSignInState.GetOtpInput -> showInputOtpDialog()
                PhoneSignInState.RequestingOtp,
                PhoneSignInState.SigningIn -> showProgressDialog()
                PhoneSignInState.OnSuccess -> null.also {
                    toastShort("Phone sign in success!")
                }
                is PhoneSignInState.OnFailed -> null.also {
                    val message = when (val signInException = state.exception) {
                        PhoneSignInException.Canceled -> "Canceled"
                        PhoneSignInException.Unknown -> "Unknown"
                        PhoneSignInException.Offline -> "No internet connection"
                        PhoneSignInException.RequestOtpTimeout -> "Timeout when requesting OTP code"
                        PhoneSignInException.SignInTimeout -> "Time out when signing in"
                        PhoneSignInException.InvalidRequest -> "Invalid (phone number) request"
                        is PhoneSignInException.InvalidOtp -> "Invalid OTP"
                        PhoneSignInException.QuotaExceeded -> "Quota exceeded"
                        is PhoneSignInException.WrappedFirebaseException ->
                            signInException.firebaseException.message
                    }
                    toastShort("Phone sign in error: $message")
                }
            }
        }

        //endregion
    }

    //endregion
    //region Dialog Helper for Sign In Flow

    private fun showInputPhoneDialog(phone: String?) = lottieInputDialog {
        type = LottieDialogType.BOTTOM_SHEET
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
            submitPhone(it)
        }
        onCancel {
            cancelPhoneSignIn()
        }
    }

    private fun showInputOtpDialog() = lottieInputDialog {
        type = LottieDialogType.BOTTOM_SHEET
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
            submitOtp(it)
        }
        onCancel {
            cancelPhoneSignIn()
        }
    }

    private fun showProgressDialog() = lottieLoadingDialog {
        type = LottieDialogType.BOTTOM_SHEET
        showTimeOutProgress = false
        withAnimation {
            fileRes         = R.raw.lottie_animation_loading
            animationSpeed  = 2.0f
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