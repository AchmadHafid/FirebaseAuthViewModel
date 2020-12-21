package io.github.achmadhafid.sample_app

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import io.github.achmadhafid.simplepref.lifecycle.SimplePrefApplication
import io.github.achmadhafid.simplepref.lifecycle.SimplePrefLifecycleOwner
import io.github.achmadhafid.simplepref.simplePref
import io.github.achmadhafid.zpack.extension.applyTheme

@Suppress("Unused")
class App : Application(), SimplePrefLifecycleOwner by SimplePrefApplication() {

    private var appTheme: Int? by simplePref("app_theme")

    override fun onCreate() {
        super.onCreate()
        attachSimplePrefContext(this)
        Logger.addLogAdapter(
            AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(false)
                    .tag("FIREBASE_AUTH_VM")
                    .build()
            )
        )
        appTheme?.let { applyTheme(it) }
    }

}

//TODO("Link & Unlink email password & email link")
