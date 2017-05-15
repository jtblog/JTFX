package com.JTNNG;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import com.fxcore2.*;
import java.io.*;
import java.util.*;
import java.text.*;
import android.text.format.*;

public class NeuralService extends Service
{

	public O2GSession mSession;
	public O2GOffersTable mOffersTable;
	public List<O2GOfferTableRow> offerRows = new ArrayList<O2GOfferTableRow>();
	//public List<String> OffersMktReqId = new ArrayList<String>();
	public Handler mHandler = new Handler();
	public List<List<Double>> Bids;
	public List<List<Double>> Asks;
	public Date dt;
	public List<List<Double>> bCCIs;
	public List<List<Double>> aCCIs;
	public List<Double> curBids;
	public List<Double> curAsks;
	public List<String> OfferIDs;
	public List<String> aOfferIDs;

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		mSession = SharedObjects.getInstance().getSession();
		if(mSession.getSessionStatus() == O2GSessionStatusCode.CONNECTED){
			mOffersTable = (O2GOffersTable) mSession.getTableManager().getTable(
				O2GTableType.OFFERS);
		 
			offerRows.clear();

			Bids = new ArrayList<List<Double>>();
			Asks = new ArrayList<List<Double>>();
			bCCIs = new ArrayList<List<Double>>();
			aCCIs = new ArrayList<List<Double>>();
			curBids = new ArrayList<Double>();
			curAsks = new ArrayList<Double>();
			OfferIDs = new ArrayList<String>();
			aOfferIDs = new ArrayList<String>();
			curBids.clear(); curAsks.clear();
			OfferIDs.clear();	aOfferIDs.clear();
			bCCIs.clear(); aCCIs.clear();
			Bids.clear(); Asks.clear();
			
			for (int i = 0; i < mOffersTable.size(); i++) {
				offerRows.add(mOffersTable.getRow(i));
				aOfferIDs.add(mOffersTable.getRow(i).getOfferID());
			}

			//OffersMktReqId.clear();
			//for (int i = 0; i < offerRows.size(); i++) {
				//O2GRequestFactory factory = mSession.getRequestFactory();
				//O2GTimeframeCollection timeFrames = factory.getTimeFrameCollection();
				//O2GTimeframe timeFrame = timeFrames.get("m5");

				//O2GRequest marketDataRequest = factory.createMarketDataSnapshotRequestInstrument(offerRows.get(i).getInstrument(), timeFrame, 100);
				//factory.fillMarketDataSnapshotRequestTime(marketDataRequest, null, null, true);
				//OffersMktReqId.add(marketDataRequest.getRequestId());
				//mSession.sendRequest(marketDataRequest);
			//}
		
			long delaytime = 0; long delaytime1 = 0;
			dt = new Date();
			int p = dt.getMinutes();
			int secs = dt.getSeconds();
			if(secs == 60 || secs == 00){
				delaytime1 = 0;
			}else{
				delaytime1 = (60 - secs)*1000;
			}
			if((p % 5) == 0){
				delaytime = 0;
			}else{
				delaytime = (p - (5 * ((int) p/5))) * 60000;
			}
			mHandler.postDelayed(new M5Runnable(), delaytime);
			mHandler.postDelayed(new M1Runnable(), delaytime1);
		}
		
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		// TODO: Implement this method
		File Bidsdir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + 
		 "/Android/data/" + getPackageName() + "/Offers/Bids");
		File Asksdir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + 
								"/Android/data/" + getPackageName() + "/Offers/Asks");
		Bidsdir.mkdirs();
		Asksdir.mkdirs();
		registerReceiver(BCR, new IntentFilter(TablesUpdateService.BROADCAST_ACTION));
	}

	public BroadcastReceiver BCR = new BroadcastReceiver(){

		@Override
		public void onReceive(Context p1, Intent p2)
		{
			// TODO: Implement this method
			mHandler.post(new TablesUpdateRunnable(p2));
		}
	};
	
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}
	
	public class ResponseListener implements IO2GResponseListener
	{

		@Override
		public void onRequestCompleted(String p1, O2GResponse p2)
		{
			// TODO: Implement this method
			mHandler.post(new ResponseRunnable(p2));
		}

		@Override
		public void onRequestFailed(String p1, String p2)
		{
			// TODO: Implement this method
		}

		@Override
		public void onTablesUpdates(O2GResponse p1)
		{
			// TODO: Implement this method
		}
	}
	
	public class M1Runnable implements Runnable
	{

		@Override
		public void run()
		{
			// TODO: Implement this method
		
			
				MyEngine(Bids, Asks);
				
				//WriteToFiles(Bids, Asks);
			
			mHandler.postDelayed(this, 60000);
		}

	}
	
	public void MyEngine(List<List<Double>> bids, List<List<Double>> asks)
	{
		// TODO: Implement this method
		for(int i = 0; i < OfferIDs.size(); i++){
			if(bids.get(i).size() == 26 || bids.get(i).size() > 26){
				double bSUM15 = 0;
				for(int j = bids.get(i).size() - 1; j>14; j--){
					bSUM15 = bSUM15 + bids.get(i).get(j);
				}
				double bAVG15 = bSUM15 / 15;
			
				double bSumDev15 = 0;
				//for(int j = 14; j<30; j++){
					//bSumDev15 = bSumDev15 + Math.abs(bids.get(i).get(j) - bAVG15);
				//}
				double bCurMeanD = bSumDev15 / 15;
				
				if(bCCIs.get(i).size() < 5){
					double mCCI = CCI(curBids.get(i), bAVG15, bCurMeanD, 15);
					bCCIs.get(i).add(mCCI);
				}else{
					bCCIs.get(i).remove(0);
					double mCCI = CCI(curBids.get(i), bAVG15, bCurMeanD, 15);
					bCCIs.get(i).add(mCCI);
				}
				
				Toast.makeText(getApplicationContext(), String.valueOf(bAVG15), Toast.LENGTH_SHORT).show();
				
			}else{
			
			}
		}
	}
	
	public void WriteToFiles(List<List<Double>> bids, List<List<Double>> asks)
	{
		for(int i = 0; i < mOffersTable.size(); i++){
		// TODO: Implement this method
		File bfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + 
							  "/Android/data/" + getPackageName() + "/Offers/Bids/" + mOffersTable.getRow(i).getOfferID() + ".csv");
		File afile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + 
							  "/Android/data/" + getPackageName() + "/Offers/Asks/" + mOffersTable.getRow(i).getOfferID() + ".csv");

		try
		{
			FileOutputStream bFOS = new FileOutputStream(bfile);
			FileOutputStream aFOS = new FileOutputStream(afile);
			OutputStreamWriter bOSW = new OutputStreamWriter(bFOS);
			OutputStreamWriter aOSW = new OutputStreamWriter(aFOS);
			for(int j= 0; j< bids.get(i).size(); j++){
				bOSW.append(bids.get(i).get(j) + "\n");
			}
			bOSW.close();
			bFOS.close();
			for(int j= 0; j< asks.get(i).size(); j++){
				aOSW.append(asks.get(i).get(j) + "\n");
			}
			aOSW.close();
			aFOS.close();
		}
		catch (FileNotFoundException e)
		{}catch(IOException ex){}
		}
	}
	
	public class M5Runnable implements Runnable
	{

		@Override
		public void run()
		{
			// TODO: Implement this method
			// 5 minutes updates
			
			mHandler.postDelayed(this, 300000);
		}
	}
	
	public class ResponseRunnable implements Runnable
	{

		O2GResponse p2;
		public ResponseRunnable(O2GResponse rsp){
			p2 = rsp;
		}
		
		@Override
		public void run()
		{
			// TODO: Implement this method
			
			if(p2.getType() == O2GResponseType.MARKET_DATA_SNAPSHOT){
				
				//if(OffersMktReqId.contains(p2.getRequestId())){
					//O2GResponseReaderFactory factory = mSession.getResponseReaderFactory();
					//O2GMarketDataSnapshotResponseReader marketSnapshotReader = factory.createMarketDataSnapshotReader(p2);
					//for (int i = 0; i < marketSnapshotReader.size(); i++) {
						//{
							//Toast.makeText(getApplicationContext(), String.valueOf(marketSnapshotReader.getNativePointer()) + "/n" + String.valueOf(p2.getNativePointer()), Toast.LENGTH_SHORT).show();
						//}
					//}

				//}
			}
		}
	}
	
	public double calculateEma(double currentPrice, double previousEma, int period){
		double weight = 2 / (period + 1);
		//if(previousEma == 0){
			//previousEma = calculateSMA(null);
		//}
		double EMA = (currentPrice * weight) + ((1 - weight) * previousEma);
		return EMA;
	}
	
	public double[] MACD (double EMA12, double EMA26){
		double MACDLine = EMA12 - EMA26;
		double Signal = 0;
		double Hist = MACDLine - Signal;
		double[] values = new double[]{MACDLine, Signal, Hist};
		return values;
	}
	
	public double CCI(double Price, double SMA, double MeanDv, int period){
		double value = (Price - SMA) / (0.015 * MeanDv);
		return value;
	}

	public double[] DonchianChannels(double[] Highs, double[] Lows){
		return null;
	}
	
	public class TablesUpdateRunnable implements Runnable
	{

		Intent intent;
		public TablesUpdateRunnable(Intent p2){
			intent = p2;
		}
		
		@Override
		public void run()
		{
			// TODO: Implement this method
			if(intent.hasExtra("OfferTableRow")){
				Bundle bundle = intent.getBundleExtra("OfferTableRow");
				double bid= (Double) bundle.get("Bid");
				double ask = (Double) bundle.get("Ask");
				String OfferID = (String) bundle.get("OfferID");
				
				if(!OfferIDs.contains(OfferID)){
					OfferIDs.add(OfferID);
					Bids.add(new ArrayList<Double>(30));
					Asks.add(new ArrayList<Double>(30));
					bCCIs.add(new ArrayList<Double>(5));
					aCCIs.add(new ArrayList<Double>(5));
					curBids.add(bid);
					curAsks.add(ask);
					
					while(Bids.get(OfferIDs.indexOf(OfferID)).size() < 30){
						Bids.get(OfferIDs.indexOf(OfferID)).add(mOffersTable.getRow(aOfferIDs.indexOf(OfferIDs.get(OfferIDs.indexOf(OfferID)))).getBid());
					}
					
					while(Asks.get(OfferIDs.indexOf(OfferID)).size() < 30){
						Asks.get(OfferIDs.indexOf(OfferID)).add(mOffersTable.getRow(aOfferIDs.indexOf(OfferIDs.get(OfferIDs.indexOf(OfferID)))).getAsk());
					}
				}else{
					try{
					curBids.set(OfferIDs.indexOf(OfferID), bid);
					curAsks.set(OfferIDs.indexOf(OfferID), ask);
					
					Bids.get(OfferIDs.indexOf(OfferID)).remove(0);
					Bids.get(OfferIDs.indexOf(OfferID)).add(bid);
					
					Asks.get(OfferIDs.indexOf(OfferID)).remove(0);
					Asks.get(OfferIDs.indexOf(OfferID)).add(ask);
					}catch(Exception e){
						
					}
				}
				
			}
		}
	}
	
}