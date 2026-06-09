package com.example.kabaddikounter

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.widget.RemoteViews

class MatchListWidget : AppWidgetProvider() {

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
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, MatchListWidget::class.java)
            )
            manager.notifyAppWidgetViewDataChanged(ids, R.id.lvMatches)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.kabaddikounter.MATCH_LIST_REFRESH"

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_match_list)

            val serviceIntent = Intent(context, MatchListWidgetService::class.java)
            views.setRemoteAdapter(R.id.lvMatches, serviceIntent)
            views.setEmptyView(R.id.lvMatches, R.id.tvEmpty)

            val refreshIntent = Intent(context, MatchListWidget::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPending = PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnRefresh, refreshPending)

            manager.updateAppWidget(widgetId, views)
        }

        fun sendRefreshBroadcast(context: Context) {
            val intent = Intent(context, MatchListWidget::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
