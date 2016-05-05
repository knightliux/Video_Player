package com.moon.android.moonplayer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.moon.android.moonplayer.util.Logger;

public class MsgService extends Service{
	
	private Timer mMsgTimer = new Timer(true);
	private Timer mUpgradeTimer = new Timer(true);
	public static final long MSG_WHEN_FIRST_GET = 2 * 1000;
	public static final long MSG_PERIOD = 600 * 1000;
	public static final long UPGRADE_WHEN_GET = 2 * 1000;
	private Logger logger = Logger.getLogger();
	@Override
	public void onCreate() {
		super.onCreate();
		mUpgradeTimer.schedule(mUpgradeTimeTask, UPGRADE_WHEN_GET);
		return;
	}
	
	private TimerTask mUpgradeTimeTask = new TimerTask(){
		@Override
		public void run() {
			UpdateData localUpdateData = RequestDAO.checkUpate(getApplicationContext());
			if (null != localUpdateData) {
				Intent intent = new Intent();
				intent.setAction(Configs.BroadCast.UPDATE_MSG);
				Constants.updateData = localUpdateData;
				sendBroadcast(intent);
			}
		}
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		try{
			mUpgradeTimer.cancel();
			mMsgTimer.cancel();
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}
	
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

}
