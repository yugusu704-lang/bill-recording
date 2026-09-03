package com.localbill.recording.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.localbill.recording.MainActivity
import com.localbill.recording.R
import java.util.concurrent.Executors

open class BillWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_OPEN_APP = "com.localbill.recording.OPEN_APP"
        const val ACTION_ADD = "com.localbill.recording.ADD_RECORD"
        const val EXTRA_ADD_RECORD = "add_record"

        private val executor = Executors.newSingleThreadExecutor()

        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val providers = listOf(
                ComponentName(context, BillWidgetProvider::class.java),
                ComponentName(context, BillWidgetProviderLarge::class.java)
            )
            for (component in providers) {
                val ids = manager.getAppWidgetIds(component)
                if (ids.isNotEmpty()) {
                    val provider = if (component.className.contains("Large")) BillWidgetProviderLarge() else BillWidgetProvider()
                    val refreshIntent = Intent(context, provider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    provider.onReceive(context, refreshIntent)
                }
            }
        }
    }

    protected open fun getLayoutResId(): Int = R.layout.widget_bill_4x2

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        executor.execute {
            try {
                val snapshot = BillWidgetDataLoader.load(context)
                for (widgetId in appWidgetIds) {
                    updateWidget(context, appWidgetManager, widgetId, snapshot)
                }
            } catch (e: Exception) {
                android.util.Log.e("BillWidgetProvider", "Failed to update widget", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_OPEN_APP -> openApp(context)
            ACTION_ADD -> openAddRecord(context)
        }
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        snapshot: BillWidgetSnapshot
    ) {
        val layoutId = getLayoutResId()
        val views = RemoteViews(context.packageName, layoutId)

        views.setTextViewText(R.id.widget_month_amount, snapshot.monthExpenseText)
        views.setTextViewText(R.id.widget_today_amount, snapshot.todayExpenseText)
        views.setTextViewText(R.id.widget_today_count, snapshot.todayCountText)
        views.setTextViewText(R.id.widget_engel, snapshot.engelText)

        if (layoutId == R.layout.widget_bill_4x3) {
            val serviceIntent = Intent(context, BillWidgetViewsService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
            views.setRemoteAdapter(R.id.widget_recent_list, serviceIntent)
            views.setEmptyView(R.id.widget_recent_list, R.id.widget_empty)
            views.setViewVisibility(R.id.widget_recent_list, if (snapshot.recentItems.isEmpty()) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.widget_empty, if (snapshot.recentItems.isEmpty()) View.VISIBLE else View.GONE)
        }

        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent(context))
        views.setOnClickPendingIntent(R.id.widget_btn_add, addPendingIntent(context))

        manager.updateAppWidget(widgetId, views)
        if (layoutId == R.layout.widget_bill_4x3) {
            manager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_recent_list)
        }
    }

    private fun openApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    private fun openAddRecord(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(EXTRA_ADD_RECORD, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    private fun openAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, BillWidgetProvider::class.java).apply {
            action = ACTION_OPEN_APP
            setPackage(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun addPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, BillWidgetProvider::class.java).apply {
            action = ACTION_ADD
            setPackage(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class BillWidgetProviderLarge : BillWidgetProvider() {
    override fun getLayoutResId(): Int = R.layout.widget_bill_4x3
}

