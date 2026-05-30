package com.example.kabaddikounter.ui

import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.kabaddikounter.R

object MatchBindingAdapters {

    /**
     * ✅ @BindingAdapter adalah bagian resmi dari Data Binding.
     *    Dipanggil dari XML via: app:subscribeState="@{isSubscribed}"
     *
     * Perbaikan: warna dan teks diambil dari resources,
     * bukan hardcode hex string.
     */
    @JvmStatic
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

    @BindingAdapter("matchStatus")
    fun TextView.setMatchStatusColor(status: String?) {
        setTextColor(
            when (status?.uppercase()) {
                "LIVE" -> ContextCompat.getColor(context, R.color.red_live)
                "END"  -> ContextCompat.getColor(context, R.color.orange_end)
                else   -> ContextCompat.getColor(context, R.color.text_secondary)
            }
        )
    }
}