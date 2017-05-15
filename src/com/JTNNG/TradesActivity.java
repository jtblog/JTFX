package com.JTNNG;

import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.fxcore2.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class TradesActivity extends Activity implements IO2GTableListener, IO2GEachRowListener, OnClickListener
{

	@Override
	public void onClick(View p1)
	{
		// TODO: Implement this method
		if(p1 instanceof TableRow){
			O2GTradeTableRow tradeRow = (O2GTradeTableRow)p1.getTag();
			String mTradeID = tradeRow.getTradeID();
			String mOfferId = tradeRow.getOfferID();
			int mAmount = tradeRow.getAmount();
			String mBuySell = tradeRow.getBuySell();
			
			//Close trade
			O2GRequestFactory factory = SharedObjects.getInstance().getSession().getRequestFactory();
			O2GValueMap vmap = factory.createValueMap();
			vmap.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
			vmap.setString(O2GRequestParamsEnum.ORDER_TYPE, "CM");
			vmap.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
			vmap.setString(O2GRequestParamsEnum.OFFER_ID, mOfferId);
			vmap.setString(O2GRequestParamsEnum.TRADE_ID, mTradeID);
			vmap.setInt(O2GRequestParamsEnum.AMOUNT, mAmount);
			if(mBuySell.equalsIgnoreCase("B")){
				vmap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
			}else{
				vmap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
			}
			O2GRequest request = factory.createOrderRequest(vmap);
			SharedObjects.getInstance().getSession().sendRequest(request);
		}
	}

    private final int STANDARD_MARGIN = 5;
    private Handler mHandler = new Handler();
    private TableLayout mTableLayout;
    private DecimalFormat[] mDecimalFormats = null;
    private Map<String, TableRow> mTradeTableRows;
    private O2GTradesTable mTradesTable;
    private O2GOffersTable mOffersTable;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trades);
        mTableLayout = (TableLayout) findViewById(R.id.tablelayout);
        mTradeTableRows = new HashMap<String, TableRow>();
getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        initializePredefinedDecimalFormats();
        initializeOffersTable();
        initializeTradesTable();
    }

    private void initializeOffersTable() {
        mOffersTable = (O2GOffersTable) SharedObjects.getInstance()
                .getSession().getTableManager().getTable(O2GTableType.OFFERS);
    }

    private void initializePredefinedDecimalFormats() {
        mDecimalFormats = new DecimalFormat[10];
        
        for (int i = 0; i < mDecimalFormats.length; i++) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setDecimalSeparatorAlwaysShown(true);
            decimalFormat.setMaximumFractionDigits(i);
            decimalFormat.setMinimumFractionDigits(i);
            mDecimalFormats[i] = decimalFormat;
        }
    }
    
    private void initializeTradesTable() {
        mTradesTable = (O2GTradesTable) SharedObjects.getInstance()
                .getSession().getTableManager().getTable(O2GTableType.TRADES);
        mTradesTable.forEachRow(this);
        mTradesTable.subscribeUpdate(O2GTableUpdateType.INSERT, this);
        mTradesTable.subscribeUpdate(O2GTableUpdateType.UPDATE, this);
        mTradesTable.subscribeUpdate(O2GTableUpdateType.DELETE, this);
    }

    @Override
    public void onAdded(String rowID, O2GRow rowData) {
        final O2GTradeTableRow row = (O2GTradeTableRow)rowData;
		
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/jtfx2.txt");
		try
		{
			FileOutputStream FOS = new FileOutputStream(file);
			OutputStreamWriter OSW = new OutputStreamWriter(FOS);
			OSW.append(row.getOpenOrderReqID());
			OSW.close();
			FOS.close();
		}
		catch (FileNotFoundException e)
		{}catch(IOException ex){}
		
        mHandler.post(new Runnable() {
            
            @Override
            public void run() {
					addTrade(row);
            }
        });
    }

    @Override
    public void onChanged(String rowID, O2GRow rowData) {
        UpdateTradesRunnable runnable = new UpdateTradesRunnable(
                (O2GTradeTableRow) rowData);
        mHandler.post(runnable);
    }

    @Override
    public void onDeleted(String rowID, O2GRow rowData) {
        final String sTradeID = rowID;
        mHandler.post(new Runnable() {
            
            @Override
            public void run() {
                TableRow tableRow = mTradeTableRows.remove(sTradeID);
                if (tableRow != null) {
                    mTableLayout.removeView(tableRow);
                }
            }
        });
    }

    @Override
    public void onStatusChanged(O2GTableStatus status) {
    }

    @Override
    public void onEachRow(String rowID, O2GRow rowData) {
        final O2GTradeTableRow row = (O2GTradeTableRow) rowData;
        addTrade(row);
		}
    

    private void addTrade(O2GTradeTableRow row) {
        TableRow tableRow = new TableRow(this);
		tableRow.setTag(row);
		tableRow.setOnClickListener(this);

        TextView tvTradeID = new TextView(this);
        tvTradeID.setPadding(0, STANDARD_MARGIN, 0, STANDARD_MARGIN);
        tvTradeID.setText(row.getTradeID());
        tvTradeID.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvTradeID);

        TextView tvAccountID = new TextView(this);
        tvAccountID.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvAccountID.setText(row.getAccountID());
        tvAccountID.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvAccountID);
        
        String sOfferID = row.getOfferID();
        O2GOfferTableRow offerRow = mOffersTable.findRow(sOfferID);
        
        TextView tvTradeSymbol = new TextView(this);
        tvTradeSymbol.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvTradeSymbol.setText(offerRow.getInstrument());
        tvTradeSymbol.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvTradeSymbol);    
        
        TextView tvTradeAmount = new TextView(this);
        tvTradeAmount.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvTradeAmount.setText(Integer.toString(row.getAmount()));
        tvTradeAmount.setGravity(Gravity.RIGHT);
        tvTradeAmount.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvTradeAmount);    
        
        TextView tvTradeSide = new TextView(this);
        tvTradeSide.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvTradeSide.setText(row.getBuySell());
        tvTradeSide.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvTradeSide);  
        
        DecimalFormat offerDecimalFormat = mDecimalFormats[offerRow.getDigits()];
        
        TextView tvOpenPrice = new TextView(this);
        tvOpenPrice.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvOpenPrice.setText(offerDecimalFormat.format(row.getOpenRate()));
        tvOpenPrice.setGravity(Gravity.RIGHT);
        tvOpenPrice.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvOpenPrice);
        
        TextView tvClosePrice = new TextView(this);
        tvClosePrice.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvClosePrice.setText(offerDecimalFormat.format(row.getClose()));
        tvClosePrice.setGravity(Gravity.RIGHT);
        tvClosePrice.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvClosePrice); 
        
        TextView tvPL = new TextView(this);
        tvPL.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvPL.setText(mDecimalFormats[1].format(row.getPL()));
        tvPL.setGravity(Gravity.RIGHT);
        tvPL.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvPL); 
        
        TextView tvGrossPL = new TextView(this);
        tvGrossPL.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvGrossPL.setText(mDecimalFormats[2].format(row.getGrossPL()));
        tvGrossPL.setGravity(Gravity.RIGHT);
        tvGrossPL.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvGrossPL);  
		
		TextView  tvStrategy = new TextView(this);
		tvStrategy.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
		tvStrategy.setText("B");
        tvStrategy.setGravity(Gravity.RIGHT);
        tvStrategy.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvStrategy);
		
		
        mTradeTableRows.put(row.getTradeID(), tableRow);
       	mTableLayout.addView(tableRow, new TableLayout.LayoutParams(
										 TableLayout.LayoutParams.FILL_PARENT,
										 TableLayout.LayoutParams.WRAP_CONTENT));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTradesTable.unsubscribeUpdate(O2GTableUpdateType.INSERT, this);
        mTradesTable.unsubscribeUpdate(O2GTableUpdateType.UPDATE, this);
        mTradesTable.unsubscribeUpdate(O2GTableUpdateType.DELETE, this);
    }

    private class UpdateTradesRunnable implements Runnable {

        private String mTradeID;
        private double mClose;
        private double mPL;
        private double mGrossPL;
        private int mDigits;
		private String mOfferId;
		private int mAmount;
		private String mBuySell;
        
        public UpdateTradesRunnable(O2GTradeTableRow tradeRow) {
            mTradeID = tradeRow.getTradeID();
            mClose = tradeRow.getClose();
            mPL = tradeRow.getPL();
            mGrossPL = tradeRow.getGrossPL();
            mDigits = mOffersTable.findRow(tradeRow.getOfferID()).getDigits();
			mOfferId = tradeRow.getOfferID();
			mAmount = tradeRow.getAmount();
			mBuySell = tradeRow.getBuySell();
        }

        public void run() {
            TableRow tableRow = mTradeTableRows.get(mTradeID);
			
            if (tableRow == null) {
                return;
            }

            DecimalFormat offerDecimalFormat = mDecimalFormats[mDigits];
            
            ((TextView) tableRow.getChildAt(6)).setText(offerDecimalFormat.format(mClose));
            ((TextView) tableRow.getChildAt(7)).setText(mDecimalFormats[1].format(mPL));
            ((TextView) tableRow.getChildAt(8)).setText(mDecimalFormats[2].format(mGrossPL));
			
			
			//Automated close
			
			if(((TextView) tableRow.getChildAt(9)).getText() == "A"){
			if(mPL < 2 || mGrossPL < 2){
				//Close trade
				O2GRequestFactory factory = SharedObjects.getInstance().getSession().getRequestFactory();
				O2GValueMap vmap = factory.createValueMap();
				vmap.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
				vmap.setString(O2GRequestParamsEnum.ORDER_TYPE, "CM");
				vmap.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
				vmap.setString(O2GRequestParamsEnum.OFFER_ID, mOfferId);
				vmap.setString(O2GRequestParamsEnum.TRADE_ID, mTradeID);
				vmap.setInt(O2GRequestParamsEnum.AMOUNT, mAmount);
				if(mBuySell.equalsIgnoreCase("B")){
					vmap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
				}else{
					vmap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
				}
				O2GRequest request = factory.createOrderRequest(vmap);
				SharedObjects.getInstance().getSession().sendRequest(request);
			}
			}
			
			if(((TextView) tableRow.getChildAt(9)).getText() == "B"){
				//Close trade
				O2GRequestFactory factory = SharedObjects.getInstance().getSession().getRequestFactory();
				O2GValueMap vmap = factory.createValueMap();
				vmap.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
				vmap.setString(O2GRequestParamsEnum.ORDER_TYPE, "CM");
				vmap.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
				vmap.setString(O2GRequestParamsEnum.OFFER_ID, mOfferId);
				vmap.setString(O2GRequestParamsEnum.TRADE_ID, mTradeID);
				vmap.setInt(O2GRequestParamsEnum.AMOUNT, mAmount);
				if(mBuySell.equalsIgnoreCase("B")){
					vmap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
				}else{
					vmap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
				}
				O2GRequest request = factory.createOrderRequest(vmap);
				SharedObjects.getInstance().getSession().sendRequest(request);
			}
			
			if(((TextView) tableRow.getChildAt(9)).getText() == "-"){
				//Close trade
				O2GRequestFactory factory = SharedObjects.getInstance().getSession().getRequestFactory();
				O2GValueMap vmap = factory.createValueMap();
				vmap.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
				vmap.setString(O2GRequestParamsEnum.ORDER_TYPE, "CM");
				vmap.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
				vmap.setString(O2GRequestParamsEnum.OFFER_ID, mOfferId);
				vmap.setString(O2GRequestParamsEnum.TRADE_ID, mTradeID);
				vmap.setInt(O2GRequestParamsEnum.AMOUNT, mAmount);
				if(mBuySell.equalsIgnoreCase("B")){
					vmap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
				}else{
					vmap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
				}
				O2GRequest request = factory.createOrderRequest(vmap);
				SharedObjects.getInstance().getSession().sendRequest(request);
			}
        }
    }

}