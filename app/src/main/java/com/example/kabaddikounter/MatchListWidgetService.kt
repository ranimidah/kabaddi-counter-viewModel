package com.example.kabaddikounter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.repository.MatchRepository
import com.example.kabaddikounter.service.MatchApiData
import kotlinx.coroutines.runBlocking

class MatchListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return MatchListRemoteViewsFactory(applicationContext)
    }
}

class MatchListRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var matches: List<MatchApiData> = emptyList()
    private val repository = MatchRepository(context)

    override fun onCreate() {}

    override fun onDataSetChanged() {
        runBlocking {
            repository.getMatches()
                .onSuccess { matches = it }
                .onFailure { matches = emptyList() }
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = matches.size

    override fun getViewAt(position: Int): RemoteViews {
        val match = matches[position]
        return RemoteViews(context.packageName, R.layout.widget_match_list_item).apply {
            setTextViewText(R.id.tvTeams, "${match.team_a} vs ${match.team_b}")
            setTextViewText(R.id.tvScore, "${match.score_a}  —  ${match.score_b}")
            setTextViewText(R.id.tvStatus, match.status)
            if (match.status == "LIVE") {
                setTextColor(R.id.tvStatus, Color.parseColor("#FF4444"))
            } else {
                setTextColor(R.id.tvStatus, Color.GRAY)
            }
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false
}
