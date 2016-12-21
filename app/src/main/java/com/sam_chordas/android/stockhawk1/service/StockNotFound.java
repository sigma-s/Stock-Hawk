package com.sam_chordas.android.stockhawk1.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk1.R;

public class StockNotFound extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.stock_not_found),Toast.LENGTH_SHORT).show();
    }
}
