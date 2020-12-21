package io.github.achmadhafid.firebase_auth_view_model

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal inline val Context.isConnected: Boolean
    get() = true

internal inline fun <reified VM : ViewModel> AppCompatActivity.getViewModel(): VM =
    ViewModelProvider(this).get(VM::class.java)

internal inline fun <reified VM : ViewModel> Fragment.getViewModel(): VM =
    ViewModelProvider(this).get(VM::class.java)
