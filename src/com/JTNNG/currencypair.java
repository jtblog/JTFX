package com.JTNNG;

import android.content.*;
import android.widget.*;
import com.fxcore2.*;
import com.tictactec.ta.lib.*;
import java.util.*;

public class currencypair
{
	public O2GOfferTableRow m_offer_row;

	//1mins chart
	double RSI_upper = 79;
	double RSI_lower = 19;

	private boolean rsi_ask = false;
	private boolean rsi_bid = false;

	//1 min
	private List<Double> bid_prices = new ArrayList<Double>();
	private double[] d_bids;
	private double curr_bid = 0.0;
	private double curr_ask = 0.0;
	// Ask|Buy
	private int up = 0;
	// Bid|Sell
	private int down = 0;
	private double[] d_vol= new double[3];
	private double curr_bid_high;
	private List<Double> bid_highs = new ArrayList<Double>();
	private double curr_bid_low;
	private List<Double> bid_lows = new ArrayList<Double>();
	private double[] d_bid_highs;
	private double[] d_bid_lows;

	/*
	 private List<Double> price_diffs = new ArrayList<Double>();
	 private List<Double> vol= new ArrayList<Double>();
	 private double[] d_bid;
	 private double[] d_ask;
	 private List<Double> bid_vol = new ArrayList<Double>();
	 private List<Double> ask_vol= new ArrayList<Double>();
	 */

	private Context con = null;

	public void setContext(Context c){
		con = c;
	}

	public currencypair(O2GOfferTableRow offer_row){
		m_offer_row = offer_row;
		
		d_bids = new double[bid_prices.size()];
		d_bid_highs = new double[bid_highs.size()];
		d_bid_lows = new double[bid_lows.size()];
		
		curr_bid_high = m_offer_row.getBid();
		curr_bid_low = m_offer_row.getBid();
		
		mInstance = this;
		d_vol= new double[3];
	}

    public currencypair mInstance;

	public currencypair getInstance() {
        //if (mInstance == null) {
		//mInstance = new currencypair(offer_row);
        //}
        return mInstance;
    }

	public double[] getVolumes(){
		double[] v = new double[2];

		/*
		 if(bid_vol.size() > 1){
		 v[0] = ask_vol.get(ask_vol.size() - 1);
		 v[1] = bid_vol.get(bid_vol.size() - 1);
		 }*/

		return getMD();
	}

	public String getSymbol(){
		return this.m_offer_row.getInstrument();
	}

	public double Ask(){
		return this.m_offer_row.getAsk();
	}

	public double Bid(){
		return this.m_offer_row.getBid();
	}

	public double prevBid(){
		if(d_bids.length > 1){
			return d_bids[(d_bids.length - 2)];
		}else{
			return this.m_offer_row.getBid();
		}
	}

	public boolean update(O2GOfferTableRow offer_row){
		this.m_offer_row = offer_row;

		try{

			if(m_offer_row.getAsk() > curr_ask && curr_ask != 0){
				up = up + 1;
			}
			if(m_offer_row.getBid() < curr_bid && curr_bid != 0){
				down = down + 1;
			}

			int v = m_offer_row.getVolume();
			if((up + down) != 0){
				if(up != 0){
					d_vol[0] = (up / (up + down)) * v;
					d_vol[1] = v - d_vol[0];
					d_vol[2] = v;
				}
				if(down != 0){
					d_vol[1] = (down / (up + down)) * v;
					d_vol[0] = v - d_vol[1];
					d_vol[2] = v;
				}
			}

			curr_ask = m_offer_row.getAsk();
			curr_bid = m_offer_row.getBid();

			int sec = m_offer_row.getTime().get(GregorianCalendar.SECOND);

			int sz;
			if(d_bids.length >= 1){
				sz = d_bids.length;
			}else{
				sz = bid_prices.size();
			}

			//Filling bids when seconds in zero
			if(sec == 0){

				up = 0;
				down = 0;
				
				/*
				 if(bid_vol.size() >= 30){
				 for(int i = 0 ; i < bid_vol.size() - 29; i++){
				 bid_vol.remove(0);
				 ask_vol.remove(0);
				 }
				 }else{
				 if(bid_vol.size() > 1){
				 if(d_bid.length > 0){
				 if(d_bid[d_bid.length-1] != bid_vol.get(bid_vol.size() - 1)){
				 bid_vol.add(d_bid[d_bid.length-1]);
				 ask_vol.add(d_ask[d_ask.length-1]);
				 }
				 }
				 }else{
				 if(d_bid.length > 0){
				 bid_vol.add(d_bid[d_bid.length-1]);
				 ask_vol.add(d_ask[d_ask.length-1]);
				 }
				 }

				 }

				 for(int i = 0; i < price_diffs.size(); i++){
				 price_diffs.remove(i);
				 vol.remove(i);
				 }
				 */

				if(sz >= 30){
					int ul = Math.abs(sz - 29);
					for(int i = 0; i < ul; i++){
						bid_prices.remove(i);
						bid_highs.remove(i);
						bid_lows.remove(i);
					}
					if(sz >= 1){
						bid_prices.add(sz - 1, m_offer_row.getBid());
						bid_highs.add(sz - 1, curr_bid_high);
						bid_lows.add(sz - 1, curr_bid_low);
					}else{
						bid_prices.add(m_offer_row.getBid());
						bid_highs.add(curr_bid_high);
						bid_lows.add(curr_bid_low);
					}
				}else{
					if(sz >= 1){
						bid_prices.add(sz - 1, m_offer_row.getBid());
						bid_highs.add(sz - 1, curr_bid_high);
						bid_lows.add(sz - 1, curr_bid_low);
					}else{
						bid_prices.add(m_offer_row.getBid());
						bid_highs.add(curr_bid_high);
						bid_lows.add(curr_bid_low);
					}
				}

				d_bids = new double[bid_prices.size()];
				d_bid_highs = new double[bid_highs.size()];
				d_bid_lows = new double[bid_lows.size()];

				for(int i = 0; i < d_bids.length; i++){
					d_bids[i] = (double) bid_prices.get(i);
					d_bid_highs[i] = (double) bid_highs.get(i);
					d_bid_lows[i] = (double) bid_lows.get(i);
				}
				
				curr_bid_high = max(curr_bid_high, m_offer_row.getBid());
				curr_bid_low = min(curr_bid_low, m_offer_row.getBid());

				/*
				 update1();
				 */
				 
				return true;

			}else{
				//Seconds is not zero

				curr_bid_high = max(curr_bid_high, m_offer_row.getBid());
				curr_bid_low = min(curr_bid_low, m_offer_row.getBid());
				
				if(bid_prices.size() < 1){
					
					d_bids = new double[1];
					d_bid_highs = new double[1];
					d_bid_lows = new double[1];
					
					d_bids[0] = m_offer_row.getBid();
					d_bid_highs[0] = curr_bid_high;
					d_bid_lows[0] = curr_bid_low;
					
				}else{
					d_bids = new double[bid_prices.size() + 1];
					d_bid_highs = new double[bid_highs.size() + 1];
					d_bid_lows = new double[bid_lows.size() + 1];

					for(int i = 0; i < d_bids.length - 1; i++){
						d_bids[i] = (double) bid_prices.get(i);
						d_bid_highs[i] = (double) bid_highs.get(i);
						d_bid_lows[i] = (double) bid_lows.get(i);
					}

					int end0 = d_bids.length - 1;
					d_bids[end0] = m_offer_row.getBid();
					int end1 = d_bid_highs.length - 1;
					d_bids[end1] = curr_bid_high;
					int end2 = d_bid_lows.length - 1;
					d_bids[end2] = curr_bid_low;

				}

				/*
				 if(close_bids.length > 1){
				 price_diffs.add((close_bids[close_bids.length - 1] - close_bids[close_bids.length - 2]) * 1000);
				 vol.add(1000 * Double.parseDouble(String.valueOf(m_offer_row.getVolume())));
				 }
				 */

				/*
				 update1();
				 */
				return true;
			}

		}catch(Exception e){
			if(con != null){
				Toast.makeText(con, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			return false;
		}

	}

	public double[] getData(){
		return d_bids;
	}

	public double[][] getBBANDS(){
		int period = 15;
		if(d_bids.length >= (period + 1)){

			double[] outU = new double[d_bids.length];
			double[] outM = new double[d_bids.length];
			double[] outL = new double[d_bids.length];

			MInteger begin = new MInteger();
			MInteger length = new MInteger();

			Core C = new Core();
			//RetCode RCode = C.sma(0, data1.length - 1, data1, 2, begin, length, out);
			RetCode RCode = C.bbands(0, d_bids.length - 1, d_bids,  period, 2, 2, MAType.Sma, begin, length, outU, outM, outL);

			double[] outUpper = new double[outU.length - period];
			double[] outMiddle = new double[outM.length - period];
			double[] outLower = new double[outL.length - period];

			if (RCode == RetCode.Success) {
				for (int i = 0; i < outUpper.length; i++) {
					outUpper[i] = outU[i];
				}
				for (int i = 0; i < outMiddle.length; i++) {
					outMiddle[i] = outM[i];
				}
				for (int i = 0; i < outLower.length; i++) {
					outLower[i] = outL[i];
				}
				double[][] val = new double[][]{outUpper, outMiddle, outLower};
				return val;
			}
			else {
				double[][] val = new double[][]{new double[]{0.0}, new double[]{0.0}, new double[]{0.0}};
				return val;
			}
		}else{
			double[][] val = new double[][]{new double[]{0.0}, new double[]{0.0}, new double[]{0.0}};
			return val;
		}

	}

	private double lastEMA(double[] in){

		int period = in.length;
		if(in.length >=  1){

			double[] out = new double[in.length];

			MInteger begin = new MInteger();
			MInteger length = new MInteger();

			Core C = new Core();
			RetCode RCode = C.ema(0, in.length - 1, in, period, begin, length, out);

			//double[] outReal = new double[(out.length - period)];

			return out[0];
		}else{
			//double[] outReal = new double[1];
			//outReal[0] = 0.0;
			return 0.0;
		}
	}

	private double max(double first, double second){
		return Math.max(first, second);
	}
	
	private double min(double first, double second){
		return Math.min(first, second);
	}
	
	public double[] getMD(){
		int period = 15;
		
		double[] mds = new double[d_bids.length];
		mds[0] = lastEMA(d_bids);
		for(int i = 1; i < mds.length; i++){
			mds[i] = mds[i-1] + ((d_bids[i] - mds[i-1]) / (0.6 * period * Math.pow(d_bids[i] / mds[i-1], 4))); 
		}
		return mds;
	}
	
	public double[][] getAroon(){
		int period = 5;
		if(d_bid_highs.length >= (period + 1)){
			
			double[] out_up = new double[d_bid_highs.length + 1 - period];
			double[] out_down = new double[d_bid_lows.length + 1 - period];
			
			MInteger begin = new MInteger();
			MInteger length = new MInteger();

			Core C = new Core();
			RetCode RCode = C.aroon(0, d_bid_highs.length - 1, d_bid_highs, d_bid_lows, period, begin, length, out_down, out_up);
			
			return new double[][]{out_up, out_down};
		}else{
			return new double[][]{new double[]{0.0}, new double[]{0.0}};
		}
	}

	public double[] getRSI(){

		int period = 5;
		if(d_bids.length >= (period + 1)){

			double[] out = new double[d_bids.length];

			MInteger begin = new MInteger();
			MInteger length = new MInteger();

			Core C = new Core();
			RetCode RCode = C.rsi(0, d_bids.length - 1, d_bids, period, begin, length, out);

			double[] outReal = new double[(out.length - period)];

			if (RCode == RetCode.Success) {

				for (int i = 0; i < outReal.length; i++) {
					outReal[i] = out[i];
				}
			}
			else {
				outReal = new double[1];
				outReal[0] = 0.0;
			}
			return outReal;
		}else{
			double[] outReal = new double[1];
			outReal[0] = 0.0;
			return outReal;
		}
	}

	public double[] getStdDev(double[] in){

		int period = in.length;

		if(in.length > 2){

			double[] out = new double[in.length];

			MInteger begin = new MInteger();
			MInteger length = new MInteger();

			Core C = new Core();
			RetCode RCode = C.stdDev(0, in.length - 1, in, period, 1, begin, length, out);

			double[] outReal = new double[1];

			if (RCode == RetCode.Success) {

				for (int i = 0; i < outReal.length; i++) {
					outReal[i] = out[i];
				}
			}
			else {
				outReal = new double[1];
				outReal[0] = 0.0;
			}

			return outReal;
		}else{
			double[] outReal = new double[1];
			outReal[0] = 0.0;
			return outReal;
		}
	}

	public double[] getTSF(){

		int period = 5;
		if(d_bids.length >= (period + 1)){

			double[] out = new double[d_bids.length];

			MInteger begin = new MInteger();
			MInteger length = new MInteger();

			Core C = new Core();
			RetCode RCode = C.tsf(0, d_bids.length - 1, d_bids, period, begin, length, out);

			double[] outReal = new double[(out.length - period + 1)];

			if (RCode == RetCode.Success) {

				for (int i = 0; i < outReal.length; i++) {
					outReal[i] = out[i];
				}
			}
			else {
				outReal = new double[1];
				outReal[0] = 0.0;
			}
			return outReal;
		}else{
			double[] outReal = new double[1];
			outReal[0] = 0.0;
			return outReal;
		}
	}

	private void RSI_logic(){

		double[] RSI = getRSI();

		if(RSI.length > 2){
			double last = RSI[RSI.length - 1];
			double b4last = RSI[RSI.length - 2];
			if(b4last > RSI_upper && last > 50 && last < RSI_upper){
				rsi_bid = false;
				rsi_ask = true;
			}else if(b4last < RSI_lower && last < 50 && last > RSI_lower){
				rsi_ask = false;
				rsi_bid = true;
			}else{
				rsi_bid = false;
				rsi_ask = false;
			}
		}else{
			rsi_bid = false;
			rsi_ask = false;
		}

	}

	public boolean RSI_Sell(){
		RSI_logic();
		return rsi_bid;
	}

	public boolean RSI_Buy(){
		RSI_logic();
		return rsi_ask;
	}

	private double CDF(double x){
		//http://en.wikipedia.org/wiki/Normal_distribution
		double sum = x;
		double value = x;
		for(int i = 1; i < 100; i++){
			value = value * x * x / (2 * i + 1);
			sum = sum + value;
		}

		return 0.5 + (sum / Math.sqrt(2 * Math.PI)) * Math.exp(-(x * x) / 2);

	}

	private void update1(){
		try{
			/*
			 d_bid = new double[price_diffs.size()];
			 d_ask = new double[price_diffs.size()];

			 for(int i= 0; i<d_bid.length;i++){
			 d_bid[i] = price_diffs.get(i);
			 }

			 double[] std = getStdDev(d_bid);
			 int upl = Math.min(d_bid.length, vol.size());

			 for(int i = upl - 1; i > -1; i--){
			 double x0 = CDF(d_bid[i] / std[0]);
			 d_bid[i] = x0 * vol.get(i);

			 String s = String.valueOf(d_bid[i]);

			 if(s.contains("E")){
			 s = s.split("E")[0];
			 d_bid[i] = Double.parseDouble(s);
			 }

			 d_ask[i] =vol.get(i) - Math.abs(d_bid[i]);

			 if(Double.isNaN(d_bid[i]) || Double.isNaN(d_ask[i])){
			 i = 0;
			 d_bid = new double[]{0.0};
			 d_ask = new double[]{0.0};
			 }

			 }
			 */

		}catch(Exception e){

		}

	}

}