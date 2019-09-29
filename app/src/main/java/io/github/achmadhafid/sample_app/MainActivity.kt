package io.github.achmadhafid.sample_app

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import io.github.achmadhafid.sample_app.auth.AnonymousSignInActivity
import io.github.achmadhafid.sample_app.auth.EmailSignInActivity
import io.github.achmadhafid.sample_app.auth.GoogleSignInActivity
import io.github.achmadhafid.sample_app.auth.PhoneSignInActivity
import io.github.achmadhafid.simplepref.SimplePref
import io.github.achmadhafid.zpack.ktx.bindView
import io.github.achmadhafid.zpack.ktx.onSingleClick
import io.github.achmadhafid.zpack.ktx.setMaterialToolbar
import io.github.achmadhafid.zpack.ktx.startActivity

class MainActivity : BaseActivity(R.layout.activity_main), SimplePref {

    //region View Binding

    private val btnDemoAnonymous: MaterialButton by bindView(R.id.btn_anonymous_sign_in_demo)
    private val btnDemoGoogle: MaterialButton by bindView(R.id.btn_google_sign_in_demo)
    private val btnDemoEmail: MaterialButton by bindView(R.id.btn_email_sign_in_demo)
    private val btnDemoPhone: MaterialButton by bindView(R.id.btn_phone_sign_in_demo)

    //endregion
    //region Lifecycle Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMaterialToolbar(R.id.toolbar)
        //region setup view

        btnDemoAnonymous.onSingleClick {
            startActivity<AnonymousSignInActivity>()
        }
        btnDemoGoogle.onSingleClick {
            startActivity<GoogleSignInActivity>()
        }
        btnDemoEmail.onSingleClick {
            startActivity<EmailSignInActivity>()
        }
        btnDemoPhone.onSingleClick {
            startActivity<PhoneSignInActivity>()
        }

        //endregion
    }

    //endregion

}