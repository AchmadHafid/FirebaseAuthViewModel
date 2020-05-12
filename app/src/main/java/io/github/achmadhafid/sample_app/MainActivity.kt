package io.github.achmadhafid.sample_app

import android.os.Bundle
import androidx.lifecycle.observe
import io.github.achmadhafid.sample_app.auth.AnonymousSignInActivity
import io.github.achmadhafid.sample_app.auth.EmailSignInActivity
import io.github.achmadhafid.sample_app.auth.GoogleSignInActivity
import io.github.achmadhafid.sample_app.auth.PhoneSignInActivity
import io.github.achmadhafid.sample_app.databinding.ActivityMainBinding
import io.github.achmadhafid.simplepref.livedata.simplePrefLiveData
import io.github.achmadhafid.zpack.extension.intent
import io.github.achmadhafid.zpack.extension.view.onSingleClick

class MainActivity : BaseActivity() {

    //region View Binding

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    //endregion
    //region Lifecycle Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        //region setup view

        binding.btnAnonymousSignInDemo.onSingleClick {
            startActivity(intent<AnonymousSignInActivity>())
        }
        binding.btnGoogleSignInDemo.onSingleClick {
            startActivity(intent<GoogleSignInActivity>())
        }
        binding.btnEmailSignInDemo.onSingleClick {
            startActivity(intent<EmailSignInActivity>())
        }
        binding.btnPhoneSignInDemo.onSingleClick {
            startActivity(intent<PhoneSignInActivity>())
        }

        binding.cbAuthCallbackMode.setOnCheckedChangeListener { _, isChecked ->
            authCallbackMode = isChecked
        }

        //endregion
        //region setup preference observer

        simplePrefLiveData(authCallbackMode, ::authCallbackMode).observe(this) {
            binding.cbAuthCallbackMode.isChecked = it
        }

        //endregion
    }

    //endregion

}
