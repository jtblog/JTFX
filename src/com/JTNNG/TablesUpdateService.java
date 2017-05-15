package com.JTNNG;

import android.app.*;
import android.content.*;
import android.os.*;
import com.fxcore2.*;
import android.widget.*;

public class TablesUpdateService extends Service
{

	public O2GSession mSession;
	public O2GOffersTable mOffersTable;
	public Handler mHandler = new Handler();
	public static String BROADCAST_ACTION = "com.JTNNG.TablesUpdate";
	public Bundle BDOffers;
	public Intent intent;
	

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