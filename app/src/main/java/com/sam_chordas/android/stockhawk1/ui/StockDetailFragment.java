package com.sam_chordas.android.stockhawk1.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk1.R;
import com.sam_chordas.android.stockhawk1.data.QuoteColumns;
import com.sam_chordas.android.stockhawk1.data.QuoteHistoricalColumns;
import com.sam_chordas.android.stockhawk1.data.QuoteProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Goodbox on 17-12-2016.
 */

public class StockDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String LOG_TAG = StockDetailFragment.class.getSimpleName();
    public static final String ARG_SYMBOL = "ARG_SYMBOL";
    private static final int CURSOR_LOADER_ID = 1;
    private static final int CURSOR_LOADER_ID_FOR_LINE_CHART = 2;

    private String mSymbol;

    @Bind(R.id.stock_name)
    TextView mNameView;
    @Bind(R.id.stock_symbol)
    TextView mSymbolView;
    @Bind(R.id.stock_bidprice)
    TextView mBidPriceView;
    @Bind(R.id.stock_change)
    TextView mChange;
    @Bind(R.id.stock_chart)
    LineChartView mChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_SYMBOL)) {
            mSymbol = getArguments().getString(ARG_SYMBOL);
        }

        if (getActionBar() != null) {
            getActionBar().setElevation(0);
            if (getActivity() instanceof StockDetailActivity) {
                getActionBar().setTitle("");
            }
        }

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        getLoaderManager().initLoader(CURSOR_LOADER_ID_FOR_LINE_CHART, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stock_detail, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CURSOR_LOADER_ID) {
            return new CursorLoader(getContext(), QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP,
                            QuoteColumns.NAME},
                    QuoteColumns.SYMBOL + " = \"" + mSymbol + "\"",
                    null, null);
        } else if (id == CURSOR_LOADER_ID_FOR_LINE_CHART) {

            String sortOrder = QuoteColumns._ID + " ASC";
            return new CursorLoader(getContext(), QuoteProvider.QuotesHistoricData.CONTENT_URI,
                    new String[]{QuoteHistoricalColumns._ID, QuoteHistoricalColumns.SYMBOL,
                            QuoteHistoricalColumns.BIDPRICE, QuoteHistoricalColumns.DATE},
                    QuoteHistoricalColumns.SYMBOL + " = \"" + mSymbol + "\"",
                    null, sortOrder);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CURSOR_LOADER_ID && data != null && data.moveToFirst()) {

            String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
            mSymbolView.setText(getString(R.string.stock_detail_tab_header, symbol));

            String bidPrice = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
            mBidPriceView.setText(bidPrice);

            String name = data.getString(data.getColumnIndex(QuoteColumns.NAME));
            mNameView.setText(name);

            String change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));
            String percentChange = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
            String mixedChange = change + " (" + percentChange + ")";
            mChange.setText(mixedChange);

        } else if (loader.getId() == CURSOR_LOADER_ID_FOR_LINE_CHART && data != null &&
                data.moveToFirst()) {
            updateChart(data);
        } else if(loader.getId() == CURSOR_LOADER_ID_FOR_LINE_CHART && data == null){
            Log.d("TAG","PROBLEM IS HERE! IN THIS SECTION ! CHECK THIS OUT!");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do
    }

    @Nullable
    private ActionBar getActionBar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            return activity.getSupportActionBar();
        }
        return null;
    }

    private void updateChart(Cursor data) {

        List<AxisValue> axisValuesX = new ArrayList<>();
        List<PointValue> pointValues = new ArrayList<>();

        int counter = -1;
        do {
            counter++;

            String date = data.getString(data.getColumnIndex(
                    QuoteHistoricalColumns.DATE));
            String bidPrice = data.getString(data.getColumnIndex(
                    QuoteHistoricalColumns.BIDPRICE));

            // We have to show chart in right order.
            int x = data.getCount() - 1 - counter;

            // Point for line chart (date, price).
            PointValue pointValue = new PointValue(x, Float.valueOf(bidPrice));
            pointValue.setLabel(date);
            pointValues.add(pointValue);

            // Set labels for x-axis (we have to reduce its number to avoid overlapping text).
            if (counter != 0 && counter % (data.getCount() / 4) == 0) {
                AxisValue axisValueX = new AxisValue(x);
                axisValueX.setLabel(date);
                axisValuesX.add(axisValueX);
            }

        } while (data.moveToNext());

        // Prepare data for chart
        Line line = new Line(pointValues).setColor(Color.WHITE).setCubic(false);
        List<Line> lines = new ArrayList<>();
        lines.add(line);
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        // Init x-axis
        Axis axisX = new Axis(axisValuesX);
        //axisX.setAutoGenerated(true);
        axisX.setHasLines(true);
        axisX.setMaxLabelChars(4);
        lineChartData.setAxisXBottom(axisX);

        // Init y-axis
        Axis axisY = new Axis();
        axisY.setAutoGenerated(true);
        axisY.setHasLines(true);
        axisY.setMaxLabelChars(4);
        lineChartData.setAxisYLeft(axisY);

        // Update chart with new data.
        mChart.setInteractive(false);
        mChart.setLineChartData(lineChartData);

        // Show chart
        mChart.setVisibility(View.VISIBLE);
    }
}
