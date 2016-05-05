package com.moon.android.moonplayer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;

import com.moon.android.moonplayer.util.Logger;

public class Traffics {

	private static Logger logger = Logger.getLogger();
	public static final String PLAYER_PKG = "com.farcore.videoplayer";
	public static final int getUID(Context context){
		try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(PLAYER_PKG, PackageManager.GET_ACTIVITIES);
            return ai.uid;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
		return 0;
	}
	
	public static long getTraffics(Context context){
		int uid = getUID(context);
		long traffics = TrafficStats.getUidRxBytes(uid);
		return traffics;
	}
	
}
