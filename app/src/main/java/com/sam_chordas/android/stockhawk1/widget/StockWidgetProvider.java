package com.sam_chordas.android.stockhawk1.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk1.R;
import com.sam_chordas.android.stockhawk1.ui.StockDetailActivity;
import com.sam_chordas.android.stockhawk1.ui.StockDetailFragment;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    //variables to pass data along with broadcast
    public static final String INTENT_ACTION = "com.sam_chordas.android.stockhawk1.widget.StockWidgetProvider.INTENT_ACTION";
    public static final String EXTRA_SYMBOL = "com.sam_chordas.android.stockhawk1.widget.StockWidgetProvider.EXTRA_SYMBOL";

    private static final String TAG = StockWidgetProvider.class.getSimpleName();


    // /method called on item touch from widget
    @Override
    public void onReceive(Context context, Intent intent) {

        AppWidgetManager manager = AppWidgetManager.getInstance(context);


        if(intent.getAction().equals(INTENT_ACTION)){

            String symbol = intent.getStringExtra(EXTRA_SYMBOL);

            Intent showHistoricalData = new Intent(context, StockDetailActivity.class);
            showHistoricalData.putExtra(StockDetailFragment.ARG_SYMBOL, symbol);
            showHistoricalData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(showHistoricalData);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            rv.setRemoteAdapter(R.id.stock_widget,intent);
            rv.setEmptyView(R.id.stock_widget,R.id.empty_widget);

            Intent symbol = new Intent(context, StockWidgetProvider.class);
            symbol.setAction(StockWidgetProvider.INTENT_ACTION);
            symbol.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, symbol,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.stock_widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);



    }
}

