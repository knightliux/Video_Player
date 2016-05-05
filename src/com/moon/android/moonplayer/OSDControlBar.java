package com.moon.android.moonplayer;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.moon.android.moonplayer.DisplayMode.DisplayModeEntity;
import com.moon.android.moonplayer.util.Logger;
import com.moon.android.moonplayer.view.ScrollAlwaysTextView;
import com.moon.android.moonplayer.view.TimerView;
import com.moon.android.moonplayer.view.VideoView;

public class OSDControlBar extends OSD implements OnKeyListener{

	private Logger logger = Logger.getLogger();
	private View mView;
	public static final int TRAFFIC_SPACNIG = 3;
	public static final int FORWARD_TIME = 30;
	public static final int DIRECTION_FOWARD = 0;
	public static final int DIRECTION_BACK = 1;
	private LinearLayout mControlBar;
	private TimerView mTextCurrentTime;
	private TimerView mTextTotalTime;
	private ScrollAlwaysTextView mTextVideoName;
	private TextView mTextTraffic;
	private LongClickSeek mSeekVideo;
	private ImageButton mBtnPauseBig;
	private ImageButton mBtnBack;
	private ImageButton mBtnFoward;
	private ImageButton mBtnPlay;
	private ImageButton mBtnPre;
	private ImageButton mBtnNext;
	private ImageButton mBtnDisplayMode;
	private LinearLayout mContainerDisplay;
	private ListView mListViewDisplayMode;
	private DisplayModeAdapter mDisplayAdapter;
	private int mTotalTime;
	private Timer mVolumeOldTimer;
	
	private VodPlayerActivity mVodPlayer;
	private OSDVolume mOSDVolume;
	private OSDManager mOSDManager;
	
	private boolean isProgressAuto = true;
	private VideoView mVideoView;
	
	private int currentDisplayMode;
	private Timer mTimerSeek = null;
	private TimerTask mTimerTaskSeek = null;
	private int descPos = 0;
	
	public OSDControlBar(VodPlayerActivity vodPlayer,View view){
		mVodPlayer = vodPlayer;
		mView = view; 
		currentDisplayMode = DisplayMode.getMode(mVodPlayer);
		initWidget();
	}

	public OSDControlBar(VodPlayerActivity vodPlayerActivity, View view,
			VideoView videoView) {
		mVodPlayer = vodPlayerActivity;
		mView = view; 
		initWidget();
		mVideoView = videoView;
		currentDisplayMode = DisplayMode.getMode(mVodPlayer);
	}

	private void initWidget() {
		setProperity(PROPERITY_LEVEL_3);
		setCompatibility(COMPATIBILITY_01);
		
		mControlBar = (LinearLayout) mView.findViewById(R.id.control_bar);
		mTextCurrentTime = (TimerView) mView.findViewById(R.id.text_current_time);
		mTextTotalTime = (TimerView) mView.findViewById(R.id.text_total_time);
		mSeekVideo = (LongClickSeek) mView.findViewById(R.id.video_progress);
		mBtnPauseBig = (ImageButton) mView.findViewById(R.id.pause_big);
		mBtnBack = (ImageButton) mView.findViewById(R.id.foward_back);
		mBtnPlay = (ImageButton) mView.findViewById(R.id.foward_play);
		mBtnFoward = (ImageButton) mView.findViewById(R.id.foward_to);
		mBtnPre = (ImageButton) mView.findViewById(R.id.foward_previous);
		mBtnNext = (ImageButton) mView.findViewById(R.id.foward_next);
		mBtnDisplayMode = (ImageButton) mView.findViewById(R.id.display_mode);
		mContainerDisplay = (LinearLayout) mView.findViewById(R.id.display_mode_container);
		mListViewDisplayMode = (ListView) mView.findViewById(R.id.display_mode_listview);
		mTextVideoName = (ScrollAlwaysTextView) mView.findViewById(R.id.video_name);
		mTextTraffic = (TextView) mView.findViewById(R.id.traffic);
		
		mDisplayAdapter = new DisplayModeAdapter(mVodPlayer, DisplayMode.getDisplayMode(mVodPlayer));
		mListViewDisplayMode.setAdapter(mDisplayAdapter);
		mBtnDisplayMode.setOnClickListener(mDisplayModeClickListener);
		mListViewDisplayMode.setOnItemClickListener(mDisplayItemClickListener);
		
		mBtnBack.setOnClickListener(mFowardClickListener);
		mBtnFoward.setOnClickListener(mFowardClickListener);
		mBtnPlay.setOnClickListener(mPlayClickListener);
		
		mSeekVideo.setOnKeyListener(this);
		mBtnPauseBig.setOnKeyListener(this);
		mBtnBack.setOnKeyListener(this);
		mBtnPlay.setOnKeyListener(this);
		mBtnFoward.setOnKeyListener(this);
		mBtnPre.setOnKeyListener(this);
		mBtnNext.setOnKeyListener(this);
		mBtnDisplayMode.setOnKeyListener(this);
		mListViewDisplayMode.setOnKeyListener(this);
		mBtnPauseBig.setOnClickListener(mPlayClickListener);
		mBtnBack.setOnClickListener(mFowardClickListener);
		mBtnFoward.setOnClickListener(mFowardClickListener);
		mBtnDisplayMode.setOnClickListener(mDisplayModeClickListener);
		mBtnPre.setOnClickListener(mVodSelectListener);
		mBtnNext.setOnClickListener(mVodSelectListener);
		
		mOSDManager = new OSDManager();
		mOSDVolume = (OSDVolume) mOSDManager.getOSD(OSDManager.OSD_VOLUME);
	}
	
	private OnClickListener mVodSelectListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean isPreVod = v == mBtnPre ? true : false;
			mVodPlayer.play2WichVod(isPreVod);
		}
	};
	
	private OnItemClickListener mDisplayItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			changeDisplayMode(position);	
		}
	};
	
	private void changeDisplayMode(int position){
		if(currentDisplayMode != position){
			List<DisplayModeEntity> listMode = DisplayMode.getDisplayMode(mVodPlayer);
			DisplayModeEntity displayEntity = listMode.get(position);
			mVodPlayer.setVideoScale(displayEntity.mode);
			DisplayMode.setMode(mVodPlayer,displayEntity.mode);
			mDisplayAdapter = new DisplayModeAdapter(mVodPlayer, DisplayMode.getDisplayMode(mVodPlayer));
			mListViewDisplayMode.setAdapter(mDisplayAdapter);
			mListViewDisplayMode.setSelection(position);
		}
		currentDisplayMode = position;
	}
	
	public void changeDisplayMode(){
		int tempMode = currentDisplayMode;
		int mode = ++tempMode >= mDisplayAdapter.getCount() ? 0 : tempMode;
		changeDisplayMode(mode);	
	}
	
	public String getDisplayMode(){
		try{
			return DisplayMode.getDisplayMode(mVodPlayer).get(currentDisplayMode).getModeString();
		}catch(Exception e){
//			e.printStackTrace();
		}
		return "";
	}
	
	private OnClickListener mDisplayModeClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int visible = mContainerDisplay.getVisibility();
			int setVisible = visible == View.GONE ? View.VISIBLE : View.GONE;
			mContainerDisplay.setVisibility(setVisible);
		}
	};
	
	private OnClickListener mFowardClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v == mBtnBack){
				vodBack();
			}
			if(v == mBtnFoward){
				vodFoward();
			}
		}
	};
	
	private void seek(int position){
		mVideoView.seekTo(position);
	}
	
	private void vodBack(){
		int pos = (int)mVideoView.getCurrentPosition();
		int destPos = pos - FORWARD_TIME * 1000;
		if(destPos <= 0)
			destPos = 0;
		seekFoward(false);
	}
	
	private void vodFoward(){
		int pos =(int) mVideoView.getCurrentPosition();
		int destPos = pos + FORWARD_TIME * 1000;
		if(destPos >= mTotalTime)
			destPos = mTotalTime;
		seekFoward(true);
	}
	
	private OnClickListener mPlayClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mVodPlayer.play();
		}
	};
	
	public void setPlayIcon(boolean isPlay){
		if(isPlay){
			mBtnPlay.setBackgroundResource(R.drawable.bg_p_pause);
		} else {
			mBtnPlay.setBackgroundResource(R.drawable.bg_p_play);
		}
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		setNewDismissClock();
		AudioManage audioManage = AudioManage.getInstance();
		if(event.getAction() == KeyEvent.ACTION_DOWN)
			switch(keyCode){  
			case KeyEvent.KEYCODE_VOLUME_UP:
				audioManage.upVolume(mVodPlayer);
				mOSDVolume.setVolume();
				mOSDManager.showOSD(mOSDVolume);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				audioManage.lowerVolume(mVodPlayer);
				mOSDVolume.setVolume();
				mOSDManager.showOSD(mOSDVolume);
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER:
				if(v == mSeekVideo)
					mVodPlayer.play();
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if(v == mSeekVideo){
					seekFoward(true);
					return true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if(v == mSeekVideo){
					seekFoward(false);
					return true;
				}
				break;
			}
		return false;
	}
	
	private void seekFoward(boolean isfoward){
		int fowardStep = isfoward ? 1 : -1;
		descPos = mSeekVideo.getProgress() + fowardStep * FORWARD_TIME * 1000;
		mSeekVideo.setProgress(descPos);
		//check for small stream while seeking
		if(descPos >= mTotalTime)
			descPos = mTotalTime;
		if(descPos <= 0)
			descPos = 0;
		/* 
		 *  用户移动seekbar后，避免太频繁的SEEK，2s后再跳转
		 */
		if(null != mTimerSeek){
			mTimerSeek.cancel();
			mTimerSeek = null;
			mTimerTaskSeek.cancel();
			mTimerTaskSeek = null;
		}
		mTimerSeek = new Timer();
		mTimerTaskSeek = new TimerTask() {
			@Override
			public void run() {
				seek(descPos);
			}
		};
		mTimerSeek.schedule(mTimerTaskSeek, Configs.SEEK_TIME);
	}
	
	
	final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			setVisibility(View.GONE);
		}
	};
	
	private void setNewDismissClock(){
		if(null != mVolumeOldTimer)
			mVolumeOldTimer.cancel();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(0);
			}
		};
		Timer timer = new Timer();
		timer.schedule(timerTask, OSD.OSD_SHOW_TIME * 1000);
		mVolumeOldTimer = timer;
	}
	
	@Override
	public void setVisibility(int visible) {
		mControlBar.setVisibility(visible);
		mContainerDisplay.setVisibility(View.GONE);
		if(View.VISIBLE == visible){
			mSeekVideo.requestFocus();
			mSeekVideo.requestFocusFromTouch();
			isProgressAuto = false;
			int currentPos =(int) mVideoView.getCurrentPosition();
			mSeekVideo.setProgress(currentPos);
			logger.i("currentPos time = " + currentPos);
		} else {
			isProgressAuto = true;
		}
		setNewDismissClock();
	}
	
	public void setVodTitle(String title){
		mTextVideoName.setText(title);
	}
	
	public void setTraffics(long traffic){
		mTextTraffic.setText(String.valueOf(traffic).concat(getString(R.string.kbs)));
	}
	
	private String getString(int strRes){
		return mVodPlayer.getResources().getString(strRes);
	}
	
	public void setTraffics(String traffic){
		mTextTraffic.setText(traffic.concat(getString(R.string.kbs)));
	}
	
	public void setProgress(String currentTime,String totalTime,int progress){
		mTextCurrentTime.setText(currentTime);
		mTextTotalTime.setText(totalTime);
		if(isProgressAuto){
			mSeekVideo.setProgress(progress);
		}
	}

	public int getCurrentProgress(){
		return mSeekVideo.getProgress();
	}

	public void setTotalTime(int totalTime){
		mTotalTime = totalTime;
		mSeekVideo.setMax(totalTime);
		mTextTotalTime.setTextByMillisecond(totalTime);
	}

	public void setCurrentPlayTime(int currentPlayTime){
		mTextCurrentTime.setTextByMillisecond(currentPlayTime);
	}
	
	public void setVodSelectFocusable(boolean hasPre,boolean hasNext){
		int preDrawableRes = hasPre ? R.drawable.bg_p_previous : R.drawable.p_icon_pre_unfocus;
		mBtnPre.setFocusable(hasPre);
		mBtnPre.setFocusableInTouchMode(hasPre);
		mBtnPre.setBackgroundResource(preDrawableRes);
		
		int nextDrawableRes = hasNext ? R.drawable.bg_p_next : R.drawable.p_icon_next_unfocus;
		mBtnNext.setFocusable(hasNext);
		mBtnNext.setFocusableInTouchMode(hasNext);
		mBtnNext.setBackgroundResource(nextDrawableRes);
	}
	
	public void setSecondProgress(int second){
 		mSeekVideo.setSecondaryProgress(second);
	}
	
	public int getVisibility(){
		return mControlBar.getVisibility();
	}
	
}
