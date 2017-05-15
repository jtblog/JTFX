package com.JTNNG;

import android.app.*;
import android.content.*;
import java.util.*;

public class JTFXBootC extends BroadcastReceiver
{

	@Override
	public void onReceive(Context p1, Intent p2)
	{
		// TODO: Implement this method
		if(p2.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
		{
			ActivityManager AM = (ActivityManager) p1.getSystemService(p1.ACTIVITY_SERVICE);
		
			for(int i = 0; i < AM.getRunningAppProcesses().size(); i++){
				if(AM.getRunningAppProcesses().get(i).processName == ""){
					Intent JTFXI = p1.getPackageManager().getLaunchIntentForPackage("com.JTNNG");
					JTFXI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					p1.startActivity(JTFXI);
				}else{
					Intent JTFXI = p1.getPackageManager().getLaunchIntentForPackage("com.JTNNG");
					JTFXI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					p1.startActivity(JTFXI);
				}
			}
			
		}
	}

}