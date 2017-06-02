package com.JTNNG;

import android.app.*;
import android.content.*;
import android.os.*;
import com.fxcore2.*;
import android.widget.*;
import java.util.*;

public class TablesUpdateService extends Service
{

	public O2GSession mSession;
	public O2GOffersTable mOffersTable;
	public Handler mHandler = new Handler();
	public static String BROADCAST_ACTION = "com.JTNNG.TablesUpdate";
	public Bundle BDOffers;
	public Intent intent;
	public List<currencypair> cp;
	public List<String> checker;
	

	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		// TODO: Implement this method
		super.onStart(intent, startId);
		mSession = SharedObjects.getInstance().getSession();
		
		cp = new ArrayList<currencypair>();
		checker = new ArrayList<String>();
		
		if(mSession.getSessionStatus() == O2GSessionStatusCode.CONNECTED){
			mOffersTable = (O2GOffersTable) mSession.getTableManager().getTable(
				O2GTableType.OFFERS);
			mOffersTable.subscribeUpdate(O2GTableUpdateType.UPDATE, new TablesUpdateListener());
		}
	}
	
	public class TablesUpdateListener implements IO2GTableListener
	{

		@Override
		public void onAdded(String p1, O2GRow p2)
		{
			// TODO: Implement this method
		}

		@Override
		public void onChanged(String p1, O2GRow p2)
		{
			if(p2.getTableType() == O2GTableType.OFFERS){
			mHandler.post(new OffersTableRowChangeRunnable(p1, p2));
			}
			// TODO: Implement this method
			
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
	
	public class OffersTableRowChangeRunnable implements Runnable
	{
		public O2GOfferTableRow OfferRow;
		public OffersTableRowChangeRunnable(String p1, O2GRow p2){
			OfferRow = (O2GOfferTableRow)p2;
		}
		@Override
		public void run()
		{
			// TODO: Implement this method
			if(!(checker.contains(OfferRow.getOfferID()))){
				cp.add(new currencypair(OfferRow));
				checker.add(OfferRow.getOfferID());
			}else{
				for(int i=0; i<cp.size(); i++){
					if(cp.get(i).getSymbol().equalsIgnoreCase(OfferRow.getInstrument())){
						cp.get(i).getInstance().setContext(getApplicationContext());
						if(cp.get(i).getInstance().update(OfferRow) == true){
							double[] data1 = cp.get(i).getInstance().getVolumes();
							String s = " | ";
							for(double d : data1){
								s += String.valueOf(d) + " | ";
							}
							
							Toast.makeText(getApplicationContext(),String.valueOf(OfferRow.getTime().get(GregorianCalendar.SECOND)) + " | " + cp.get(i).getInstance().getSymbol() + s, Toast.LENGTH_SHORT).show();
							
							//Toast.makeText(getApplicationContext(),String.valueOf(data1[data1.length-1]) + " = Buy: " + String.valueOf(cp.get(i).getInstance().RSI_Buy()) +" | Sell: "+ String.valueOf(cp.get(i).getInstance().RSI_Sell()), Toast.LENGTH_SHORT).show();
							
							mHandler.post(new Arbitrage(cp));
						}
						
					}else{
						//Toast.makeText(getApplicationContext(),cp.get(i).getInstance().getErrorMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			}
			
			BDOffers = new Bundle();
			intent = new Intent(BROADCAST_ACTION);
			
			BDOffers.clear();
			BDOffers.putDouble("Bid", OfferRow.getBid());
			BDOffers.putDouble("Ask", OfferRow.getAsk());
			BDOffers.putDouble("PointSize", OfferRow.getPointSize());
			BDOffers.putDouble("High", OfferRow.getHigh());
			BDOffers.putDouble("Low", OfferRow.getLow());
			BDOffers.putDouble("PipCost", OfferRow.getPipCost());
			BDOffers.putString("OfferID", OfferRow.getOfferID());
			BDOffers.putInt("Digit", OfferRow.getDigits());
			
			intent.putExtra("OfferTableRow", BDOffers);
			sendBroadcast(intent);
		}
	}

}