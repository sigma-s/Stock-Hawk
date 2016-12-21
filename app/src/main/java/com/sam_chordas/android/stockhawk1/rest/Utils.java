package com.sam_chordas.android.stockhawk1.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import com.sam_chordas.android.stockhawk1.data.QuoteColumns;
import com.sam_chordas.android.stockhawk1.data.QuoteHistoricalColumns;
import com.sam_chordas.android.stockhawk1.data.QuoteProvider;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.sam_chordas.android.stockhawk1.R.id.change;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON) throws JSONException{
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    ContentProviderOperation cpo = null;

    jsonObject = new JSONObject(JSON);
    if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          cpo = buildBatchOperation(jsonObject);
          if(cpo!=null)
          batchOperations.add(cpo);
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              cpo = buildBatchOperation(jsonObject);
              if(cpo!=null){
              batchOperations.add(cpo);
              }
            }
          }
        }
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    try {
      bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    }catch(Exception e){
      e.printStackTrace();
      Log.d(LOG_TAG,"bidPrice for this stock is null instead of number");
    }
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) throws JSONException{
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    if (!jsonObject.getString("Change").equals("null") && !jsonObject.getString("Bid").equals("null")) {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.NAME,jsonObject.getString("Name"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } else{
      return null;
    }
    return builder.build();
  }

  public static ArrayList quoteHistoricalJsonToContentVals(String JSON) throws JSONException{
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    ContentProviderOperation cpo = null;

    jsonObject = new JSONObject(JSON);
    if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

        if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              cpo = buildHistoricalBatchOperation(jsonObject);
              if(cpo!=null) {
                batchOperations.add(buildHistoricalBatchOperation(jsonObject));
              }
            }


        }
    }

    return batchOperations;
  }

  public static ContentProviderOperation buildHistoricalBatchOperation(JSONObject jsonObject) throws JSONException{
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.QuotesHistoricData.CONTENT_URI);
    if(!jsonObject.getString("Open").equals("null")){

      builder.withValue(QuoteHistoricalColumns.SYMBOL, jsonObject.getString("Symbol"));
      builder.withValue(QuoteHistoricalColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Open")));
      builder.withValue(QuoteHistoricalColumns.DATE, jsonObject.getString("Date"));

    } else{
      return null;
    }
    return builder.build();
  }
}
