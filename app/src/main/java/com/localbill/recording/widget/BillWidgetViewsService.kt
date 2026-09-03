package com.localbill.recording.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.localbill.recording.R

class BillWidgetViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return BillWidgetRecentViewsFactory(applicationContext)
    }
}

class BillWidgetRecentViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val items = mutableListOf<BillWidgetRecentItem>()

    override fun onCreate() {
        load()
    }

    override fun onDataSetChanged() {
        load()
    }

    override fun onDestroy() {
        items.clear()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= items.size) return RemoteViews(context.packageName, R.layout.widget_bill_item)
        val item = items[position]
        val views = RemoteViews(context.packageName, R.layout.widget_bill_item)
        views.setTextViewText(R.id.widget_recent_time, item.timeText)
        views.setTextViewText(R.id.widget_recent_category, item.categoryText)
        views.setTextViewText(R.id.widget_recent_amount, item.amountText)
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = items.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true

    private fun load() {
        val snapshot = BillWidgetDataLoader.load(context)
        items.clear()
        items.addAll(snapshot.recentItems)
    }
}
