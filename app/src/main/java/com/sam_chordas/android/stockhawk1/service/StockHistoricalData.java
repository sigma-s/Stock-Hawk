package com.sam_chordas.android.stockhawk1.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk1.data.QuoteProvider;
import com.sam_chordas.android.stockhawk1.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.sam_chordas.android.stockhawk1.service.StockIntentService.EXTRA_SYMBOL;
import static com.sam_chordas.android.stockhawk1.service.StockTaskService.setStockStatus;

public class StockHistoricalData {
    private String LOG_TAG = StockHistoricalData.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    public StockHistoricalData() {
    }

    public StockHistoricalData(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public int onRunTask(Bundle params, Context context) {

        mContext = context;
        // Load historic stock data
        String stockInput = params.getString(EXTRA_SYMBOL);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date currentDate = new Date();

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(currentDate);
        calEnd.add(Calendar.DATE, 0);

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(currentDate);
        calStart.add(Calendar.MONTH, -1);

        String startDate = dateFormat.format(calStart.getTime());
        String endDate = dateFormat.format(calEnd.getTime());

        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol " + "in (","UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\""+stockInput+"\")", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\"", "UTF-8"));
            // finalize the URL for the API query.
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                    + "org%2Falltableswithkeys&callback=");

        } catch (Exception e) {
            e.printStackTrace();
            setStockStatus(mContext, StockTaskService.STATUS_UNKNOWN);
        }


        String urlString;
        String getResponse;
        int result = 0;

            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                result = 1;
                try {
                    ContentValues contentValues = new ContentValues();
                    ArrayList<ContentProviderOperation> batchOperations = Utils.quoteHistoricalJsonToContentVals(getResponse);
                    if (batchOperations != null && batchOperations.size() != 0) {
                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                            batchOperations);
                    } else {
                        Intent intent = new Intent();
                        intent.setAction("com.sam_chordas.android.stockhawk1.ui.MyStocksActivity.STOCK_NOT_FOUND");
                        mContext.sendBroadcast(intent);
                    }
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                    setStockStatus(mContext, StockTaskService.STATUS_ERROR_JSON);
                }catch (JSONException e) {
                    e.printStackTrace();
                    setStockStatus(mContext, StockTaskService.STATUS_ERROR_JSON);
                }
            }catch (IOException e) {
                e.printStackTrace();
                setStockStatus(mContext, StockTaskService.STATUS_SERVER_DOWN);
        }

        return result;
    }
}
