package io.github.achmadhafid.sample_app

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.github.achmadhafid.lottie_dialog.core.lottieLoadingDialog
import io.github.achmadhafid.lottie_dialog.core.withAnimation
import io.github.achmadhafid.lottie_dialog.core.withTitle
import io.github.achmadhafid.lottie_dialog.dismissLottieDialog
import io.github.achmadhafid.simplepref.SimplePref
import io.github.achmadhafid.simplepref.simplePref
import io.github.achmadhafid.zpack.extension.toggleTheme

abstract class BaseActivity: AppCompatActivity(), SimplePref {

    //region Preference

    private var appTheme: Int? by simplePref("app_theme")
    protected var authCallbackMode by simplePref("auth_callback_mode") { false }

    //endregion
    //region Lifecycle Callback

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_action_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {
                appTheme = toggleTheme()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //endregion
    //region UI Helper

    protected fun showLoadingDialog() {
        lottieLoadingDialog(Int.MAX_VALUE) {
            withAnimation {
                fileRes = R.raw.lottie_animation_loading
                lottieAnimationProperties = {
                    speed = 2.0f
                }
            }
            withTitle("Please wait...")
        }
    }

    protected fun dismissDialog() {
        dismissLottieDialog()
    }

    //endregion

}
