package com.JTNNG;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.fxcore2.*;
import java.math.*;
import java.text.*;
import java.util.*;

public class OffersActivity extends Activity implements OnClickListener
{

	public static Bundle action;
	public static ArrayDeque Time = new ArrayDeque(60);

    public final int STANDARD_MARGIN = 5;
    public final int COLOR_UP = Color.rgb(99, 255, 99);
    public final int COLOR_DOWN = Color.rgb(255, 99, 99);
    public Handler mHandler = new Handler();
    public static TableLayout mTableLayout;
    public Map<String, TableRow> mOfferTableRows;
	public Map<String, Pair<BigDecimal, BigDecimal>> mActualPrices;
    public DecimalFormat[] mDecimalFormats = null;
    public O2GOffersTable mOffersTable;
	public O2GSession mSession;
	List<O2GOfferTableRow> offerRows;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offers);
		mActualPrices = Collections.synchronizedMap(new HashMap<String, Pair<BigDecimal, BigDecimal>>());
		mSession = SharedObjects.getInstance().getSession();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		SharedObjects.getInstance().setActualPrices(mActualPrices);
        mTableLayout = (TableLayout) findViewById(R.id.tablelayout);
        mOfferTableRows = new HashMap<String, TableRow>();
		
        initializePredefinedDecimalFormats();
        initializeOffersTable();
		
    }

	@Override
	protected void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		registerReceiver(BCR, new IntentFilter(TablesUpdateService.BROADCAST_ACTION));
	}
	
	@Override
	public void onClick(View p1)
	{
		// TODO: Implement this method
		if(p1 instanceof TableRow){
			O2GOfferRow offerRow = (O2GOfferRow) p1.getTag();
			SharedObjects.getInstance().setSelectedOffer(offerRow);
			Intent intent = new Intent(this, CreateOrderActivity.class);
			startActivity(intent); 
		}
	}
	
	public BroadcastReceiver BCR = new BroadcastReceiver(){
		@Override
		public void onReceive(Context p1, Intent p2)
		{
			// TODO: Implement this method
			Update(p2);
		}
	};
	
    public void initializePredefinedDecimalFormats() {
        mDecimalFormats = new DecimalFormat[10];

        for (int i = 0; i < 10; i++) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setDecimalSeparatorAlwaysShown(false);
            decimalFormat.setMaximumFractionDigits(i);
            decimalFormat.setMinimumFractionDigits(i);
            mDecimalFormats[i] = decimalFormat;
        }
    }

	public void Update(Intent p2){
		if(p2.hasExtra("OfferTableRow")){
			UpdateOffersRunnable runnable = new UpdateOffersRunnable(p2.getBundleExtra("OfferTableRow"));
			mHandler.post(runnable);
		}
	}
	
    public void initializeOffersTable() {
        mOffersTable = (O2GOffersTable) mSession.getTableManager().getTable(
			O2GTableType.OFFERS);

        int iSize = mOffersTable.size();
        offerRows = new ArrayList<O2GOfferTableRow>();
		
        for (int i = 0; i < iSize; i++) {
            offerRows.add(mOffersTable.getRow(i));
        }

        Collections.sort(offerRows, new Comparator<O2GOfferTableRow>() {

				@Override
				public int compare(O2GOfferTableRow r1, O2GOfferTableRow r2) {
					Integer iOfferID1 = Integer.parseInt(r1.getOfferID());
					Integer iOfferID2 = Integer.parseInt(r2.getOfferID());
					return iOfferID1.compareTo(iOfferID2);
				}
			}
        );

        for (int i = 0; i < iSize; i++) {
            this.initializeTableRowView(offerRows.get(i));
        }

    }

    public void initializeTableRowView(O2GOfferTableRow row) {
        TableRow tableRow = new TableRow(this);
		tableRow.setTag(row);
		tableRow.setOnClickListener(this);

        TextView tvSymbol = new TextView(this);
        tvSymbol.setPadding(0, STANDARD_MARGIN, 0, STANDARD_MARGIN);
        tvSymbol.setText(row.getInstrument());
        tvSymbol.setLayoutParams(new TableRow.LayoutParams());

        tableRow.addView(tvSymbol);

        int iDigits = row.getDigits();
        DecimalFormat decimalFormat = mDecimalFormats[iDigits];

        double dBid = row.getBid();
        TextView tvBid = new TextView(this);
        tvBid.setText(decimalFormat.format(dBid));
        tvBid.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvBid.setGravity(Gravity.RIGHT);
        tvBid.setLayoutParams(new TableRow.LayoutParams());
        tvBid.setTag(dBid);
        tableRow.addView(tvBid);

        double dAsk = row.getAsk();
        TextView tvAsk = new TextView(this);
        tvAsk.setText(decimalFormat.format(dAsk));
        tvAsk.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvAsk.setGravity(Gravity.RIGHT);
        tvAsk.setLayoutParams(new TableRow.LayoutParams());
        tvAsk.setTag(dAsk);
        tableRow.addView(tvAsk);
		mActualPrices.put(row.getOfferID(), new Pair<BigDecimal, BigDecimal>(new BigDecimal(dBid), new BigDecimal(dAsk)));

        double dSpread = OffersActivity.calculateSpread(dBid, dAsk,
														row.getPointSize());
        TextView tvSpread = new TextView(this);
        tvSpread.setText(mDecimalFormats[1].format(dSpread));
        tvSpread.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvSpread.setGravity(Gravity.RIGHT);
        tvSpread.setLayoutParams(new TableRow.LayoutParams());
        tvSpread.setTag(dSpread);
        tableRow.addView(tvSpread);

        TextView tvPipCost = new TextView(this);
        tvPipCost.setText(mDecimalFormats[2].format(row.getPipCost()));
        tvPipCost.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvPipCost.setGravity(Gravity.RIGHT);
        tvPipCost.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvPipCost);

        TextView tvHigh = new TextView(this);
        tvHigh.setText(decimalFormat.format(row.getHigh()));
        tvHigh.setTextColor(COLOR_UP);
        tvHigh.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvHigh.setGravity(Gravity.RIGHT);
        tvHigh.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvHigh);

        TextView tvLow = new TextView(this);
        tvLow.setText(decimalFormat.format(row.getLow()));
        tvLow.setTextColor(COLOR_DOWN);
        tvLow.setPadding(STANDARD_MARGIN, 0, STANDARD_MARGIN, 0);
        tvLow.setGravity(Gravity.RIGHT);
        tvLow.setLayoutParams(new TableRow.LayoutParams());
        tableRow.addView(tvLow);

        mOfferTableRows.put(row.getOfferID(), tableRow);

        mTableLayout.addView(tableRow, new TableLayout.LayoutParams(
								 TableLayout.LayoutParams.FILL_PARENT,
								 TableLayout.LayoutParams.WRAP_CONTENT));

    }

    public static double calculateSpread(double dBid, double dAsk,
										  double dPointSize) {
        return (dAsk - dBid) / dPointSize;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class UpdateOffersRunnable implements Runnable {

        private double mBid;
        private double mAsk;
        private double mPointSize;
        private double mHigh;
        private double mLow;
        private double mPipCost;
        private String mOfferID;
        private DecimalFormat mDecimalFormat = null;
		

        public UpdateOffersRunnable(Bundle bundle) {
            mBid = (Double) bundle.get("Bid");
            mAsk = (Double)bundle.get("Ask");
            mPointSize = (Double)bundle.get("PointSize");
            mHigh = (Double)bundle.get("High");
            mLow = (Double)bundle.get("Low");
            mPipCost = (Double)bundle.get("PipCost");
            mOfferID = (String)bundle.get("OfferID");
            mDecimalFormat = mDecimalFormats[(Integer)bundle.get("Digit")];
        }

        public void run() {

            TableRow tableRow = mOfferTableRows.get(mOfferID);

            if (tableRow == null) {
                return;
            }

            TextView tvBid = (TextView) tableRow.getChildAt(1);
            tvBid.setText(mDecimalFormat.format(mBid));
            this.setColor((Double) tvBid.getTag(), mBid, tvBid);
            tvBid.setTag(mBid);

            TextView tvAsk = (TextView) tableRow.getChildAt(2);
            tvAsk.setText(mDecimalFormat.format(mAsk));
            this.setColor((Double) tvAsk.getTag(), mAsk, tvAsk);
            tvAsk.setTag(mAsk);

			mActualPrices.put(mOfferID, new Pair<BigDecimal, BigDecimal>(new BigDecimal(mBid), new BigDecimal(mAsk)));

            double dSpread = OffersActivity.calculateSpread(mBid, mAsk,
															mPointSize);
            TextView tvSpread = (TextView) tableRow.getChildAt(3);
            tvSpread.setText(mDecimalFormats[1].format(dSpread));
            this.setColor((Double) tvSpread.getTag(), dSpread, tvSpread);
            tvSpread.setTag(dSpread);

            TextView tvPipCost = (TextView) tableRow.getChildAt(4);
            tvPipCost.setText(mDecimalFormats[2].format(mPipCost));

            TextView tvHigh = (TextView) tableRow.getChildAt(5);
            tvHigh.setText(mDecimalFormat.format(mHigh));

            TextView tvLow = (TextView) tableRow.getChildAt(6);
            tvLow.setText(mDecimalFormat.format(mLow));

			
			
			if(Time.size() < 60){
				Time.add(System.currentTimeMillis());
			}else{
				Time.removeFirst();
				Time.add(System.currentTimeMillis());
			}

			// Average elapsed time
			// (Long.parseLong(Time.getLast().toString()) - Long.parseLong(Time.getFirst().toString())) / Time.size();
        }



        private void setColor(double dOldValue, double dNewValue, TextView view) {
            int iResult = Double.compare(dNewValue, dOldValue);
            if (iResult == 0) {
                view.setTextColor(view.getTextColors().getDefaultColor());
            } else if (iResult > 0) {
                view.setTextColor(COLOR_UP);
            } else {
                view.setTextColor(COLOR_DOWN);
            }
        }
    }
}