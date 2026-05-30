package com.example.kabaddikounter.ui

import android.widget.Button
import androidx.core.content.ContextCompat
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.kabaddikounter.R

// ✅ WAJIB top-level — JANGAN di dalam object, class, atau companion object
// Data Binding mensyaratkan BindingAdapter bersifat static (top-level di Kotlin = static di JVM)

@BindingAdapter("matchStatus")
fun setMatchStatusColor(view: TextView, status: String?) {
    view.setTextColor(
        when (status?.uppercase()) {
            "LIVE" -> ContextCompat.getColor(view.context, R.color.red_live)
            "END"  -> ContextCompat.getColor(view.context, R.color.orange_end)
            else   -> ContextCompat.getColor(view.context, R.color.text_secondary)
        }
    )
}

@BindingAdapter("subscribeState")
fun Button.setSubscribeState(isSubscribed: Boolean) {
    if (isSubscribed) {
        text = context.getString(R.string.unsubscribe)
        setBackgroundColor(ContextCompat.getColor(context, R.color.orange_unsubscribe))
    } else {
        text = context.getString(R.string.subscribe)
        setBackgroundColor(ContextCompat.getColor(context, R.color.blue_primary))
    }
}