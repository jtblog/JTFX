package com.JTNNG;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import com.fxcore2.*;
import java.util.*;

public class Arbitrage implements Runnable
{

	@Override
	public void run()
	{
		// TODO: Implement this method

		double a0 = 0.0;
		double a1 = 0.0;
		double b0 = 0.0;
		
		for(currencypair pair : cp){
			if(pair.getInstance().getSymbol().equalsIgnoreCase("EUR/USD")){
				a0 = pair.getInstance().Ask();
			}
			if(pair.getInstance().getSymbol().equalsIgnoreCase("EUR/JPY")){
				b0 = pair.getInstance().Bid();
			}
			if(pair.getInstance().getSymbol().equalsIgnoreCase("USD/JPY")){
				a1 = pair.getInstance().Ask();
			}
		}

		//Toast.makeText(getApplicationContext(), arb.getInstance().AAB(a0, a1, b0), Toast.LENGTH_SHORT).show();

		a0 = 0.0;
		b0 = 0.0;
		double b1 = 0.0;

		for(currencypair pair : cp){
			if(pair.getInstance().getSymbol().equalsIgnoreCase("USD/JPY")){
				b0 = pair.getInstance().Bid();
			}
			if(pair.getInstance().getSymbol().equalsIgnoreCase("EUR/JPY")){
				a0 = pair.getInstance().Ask();
			}
			if(pair.getInstance().getSymbol().equalsIgnoreCase("EUR/USD")){
				b1 = pair.getInstance().Bid();
			}
		}

		//Toast.makeText(getApplicationContext(), arb.getInstance().ABB(a0, b0, b1), Toast.LENGTH_SHORT).show();

	}

	public List<currencypair> cp;

	public Arbitrage(List<currencypair> cpair){
		cp = null;
		cp = cpair;
		mInstance = this;
	}
	
	public Arbitrage mInstance;

	public Arbitrage getInstance() {
        //if (mInstance == null) {
			//mInstance = new Arbitrage();
        //}
        return mInstance;
    }
	
	public String ABB(double a0, double b0, double b1){
		double fig = (b0 * b1) / a0;
		if(fig > 1){
			return "JTFX: Arbitrage detected | " + String.valueOf(fig);
		}else{
			return "JTFX: No arbitrage detected | " + String.valueOf(fig);
		}
	}
	
	public String AAB(double a0, double a1, double b0){
		double fig = b0 / ( a0 * a1);
		if(fig > 1){
			return "JTFX: Arbitrage detected | " + String.valueOf(fig);
		}else{
			return "JTFX: No arbitrage detected | " + String.valueOf(fig);
		}
	}

}