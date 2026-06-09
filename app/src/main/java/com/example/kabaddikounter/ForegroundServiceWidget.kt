package com.example.kabaddikounter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.kabaddikounter.service.LiveScoreForegroundService

class ForegroundServiceWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE -> {
                if (LiveScoreForegroundService.isRunning) {
                    context.stopService(
                        Intent(context, LiveScoreForegroundService::class.java)
                    )
                } else {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, LiveScoreForegroundService::class.java)
                    )
                }
                // UI update is triggered by the service's onCreate/onDestroy broadcast
            }
            ACTION_UPDATE_UI -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(
                    ComponentName(context, ForegroundServiceWidget::class.java)
                )
                for (id in ids) updateWidget(context, manager, id)
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.example.kabaddikounter.FOREGROUND_SERVICE_TOGGLE"
        const val ACTION_UPDATE_UI = "com.example.kabaddikounter.FOREGROUND_SERVICE_UPDATE_UI"

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val isRunning = LiveScoreForegroundService.isRunning
            val views = RemoteViews(context.packageName, R.layout.widget_foreground_service)

            views.setTextViewText(
                R.id.tvServiceStatus,
                if (isRunning) "Aktif" else "Nonaktif"
            )
            views.setTextViewText(
                R.id.btnToggleService,
                if (isRunning) "Nonaktifkan" else "Aktifkan"
            )

            val toggleIntent = Intent(context, ForegroundServiceWidget::class.java).apply {
                action = ACTION_TOGGLE
            }
            val togglePending = PendingIntent.getBroadcast(
                context, 1, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnToggleService, togglePending)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
