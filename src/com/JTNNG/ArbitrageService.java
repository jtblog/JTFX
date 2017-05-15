package com.JTNNG;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import com.fxcore2.*;
import java.util.*;

public class ArbitrageService extends Service
{

	public Handler mHandler = new Handler();
	public O2GSession mSession;
	public O2GTradesTable mTradesTable;
	public ArrayList<String> OfferInstruments;
	public O2GOffersTable mOffersTable;
	public ArrayList<String> TradeOfferIDs;
	public Service arbitrageService = this;

	@Override
	public void onStart(Intent intent, int startId)
	{
		// TODO: Implement this method
		mSession = SharedObjects.getInstance().getSession();
		//initializeTradesTable();
		//initializeOffersTable();
		//mHandler.post(new ArbitrageRunnable());
	}

	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}
	
	private void initializeOffersTable() {
        mOffersTable = (O2GOffersTable) mSession.getTableManager().getTable(
			O2GTableType.OFFERS);

        int iSize = mOffersTable.size();
        //offerRows = new ArrayList<O2GOfferTableRow>();
		OfferInstruments = new ArrayList<String>();

        for (int i = 0; i < iSize; i++) {
            //offerRows.add(mOffersTable.getRow(i));
			OfferInstruments.add(mOffersTable.getRow(i).getInstrument().toString());
        }
		
        mOffersTable.subscribeUpdate(O2GTableUpdateType.UPDATE, new TableChangedListener());
    }
	
	public void initializeTradesTable()
	{
		// TODO: Implement this method
		mTradesTable = (O2GTradesTable) SharedObjects.getInstance()
			.getSession().getTableManager().getTable(O2GTableType.TRADES);
        mTradesTable.subscribeUpdate(O2GTableUpdateType.INSERT, new TableChangedListener());
        mTradesTable.subscribeUpdate(O2GTableUpdateType.UPDATE, new TableChangedListener());
        mTradesTable.subscribeUpdate(O2GTableUpdateType.DELETE, new TableChangedListener());
	}
	
	public class ToastRunnable implements Runnable
	{
        String p1;
		public ToastRunnable(String p1){
			p1 = p1;
		}
		
		@Override
		public void run()
		{
			// TODO: Implement this method
			Toast.makeText(arbitrageService, p1, Toast.LENGTH_SHORT).show();
		}
	}
	
	public class TableChangedListener implements IO2GTableListener
	{

		@Override
		public void onAdded(String p1, O2GRow p2)
		{
			// TODO: Implement this method
		}

		@Override
		public void onChanged(String p1, O2GRow p2)
		{
			// TODO: Implement this method
			TradeOfferIDs.clear();
			for(int i=0; i<mTradesTable.size(); i++){
				if(p2.getTableType() == O2GTableType.TRADES){
					TradeOfferIDs.add(((O2GTradeTableRow)p2).getOfferID());
				}
			}
		}

		@Override
		public void onDeleted(String p1, O2GRow p2)
		{
			// TODO: Implement this method
		}

		@Override
		public void onStatusChanged(O2GTableStatus p1)
		{
			// TODO: Implement this method
		}
	}
	
	
	public class ArbitrageRunnable implements Runnable
	{

		//Start1
		public ArbitrageRunnable(){

		}

		@Override
		public void run()
		{
			// TODO: Implement this method
			//Forex Arbitrage Strategy (The triangle)
			
			double AccBal = ((O2GAccountsTable) SharedObjects.getInstance()
                .getSession().getTableManager().getTable(O2GTableType.ACCOUNTS)).getRow(0).getBalance();
			int EURUSDIndex = OfferInstruments.indexOf("EUR/USD");
			int EURGBPIndex = OfferInstruments.indexOf("EUR/GBP");
			int GBPUSDIndex = OfferInstruments.indexOf("GBP/USD");
			int EURJPYIndex = OfferInstruments.indexOf("EUR/JPY");
			int USDJPYIndex = OfferInstruments.indexOf("USD/JPY");
			int EURCHFIndex = OfferInstruments.indexOf("EUR/CHF");
			int USDCHFIndex = OfferInstruments.indexOf("USD/CHF");
			int AUDUSDIndex = OfferInstruments.indexOf("AUD/USD");
			int AUDJPYIndex = OfferInstruments.indexOf("AUD/JPY");

			// USD-EUR-GBP-USD

			if(!TradeOfferIDs.isEmpty() && (!TradeOfferIDs.contains(mOffersTable.getRow(EURUSDIndex).getOfferID()) || !TradeOfferIDs.contains(mOffersTable.getRow(EURGBPIndex).getOfferID()) || !TradeOfferIDs.contains(mOffersTable.getRow(GBPUSDIndex).getOfferID()))){
				if((((AccBal / mOffersTable.getRow(EURUSDIndex).getAsk()) * mOffersTable.getRow(EURGBPIndex).getBid()) * mOffersTable.getRow(GBPUSDIndex).getBid()) > AccBal){
					//Toast.makeText(getApplicationContext(), "Arbitrage: USD-EUR-GBP-USD", Toast.LENGTH_LONG).show();

					O2GValueMap valueMap10 = mSession.getRequestFactory().createValueMap();
					valueMap10.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
					valueMap10.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
					valueMap10.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap10.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(EURUSDIndex).getOfferID());
					valueMap10.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap10.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap10.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap10.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap10.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, -1);
					valueMap10.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, 1);
					valueMap10.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GValueMap valueMap11 = mSession.getRequestFactory().createValueMap();
					valueMap11.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap11.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
					valueMap11.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap11.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(EURGBPIndex).getOfferID());
					valueMap11.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap11.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap11.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap11.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap11.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, 1);
					valueMap11.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, -1);
					valueMap11.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GValueMap valueMap12 = mSession.getRequestFactory().createValueMap();
					valueMap12.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap12.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
					valueMap12.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap12.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(GBPUSDIndex).getOfferID());
					valueMap12.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap12.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap12.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap12.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap12.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, 1);
					valueMap12.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, -1);
					valueMap12.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GRequestFactory requestFactory1 = mSession.getRequestFactory();
					O2GRequest request10 = mSession.getRequestFactory().createOrderRequest(valueMap10);
					if (request10 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory1.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request10);
					}

					O2GRequest request11 = mSession.getRequestFactory().createOrderRequest(valueMap11);
					if (request11 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory1.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request11);
					}
					
					O2GRequest request12 = mSession.getRequestFactory().createOrderRequest(valueMap12);
					if (request12 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory1.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request12);
					}
					// END
				}
			}


			// USD-EUR-JPY-USD

			if(!TradeOfferIDs.isEmpty() && (!TradeOfferIDs.contains(mOffersTable.getRow(EURUSDIndex).getOfferID()) || !TradeOfferIDs.contains(mOffersTable.getRow(EURJPYIndex).getOfferID()) || !TradeOfferIDs.contains(mOffersTable.getRow(USDJPYIndex).getOfferID()))){
				if((((AccBal / mOffersTable.getRow(EURUSDIndex).getAsk()) * mOffersTable.getRow(EURJPYIndex).getBid()) / mOffersTable.getRow(USDJPYIndex).getAsk()) > AccBal){
					//Toast.makeText(getApplicationContext(), "Arbitrage: USD-EUR-JPY-USD", Toast.LENGTH_LONG).show();
					
					O2GValueMap valueMap20 = mSession.getRequestFactory().createValueMap();
					valueMap20.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap20.setString(O2GRequestParamsEnum.BUY_SELL, "B");
					valueMap20.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap20.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(EURUSDIndex).getOfferID());
					valueMap20.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap20.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap20.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap20.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap20.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, -1);
					valueMap20.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, 1);
					valueMap20.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GValueMap valueMap21 = mSession.getRequestFactory().createValueMap();
					valueMap21.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap21.setString(O2GRequestParamsEnum.BUY_SELL, "S");
					valueMap21.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap21.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(EURJPYIndex).getOfferID());
					valueMap21.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap21.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap21.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap21.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap21.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, 1);
					valueMap21.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, -1);
					valueMap21.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GValueMap valueMap22 = mSession.getRequestFactory().createValueMap();
					valueMap22.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap22.setString(O2GRequestParamsEnum.BUY_SELL, "B");
					valueMap22.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap22.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(USDJPYIndex).getOfferID());
					valueMap22.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap22.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap22.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap22.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap22.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, -1);
					valueMap22.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, 1);
					valueMap22.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GRequestFactory requestFactory2 = mSession.getRequestFactory();
					O2GRequest request20 = mSession.getRequestFactory().createOrderRequest(valueMap20);
					if (request20 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory2.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request20);
					}

					O2GRequest request21 = mSession.getRequestFactory().createOrderRequest(valueMap21);
					if (request21 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory2.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request21);
					}

					O2GRequest request22 = mSession.getRequestFactory().createOrderRequest(valueMap22);
					if (request22 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory2.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request22);
					}

					// END
				}
			}


			// USD-EUR-CHF-USD

			if(!TradeOfferIDs.isEmpty() && (!TradeOfferIDs.contains(mOffersTable.getRow(EURUSDIndex).getOfferID()) || !TradeOfferIDs.contains(mOffersTable.getRow(EURCHFIndex).getOfferID()) || !TradeOfferIDs.contains(mOffersTable.getRow(USDCHFIndex).getOfferID()))){
				if((((AccBal / mOffersTable.getRow(EURUSDIndex).getAsk()) * mOffersTable.getRow(EURCHFIndex).getBid()) / mOffersTable.getRow(USDCHFIndex).getAsk()) > AccBal){
					//Toast.makeText(getApplicationContext(), "Arbitrage: USD-EUR-CHF-USD", Toast.LENGTH_LONG).show();

					//Strategy2OrderIDs.clear();

					O2GValueMap valueMap30 = mSession.getRequestFactory().createValueMap();
					valueMap30.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap30.setString(O2GRequestParamsEnum.BUY_SELL, "B");
					valueMap30.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap30.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(EURUSDIndex).getOfferID());
					valueMap30.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap30.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap30.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap30.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap30.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, -1);
					valueMap30.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, 1);
					valueMap30.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GValueMap valueMap31 = mSession.getRequestFactory().createValueMap();
					valueMap31.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap31.setString(O2GRequestParamsEnum.BUY_SELL, "S");
					valueMap31.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap31.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(EURCHFIndex).getOfferID());
					valueMap31.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap31.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap31.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap31.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap31.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, 1);
					valueMap31.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, -1);
					valueMap31.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GValueMap valueMap32 = mSession.getRequestFactory().createValueMap();
					valueMap32.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap32.setString(O2GRequestParamsEnum.BUY_SELL, "B");
					valueMap32.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap32.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(USDCHFIndex).getOfferID());
					valueMap32.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap32.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap32.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap32.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap32.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, -1);
					valueMap32.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, 1);
					valueMap32.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GRequestFactory requestFactory3 = mSession.getRequestFactory();
					O2GRequest request30 = mSession.getRequestFactory().createOrderRequest(valueMap30);
					if (request30 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory2.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request30);
					}

					O2GRequest request31 = mSession.getRequestFactory().createOrderRequest(valueMap31);
					if (request31 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory2.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request31);
					}

					O2GRequest request32 = mSession.getRequestFactory().createOrderRequest(valueMap32);
					if (request32 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory2.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request32);
					}

					// END
				}
			}


			// USD-AUD-JPY-USD

			if(!TradeOfferIDs.isEmpty() && (!TradeOfferIDs.contains(mOffersTable.getRow(AUDUSDIndex).getOfferID()) || !TradeOfferIDs.contains(mOffersTable.getRow(AUDJPYIndex).getOfferID()) || !TradeOfferIDs.contains(mOffersTable.getRow(USDJPYIndex).getOfferID()))){
				if((((AccBal / mOffersTable.getRow(AUDUSDIndex).getAsk()) * mOffersTable.getRow(AUDJPYIndex).getBid()) / mOffersTable.getRow(USDJPYIndex).getAsk() > AccBal)){
					//Toast.makeText(getApplicationContext(), "Arbitrage: USD-AUD-JPY-USD", Toast.LENGTH_LONG).show();

					//Strategy2OrderIDs.clear();

					O2GValueMap valueMap40 = mSession.getRequestFactory().createValueMap();
					valueMap40.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap40.setString(O2GRequestParamsEnum.BUY_SELL, "B");
					valueMap40.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap40.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(AUDUSDIndex).getOfferID());
					valueMap40.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap40.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap40.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap40.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap40.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, -1);
					valueMap40.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, 1);
					valueMap40.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GValueMap valueMap41 = mSession.getRequestFactory().createValueMap();
					valueMap41.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap41.setString(O2GRequestParamsEnum.BUY_SELL, "S");
					valueMap41.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap41.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(AUDJPYIndex).getOfferID());
					valueMap41.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap41.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap41.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap41.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap41.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, 1);
					valueMap41.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, -1);
					valueMap41.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GValueMap valueMap42 = mSession.getRequestFactory().createValueMap();
					valueMap42.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
					valueMap42.setString(O2GRequestParamsEnum.BUY_SELL, "B");
					valueMap42.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
					valueMap42.setString(O2GRequestParamsEnum.OFFER_ID, mOffersTable.getRow(USDJPYIndex).getOfferID());
					valueMap42.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
					valueMap42.setInt(O2GRequestParamsEnum.AMOUNT, 1000);
					valueMap42.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
					valueMap42.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
					valueMap42.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, -1);
					valueMap42.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, 1);
					valueMap42.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");

					O2GRequestFactory requestFactory4 = mSession.getRequestFactory();
					O2GRequest request40 = mSession.getRequestFactory().createOrderRequest(valueMap40);
					if (request40 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory4.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request40);
					}

					O2GRequest request41 = mSession.getRequestFactory().createOrderRequest(valueMap41);
					if (request41 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory4.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request41);
					}

					O2GRequest request42 = mSession.getRequestFactory().createOrderRequest(valueMap42);
					if (request42 == null) {
						//Toast.makeText(getApplicationContext(), requestFactory4.getLastError(), Toast.LENGTH_SHORT).show();
					} else {
						mSession.sendRequest(request42);
					}

					// END
				}
			}

			mHandler.post(this);
		}
		//End1
	}

}