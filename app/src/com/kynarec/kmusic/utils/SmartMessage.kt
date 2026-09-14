package com.kynarec.kmusic.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.kynarec.kmusic.enums.PopupType
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
fun SmartMessage(
    message: String,
    type: PopupType? = PopupType.Info,
    durationLong: Boolean = false,
    context: Context,
) {
    CoroutineScope(Dispatchers.Main).launch {
        val length = if (durationLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT

            when (type) {
                PopupType.Info -> Toasty.info(context, message, length, true).show()
                PopupType.Success -> Toasty.success(context, message, length, true).show()
                PopupType.Error -> Toasty.error(context, message, length, true).show()
                PopupType.Warning -> Toasty.warning(context, message, length, true).show()
                null -> Toasty.normal(context, message, length).show()
            }
    }
}