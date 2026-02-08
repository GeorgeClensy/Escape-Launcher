package com.geecee.escapelauncher.utils.managers

import androidx.annotation.StringRes
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R

const val DEFAULT_SPACER_SIZE = 30.0f

enum class SpacerMode(
    @StringRes val labelRes: Int,
    val value: Float,
) {
    COMPACT(R.string.set_spacer_size_compact, 20f),
    STANDARD(R.string.set_spacer_size_standard, 30f),
    RELAXED(R.string.set_spacer_size_relaxed, 40f)
}

fun currentSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(
        Migration.UNIFIED_PREFS_NAME, Context.MODE_PRIVATE
    )
}

fun resetSpacerSize(context: Context, mainAppModel: MainAppViewModel) {
    mainAppModel.updateSpacerSize(context, DEFAULT_SPACER_SIZE)
}

fun getSpacerSize(context: Context): Float {
    return currentSharedPreferences(context).getFloat("SpacerSize", DEFAULT_SPACER_SIZE)
}

fun setSpacerSize(context: Context, value: Float) {
    currentSharedPreferences(context).edit {
        putFloat("SpacerSize", value)
    }
}
