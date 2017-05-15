package com.JTNNG;

import android.app.*;
import android.os.*;
import android.content.*;
import com.fxcore2.*;
import android.widget.*;
import java.util.*;
import com.JTNNG.aidl.JTFXaidl;

public class LoginService extends Service
{

	public O2GSession mSession;
	public Handler mHandler = new Handler();
	public Service loginService = this;
	public ArrayList<String> loginParams;
	public String sLogin;
	public String sPassword;
	public String sHost;
	public String sConnection;
	public static String BROADCAST_LOGIN = "com.JTNNG.Login";

	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		//return mBinder;
		return null;
	}

	//public final JTFXaidl.Stub mBinder = new JTFXaidl.Stub(){

		//@Override
		//public void setLoginParameters(String mUsername, String mPassword, String mHost, String mConnection) throws RemoteException
		//{
			// TODO: Implement this method
			//sLogin = mUsername;
			//sPassword = mPassword;
			//sHost = mHost;
			//sConnection = mConnection;
		//}
	//};
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		// TODO: Implement this method
		
		mSession = SharedObjects.getInstance().getSession();
		mSession.subscribeSessionStatus(new SessionStatusChanged());
		mHandler.post(new LoginRunnable());
	}
	
	public class SessionStatusChanged implements IO2GSessionStatus
	{

		@Override
		public void onSessionStatusChanged(O2GSessionStatusCode p1)
		{
			// TODO: Implement this method
			mHandler.post(new LoginStatusRunnable(p1));
		}

		@Override
		public void onLoginFailed(String p1)
		{
			// TODO: Implement this method
			
		}
		
		public class LoginStatusRunnable implements Runnable
		{

			public O2GSessionStatusCode Status;
      	
			public LoginStatusRunnable(O2GSessionStatusCode p1){
				Status = p1;
			}
			
			@Override
			public void run()
			{
				// TODO: Implement this method
				Toast.makeText(getApplicationContext(), "JTFX: "+ Status.toString(), Toast.LENGTH_SHORT).show();
				if(Status == O2GSessionStatusCode.CONNECTED){
					startService(new Intent(getApplicationContext(), ArbitrageService.class));
					startService(new Intent(getApplicationContext(), NeuralService.class));
					startService(new Intent(getApplicationContext(), TablesUpdateService.class));
					stopSelf();
				}else if(Status == O2GSessionStatusCode.RECONNECTING || Status == O2GSessionStatusCode.CONNECTING){

				}else if(Status == O2GSessionStatusCode.DISCONNECTED){
					mHandler.postDelayed(new LoginRunnable(), 60000);
					Toast.makeText(getApplicationContext(), "JTFX: Attempting login in the next 1 minute".toString(), Toast.LENGTH_SHORT).show();
				}
			}

		}
	}
	
	public class LoginRunnable implements Runnable
	{

		@Override
		public void run()
		{
			// TODO: Implement this method
			if(!(SharedObjects.getInstance().getLoginParams() == null) && !(SharedObjects.getInstance().getLoginParams().isEmpty())){
				mSession = SharedObjects.getInstance().getSession();
				
				sLogin = SharedObjects.getInstance().getLoginParams().get(0);
				sPassword = SharedObjects.getInstance().getLoginParams().get(1);
				sHost = SharedObjects.getInstance().getLoginParams().get(2);
				sConnection = SharedObjects.getInstance().getLoginParams().get(3);
				
				if (!sHost.endsWith("Hosts.jsp")) {
					if (!sHost.endsWith("/")) {
						sHost = sHost + "/"; 
					}
					sHost = sHost + "Hosts.jsp"; 
				}
				
				mSession.login(sLogin, sPassword, sHost, sConnection);      
			}else{
				
			}
		}
	}
}