package io.github.achmadhafid.sample_app

import android.app.Dialog
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import io.github.achmadhafid.lottie_dialog.lottieLoadingDialog
import io.github.achmadhafid.lottie_dialog.model.LottieDialogTheme
import io.github.achmadhafid.lottie_dialog.model.LottieDialogType
import io.github.achmadhafid.lottie_dialog.withAnimation
import io.github.achmadhafid.lottie_dialog.withTitle
import io.github.achmadhafid.simplepref.SimplePref
import io.github.achmadhafid.simplepref.simplePref
import io.github.achmadhafid.zpack.ktx.toggleTheme

abstract class BaseActivity(@LayoutRes layout: Int): AppCompatActivity(layout), SimplePref {

    //region Preference

    private var appTheme: Int? by simplePref("app_theme")
    protected var authCallbackMode by simplePref("auth_callback_mode") { false }

    //endregion
    //region View

    private var dialog: Dialog? = null
        set(value) {
            field?.dismiss()
            field = value
        }

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

    override fun onBackPressed() {
        if (this::class.java == MainActivity::class.java) {
            super.onBackPressed()
        } else {
            onNavigateUp()
        }
    }

    //endregion
    //region UI Helper

    protected fun showLoadingDialog() {
        dialog = lottieLoadingDialog {
            theme = LottieDialogTheme.DAY_NIGHT
            type = LottieDialogType.BOTTOM_SHEET
            withAnimation {
                fileRes = R.raw.lottie_animation_loading
                animationSpeed = 2f
            }
            withTitle("Please wait...")
        }
    }

    protected fun dismissDialog() {
        dialog = null
    }

    //endregion

}
