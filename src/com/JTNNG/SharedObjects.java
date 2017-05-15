package com.JTNNG;

import android.util.*;
import com.fxcore2.*;
import java.math.*;
import java.util.*;

public class SharedObjects {
    
    private O2GSession mSession;
    private Map<String, Pair<BigDecimal, BigDecimal>> mActualPrices;
    private O2GOfferRow mSelectedOfferRow;
    private String mAccountID;
	private double mAccBal;
	public ArrayList<String> mLoginParams;

    private SharedObjects() {
        mSession = O2GTransport.createSession();
  mSession.useTableManager(O2GTableManagerMode.YES, null);
        mActualPrices = null;
        mSelectedOfferRow = null;
    }
    
    private static SharedObjects mInstance;
    
    public static SharedObjects getInstance() {
        if (mInstance == null) {
            mInstance = new SharedObjects();
        }
        return mInstance;
    }
   
    public O2GSession getSession() {
        return mSession;
    }
    
    public void setActualPrices(Map<String, Pair<BigDecimal, BigDecimal>> actualPrices) {
        mActualPrices = actualPrices;
    }
    
    public Map<String, Pair<BigDecimal, BigDecimal>> getActualPrices() {
        return mActualPrices;
    }
    
    public void setSelectedOffer(O2GOfferRow offerRow) {
        mSelectedOfferRow = offerRow;
    }
    
    public O2GOfferRow getSelectedOffer() {
        return mSelectedOfferRow;
    }
    
    public void setAccountID(String sAccountID) {
        mAccountID = sAccountID;
    }
    
    public String getAccountID() {
        return mAccountID;
    }
	
	public void AccBal(double sAccBal){
		mAccBal = sAccBal;
	}
	
	public double getAccBal(){
		return mAccBal;
	}

	public ArrayList<String> getLoginParams(){
		return mLoginParams;
	}

	public void setLoginParams(ArrayList<String> sLoginParams){
		mLoginParams = sLoginParams;
	}
	
}