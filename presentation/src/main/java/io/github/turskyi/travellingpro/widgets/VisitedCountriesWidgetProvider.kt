package io.github.turskyi.travellingpro.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.os.BuildCompat
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.domain.models.entities.VisitedCountryModel
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.features.home.view.ui.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class VisitedCountriesWidgetProvider : AppWidgetProvider(), KoinComponent {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        @OptIn(BuildCompat.PrereleaseSdkCheck::class)
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_visited_countries)

            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            val appLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
            val localizedContext = if (!appLocales.isEmpty && appLocales[0] != null) {
                val config = android.content.res.Configuration(context.resources.configuration)
                config.setLocale(appLocales[0])
                context.createConfigurationContext(config)
            } else {
                context
            }
            views.setTextViewText(
                R.id.tv_widget_visited_label,
                localizedContext.getString(R.string.widget_visited_countries_title),
            )

            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            scope.launch {
                try {
                    val koinComponent = object : KoinComponent {}
                    val interactor: CountriesInteractor = koinComponent.get()
                    interactor.setVisitedCountries(
                        onSuccess = { countries: List<VisitedCountryModel> ->
                            val visitedCount = countries.size
                            views.setTextViewText(R.id.tv_widget_visited_count, visitedCount.toString())
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        },
                        onError = { _ ->
                            views.setTextViewText(R.id.tv_widget_visited_count, "0")
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                    )
                } catch (_: Exception) {
                    views.setTextViewText(R.id.tv_widget_visited_count, "0")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, VisitedCountriesWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun isPinWidgetSupported(context: Context): Boolean {
            return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) &&
                    AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
        }

        fun requestPinWidget(context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val myProvider = ComponentName(context, VisitedCountriesWidgetProvider::class.java)
                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                }
            }
        }
    }
}
