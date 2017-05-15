package com.JTNNG;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.CompoundButton.*;
import com.fxcore2.*;
import java.io.*;
import java.math.*;
import java.util.*;

public class CreateOrderActivity extends Activity implements OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener{
    private Map<String, Pair<BigDecimal, BigDecimal>> mActualPrices;
    private O2GSession mSession;
    private O2GOfferRow mOfferRow;
    public List<O2GOrderRow> Strategy2OrderRows;
    private RadioButton mRBBuy;
    private RadioButton mRBSell;
    
    private EditText mETAmount;
    private EditText mETRate;
    
    private SeekBar mSBAmount;
    private SeekBar mSBRate;
    
    private Spinner mSpinnerOrderType;
    
    private BigDecimal mCurrentRate;
	private Bundle action;
	private Button A;
	public EditText Test;
	public Handler mHandler;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_order);
        
		A =(Button) findViewById(R.id.btnCreateOrder);
		Test = (EditText) findViewById(R.id.test);
		
		mHandler = new Handler();
		
        Spinner spinner = (Spinner) findViewById(R.id.spinnerOrderType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.order_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
		
		Strategy2OrderRows = new ArrayList<O2GOrderRow>();
		Strategy2OrderRows.clear();
	
        mSession = SharedObjects.getInstance().getSession();
        mActualPrices = SharedObjects.getInstance().getActualPrices();
        mOfferRow = SharedObjects.getInstance().getSelectedOffer();
        
        mRBBuy = (RadioButton)findViewById(R.id.rbBuy);
        mRBBuy.setTag("B");
        
        mRBSell = (RadioButton)findViewById(R.id.rbSell);
        mRBSell.setTag("S");
        
        mRBBuy.setOnCheckedChangeListener(this);
        mRBSell.setOnCheckedChangeListener(this);
        
        TextView tvSymbol = (TextView)findViewById(R.id.tvSymbol);
        tvSymbol.setText(mOfferRow.getInstrument());
        
        mETAmount = (EditText)findViewById(R.id.etAmount);
        mETAmount.setText("100");
        
        mETRate = (EditText)findViewById(R.id.etRate);
        setRate(false);
        
        mSBAmount = (SeekBar)findViewById(R.id.sbAmount);
        mSBAmount.setOnSeekBarChangeListener(this);
        
        mSBRate = (SeekBar)findViewById(R.id.sbRate);
        mSBRate.setOnSeekBarChangeListener(this);
        
        mSpinnerOrderType = (Spinner)findViewById(R.id.spinnerOrderType);
		
		//Automation
			if(getIntent().hasExtra("action")){
				action = getIntent().getBundleExtra("action");
				//From Automation
				if(action.containsKey("B")){
					mRBBuy.setChecked(true);
					mRBSell.setChecked(false);
					A.performClick();
				}else if(action.containsKey("S")){
					mRBSell.setChecked(true);
					mRBBuy.setChecked(false);
					A.performClick();
				}
			}
			
    }

    private void setRate(boolean bBid) {
        Pair<BigDecimal, BigDecimal> pair = mActualPrices.get(mOfferRow.getOfferID()); 
        BigDecimal bdPrice = (bBid) ? pair.first : pair.second;
        mCurrentRate = bdPrice;
        mETRate.setText(mCurrentRate.toString());
    }
    
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        
            if (isChecked) {
                if ("B".equals(buttonView.getTag().toString())) {
                    mRBSell.setChecked(false);
                    setRate(false);
                }
                else {
                    mRBBuy.setChecked(false);
                    setRate(true);
                }
                mSBRate.setProgress(50);
            }
        }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        if (seekBar.equals(mSBAmount)) {
            int iAmount = (progress + 1) * 10;
            mETAmount.setText(Integer.toString(iAmount));
        }
        else if (seekBar.equals(mSBRate)) {
            int iProgress = (progress + 1) - 50;
            BigDecimal changedPrice = mCurrentRate.add(BigDecimal.valueOf(iProgress * mOfferRow.getPointSize()));
            changedPrice = changedPrice.setScale(mOfferRow.getDigits(), RoundingMode.HALF_UP);
            mETRate.setText(changedPrice.toString());
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }
    
    @Override
    public void onBackPressed() {
        this.finish();
    }
    
    public void createOrderHandler(View view) {
        O2GValueMap valueMap = mSession.getRequestFactory().createValueMap();
        valueMap.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
        
        boolean bBuy = mRBBuy.isChecked();
        valueMap.setString(O2GRequestParamsEnum.BUY_SELL, bBuy ? "B" : "S");
        
        String sSelectedOrderType = mSpinnerOrderType.getSelectedItem().toString();
        boolean bMarketOrder = sSelectedOrderType.contains("Market");
        if (bMarketOrder) {
            valueMap.setString(O2GRequestParamsEnum.ORDER_TYPE, "OM");
        }
        else {
            double dRate = Double.parseDouble(mETRate.getText().toString());
            valueMap.setDouble(O2GRequestParamsEnum.RATE, dRate);
            String sLimitOrderType = "";
            if (bBuy) {
                double dAsk = mActualPrices.get(mOfferRow.getOfferID()).second.doubleValue();
                sLimitOrderType = (dRate > dAsk) ? "SE" : "LE";
            }
            else {
                double dBid = mActualPrices.get(mOfferRow.getOfferID()).second.doubleValue();
                sLimitOrderType = (dRate < dBid) ? "SE" : "LE";
            }
            valueMap.setString(O2GRequestParamsEnum.ORDER_TYPE, sLimitOrderType);
        }
        
        valueMap.setString(O2GRequestParamsEnum.OFFER_ID, mOfferRow.getOfferID());
        valueMap.setString(O2GRequestParamsEnum.ACCOUNT_ID, SharedObjects.getInstance().getAccountID());
        int iAmountK = Integer.parseInt(mETAmount.getText().toString());
        int iAmount = iAmountK * 1000;
        valueMap.setInt(O2GRequestParamsEnum.AMOUNT, iAmount);
		valueMap.setString(O2GRequestParamsEnum.PEG_TYPE_STOP, Constants.Peg.FromClose);
		valueMap.setString(O2GRequestParamsEnum.PEG_TYPE_LIMIT, Constants.Peg.FromOpen);
		
		
		if(bBuy){
			//Buy take profit at offset -1
			valueMap.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, -1);
			//Buy stoploss at offset 1
			valueMap.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, 1);
		}else{
			//Sell take profit at offset 1
			valueMap.setDouble(O2GRequestParamsEnum.PEG_OFFSET_STOP, 1);
			//Sell stoploss at offset -1
			valueMap.setDouble(O2GRequestParamsEnum.PEG_OFFSET_LIMIT, -1);
		}
		
        valueMap.setString(O2GRequestParamsEnum.TIME_IN_FORCE, "GTC");
        O2GRequestFactory requestFactory = mSession.getRequestFactory();
        if (requestFactory != null) {
            O2GRequest request = mSession.getRequestFactory().createOrderRequest(valueMap);
            if (request == null) {
                Toast.makeText(CreateOrderActivity.this, requestFactory.getLastError(), Toast.LENGTH_SHORT).show();
            } else {
                mSession.sendRequest(request);
				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/jtfx1.txt");
				try
				{
					FileOutputStream FOS = new FileOutputStream(file);
					OutputStreamWriter OSW = new OutputStreamWriter(FOS);
					OSW.append(request.getRequestId());
					OSW.close();
					FOS.close();
				}
				catch (FileNotFoundException e)
				{}catch(IOException ex){}
            }
        }
        
		

		A.setEnabled(false);
		Intent intent = new Intent(this, TradesActivity.class);
		startActivity(intent);
    }
}