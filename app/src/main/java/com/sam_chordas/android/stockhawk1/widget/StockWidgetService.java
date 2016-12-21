package com.sam_chordas.android.stockhawk1.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk1.R;
import com.sam_chordas.android.stockhawk1.data.QuoteColumns;
import com.sam_chordas.android.stockhawk1.data.QuoteProvider;

public class StockWidgetService extends RemoteViewsService {
    public StockWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    public class StockRemoteViewsFactory implements RemoteViewsFactory {
        private Context context;
        private Cursor cursor;
        private int appWidgetId;

        public StockRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            cursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP,
                            QuoteColumns.NAME},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }

        @Override
        public void onDataSetChanged() {
            cursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP,
                            QuoteColumns.NAME},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }

        @Override
        public void onDestroy() {
            //close the cursor.
            if (this.cursor != null)
                this.cursor.close();
        }

        @Override
        public int getCount() {
            //Meta-function for the AppWidgetManager
            return (this.cursor != null) ? this.cursor.getCount() : 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(this.context.getPackageName(), R.layout.list_item_quote);

            if (this.cursor.moveToPosition(position)) {
                String symbol = cursor.getString(1);
                String bidPrice = cursor.getString(2);
                String change = cursor.getString(3);
                Integer isUp = cursor.getInt(4);
                String name=cursor.getString(5);

                rv.setTextViewText(R.id.stock_symbol, symbol);
                rv.setTextViewText(R.id.bid_price, bidPrice);
                rv.setTextViewText(R.id.change, change);

                if(isUp==1){
                    rv.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_green);
                } else {
                    rv.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);
                }

                Bundle extras = new Bundle();
                extras.putString(StockWidgetProvider.EXTRA_SYMBOL, symbol);

                Intent fillIntent = new Intent();
                fillIntent.putExtras(extras);
                rv.setOnClickFillInIntent(R.id.list_quote, fillIntent);

            }

            return rv;

        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            //we have only one type of view to display so returning 1.
            return 1;
        }

        @Override
        public long getItemId(int position) {
            //Return the data from the ID column of the table.
            return this.cursor.getInt(0);
        }

        @Override
        public boolean hasStableIds() {
            /**
             * As the table contains a column called ID,
             * whose value we are returning at getItemId(),
             * and also is a primary column,
             * every Id is unique and hence stable.
             */
            return true;
        }


    }

}
