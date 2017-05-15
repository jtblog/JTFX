package com.JTNNG;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.fxcore2.*;
import java.util.*;
import android.content.pm.*;
import com.JTNNG.aidl.*;

public class LoginDialogActivity extends Activity{
    public O2GSession mSession;
    public EditText mEditTextLogin;
    public EditText mEditTextPassword;
    public EditText mEditTextConnection;
    public EditText mEditTextHost;
    public Button mLoginButton;
    public SharedPreferences mPreferences;
	public Activity lda = this;
    public Handler mHandler = new Handler();
	public ArrayList<String> loginParams = new ArrayList<>();
    public JTFXaidl mService;
	public static String BROADCAST_LOGIN = "com.JTNNG.Login";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSession = SharedObjects.getInstance().getSession();
        mSession.subscribeSessionStatus(new SessionStatusChanged());
        
        mEditTextLogin = (EditText)this.findViewById(R.id.etLogin);
        mEditTextPassword = (EditText)this.findViewById(R.id.etPassword);
        mEditTextConnection = (EditText)this.findViewById(R.id.etConnection);
        mEditTextHost = (EditText)this.findViewById(R.id.etHost);
        mLoginButton = (Button)this.findViewById(R.id.btnLogin);
        
		loginParams.clear();
		mPreferences = getPreferences(Context.MODE_PRIVATE);
        String sLastLogin = mPreferences.getString("login", "");
        mEditTextLogin.setText(sLastLogin);
		mEditTextPassword.setText(mPreferences.getString("password", ""));
        mEditTextConnection.setText(mPreferences.getString("connection", ""));
        mEditTextHost.setText(mPreferences.getString("host", ""));
		loginParams.add(sLastLogin);
		loginParams.add(mPreferences.getString("password", ""));
		loginParams.add(mPreferences.getString("host", ""));
		loginParams.add(mPreferences.getString("connection", ""));
		SharedObjects.getInstance().setLoginParams(loginParams);
		
        if (!"".equals(sLastLogin)) {
            mEditTextPassword.requestFocus();
        }
        else {
            mEditTextLogin.requestFocus();
        }
		
		Intent intent = new Intent(this, LoginService.class);
		startService(intent);
		
    }
	
    public void btnLogin_Click(View view) {
		login();
    }
    
    public void login() {
		
        String sLogin = mEditTextLogin.getText().toString();
        String sPassword = mEditTextPassword.getText().toString();
        String sConnection = mEditTextConnection.getText().toString();
        String sHost = mEditTextHost.getText().toString();
        if (!sHost.endsWith("Hosts.jsp")) {
            if (!sHost.endsWith("/")) {
                sHost = sHost + "/"; 
            }
            sHost = sHost + "Hosts.jsp"; 
        }
        mSession.login(sLogin, sPassword, sHost, sConnection);
    }
    
    @Override
    protected void onDestroy() {
        mSession.unsubscribeSessionStatus(new SessionStatusChanged());
    }
    
    private class LoginErrorRunnable implements Runnable {
    
        private String mLoginError;
        
        public LoginErrorRunnable(String sLoginError) {
            this.mLoginError = sLoginError;
            
        }

        public void run() {
            Toast.makeText(getApplicationContext(), "JTFX: " + mLoginError, Toast.LENGTH_SHORT).show();
        }
    }
    
    
    private class SessionStatusChangedRunnable implements Runnable {
        
        private O2GSessionStatusCode mCode;
        
        public SessionStatusChangedRunnable(O2GSessionStatusCode eCode) {
            mCode = eCode;
        }
        
        public void run() {
            
            if (mCode == O2GSessionStatusCode.CONNECTED) {
                waitTableManagerRefresh();
				setAccountID();
                commitPreferences();
				
                startActivity(new Intent(LoginDialogActivity.this, MainActivity.class));
            }else if(mCode == O2GSessionStatusCode.DISCONNECTED){
				
			}
			else if (mCode == O2GSessionStatusCode.RECONNECTING){
				
			}
        }

		private void setAccountID()
		{
			// TODO: Implement this method
			O2GResponse response = mSession.getLoginRules().getTableRefreshResponse(O2GTableType.ACCOUNTS);
			O2GAccountsTableResponseReader reader = mSession.getResponseReaderFactory().createAccountsTableReader(response);
			SharedObjects.getInstance().setAccountID(reader.getRow(0).getAccountID());
		}

        private void waitTableManagerRefresh() {
            O2GTableManager tableManger = mSession.getTableManager();
            while (tableManger.getStatus() != O2GTableManagerStatus.TABLES_LOADED &&
                   tableManger.getStatus() != O2GTableManagerStatus.TABLES_LOAD_FAILED) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
    }

    private void commitPreferences() {
        String sLogin = mEditTextLogin.getText().toString();
		String sPassword = mEditTextPassword.getText().toString();
        String sConnection = mEditTextConnection.getText().toString();
        String sHost = mEditTextHost.getText().toString();

        Editor editor = mPreferences.edit();
        editor.putString("login", sLogin);
		editor.putString("password", sPassword);
        editor.putString("connection", sConnection);
        editor.putString("host", sHost);
		loginParams.clear();
		loginParams.add(sLogin);
		loginParams.add(sPassword);
		loginParams.add(sHost);
		loginParams.add(sConnection);
        editor.commit();
		
		SharedObjects.getInstance().setLoginParams(loginParams);
    }
	
	public class SessionStatusChanged implements IO2GSessionStatus
	{

		@Override
		public void onSessionStatusChanged(O2GSessionStatusCode p1)
		{
			// TODO: Implement this method
			mHandler.post(new SessionStatusChangedRunnable(p1));
		}

		@Override
		public void onLoginFailed(String p1)
		{
			// TODO: Implement this method
			mHandler.post(new LoginErrorRunnable(p1));
		}
	}
}