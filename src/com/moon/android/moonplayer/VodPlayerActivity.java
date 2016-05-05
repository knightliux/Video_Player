package com.moon.android.moonplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.farcore.playerservice.Player;
import com.moon.android.moonplayer.history.HistoryDAO;
import com.moon.android.moonplayer.history.HistoryItem;
import com.moon.android.moonplayer.service.VodVideo;
import com.moon.android.moonplayer.util.Logger;
import com.moon.android.moonplayer.util.PackageUtils;
import com.moon.android.moonplayer.util.RequestUtil;
import com.moon.android.moonplayer.util.ScreenUtils;
import com.moon.android.moonplayer.view.CustomToast;
import com.moon.android.moonplayer.view.GGTextView;
import com.moon.android.moonplayer.view.VideoView;

public class VodPlayerActivity extends Activity 
 						implements OnKeyListener{
	public Logger logger = Logger.getLogger();
	private static final String FORCE_CMD = "switch_chan";
	private static final String PERIOD = ".";
	private static final String PLAY_SERVER = "http://127.0.0.1:9906/";
	private static final String GET_FORCE_PLAY_INFO ="http://127.0.0.1:9906/api?func=query_chan_info&id=";
	private static final String SYS_MUSIC_COMMAND = "com.android.music.musicservicecommand.pause";
	private static final String CLOSE_FORCE = "http://127.0.0.1:9906/api?func=exit_process";
	private static final String S_COMMAND = "command";
	private static final String S_STOP = "stop";
	private String mAppMSG= "";
	private String mAppID;
	private static final String mVodCacheName = "vodCache";
	private static final long GET_CACHE_PEROID = 2;
	private static final long START_CACHE_TIME = 1;
	private int mPlayStatus = VideoInfo.PLAYER_UNKNOWN;
	
	private VideoView mVideoView;
	private VodVideo mCurrentVideo;
	private List<VodVideo> mListVideos;
	private String mVodTitle;
	private int mPlayVodPos;
	private Player mPlayer;
	private String mVideoPath;
	private View mContainer;
	//OSD
	private OSDVolume mOSDVolume;
	private OSDControlBar mOSDControl;
	private OSDPause mOSDPause;
	private OSDManager mOSDManager;
	private OSDLoading mOSDLoading;
	private OSDCache mOSDCache;
	private boolean isLoadingVod = true;
	private TextView mPormptText;
	private String mForcePlayInfo;
	private boolean mIsVodPause = false;
	private long mHistoryPlayPos = 0;
	boolean isPlayHistory = false;
	private int mScreenHeight;
	private int mScreenWidth;
	
	private String mAdUrl;
	/*
	 * �Ƿ����ڲ�����һ��  ������һ�� ��ǵ�һ�ν��벥����ʷ��¼
	 */
	private boolean isPlayDirect = false;
	public static final String sdkPath = Environment.getExternalStorageDirectory().toString() 
			+ "/" + mVodCacheName;
	
	public static final String upgradepath = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ Configs.PKG_NAME+ "/";
	
	private Timer mTimerCache = new Timer();
	private TimerTask mTimerTaskCache = new TimerTask() {
		@Override
		public void run() {
			mForcePlayInfo = RequestUtil.getInstance().request(GET_FORCE_PLAY_INFO + mCurrentVideo.getChannelId());
			logger.e("ChannelId="+mCurrentVideo.getChannelId());
			logger.e(mForcePlayInfo);
			mHandler.sendEmptyMessage(0);
		}
	};
	
	private Timer mTimerGetPlayPos = new Timer();
	private long mPreiousTime = 0;
	private TimerTask mTimerTaskGetPlayPos = new TimerTask() {
		@Override
		public void run() {
			mHandlerPlayPos.sendEmptyMessage(0);
		}
	};
	
	/**
	 * update start 
	 */
	private void startUpdatAndGetMsgService() {
		try{
	  		Intent intent = new Intent(this,MsgService.class);
	  		startService(intent);
		}catch(Exception e){
			logger.e(e.toString());
		}
  	}
    
    private void registerMsgReceiver() {
    	try{
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Configs.BroadCast.UPDATE_MSG);
			registerReceiver(mUpdateMsgReceiver, intentFilter);
    	}catch(Exception e){
    		logger.e(e.toString());
    	}
	}
	
	private BroadcastReceiver mUpdateMsgReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if(Configs.BroadCast.UPDATE_MSG.equals(intent.getAction())){
				UpdateData updata = Constants.updateData;
				String appUrl = updata.getUrl();
				String msg = updata.getMsg();
				initPopWindow(appUrl, msg);
			} 
		}
	};
	
	private String mUpdateUrl;
	private Button mBtnSubmit;
	private Button mBtnCancel;
	private PopupWindow mUpdatePopupWindow;
	public static final int START_DOWNLOAD = 0x11111;
	private void initPopWindow(final String appUrl,
			String msg) {
		mUpdateUrl = appUrl;
		View view = LayoutInflater.from(this).inflate(R.layout.update_dialog, null);
		mBtnSubmit = (Button) view.findViewById(R.id.dialog_submit);
		mBtnCancel = (Button) view.findViewById(R.id.dialog_cancel);
		mBtnSubmit.setOnClickListener(mDialogClickListener);
		mBtnCancel.setOnClickListener(mDialogClickListener);
		TextView textContent = (TextView) view.findViewById(R.id.text_content);
		textContent.setText(msg);
		
		int width = getWindowManager().getDefaultDisplay().getWidth();
		int height = getWindowManager().getDefaultDisplay().getHeight();
		mUpdatePopupWindow = new PopupWindow(view,width,height,true);
		mUpdatePopupWindow.showAsDropDown(view,0,0);
		mUpdatePopupWindow.setOutsideTouchable(false);
	}
	
	private OnClickListener mDialogClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mUpdatePopupWindow.dismiss();
			if(v == mBtnSubmit){
				Toast.makeText(VodPlayerActivity.this, R.string.start_download, Toast.LENGTH_LONG).show();
				downFile(mUpdateUrl);
			}
		}
	};
	
	private void downFile(final String paramString) {
		new Thread() {
			public void run() {
				try {
					DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
					HttpGet localHttpGet = new HttpGet(paramString.trim());
					HttpEntity localHttpEntity = localDefaultHttpClient
							.execute(localHttpGet).getEntity();
					localHttpEntity.getContentLength();
					InputStream localInputStream = localHttpEntity.getContent();
					FileOutputStream localFileOutputStream = null;
					byte[] arrayOfByte;
					if (localInputStream != null) {
						localFileOutputStream = openFileOutput(Configs.APK_NAME,1);
						arrayOfByte = new byte[1024];
						int j = 0;
						while ((j = localInputStream.read(arrayOfByte)) != -1) {
							localFileOutputStream.write(arrayOfByte, 0, j);
						}
						localFileOutputStream.flush();
					}
					if (localFileOutputStream != null)
						localFileOutputStream.close();
					mUpdateHandler.sendEmptyMessage(START_DOWNLOAD);
					return;
				} catch (ClientProtocolException localClientProtocolException) {
					localClientProtocolException.printStackTrace();
					return;
				} catch (IOException localIOException) {
					localIOException.printStackTrace();
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private Handler mUpdateHandler = new Handler(){
		public void handleMessage(Message msg) {
			File file = new File(upgradepath + "/files/",
					Configs.APK_NAME);
			PackageUtils.startIntallApkFromFile(VodPlayerActivity.this, file);
		};
	};
	
	/**
	 * update end
	 */
	private OnPreparedListener mVideoPrepareListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer paramAnonymousMediaPlayer) {
			mOSDManager.closeOSD(mOSDLoading);
			logger.i("history position = " + mHistoryPlayPos);
			if(!isPlayDirect)
				mVideoView.seekTo((int)mHistoryPlayPos);
			mVideoView.start();
			isLoadingVod = false;
			int i =(int) mVideoView.getDuration();
			mOSDControl.setTotalTime(i);
			mOSDControl.setVodSelectFocusable(hasPreVod(),hasNextVod());
			try{
				mTimerGetPlayPos.schedule(mTimerTaskGetPlayPos, 0, 1000);
			}catch(Exception e){
				e.printStackTrace();
			}
			return;
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			mPormptText.setText(mForcePlayInfo);
			Long playInfo[] = parseForcePlayInfo(mForcePlayInfo);
			long traffic = playInfo[1] / 8;
			mOSDControl.setTraffics(traffic);
			mOSDCache.setTraffic(traffic);
			mOSDLoading.setPromptText((int)traffic);
			int currentPos = mOSDControl.getCurrentProgress();
			mOSDControl.setSecondProgress(currentPos + (int)(playInfo[0] * 1000));
		}
	};
	
	private Long[] parseForcePlayInfo(String forcePlayInfo) {
		Document doc = null;
		try {
			String parseXml = mForcePlayInfo.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "").replace("ver=1.0", "");
			doc = DocumentHelper.parseText(parseXml); 
			Node root = doc.getRootElement();
			Element element = (Element) root.selectSingleNode("/forcetv/channel");
			long cacheTime = Integer.valueOf(element.attribute("cache_time").getValue());
			long traffic = Integer.valueOf(element.attribute("download_flowkbps").getValue());
			logger.e("cacheTime="+cacheTime+"   traffic="+traffic);
			return new Long[]{cacheTime,traffic};
		} catch (DocumentException e) {
			logger.e(e.toString());
		} catch (Exception e) {
			logger.e(e.toString());
		}
		return new Long[]{1l,1l};
	};
	
	/**
	 * ��ʱ����1sǰMediaPlayer��ʱ������жԱȣ����ʱ�����δ��
	 * ���ʱ�����ͬ��������˿��٣���ʾcache����.
	 */
	private Handler mHandlerPlayPos = new Handler(){
		public void handleMessage(Message msg) {
			int currentPlayPos =(int) mVideoView.getCurrentPosition();
			if(currentPlayPos <=3) return;  //ǰ3�벻�ж��Ƿ���Կ���
			if(mIsVodPause || isLoadingVod) return;
			if(mPreiousTime == currentPlayPos){
				if(View.GONE == mOSDCache.getVisibility())
					mOSDManager.showOSD(mOSDCache);
			} else {
				if(View.VISIBLE == mOSDCache.getVisibility())
					mOSDManager.closeOSD(mOSDCache);
			}
			mOSDControl.setCurrentPlayTime(currentPlayPos);
			mPreiousTime = currentPlayPos;
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		stopSysMusic();
		setContentView(R.layout.activity_vod_player);
		initData();
		startUpdatAndGetMsgService();
		registerMsgReceiver();
	}

	private void initData() {
		getIntentData();
		initWidget();
		startVod();
//		mAppMS
		showMsg(mAppMSG);
		
	}
	
	private void startVod(){
		if(mCurrentVideo.getUrl().indexOf("http")==0){
			logger.i("返回HTTP地址:"+mCurrentVideo.getUrl());
			String Url= mCurrentVideo.getUrl();
		
//			setVideoScale(mode);
			getScreenSize();
			int mode = DisplayMode.getMode(VodPlayerActivity.this);
			setVideoScale(mode);
			mVideoView.setVideoPath(Url);
			mVideoView.setOnPreparedListener(mVideoPrepareListener);
			setTitle();
			try{
				mTimerCache.schedule(mTimerTaskCache, START_CACHE_TIME * 1000, GET_CACHE_PEROID * 1000);  
			}catch(Exception e){
				e.printStackTrace();
			}
		
		
		}else{
			FinalHttp finalHttp = new FinalHttp();
			finalHttp.addHeader("User-Agent", "cwhttp/v1.0");
			finalHttp.get(getStartUrl(), mStartVodCallBack);
		}
		
		
	}
	
	private AjaxCallBack<Object> mStartVodCallBack = new AjaxCallBack<Object>() {
		public void onSuccess(Object t) {
			super.onSuccess(t);
			logger.i((String)t);
			mCheckPlayHandler.sendEmptyMessage(Configs.SUCCESS);
		};
		
		public void onFailure(Throwable t, int errorNo, String strMsg) {
			super.onFailure(t, errorNo, strMsg);
		};
	};
	
	private String getStartUrl(){
		
		logger.i(mCurrentVideo.getUrl()+"--------");
		
		
			String chnnelId = mCurrentVideo.getChannelId();
			String streamIp = mCurrentVideo.getStreamip();
			String link = mCurrentVideo.getLink();
			StringBuilder sb = new StringBuilder("http://127.0.0.1:9906/cmd.xml?cmd=");
			sb.append(FORCE_CMD).append("&id=").append(chnnelId).append("&server=").append(streamIp);
			
			if (link != null && !link.equals("")) {
				sb.append("&link=").append(link);
			}
			logger.i(sb.toString());
			
			
			return sb.toString();
	
		
	}
	
	private void getIntentData(){
		Intent intent = getIntent();
		mAdUrl=intent.getStringExtra(Configs.INTENT_PARAM_2);
		mCurrentVideo = new VodVideo();
		mListVideos = (List<VodVideo>) intent.getSerializableExtra("videolist");
		mVodTitle = intent.getStringExtra("programName");
		isPlayHistory = intent.getBooleanExtra("hasHistory", false);
		
		if(isPlayHistory){
			logger.i("Play history");
			mPlayVodPos = intent.getIntExtra("history_vod_index", 0);
			mHistoryPlayPos = intent.getIntExtra("history_vod_pos", 0);
		} else {
			logger.i("Not play history");
			mPlayVodPos = intent.getIntExtra("index",0);
			logger.i("Play position = " + mPlayVodPos);
		}
		if(mPlayVodPos < mListVideos.size()){
			VodVideo vodVideo = mListVideos.get(mPlayVodPos);
			mCurrentVideo.setStreamip(vodVideo.getStreamip());
			mCurrentVideo.setChannelId(vodVideo.getChannelId());
			mCurrentVideo.setType(vodVideo.getType());
			mCurrentVideo.setLink(vodVideo.getLink());
			mCurrentVideo.setName(vodVideo.getName());
			mCurrentVideo.setUrl(vodVideo.getUrl());
			logger.i("Player : Streamip = " + vodVideo.getStreamip() 
					+" ChannelId = " + vodVideo.getChannelId() + 
					" Type = " + vodVideo.getType() + " Link = " + vodVideo.getLink()
					+ " Name = " + vodVideo.getName());
			
		}
		
		mAppMSG = intent.getStringExtra("appmsg");
		try{
			String appMSG[] = mAppMSG.split("\\+\\+\\+");
			if(appMSG.length == 2){
				mAppMSG = appMSG[0];
				mAppID = appMSG[1];
			} else if(appMSG.length == 1){
				mAppMSG = appMSG[0];
			}
		}catch(Exception e){
			logger.e(e.toString());
		}
	}
	
	private GGTextView mScrollText;
	private void initWidget() {
		mVideoView = (VideoView) findViewById(R.id.video_view);
		
		mContainer = findViewById(R.id.osd_container);
		mPormptText = (TextView) findViewById(R.id.prompt_text);
		
		mOSDVolume = new OSDVolume(getApplicationContext(), mContainer);
		mOSDPause = new OSDPause(mContainer,mAdUrl);
		mOSDLoading = new OSDLoading(mContainer);
		mOSDCache = new OSDCache(mContainer);
		mOSDManager = new OSDManager();
		
		mOSDManager.addOSD(OSDManager.OSD_VOLUME,mOSDVolume);
		mOSDManager.addOSD(OSDManager.OSD_PAUSE,mOSDPause);
		mOSDManager.addOSD(OSDManager.OSD_LOADING,mOSDLoading);
		mOSDManager.addOSD(OSDManager.OSD_CACHE, mOSDCache);
		mOSDControl = new OSDControlBar(VodPlayerActivity.this, mContainer,mVideoView);
		mOSDManager.addOSD(OSDManager.OSD_CONTROLBAR,mOSDControl);
		mOSDManager.showOSD(mOSDLoading);
		mVideoView.setOnKeyListener(this);
		mVideoView.setOnTouchListener(mVideoViewTouchListener);
		mVideoView.setOnCompletionListener(mVideoCompleteListener);
		mVideoView.setOnPreparedListener(mVideoPrepareListener);
		
		mScrollText = (GGTextView) findViewById(R.id.marquee_text);
	}
	
	private OnCompletionListener mVideoCompleteListener = new OnCompletionListener(){
		@Override
		public void onCompletion(MediaPlayer arg0) {
			play2WichVod(false);
		}
	};
	
	private OnTouchListener mVideoViewTouchListener =  new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(isLoadingVod) return false;
			int visibility = View.VISIBLE == mOSDControl.getVisibility() 
					? View.GONE : View.VISIBLE;
			mOSDControl.setVisibility(visibility);
			return false;
		}
	};
	
	private Handler mCheckPlayHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int msgWhat = msg.what;
			if(Configs.SUCCESS == msgWhat) {
				mVideoPath = new StringBuilder(PLAY_SERVER).append(mCurrentVideo.getChannelId())
						.append(PERIOD).append(mCurrentVideo.getType()).toString();
				logger.i(mVideoPath);
				getScreenSize();
				int mode = DisplayMode.getMode(VodPlayerActivity.this);
				setVideoScale(mode);
				mVideoView.setVideoPath(mVideoPath);
				mVideoView.setOnPreparedListener(mVideoPrepareListener);
				setTitle();
				try{
					mTimerCache.schedule(mTimerTaskCache, START_CACHE_TIME * 1000, GET_CACHE_PEROID * 1000);  
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	};
	
	public void setVideoScale(int mode){
		mVideoView.setShowMode(mScreenWidth,mScreenHeight,mode);
		int widthScreen = mScreenWidth;
		int heightScreen = mScreenHeight;
		switch(mode){
		case DisplayMode.SCREEN_4_3_MODE:
			widthScreen = heightScreen * 4 / 3;
			mVideoView.setShowMode(mScreenWidth, mScreenHeight, 0);
//			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ZOOM, 0.2f);
			break;
		case DisplayMode.SCREEN_16_9_MODE:
			widthScreen = heightScreen * 16 / 9;
			mVideoView.setShowMode(mScreenWidth, mScreenHeight, 1);
//			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ZOOM, 9.0f/16);
			break;
		case DisplayMode.SCREEN_FULL_MODE:
			mVideoView.setShowMode(mScreenWidth, mScreenHeight, 2);
//			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ZOOM, 1.0f);
			break;
		}
//		mVideoView.setVideoScale(widthScreen, heightScreen);
	}
	
	private void setTitle(){
		mVodTitle = null == mVodTitle ? "" : mVodTitle;
		StringBuilder title = new StringBuilder(mVodTitle);
		title.append(" " + mCurrentVideo.getName());
		mOSDControl.setVodTitle(title.toString());
	}
	
	private void getScreenSize() {
		Integer[] screenSize = ScreenUtils.getScreenSize(this);
		if(null != screenSize)
			mScreenHeight = screenSize[0];
		if(null != screenSize)
			mScreenWidth = screenSize[1];
	}
	
	public boolean sendHttpRequest(String channelId, String streamIp, String cmd, String link) {
		StringBuilder s4 = new StringBuilder("http://127.0.0.1:9906/cmd.xml?cmd=");
		s4.append(cmd).append("&id=").append(channelId).append("&server=").append(streamIp);
		if (link != null && !link.equals("")) {
			s4.append("&link=").append(link);
		}
		URL url = null;
		try {
			url = new URL(s4.toString());
			StringBuffer stringbuffer;
			BufferedReader bufferedreader;
			stringbuffer = new StringBuffer();
			HttpURLConnection httpurlconnection = (HttpURLConnection) url.openConnection();
			httpurlconnection.setAllowUserInteraction(false);
			bufferedreader = new BufferedReader(new InputStreamReader(
					httpurlconnection.getInputStream()));
			String s5 = null;
			while ((s5 = bufferedreader.readLine()) != null) {
				stringbuffer.append(s5).append("\n");
			}
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public int getPlayStatus(){
		return mPlayStatus;
	}
	
	private boolean checkCanPlay(boolean is2previous){
		int pos = is2previous ? mPlayVodPos-- : mPlayVodPos++;
		return mPlayVodPos >=0 && mPlayVodPos < mListVideos.size();
	}
	
	public void play2WichVod(boolean is2previous){
		isPlayDirect = true;
		if(checkCanPlay(is2previous)){
			mCurrentVideo = mListVideos.get(mPlayVodPos);
			isLoadingVod = true;
			mOSDManager.showOSD(mOSDLoading);
			startVod();
		} else {
			finish();
		}
	}
	
	private void stopSysMusic(){
		try{
			Intent intent = new Intent();
	        intent.setAction(SYS_MUSIC_COMMAND);
	        intent.putExtra(S_COMMAND,S_STOP);
	        sendBroadcast(intent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		playPause();
	}
	
	private void playPause(){
		if(null != mPlayer)
			try {
				mPlayer.Stop();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	}
	
	protected void onDestroy() {
		super.onDestroy();
		try{
			if(null != mTimerCache)
				mTimerCache.cancel();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		AudioManage audioManage = AudioManage.getInstance();
		if(event.getAction() == KeyEvent.ACTION_DOWN)
			switch(keyCode){
			case KeyEvent.KEYCODE_VOLUME_UP:
				audioManage.upVolume(getApplicationContext());
				mOSDVolume.setVolume();
				mOSDManager.showOSD(mOSDVolume);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				audioManage.lowerVolume(getApplicationContext());
				mOSDVolume.setVolume();
				mOSDManager.showOSD(mOSDVolume);
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if(!isLoadingVod)
					play();
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_MENU :
				if(!isLoadingVod)
					mOSDManager.showOSD(mOSDControl);
				return true;
			// key : ZOOM
			case 258:
				mOSDControl.changeDisplayMode();
				if(null == ct){
					ct = new CustomToast(getApplicationContext(),getString(R.string.video_pro)+mOSDControl.getDisplayMode(), 24);
					ct.show();
				} else {
					ct.cancel();
					ct = new CustomToast(getApplicationContext(),getString(R.string.video_pro)+mOSDControl.getDisplayMode(), 24);
					ct.show();
				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_MUTE:
				try{
					audioManage.setSlience(getApplicationContext());
					mOSDVolume.setVolume();
					mOSDVolume.setVisibility(View.VISIBLE);
				}catch(Exception e){
					logger.e(e.toString());
				}
				return true; 
			}
		return false;
	};
	
	private CustomToast ct;
	
	public void play(){
		mOSDControl.setPlayIcon(mIsVodPause);
		if(mIsVodPause){
			mVideoView.start();
			mOSDManager.closeOSD(mOSDPause);
			mIsVodPause = false;
		} else {
			mVideoView.pause();
			mOSDManager.showOSD(mOSDPause);
			mIsVodPause = true;
		}
	}
	
    private boolean hasPreVod(){
    	return mPlayVodPos >= 1;
    }
    
    private boolean hasNextVod(){
    	return mPlayVodPos < mListVideos.size() - 1;
    }
    
    @Override
    public void onBackPressed() {
    	if(mOSDControl.getVisibility() == View.VISIBLE){
    		 mOSDManager.closeOSD(mOSDControl);
    	} else {
    		exit();
    	}
    }
    
    private void exit(){
		try{
			mTimerCache.cancel();
			mTimerGetPlayPos.cancel();
			mTimerTaskCache.cancel();
			mTimerTaskGetPlayPos.cancel();
			unregisterReceiver(mUpdateMsgReceiver);
			savePlayHistory();
			ScreenZoom.toFull(getApplicationContext());
		} catch(Exception e){
			logger.e(e.toString());
		} finally {
			finish();
		} 
    }
    
    private Runnable mRunnableExit = new Runnable() {
		public void run() {
		}
	};
    
    private Handler mHandlerExit = new Handler(){
    	public void handleMessage(Message msg) {
    		
    	};
    };
    
    private void closeForce(){
    	new FinalHttp().get(CLOSE_FORCE, new AjaxCallBack<Object>() {
    		@Override
    		public void onSuccess(Object t) {
    			super.onSuccess(t);
    			logger.i("ԭ��Դ�ر�����ɹ� = " + t.toString());
    			Toast.makeText(getApplicationContext(), "ԭ���رճɹ�", Toast.LENGTH_LONG).show();
    		}
    		
    		@Override
    		public void onFailure(Throwable t, int errorNo, String strMsg) {
    			super.onFailure(t, errorNo, strMsg);
    			logger.i("ԭ��Դ�ر�����ɹ� = " + t.toString());
    			Toast.makeText(getApplicationContext(), "ԭ���ر�ʧ��", Toast.LENGTH_LONG).show();
    		}
		});
    }
    
    private void savePlayHistory() {
    	HistoryDAO historyDAO = new HistoryDAO(getApplicationContext());
    	if(null != mListVideos.get(0))
    		historyDAO.delete(mListVideos.get(0).getSubcatid());
    	HistoryItem item = new HistoryItem();
    	item.setPlayIndex(String.valueOf(mPlayVodPos));
    	item.setPlayPos(String.valueOf(mVideoView.getCurrentPosition()));
    	item.setSubcatid(mListVideos.get(0).getSubcatid());
    	historyDAO.insert(getApplicationContext(), item);
	}

	//2014.3.3
    @Override
    protected void onStop() {
    	exit();
    	super.onStop();
    }
    
    
	private void showMsg(String paramString) {
		
		mScrollText.setText(paramString);
		mScrollText.init(getWindowManager());
		mScrollText.startScroll();
	}
}